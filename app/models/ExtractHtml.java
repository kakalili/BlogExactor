package models;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

/**
 * @author khaclinh
 * 
 */
public class ExtractHtml {

    public static final int SHORTEST_TEXT = 50;
    private static final List<String> STRING_REPLACE = Arrays.asList("strong", "b", "i");
    private Pattern unLikePattern = Pattern.compile("display\\:none|visibility\\:hidden");
    protected final int shortestText;
    protected final List<String> strReplace;
    protected String strParaSelector = "p";

    public ExtractHtml() {
        this(SHORTEST_TEXT, STRING_REPLACE);
    }

    public ExtractHtml(int shortestText) {
        this(shortestText, STRING_REPLACE);
    }

    public ExtractHtml(int shortestText, List<String> strReplace) {
        this.shortestText = shortestText;
        this.strReplace = strReplace;
    }

    public void setStrSelector(String strParaSelector) {
        this.strParaSelector = strParaSelector;
    }

    public String extractElementToText(Element element) {
        removeElementNegScores(element);
        StringBuilder strBuild = new StringBuilder();
        put(element, strBuild, strParaSelector);
        String str = strTrim(strBuild.toString());
        if (str.length() > 100)
            return str;

        if (str.isEmpty() || !element.text().isEmpty() && str.length() <= element.ownText().length())
            str = element.text();

        // if jsoup failed to parse the whole html now parse this smaller 
        // snippet again to avoid html tags disturbing our text:
        return Jsoup.parse(str).text();
    }
    
    public static String strTrim(String str) {
        if (str.isEmpty())
            return "";

        StringBuilder strBuild = new StringBuilder();
        boolean prev = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == ' ' || (int) c == 9 || c == '\n') {
                prev = true;
                continue;
            }

            if (prev)
            	strBuild.append(' ');

            prev = false;
            strBuild.append(c);
        }
        return strBuild.toString().trim();
    }

    protected void removeElementNegScores(Element element) {
        Elements weightItems = element.select("*[weightMark]");
        for (Element item : weightItems) {
            int weight = Integer.parseInt(item.attr("weightMark"));
            if (weight < 0 || item.text().length() < shortestText)
                item.remove();
        }
    }

    protected void put(Element element, StringBuilder strBuild, String tagName) {
        LOOP:
        for (Element node : element.select(tagName)) {
            Element elementTemp = node;
            // check all elements until 'node'
            while (elementTemp != null && !elementTemp.equals(element)) {
                if (unLike(elementTemp))
                    continue LOOP;
                elementTemp = elementTemp.parent();
            }

            String text = elementToText(node);
            if (text.isEmpty() || text.length() < shortestText || text.length() > numOfStr(text) * 2)
                continue;

            strBuild.append(text);
            strBuild.append("\n\n");
        }
    }
    public int numOfStr(String str) {
        int length = str.length();
        int c = 0;
        for (int i = 0; i < length; i++) {
            if (Character.isLetter(str.charAt(i)))
                c++;
        }
        return c;
    }
    boolean unLike(Node node) {
        if (node.attr("class") != null && node.attr("class").toLowerCase().contains("caption"))
            return true;

        String style = node.attr("style");
        String classStr = node.attr("class");
        if (unLikePattern.matcher(style).find() || unLikePattern.matcher(classStr).find())
            return true;
        return false;
    }

    void putTextPassHid(Element element, StringBuilder strBuild) {
        for (Node node : element.childNodes()) {
            if (unLike(node))
                continue;
            if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                String str = textNode.text();
                strBuild.append(str);
            } else if (node instanceof Element) {
                Element elementTemp = (Element) node;
                if (strBuild.length() > 0 && elementTemp.isBlock() && !findLastSpace(strBuild))
                	strBuild.append(" ");
                else if (elementTemp.tagName().equals("br"))
                	strBuild.append(" ");
                putTextPassHid(elementTemp, strBuild);
            }
        }
    }

    boolean findLastSpace(StringBuilder strBuild) {
        if (strBuild.length() == 0)
            return false;
        return Character.isWhitespace(strBuild.charAt(strBuild.length() - 1));
    }

//    protected String nodeToText(Element element) {
//        return element.text();
//    }

    protected String elementToText(Element element) {
        StringBuilder strBuild = new StringBuilder(200);
        putTextPassHid(element, strBuild);
        return strBuild.toString();
    }

//    public ExtractHtml setUnLikePattern(String unLikePattern) {
//        this.unLikePattern = Pattern.compile(unLikePattern);
//        return this;
//    }
//
//    public ExtractHtml appendUnlikePattern(String str) {
//        return setUnLikePattern(unLikePattern.toString() + "|" + str);
//    }
}


