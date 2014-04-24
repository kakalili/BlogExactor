/**
 * 
 */
package models;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.validator.UrlValidator;


//import org.crow.utils.HtmlUtils;


/**
 * @author khaclinh
 *
 */
public class Extractor {

	public static boolean checkUrl(String url) {
		UrlValidator urlValidator = new UrlValidator();
		if (urlValidator.isValid(url)) {
			return true;
		}
		else {
			return false;
		}
	}
	public static String contentExtract(String url) {
		ExtractText extractText = new ExtractText();
//		String url = 
//				"http://keiko.blog.bai.ne.jp/?eid=121904"
//				"http://boilerpipe-web.appspot.com/extract?url=http%3A%2F%2Fasakonayuki.blog121.fc2.com%2Fblog-date-200803.html&extractor=ArticleExtractor&output=htmlFragment&extractImages="
//				"http://info.movies.yahoo.co.jp/detail/tymv/id345056/"
//				"http://programming-10000.hatenadiary.jp/entry/20130226/1361897094"
//				"http://asakonayuki.blog121.fc2.com/blog-date-200803.html"
//				"http://taigh.blog.bai.ne.jp/?eid=121911"
//				"http://hn.24h.com.vn/bong-da/qbv-benh-messi-la-bat-cong-voi-ribery-c48a583661.html"
//				"http://jeyamarticle.com/business-finance/f1-show-cars/"
//				"http://ameblo.jp/staff/entry-11649218224.html"
//				"http://blog.zuzara.com/2006/06/06/84/"
//				"http://tn2.jugem.jp/?eid=26"
//				"http://gin-eva-sla.jugem.jp/?eid=329"
//				"http://kazewookosukurokishi.at.webry.info/200803/article_1.html"
//				"http://www.gizmodo.jp/2008/03/iphone_sdk.html"
//				"http://e0166.blog89.fc2.com/blog-entry-423.html"
//				"http://d.hatena.ne.jp/amachang/20080306/1204787459"
//				"http://blog.creamu.com/mt/2008/03/400pi_diagona_pack.html"
				
//				;
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("/home/khaclinh/Desktop/aa.txt", true)));
		    out.println(url+"\n");
		    out.close();
		} catch (IOException e) {
		    //oh noes!
		}
	    //valid URL
	    if (checkUrl(url)) {
	       String text = extractText.getExtractText(url).getExtractText();
	       return text;
	    } else {
	       return "入力したURLがエラー";
	    }
	}
	
	
	public static String textHtmlExtract(String url) {
		ExtractText extractText = new ExtractText();
		 
	    //valid URL
	    if (checkUrl(url)) {
	       String textHtml = extractText.getExtractText(url).getArticleHtmlString();
	       return textHtml;
	    } else {
	       return "入力したURLがエラー";
	    }
	}
}
