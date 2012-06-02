/**
 * VmImageCoupling - Technion
 * 
 * Author: Assaf Israel, 2012
 */
package il.ac.technion.configuration;


import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

public class XmlByPopularityTest {

	@Test
	public void testReadImageFile() throws IOException {
		String imagesFilePath = XmlByPopularityTest.class.getResource("images.xml").getFile();
		File imagesFile = new File(imagesFilePath);
		XmlByPopularity xbp = new XmlByPopularity();
		xbp.read(imagesFile, null);
		Assert.assertNotNull(xbp.imageXmlDescriptions);
		Assert.assertFalse(xbp.imageXmlDescriptions.keySet().isEmpty());
	}

	@Test
	public void testReadPopularityFile() throws IOException {
		String imagesFilePath = XmlByPopularityTest.class.getResource("images.xml").getFile();
		File imagesFile = new File(imagesFilePath);
		String imagePopularityFilePath = XmlByPopularityTest.class.getResource("popularity.csv").getFile();
		File imagePopularityFile = new File(imagePopularityFilePath);
		XmlByPopularity xbp = new XmlByPopularity();
		xbp.read(imagesFile, imagePopularityFile);
		Assert.assertNotNull(xbp.vmXmlDescriptions);
		Assert.assertFalse(xbp.vmXmlDescriptions.keySet().isEmpty());
	}	
}
