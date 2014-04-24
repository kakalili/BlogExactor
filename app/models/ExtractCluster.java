/**
 * 
 */
package models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;


/**
 * @author khaclinh
 * 
 */
public class ExtractCluster {
	private static final Pattern POSSIBLE_TEXT_NODES = Pattern
			.compile("p|div|td|h1|h2|h3|article|section|span|tmp|li|font|em|img");
	private static final String[] UNWRAP_TAGS = { "b", "u", "i", "font", "em" };
	private static final Pattern NEGATIVE_STYLE = Pattern
			.compile("hidden|display: ?none|font-size: ?small");
	private String strUnlike = "com(bx|ment|munity)|dis(qus|cuss)|e(xtra|[-]?mail)|foot|"
            + "header|menu|re(mark|ply)|rss|sh(are|outbox)|sponsor"
            + "a(d|ll|gegate|rchive|ttachment)|(pag(er|ination))|popup|print|"
            + "login|si(debar|gn|ngle)";
	private Pattern UNLIKELY = Pattern.compile(strUnlike);
	private String strPos = "(^(body|content|h?entry|main|page|post|text|blog|story|haupt))"
            + "|arti(cle|kel)|instapaper_body";
    private Pattern POSITIVE = Pattern.compile(strPos);
    // Most likely negative candidates
    private String strNeg = "nav($|igation)|user|com(ment|bx)|(^com-)|contact|"
            + "foot|masthead|(me(dia|ta))|outbrain|promo|related|scroll|(sho(utbox|pping))|"
            + "sidebar|sponsor|tags|tool|widget|player|disclaimer|toc|infobox|vcard";
    private Pattern NEGATIVE = Pattern.compile(strNeg);
    private static final Pattern NODES = Pattern.compile("p|div|td|h1|h2|article|section");
	private static final int CLUSTER_DISTANCE = 4;
	private static final int WORDS = 5;
	private static final int SENT = WORDS * 15;

	private double threasholdRatio;
	private static final double DEFAULT_RATIO = 0.1;
	private ExtractTextType extractTextType;
	private int largestElemIndex = 0;
	private int numOfURLs;

	public ExtractCluster() {
		extractTextType = new ExtractTextType();
	}

	public ExtractTextType getText(Document document) {
		// Document document =
		// Jsoup.connect(url).timeout(60000).userAgent(USER_AGENT).get();
		// Document document = Jsoup.parse(new File("art.html"), "UTF-8");
		Element bodyElement = removeFat(document.body());

		// System.out.println(bodyElement);
		// articleFinder(bodyElement);
		Elements flattenElements = new Elements(flattenDOM(bodyElement));
		if (!flattenElements.isEmpty()) {
			Elements elementsOfInterest = calculateBlockSizeRatios(flattenElements);
			Set<Element> mainCluster = findMainCluster(elementsOfInterest);
			Set<Element> largestCluster = null;
			int mainClusterTextSize = 0;
			for (Element e : mainCluster) {
				mainClusterTextSize += e.text().length();
			}
			List<Set<Element>> clusterSet = findClusters(elementsOfInterest);
			int maxCSize = 0;
			Set<Element> lCluster = null;
			for (Set<Element> c : clusterSet) {
				int textSize = 0;
				for (Element elem : c) {
					textSize += elem.text().length();
				}
				if (maxCSize < textSize) {
					maxCSize = textSize;
					lCluster = c;
				}
			}
			if (maxCSize >= mainClusterTextSize) {
				largestCluster = lCluster;
			} else {
				largestCluster = mainCluster;
			}
			if (largestCluster != null) {
				StringBuffer extractTextBuffer = new StringBuffer();
				String es = null;
				for (Element element : largestCluster) {
					extractTextBuffer.append(element.text() + "\n");
					if (es == null)
						es = element.toString() + "\n";
					else
						es += (element.toString() + "\n");
				}
				extractTextType.setExtractText(extractTextBuffer.toString());
				extractTextType.setArticleHtmlString(es);
			}
			// System.out.println("no of URLs: " + (double) numOfURLs/ (double)
			// bodyElement.text().length());

			extractTextType.setAllText(bodyElement.text());
			extractTextType.setPageTitle(document.title());
		}
		return extractTextType;
	}

