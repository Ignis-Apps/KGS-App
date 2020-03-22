package de.kgs.vertretungsplan.loader;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import de.kgs.vertretungsplan.coverPlan.CoverItem;
import de.kgs.vertretungsplan.coverPlan.CoverPlan;

class CoverPlanAnalyser {


    private static final String[] correctHeaders = new String[]{"Klasse(n)", "Stunde", "Fach", "Raum", "Anmerkung", "Vertr. von", "Neu", "Entfall"};

    static CoverPlan getCoverPlan(Document document) throws Exception {

        CoverPlan.Builder coverPlan = new CoverPlan.Builder();

        validateParser(document.getElementsByClass("list").first());

        int infoIndex = 0;

        List<CoverItem> coverItems = new ArrayList<>();

        for (Element element : document.getAllElements()) {

            switch (element.className()) {
                case "info":
                    if (infoIndex != 0)
                        break;
                    coverPlan.setDailyInfo(getDailyInfoHeader(element.getAllElements()));
                    coverPlan.setDailyInfoBody(getDailyInfoMessage(element.getAllElements()));
                    infoIndex++;
                    break;
                case "mon_title":
                    coverPlan.setTitle(element.ownText());
                    break;
                case "mon_head":
                    coverPlan.setLastUpdate(getLastUpdated(element));
                    break;
                case "list odd":
                case "list even":
                    coverItems.add(getCoverItem(element));
                    break;
            }
        }
        coverPlan.setCoverItems(coverItems);
        return coverPlan.build();
    }

    // TODO : Error handling
    private static void validateParser(Element dataHeader) {

        if (dataHeader.children() == null || dataHeader.children().size() < 8)
            System.err.println("HTML Parser will not work");

        for (int i = 0; i < 8; i++)
            if (!dataHeader.child(i).text().equals(correctHeaders[i])) {
                System.err.println("HTML might not work !");
                return;
            }
    }

    private static CoverItem getCoverItem(Element dataRow) throws IndexOutOfBoundsException {

        return new CoverItem.Builder()
                .setClass(dataRow.child(0).text())
                .setHour(dataRow.child(1).text())
                .setSubject(dataRow.child(2).text())
                .setRoom(dataRow.child(3).text())
                .setAnnotation(dataRow.child(4).text())
                .setRelocated(dataRow.child(5).text())
                .isNewEntry(dataRow.child(6).text().equals("X"))
                .isCanceled(dataRow.child(7).text().equals("X"))
                .build();

    }

    private static String getLastUpdated(Element mon_head) {
        Elements es = mon_head.children();
        String info = es.text();
        final String split = "Stand:";
        return info.substring(info.indexOf(split) + split.length()).trim();
    }


    // cp.dailyInfoHeader =
    private static String getDailyInfoHeader(Elements elements) {

        Elements tableHeaderElements = elements.select("tbody tr th");

        // TODO but currently there is no data
        for (int i = 0; i < tableHeaderElements.size(); i++) {
            return tableHeaderElements.get(i).text();
        }
        return null;

    }

    private static List<String> getDailyInfoMessage(Elements elements) {

        Elements tableRowElements = elements.select(":not(thead) tr");
        List<String> dailyInfoBody = new ArrayList<>();

        for (int i = 0; i < tableRowElements.size(); i++) {
            Element row = tableRowElements.get(i);
            Elements rowItems = row.select("td");
            StringBuilder r = new StringBuilder();
            for (int j = 0; j < rowItems.size(); j++) {
                if (j != 0)
                    r.append(" ");
                r.append(rowItems.get(j).text());
            }
            dailyInfoBody.add(r.toString());
        }
        return dailyInfoBody;

    }

    private static void processDailyInfo(Elements tElements, CoverPlan cp) {


    }

}