/**
 * VmImageCoupling - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;

public class XmlByPopularity {

	private static Logger logger = Logger.getLogger(XmlByPopularity.class);

	Map<String, String> vmXmlDescriptions = new HashMap<String, String>();
	Map<String, ImageDescription> imageDesciptions = new HashMap<String, XmlByPopularity.ImageDescription>();
	Map<String, String> imageXmlDescriptions = new HashMap<String, String>();

	private class ImageDescription {
		int defaultmem;
		int defaultdisksize;
		int defaultcpu;
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			logger.fatal("Invalid arguments");
			return;
		}
		
		String imagesFilePath = XmlByPopularity.class.getResource(args[0]).getFile();
		File imagesFile = new File(imagesFilePath);
		String imagePopularityFilePath = XmlByPopularity.class.getResource(args[1]).getFile();
		File imagePopularityFile = new File(imagePopularityFilePath);
		XmlByPopularity xbp = new XmlByPopularity();
		xbp.read(imagesFile, imagePopularityFile);
		
		File outputFile = new File(args[2]);
		outputFile.createNewFile();
		xbp.write(outputFile);
	}
	
	/**
	 * @param outputFile
	 * @throws IOException 
	 */
	private void write(File outputFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		try {
			writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><experiment><images>");
			for (String imId : imageXmlDescriptions.keySet()) {
				writer.append(imageXmlDescriptions.get(imId) + "\n");
			}
			writer.append("</images><vms>");
			for (String imId : vmXmlDescriptions.keySet()) {
				writer.append(vmXmlDescriptions.get(imId) + "\n");
			}
			writer.append("</vms></experiment>");
		} finally {
			writer.close();
			System.out.println("Done");
		}
	}

	public void read(File imagesFile, File imagePopularityFile)
			throws IOException {
		processImageDescriptions(imagesFile);
		processVmDescriptions(imagePopularityFile);
	}

	/**
	 * An image description looks like this:<br/>
	 * &lt;image&gt;<br/>
	 * &lt;id&gt;pok1img-3834.0&lt;/id&gt;<br/>
	 * &lt;description&gt;DIRDB-RHEL5.5_x64-DB2 9.7 fp3a-medium -Good for ISD DB2
	 * testing&lt;/description&gt;<br/>
	 * &lt;size unit="bytes"&gt;39739301197&lt;/size&gt;<br/>
	 * &lt;/image&gt;<br/>
	 * 
	 * @param imagesFile
	 * @return
	 */
	private void processImageDescriptions(File imagesFile) {

		if (imagesFile == null)
			return;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(false);
		DocumentBuilder builder;
		Document imDoc;

		try {
			builder = factory.newDocumentBuilder();
			imDoc = builder.parse(imagesFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			XPathExpression expr = xpath.compile("//image");
			NodeList nl = (NodeList) expr.evaluate(imDoc, XPathConstants.NODESET);

			for (int i = 0; i < nl.getLength(); i++) {
				StringBuilder xmlDesc = new StringBuilder("<image>");
				Node rNode = nl.item(i);
				NodeList childNodes = rNode.getChildNodes();
				String id = null;
				ImageDescription imageDescription = null;
				for (int j = 0; j < childNodes.getLength(); j++) {
					Node cNode = childNodes.item(j);
					if ("id".equals(cNode.getNodeName())) {
						id = cNode.getTextContent();
						xmlDesc.append("<id>" + id + "</id>");
					} else if ("name".equals(cNode.getNodeName())) {
						xmlDesc.append("<description> " + cNode.getTextContent()
								+ "</description>");
					} else if ("attributes".equals(cNode.getNodeName())) {
						Gson gson = new Gson();
						imageDescription = gson.fromJson(cNode.getTextContent(),
								ImageDescription.class);
						xmlDesc.append("<size unit=\"GB\">"
								+ imageDescription.defaultdisksize + "</size>");
					}
				}
				xmlDesc.append("</image>");
				imageXmlDescriptions.put(id, xmlDesc.toString());
				imageDesciptions.put(id, imageDescription);
			}

		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
		return;
	}

	/**
	 * A VM description looks like this:<br/>
	 * &lt;vm&gt;<br/>
	 * &lt;image&gt;pok1img-3834.0&lt;/image&gt;<br/>
	 * &lt;ram&gt;2&lt;/ram&gt;<br/>
	 * &lt;count&gt;643&lt;/count&gt;<br/>
	 * &lt;/vm&gt;<br/>
	 * 
	 * @param imagesFile
	 * @param imagePopularityFile
	 * @return
	 * @throws IOException
	 */
	private void processVmDescriptions(File imagePopularityFile)
			throws IOException {

		if (imagePopularityFile == null) return;

		BufferedReader reader = new BufferedReader(new FileReader(
				imagePopularityFile));

		Map<String, Integer> vmCountsPerImage = new HashMap<String, Integer>();
		
		while (reader.ready()) {
			String line = reader.readLine();
			String id = line.split("\t")[0];
			int count = Integer.parseInt(line.split("\t")[1]);
			vmCountsPerImage.put(id, count);
		}

		reader.close();
		
		for (String imId : vmCountsPerImage.keySet()) {
			String updatedImId = imId;
			if (!imageDesciptions.containsKey(imId)) {
				for (String imIdCandidate : imageDesciptions.keySet()) {
					if (!vmXmlDescriptions.containsKey(imIdCandidate)) {
						updatedImId = imIdCandidate;
						break;
					}
				}
				if (updatedImId == imId) {
					logger.warn("No details for image: " + imId);
					continue;
				}
			}
			
			StringBuilder xmlDesc = new StringBuilder("<vm>");
			ImageDescription imDesc = imageDesciptions.get(updatedImId);
			xmlDesc.append("<image>" + updatedImId + "</image>");
			xmlDesc.append("<ram>" + imDesc.defaultmem + "</ram>");
			xmlDesc.append("<count>" + vmCountsPerImage.get(imId) + "</count>");
			xmlDesc.append("</vm>");
			
			vmXmlDescriptions.put(updatedImId, xmlDesc.toString());
		}
	}

	/**
	 * @return the vmXmlDescriptions
	 */
	public Map<String, String> getVmXmlDescriptions() {
		return vmXmlDescriptions;
	}

	/**
	 * @return the hostXmlDescriptions
	 */
	public Map<String, String> getHostXmlDescriptions() {
		return imageXmlDescriptions;
	}
}