	private Element removeFat(Element doc) {

		this.numOfURLs = doc.getElementsByTag("a").size();

		for (int i = 0; i < UNWRAP_TAGS.length; i++) {
			doc.select(UNWRAP_TAGS[i]).unwrap();
		}
		Elements scripts = doc.getElementsByTag("script");
		for (Element item : scripts) {
			item.remove();
		}

		Elements noscripts = doc.getElementsByTag("noscript");
		for (Element item : noscripts) {
			item.remove();
		}

		Elements styles = doc.getElementsByTag("style");
		for (Element style : styles) {
			style.remove();
		}

		/*
		 * Elements uls = doc.getElementsByTag("ul"); for (Element ul : uls) {
		 * if (ul.text().length()>WORDS) { String text = ul.text(); Elements
		 * liElements = ul.select("li"); for (Element li : liElements) {
		 * li.remove(); } ul.tagName("div"); ul.text(text); } }
		 */
		Elements nonTextElements = doc.getAllElements();
		for (Element nonTextElement : nonTextElements) {
			String style = nonTextElement.attr("style");
			if (!nonTextElement.hasText()
					|| nonTextElement.text().length() <= WORDS
					|| (style != null && !style.isEmpty() && NEGATIVE_STYLE
							.matcher(style).find())) {
				nonTextElement.remove();
			}
		}
		return doc;
	}

	private Map<Integer, Double> calculateSize(Elements elements) {
		Map<Integer, Double> sizeMap = new LinkedHashMap<Integer, Double>();
		for (int i = 0; i < elements.size(); i++) {
			sizeMap.put(i, (double) elements.get(i).text().length());
		}
		return sizeMap;
	}
	
	private Map<Integer, Double> findMaxAndAvg(Collection<Double> values) {
		double max = 0;
		int maxIndex = 0;
		Map<Integer, Double> maxElement = new HashMap<Integer, Double>();
		Object[] valuesArr = values.toArray();
		double total = 0.0;
		for (int i = 0; i < valuesArr.length; i++) {
			if (max <= (Double) valuesArr[i]) {
				max = (Double) valuesArr[i];
				maxIndex = i;
			}
			total = total + (Double) valuesArr[i];
		}
		this.largestElemIndex = maxIndex;
		this.threasholdRatio = Math.max(total / (values.size() * max), SENT
				/ (values.size() * max));
		// System.out.println(threasholdRatio);
		maxElement.put(maxIndex, max);
		return maxElement;
	}


	public Elements calculateBlockSizeRatios(Elements mainElements) {
		Map<Integer, Double> sizeMap = calculateSize(mainElements);

		Map<Integer, Double> k = findMaxAndAvg(sizeMap.values());
		int sizeOfMap = sizeMap.size();
		Set<Integer> keySet = sizeMap.keySet();
		int maxIndex = 0;
		for (Integer j : k.keySet()) {
			maxIndex = j;
		}
		for (Integer key : keySet) {
			sizeMap.put(key, sizeMap.get(key) / k.get(maxIndex));
		}
		for (int i = 0; i < sizeOfMap; i++) {
			// if (sizeMap.get(i)<Math.min(DEFAULT_RATIO, threasholdRatio)) {
			if (sizeMap.get(i) < DEFAULT_RATIO) {

				mainElements.set(i, null);
			}
		}
		return mainElements;
	}

	private Set<Element> findMainCluster(Elements elementsOfInterest) {
		Set<Element> htmlElements = new LinkedHashSet<Element>();
		int beg = largestElemIndex, end = largestElemIndex, negNullCounter = 0, posNullCounter = 0, index = 0;
		while (negNullCounter <= CLUSTER_DISTANCE
				|| posNullCounter <= CLUSTER_DISTANCE) {
			if (largestElemIndex > 0 && negNullCounter < CLUSTER_DISTANCE
					&& (largestElemIndex - index) > 0
					&& elementsOfInterest.get(largestElemIndex - index) != null) {

				beg--;
			} else {
				negNullCounter++;
			}
			if (largestElemIndex > 0
					&& posNullCounter < CLUSTER_DISTANCE
					&& elementsOfInterest.size() > (largestElemIndex + index + 1)
					&& elementsOfInterest.get(largestElemIndex + index) != null) {
				end++;

			} else {
				posNullCounter++;
			}
			index++;
		}
		// System.out.println(beg+" "+end);
		// if (largestElemIndex==0) {
		// htmlElements.add(elementsOfInterest.get(0));
		// }
		while (end - beg >= 0) {
			Element element = elementsOfInterest.get(beg);
			if (element != null
					&& (element.isBlock() || POSSIBLE_TEXT_NODES.matcher(
							element.tagName()).matches())) {

				htmlElements.add(element);
			}
			beg++;
		}
		return htmlElements;
	}

