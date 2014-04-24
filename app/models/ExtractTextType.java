/**
 * 
 */
package models;

import org.jsoup.nodes.Element;

/**
 * @author khaclinh
 *
 */
public class ExtractTextType {
	private String pageTitle;
	private String largestTextBlock;
	private Element largestHTMLBlock;
	private String allText;
	private String articleText;
	private Element articleHTML;
	private String articleHtmlString;
	private String extractText;
	/**
	 * @return the pageTitle
	 */
	public String getPageTitle() {
		return pageTitle;
	}
	/**
	 * @param pageTitle the pageTitle to set
	 */
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	/**
	 * @return the largestTextBlock
	 */
	public String getLargestTextBlock() {
		return largestTextBlock;
	}
	/**
	 * @param largestTextBlock the largestTextBlock to set
	 */
	public void setLargestTextBlock(String largestTextBlock) {
		this.largestTextBlock = largestTextBlock;
	}
	/**
	 * @return the allText
	 */
	public String getAllText() {
		return allText;
	}
	/**
	 * @param allText the allText to set
	 */
	public void setAllText(String allText) {
		this.allText = allText;
	}
	/**
	 * @return the largestHTMLBlock
	 */
	public Element getLargestHTMLBlock() {
		return largestHTMLBlock;
	}
	/**
	 * @param largestHTMLBlock the largestHTMLBlock to set
	 */
	public void setLargestHTMLBlock(Element largestHTMLBlock) {
		this.largestHTMLBlock = largestHTMLBlock;
	}
	/**
	 * @return the articleText
	 */
	public String getArticleText() {
		return articleText;
	}
	/**
	 * @param articleText the articleText to set
	 */
	public void setArticleText(String articleText) {
		this.articleText = articleText;
	}
	/**
	 * @return the articleHTML
	 */
	public Element getArticleHTML() {
		return articleHTML;
	}
	/**
	 * @param articleHTML the articleHTML to set
	 */
	public void setArticleHTML(Element articleHTML) {
		this.articleHTML = articleHTML;
	}
	
	public void setArticleHtmlString(String articleHtmlString) {
		this.articleHtmlString = articleHtmlString;
	}
	
	public String getArticleHtmlString(){
		return articleHtmlString;
	}
	/**
	 * @return the extractText
	 */
	public String getExtractText() {
		return extractText;
	}
	/**
	 * @param extractText the extractText to set
	 */
	public void setExtractText(String extractText) {
		this.extractText = extractText;
	}

}
