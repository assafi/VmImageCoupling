/**
 * VmImageCoupling - Software Design, 236700 - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.configuration;

import il.ac.technion.beans.Host;
import il.ac.technion.beans.Image;
import il.ac.technion.beans.VM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Configuration {

	private static final String LINE_DELIM = System.getProperty("line.separator");

	private List<Host> hosts;
	private List<Image> images;
	private List<VM> vms;
	
	private int totalImgSize = 0;
	private int totalHostStorage = 0;
	private int totalVmSize = 0;
	private int totalHostRAM = 0;

	private Random random = new Random(System.currentTimeMillis());

	private double numVmsFactor = 1.0;
	private double storageCapacityFactor = 1.0;
	private double ramCapacityFactor = 1.0;

	public Configuration(String setupFilePath) throws IOException,
			ConfigurationException {
		File setupFile = new File(setupFilePath);
		if (!setupFile.exists()) {
			throw new IllegalArgumentException("File not found");
		}

		if (!setupFile.canRead()) {
			throw new IOException("Can't read file " + setupFile);
		}

		parseFile(setupFile);
	}

	private void parseFile(File setupFile) throws IOException,
			ConfigurationException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		Document doc;
		try {
			builder = factory.newDocumentBuilder();
			doc = builder.parse(setupFile);
		} catch (SAXException e) {
			throw new ConfigurationException(e);
		} catch (ParserConfigurationException e) {
			throw new ConfigurationException(e);
		}

		XPathFactory xFactory = XPathFactory.newInstance();
		XPath xpath = xFactory.newXPath();

		numVmsFactor  = getNumVmsFactor(doc, xpath);
		storageCapacityFactor = getStorageCapacityFactor(doc, xpath);
		ramCapacityFactor = getRamCapacityFactor(doc, xpath);
		hosts = getHosts(doc, xpath);
		images = getImages(doc, xpath);
		vms = getVMs(doc, xpath);
		
		filterUnusedImages();
		System.out.println("Total Host storage: " + totalHostStorage + ", factor: " + storageCapacityFactor);
		System.out.println("Total Image size: " + totalImgSize);
		System.out.println("Total Host RAM: " + totalHostRAM + ", factor: " + ramCapacityFactor);
		System.out.println("Total VM size: " + totalVmSize);
		System.out.println("Number of hosts: " + hosts.size());
		System.out.println("Number of VMs: " + vms.size() + ", factor: " + numVmsFactor);
	}

	private double getRamCapacityFactor(Document doc, XPath xpath) throws ConfigurationException {
		return getFactor("RamCapacityFactor", doc, xpath);
	}

	private double getStorageCapacityFactor(Document doc, XPath xpath) throws ConfigurationException {
		return getFactor("StorageCapacityFactor", doc, xpath);
	}

	private double getNumVmsFactor(Document doc, XPath xpath) throws ConfigurationException {
		return getFactor("NumVMsFactor",doc, xpath);
	}

	private double getFactor(String nodeName, Document doc, XPath xpath)
			throws ConfigurationException {
		double $ = 1.0;
		XPathExpression expr;
		try {
			expr = xpath.compile("//" + nodeName);
			$ = (Double) expr.evaluate(doc, XPathConstants.NUMBER);
		} catch (XPathExpressionException e) {
			throw new ConfigurationException(e);
		}
		return $;
	}

	private void filterUnusedImages() {
		List<Image> blackList = new ArrayList<Image>(images);
		for (VM vm : vms) {
			blackList.remove(vm.image);
		}
		for (Image image : blackList) {
			images.remove(image);
			totalImgSize -= image.size;
		}
	}

	private List<Host> getHosts(Document doc, XPath xpath)
			throws ConfigurationException {
		List<Host> $ = new LinkedList<Host>();
		int hostId = 1;
		XPathExpression expr;
		try {
			expr = xpath.compile("//host");
			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nl.getLength(); i++) {
				Node rNode = nl.item(i);
				int storageCapacity = (int)Math.round(getIntFromNode("storage", rNode) * storageCapacityFactor);
				int ramCapacity = (int)Math.round(getIntFromNode("ram", rNode) * ramCapacityFactor);
				int count = getIntFromNode("count", rNode);

				for (int j = 0; j < count; j++) {
					$.add(new Host(hostId++, ramCapacity, storageCapacity));
				}
				
				totalHostStorage += count * storageCapacity;
				totalHostRAM += count * ramCapacity;
			}

		} catch (XPathExpressionException e) {
			throw new ConfigurationException(e);
		}
		
		return $;
	}

	private List<Image> getImages(Document doc, XPath xpath)
			throws ConfigurationException {
		List<Image> $ = new LinkedList<Image>();
		XPathExpression expr;
		try {
			expr = xpath.compile("//images/image");
			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nl.getLength(); i++) {
				Node rNode = nl.item(i);
				String imgId = getStringFromNode("id", rNode);
				String desc = getStringFromNode("description", rNode);
				long size = getLongFromNode("size", rNode);
				String unit = getAttributeFromNode("size", "unit", rNode);

				if ("bytes".equals(unit)) {
					size >>= 30;
				}

				if (null != findImageById(imgId, $))
					throw new ConfigurationException(
							"Can't have two images with the same ID [" + imgId + "]");
				$.add(new Image(imgId, (int) size, desc));
				
				totalImgSize += size;
			}

		} catch (XPathExpressionException e) {
			throw new ConfigurationException(e);
		}
		
		return $;
	}

	private List<VM> getVMs(Document doc, XPath xpath)
			throws ConfigurationException {
		List<VM> $ = new LinkedList<VM>();
		int vmId = 1;
		XPathExpression expr;
		try {
			expr = xpath.compile("//vm");
			NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nl.getLength(); i++) {
				Node rNode = nl.item(i);
				String imageType = getAttributeFromNode("image", "type", rNode);
				Image img = null;
				if ("".equals(imageType)) {
					String imageId = getStringFromNode("image", rNode);
					img = findImageById(imageId, images);
				} else if (!"random".equals(imageType)) {
					throw new ConfigurationException("Invalid image id or type");
				}
				int ram = getIntFromNode("ram", rNode);
				int count = (int) Math.round(getIntFromNode("count", rNode) * numVmsFactor);

				for (int j = 0; j < count; j++) {
					if ("random".equals(imageType)) {
						img = randomImage();
					}
					$.add(new VM(vmId++, img, ram));
				}
				
				totalVmSize += count * ram;
			}

		} catch (XPathExpressionException e) {
			throw new ConfigurationException(e);
		}
		return $;
	}

	private Image randomImage() {
		int imageOrdinal = random.nextInt(images.size());
		return images.get(imageOrdinal);
	}

	private static Image findImageById(String imageId, List<Image> images) {
		for (Image img : images) {
			if (img.id.equals(imageId)) {
				return img;
			}
		}
		return null;
	}

	private String getStringFromNode(String tag, Node rNode)
			throws ConfigurationException {
		NodeList nl = rNode.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName() == tag) {
				return n.getTextContent();
			}
		}
		throw new ConfigurationException("Invalid config file - missing " + tag
				+ " tag");
	}

	private int getIntFromNode(String tag, Node rNode)
			throws ConfigurationException {
		NodeList nl = rNode.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName() == tag) {
				try {
					return Integer.parseInt(n.getTextContent());
				} catch (NumberFormatException nfe) {
					throw new ConfigurationException("Bad integer", nfe);
				}
			}
		}
		throw new ConfigurationException("Invalid config file - missing " + tag
				+ " tag");
	}

	private String getAttributeFromNode(String node, String attr, Node rNode) {
		NodeList nl = rNode.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName() == node
					&& n.getAttributes().getNamedItem(attr) != null) {
				return n.getAttributes().getNamedItem(attr).getNodeValue();
			}
		}

		return "";
	}

	private long getLongFromNode(String node, Node rNode)
			throws ConfigurationException {
		NodeList nl = rNode.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeName() == node) {
				try {
					return Long.parseLong(n.getTextContent());
				} catch (NumberFormatException nfe) {
					throw new ConfigurationException("Bad long", nfe);
				}
			}
		}

		throw new ConfigurationException("Invalid config file - missing " + node
				+ " tag");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("===== Hosts =====" + LINE_DELIM);
		for (Host h : hosts) {
			sb.append(h.summary() + LINE_DELIM);
		}
		sb.append("===== Images =====" + LINE_DELIM);
		for (Image im : images) {
			sb.append(im.description() + LINE_DELIM);
		}
		sb.append("===== VMs =====" + LINE_DELIM);
		for (VM vm : vms) {
			sb.append(vm.summary() + LINE_DELIM);
		}
		return sb.toString();
	}

	/**
	 * @return the hosts
	 */
	public List<Host> getHosts() {
		return new ArrayList<Host>(hosts);
	}

	/**
	 * @return the images
	 */
	public List<Image> getImages() {
		return images;
	}

	/**
	 * @return the vms
	 */
	public List<VM> getVms() {
		return vms;
	}

	public Map<Integer, VM> getId2VmMap() {
		Map<Integer, VM> $ = new HashMap<Integer, VM>();
		for (VM vm : vms) {
			$.put(vm.id, vm);
		}
		return $;
	}

	public Map<Image, List<VM>> getImageMap() {
		Map<Image, List<VM>> $ = new HashMap<Image, List<VM>>();
		for (Image im : images) {
			$.put(im, new LinkedList<VM>());
		}
		for (VM vm : vms) {
			$.get(vm.image).add(vm);
		}
		return $;
	}
}