	private List<Set<Element>> findClusters(Elements elements) {
		int nullCounter = 0;
		List<Set<Element>> clusters = new LinkedList<Set<Element>>();
		Set<Element> htmlElements = null;
		for (Element element : elements) {
			if (element != null
					&& (element.isBlock() || POSSIBLE_TEXT_NODES.matcher(
							element.tagName()).matches())) {

				if (htmlElements != null) {
					htmlElements.add(element);
				} else {
					htmlElements = new LinkedHashSet<Element>();
					htmlElements.add(element);
				}
				nullCounter = 0;
			} else if (element == null && htmlElements != null
					&& htmlElements.size() > 0) {
				nullCounter++;
			}
			if (nullCounter == CLUSTER_DISTANCE) {
				clusters.add(htmlElements);
				htmlElements = null;
				nullCounter = 0;
			}
		}
		if (clusters.size() == 0 && htmlElements != null) {
			clusters.add(htmlElements);
		}
		return clusters;
	}

	private Set<Element> flattenDOM(Element bodyElement) {
		final Set<Element> flatDOM = new LinkedHashSet<Element>();
		bodyElement.traverse(new NodeVisitor() {
			private int parentTextSize = 0;

			@Override
			public void head(Node node, int depth) {
				if (node instanceof Element) {
					Element innerElement = (Element) node;
					Element parentElement = innerElement.parent();
					if (parentElement != null) {
						parentTextSize = parentElement.ownText().length();
					}
					// if ((innerElement.isBlock() ||
					// POSSIBLE_TEXT_NODES.matcher(innerElement.tagName()).matches())&&
					// innerElement.text().length()>50) {

					if (innerElement.ownText().length() >= WORDS
							&& parentTextSize == 0) {
						flatDOM.add(innerElement);
					}
				}
			}

			@Override
			public void tail(Node node, int depth) {
				// System.out.println("Exiting tag: " + node.nodeName());
			}
		});

		return flatDOM;
	}
	public void setStrUnlike(String strUnlike) {
        this.strUnlike = strUnlike;
        UNLIKELY = Pattern.compile(strUnlike);
    }

    public void addStrUnlikely(String strUnlikeMatches) {
        setStrUnlike(strUnlike + "|" + strUnlikeMatches);
    }

    public void setStrPos(String strPos) {
        this.strPos = strPos;
        POSITIVE = Pattern.compile(strPos);
    }

    public void addStrPos(String str_Pos) {
        setStrPos(strPos + "|" + str_Pos);
    }

    public void setStrNeg(String strNeg) {
        this.strNeg = strNeg;
        NEGATIVE = Pattern.compile(strNeg);
    }

    public void addStrNeg(String neg) {
        setStrNeg(strNeg + "|" + neg);
    }

    public ExtractTextType extractContent(Document document) {
        if (document == null)
            throw new NullPointerException("missing document");

        // remove the clutter
        removeClutterDocument(document);

        // init elements
        Collection<Element> nodes = docToNodes(document);
        int maxWei = 0;
        Element largestElement = null;
        for (Element child : nodes) {
            int curWei = weightCalculate(child);
            if (curWei > maxWei) {
                maxWei = curWei;
                largestElement = child;
                if (maxWei > 200)
                    break;
            }
        }
        if (largestElement != null) {
        	ExtractHtml htmlProcess = new ExtractHtml();
        	String text = htmlProcess.extractElementToText(largestElement);
        	extractTextType.setExtractText(text);
        	extractTextType.setArticleHtmlString(largestElement.toString());
//          System.out.println(text);
        }
        extractTextType.setAllText(largestElement.text());
		extractTextType.setPageTitle(document.title());
		
		return extractTextType;
       
    }

    protected int weightCalculate(Element element) {
        int weight = calWeight(element);
        if(element.ownText().length() > 10)
        weight += (int) Math.round(element.ownText().length() / 100.0 * 10);
        weight += weightInnerNodes(element);
        return weight;
    }

