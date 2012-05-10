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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

		hosts = getHosts(doc, xpath);
		images = getImages(doc, xpath);
		vms = getVMs(doc, xpath);
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
				int storageCapacity = getIntFromNode("storage", rNode);
				int ramCapacity = getIntFromNode("ram", rNode);
				int count = getIntFromNode("count", rNode);

				for (int j = 0; j < count; j++) {
					$.add(new Host(hostId++, ramCapacity, storageCapacity));
				}
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
				int imgId = getIntFromNode("id", rNode);
				String desc = getStringFromNode("description", rNode);
				int size = getIntFromNode("size", rNode);

				if (null != findImageById(imgId,$))
					throw new ConfigurationException(
							"Can't have two images with the same ID [" + imgId + "]");
				$.add(new Image(imgId, size, desc));
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
				int imageId = getIntFromNode("image", rNode);
				Image img = findImageById(imageId, images);
				if (img == null) {
					throw new ConfigurationException("Invalid image id: " + imageId);
				}
				int ram = getIntFromNode("ram", rNode);
				int count = getIntFromNode("count", rNode);

				for (int j = 0; j < count; j++) {
					$.add(new VM(vmId++, img, ram));
				}
			}

		} catch (XPathExpressionException e) {
			throw new ConfigurationException(e);
		}
		return $;
	}

	private static Image findImageById(int imageId, List<Image> images) {
		for (Image img : images) {
			if (img.id == imageId) {
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
					throw new ConfigurationException("Bad double", nfe);
				}
			}
		}
		throw new ConfigurationException("Invalid config file - missing " + tag
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
		return hosts;
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
	
	public Map<Image,List<VM>> getImageMap() {
		Map<Image,List<VM>> $ = new HashMap<Image, List<VM>>();
		for (Image im : images) {
			$.put(im, new LinkedList<VM>());
		}
		for (VM vm : vms) {
			$.get(vm.image).add(vm);
		}
		return $;
	}
}
