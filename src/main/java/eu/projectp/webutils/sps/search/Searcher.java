package eu.projectp.webutils.sps.search;

import eu.projectp.webutils.sps.scrape.Scraper;
import eu.projectp.webutils.sps.util.Result;
import eu.projectp.webutils.sps.util.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Searcher {

    private static final Scraper scraper = new Scraper();
    private static JSONObject SUBST_PLAN;
    public static boolean webUntisOffline = false;
    private static final String RESULT_TEMPLATE = """
            <h3 style="text-align: center">Klasse: %class%</h3>
            %cancel_text%
            <p style="text-align: center">Stunde: <span style="color: white">%hours%</span></p>
            <p style="text-align: center">Fach: <span style="color: white">%subject%</span></p>
            <p style="text-align: center">Raum: <span style="color: white">%room%</span></p>
            <p style="text-align: center">Lehrkraft: <span style="color: white">%teacher%</span></p>
            <p style="text-align: center">Info Text: <span style="color: gray">%info_text%</span></p>
            """;
    private static final String RESULT_TEMPLATE0 = """
            <p style="font-family:'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: white; text-align: center">%class% <a href="%search_link%" class="search_button">Suchen</a></p>
            """;

    public static void start() {
        System.out.println("[Searcher/INFO] Starting WebScraper...");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                scrape();
            }
        }, 0, 60000 * 5);

        System.out.println("[Searcher/INFO] WebScraper started!");
    }

    private static void scrape() {
        try {
            System.out.println("[Searcher/INFO] Scraping...");

            Document doc = scraper.scrape();

            System.out.println("[Searcher/INFO] Parsing page...");

            doc.normalize();
            Element docElement = doc.getDocumentElement();
            parsePage(docElement);

            System.out.println("[Searcher/INFO] Reloaded page successfully!");
        } catch (Exception e) {
            webUntisOffline = true;
            scrape();
        }
    }

    private static void parsePage(Element root) {
        Node body = getNodeByClassIndex(root, "claro dj_a11y dijit_a11y WebUntisMonitor", 0);
        Node substToday = getNodeByTypeIndex(body, "div", 1);
        Node substTomorrow = getNodeByTypeIndex(body, "div", 3);

        JSONObject substPlan = new JSONObject();

        JSONObject substPlanToday = parseSubstPlan(substToday);
        JSONObject substPlayTomorrow = parseSubstPlan(substTomorrow);

        substPlan.put("today", substPlanToday);
        substPlan.put("tomorrow", substPlayTomorrow);

        SUBST_PLAN = substPlan;
    }

    private static JSONObject parseSubstPlan(Node parent) {
        JSONObject substObj = new JSONObject();
        JSONObject headerObj = new JSONObject();
        substObj.put("header", headerObj);

        Node c0 = getNodeByClassIndex(parent, "gp_SubstitutionMonitor", 0);

        Node header = getNodeByTypeIndex(c0, "div", 0);
        Node title = getNodeByClassIndex(header, "title", 0);
        Node status = getNodeByClassIndex(header, "status", 0);
        Node titleText = getNodeByTypeIndex(title, "span", 0);

        headerObj.put("date", titleText.getTextContent().trim());
        headerObj.put("status", status.getTextContent().trim());


        Node c1 = getNodeByClassIndex(c0, "grupet_widget_ScrollableTable grupet_widget_AutoScrollingTable", 0);
        Node c2 = getNodeByTypeIndex(c1, "div", 1);
        Node table = getNodeByTypeIndex(c2, "table", 0);
        Node tableBody = getNodeByTypeIndex(table, "tbody", 0);

        JSONArray substList = new JSONArray();
        for (int i = 0; i < tableBody.getChildNodes().getLength(); i++) {
            Node node = tableBody.getChildNodes().item(i);
            if (node == null) continue;
            if (!node.getNodeName().equals("tr")) continue;
            if (node.getAttributes() == null) continue;
            if (node.getAttributes().getNamedItem("class") == null) continue;
            String className = node.getAttributes().getNamedItem("class").getNodeValue();
            if (!className.contains("odd") && !className.contains("even")) continue;

            String substedClass = getNodeByTypeIndex(node, "td", 0).getTextContent().trim();
            String hours = getNodeByTypeIndex(node, "td", 2).getTextContent().trim();
            String classes = getNodeByTypeIndex(node, "td", 3).getTextContent().trim();
            String subject = getNodeByTypeIndex(node, "td", 4).getTextContent().trim();
            String room = getNodeByTypeIndex(node, "td", 5).getTextContent().trim().replaceAll("\n", "").replaceAll(" ", "");
            String teacher = getNodeByTypeIndex(node, "td", 6).getTextContent().trim();
            String infoText = getNodeByTypeIndex(node, "td", 7).getTextContent().trim();

            if (substedClass.endsWith("F")) continue;

            JSONObject subst = new JSONObject();
            subst.put("class", substedClass);
            subst.put("hours", hours);
            subst.put("classes", classes);
            if (subject.equals("SF")) {
                subst.put("subject", subject + " (Schülerfirma)");
            } else {
                subst.put("subject", subject);
            }
            subst.put("room", room);
            subst.put("teacher", teacher);
            subst.put("info_text", infoText);
            subst.put("cancelled", !getNodeByClassIndex(node, " cancelStyle", 0).equals(node));

            substList.put(subst);
        }

        substObj.put("list", substList);
        return substObj;
    }

    public static Result search(String query, String day) {
        if (SUBST_PLAN == null) {
            return new Result(null, false, Utils.getWebPage("wait"));
        }
        try {
            List<JSONObject> foundQuery = searchQuery(query, SUBST_PLAN.getJSONObject(day));

            if (foundQuery.isEmpty()) {
                return new Result(null, false, Utils.getWebPage("not_found"));
            } else {
                AtomicReference<String> result = new AtomicReference<>(Utils.getWebPage("result"));
                AtomicInteger i = new AtomicInteger(0);
                foundQuery.forEach(jsonObject -> {
                    String str = RESULT_TEMPLATE + "<hr>";
                    if (i.get() == 0) {
                        str = "<hr>" + str;
                    }

                    str = str
                            .replaceAll("%class%", jsonObject.getString("class"))
                            .replaceAll("%hours%", jsonObject.getString("hours"))
                            .replaceAll("%subject%", jsonObject.getString("subject"))
                            .replaceAll("%room%", jsonObject.getString("room"))
                            .replaceAll("%teacher%", jsonObject.getString("teacher"))
                            .replaceAll("%info_text%", jsonObject.getString("info_text"));

                    if (jsonObject.getBoolean("cancelled")) {
                        str = str.replaceAll("%cancel_text%", "<p style=\"text-align: center; color: crimson\">Entfällt</p>");
                    } else {
                        str = str.replaceAll("%cancel_text%", "");
                    }
                    result.set(result.get().replaceFirst("%a%", str));
                    i.incrementAndGet();
                });

                result.set(result.get().replaceAll("%a%", "").replaceAll("%cancel_text%", ""));

                return new Result(result.get(), true, null);
            }
        } catch (Throwable e) {
            return Result.getErrorFromException(e);
        }
    }

    private static List<JSONObject> searchQuery(String query, JSONObject plan) {
        List<JSONObject> list = new ArrayList<>();
        for (Object o : plan.getJSONArray("list")) {
            JSONObject obj = (JSONObject) o;
            if (obj.getString("class").equals(query)) {
                list.add(obj);
            }
        }
        return list;
    }

    public static Result searchClasses(String day) {
        if (SUBST_PLAN == null) {
            return new Result(null, false, Utils.getWebPage("wait"));
        }
        String result = Utils.getWebPage("show_classes");
        TreeMap<String, Integer> classes = new TreeMap<>((o1, o2) -> {
            try {
                return -Float.compare(Float.parseFloat(o1), Float.parseFloat(o2));
            } catch (NumberFormatException e) {
                return -1;
            }
        });
        for (Object o : SUBST_PLAN.getJSONObject(day).getJSONArray("list")) {
            JSONObject subst = (JSONObject) o;
            if (!classes.containsKey(subst.getString("class"))) {
                classes.put(subst.getString("class"), 1);
            } else {
                classes.put(subst.getString("class"), classes.get(subst.getString("class")) + 1);
            }
        }
        for (Map.Entry<String, Integer> set : classes.entrySet()) {
            String TEMPLATE = RESULT_TEMPLATE0;
            if (set.getValue() > 1) {
                TEMPLATE = TEMPLATE.replaceAll("%class%", "<span style=\"font-weight: bold;\">" + set.getKey() + "</span> (" + set.getValue() + " Vertretungen)");
            } else {
                TEMPLATE = TEMPLATE.replaceAll("%class%", "<span style=\"font-weight: bold;\">" + set.getKey() + "</span> (1 Vertretung)");
            }
            TEMPLATE = TEMPLATE.replaceAll("%search_link%", "/search_" + day + "?class=" + set.getKey());
            result = result.replaceFirst("%a%", TEMPLATE);
        }
        result = result.replaceAll("%a%", "");
        return new Result(result, true, null);
    }

    private static Node getNodeByTypeIndex(Node parent, String type, int index) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            Node node = parent.getChildNodes().item(i);
            if (node == null) continue;
            if (node.getNodeName().equals(type)) {
                nodes.add(node);
            }
        }
        if (!nodes.isEmpty()) {
            return nodes.get(index);
        }
        return parent;
    }

    private static Node getNodeByClassIndex(Node parent, String className, int index) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            Node node = parent.getChildNodes().item(i);
            if (node == null) continue;
            if (node.getAttributes() == null) continue;
            if (node.getAttributes().getNamedItem("class") == null) continue;
            if (node.getAttributes().getNamedItem("class").getNodeValue().equals(className)) {
                nodes.add(node);
            }
        }
        if (!nodes.isEmpty()) {
            return nodes.get(index);
        }
        return parent;
    }

    private static void printClass(Node node) {
        if (node.getAttributes() == null) return;
        if (node.getAttributes().getNamedItem("class") == null) return;
        System.out.println("Node Class: " + node.getAttributes().getNamedItem("class").getNodeValue());
    }

}