    protected int weightInnerNodes(Element element) {
        int weight = 0;
        Element caption = null;
        List<Element> pEls = new ArrayList<Element>(5);
        for (Element child : element.children()) {
            int length = child.ownText().length();
            if (length < 20)
                continue;

            if (length > 200)
                weight += Math.max(50, length / 10);

            if (child.tagName().equals("h1") || child.tagName().equals("h2")) {
                weight += 30;
            } else if (child.tagName().equals("div") || child.tagName().equals("p")) {
                weight += calcWeightForChild(child, child.ownText());
                if (child.tagName().equals("p") && length > 50)
                    pEls.add(child);

                if (child.className().toLowerCase().equals("caption"))
                    caption = child;
            }
        }

        // use caption and image
        if (caption != null)
            weight += 30;

        if (pEls.size() >= 2) {
            for (Element childElement : element.children()) {
                if ("h1;h2;h3;h4;h5;h6".contains(childElement.tagName())) {
                    weight += 20;
                    // headerEls.add(subEl);
                } else if ("table;li;td;th".contains(childElement.tagName())) {
                    addWeight(childElement, -30);
                }

                if ("p".contains(childElement.tagName()))
                    addWeight(childElement, 30);
            }
        }
        return weight;
    }

    public void addWeight(Element element, int weight) {
        int oldElement = getWeight(element);
        setWeight(element, weight + oldElement);
    }

    public int getWeight(Element element) {
        int oldElement = 0;
        try {
            oldElement = Integer.parseInt(element.attr("weightMark"));
        } catch (Exception ex) {
        }
        return oldElement;
    }

    public void setWeight(Element element, int weight) {
        element.attr("weightMark", Integer.toString(weight));
    }

    private int calcWeightForChild(Element child, String ownText) {
        int c = count(ownText, "&quot;");
        c += count(ownText, "&lt;");
        c += count(ownText, "&gt;");
        c += count(ownText, "px");
        int val;
        if (c > 5)
            val = -30;
        else
            val = (int) Math.round(ownText.length() / 25.0);

        addWeight(child, val);
        return val;
    }

    public int count(String str, String substring) {
        int c = 0;
        int indexTemp = str.indexOf(substring);
        if (indexTemp >= 0) {
            c++;
            c += count(str.substring(indexTemp + substring.length()), substring);
        }
        return c;
    }
    private int calWeight(Element element) {
        int weight = 0;
        if (POSITIVE.matcher(element.className()).find())
            weight += 35;

        if (POSITIVE.matcher(element.id()).find())
            weight += 40;

        if (UNLIKELY.matcher(element.className()).find())
            weight -= 20;

        if (UNLIKELY.matcher(element.id()).find())
            weight -= 20;

        if (NEGATIVE.matcher(element.className()).find())
            weight -= 50;

        if (NEGATIVE.matcher(element.id()).find())
            weight -= 50;

        String style = element.attr("style");
        if (style != null && !style.isEmpty() && NEGATIVE_STYLE.matcher(style).find())
            weight -= 50;
        return weight;
    }
    protected void removeClutterDocument(Document document) {
//        stripUnlikelyCandidates(document);
        removeScriptsAndStyles(document);
    }

    protected void stripUnlikelyCandidates(Document doc) {
        for (Element child : doc.select("body").select("*")) {
            String className = child.className().toLowerCase();
            String id = child.id().toLowerCase();

            if (NEGATIVE.matcher(className).find()
                    || NEGATIVE.matcher(id).find()) {
//                print("REMOVE:", child);
                child.remove();
            }
        }
    }


    public Collection<Element> docToNodes(Document document) {
        Map<Element, Object> child = new LinkedHashMap<Element, Object>(64);
        int weight = 100;
        for (Element element : document.select("body").select("*")) {
            if (NODES.matcher(element.tagName()).matches()) {
                child.put(element, null);
                setWeight(element, weight);
                weight = weight / 2;
            }
        }
        return child.keySet();
    }

    private Document removeScriptsAndStyles(Document document) {
        Elements scripts = document.getElementsByTag("script");
        for (Element item : scripts) {
            item.remove();
        }

        Elements noscripts = document.getElementsByTag("noscript");
        for (Element item : noscripts) {
            item.remove();
        }

        Elements styles = document.getElementsByTag("style");
        for (Element style : styles) {
            style.remove();
        }

        return document;
    }

}
