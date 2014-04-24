/**
 * 
 */
package models;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author khaclinh
 *
 */
public class ExtractText {
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0";

	public ExtractTextType getExtractText(String url) {
		ExtractTextType eTextType = null;
		try {
			Document document = Jsoup.connect(url).timeout(60000).userAgent(USER_AGENT).get();
//			System.out.println(document.toString());
//			eTextType = getExtractText(document);
			eTextType = getExtractTextFeatures(document);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return eTextType;
	}
	
	public ExtractTextType getExtractText(Document document) {
		ExtractCluster extractCluster = new ExtractCluster();
		return extractCluster.getText(document);
	}
	
	public ExtractTextType getExtractTextFeatures(Document document) {
		ExtractCluster extractCluster = new ExtractCluster();
		return extractCluster.extractContent(document);
	}
	
}
