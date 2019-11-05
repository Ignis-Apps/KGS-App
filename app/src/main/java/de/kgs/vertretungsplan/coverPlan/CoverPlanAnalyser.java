package de.kgs.vertretungsplan.coverPlan;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class CoverPlanAnalyser {


	static CoverPlan getCoverPlan(Document document) throws Exception{
		
		CoverPlan coverPlan = new CoverPlan();

		validateParser(document.getElementsByClass("list").first());

		int infoIndex = 0;

		for(Element e : document.getAllElements() ){

			switch (e.className()) {

				case "info":
					if( infoIndex == 0 ){
						processDailyInfo(e.getAllElements(),coverPlan);
						infoIndex++;
					}
					break;

				case "mon_title":
					coverPlan.title = e.ownText();
					break;
					
				case "mon_head":
					processLastCoverPlanUpdate(coverPlan, e);
					break;

				case "list odd": case "list even":
					processCoverItem(coverPlan, e);
					break;

			}

		}

		return coverPlan;
	}

	 private static final String[] correctHeaders = new String[]{"Klasse(n)","Stunde","Fach","Raum","Anmerkung","Vertr. von","Neu","Entfall"};

	 // TODO : Error handling
	 private static void validateParser(Element dataHeader){

		if( dataHeader.children() == null || dataHeader.children().size() < 8 )
			System.err.println("HTML Parser will not work");

		for(int i = 0; i < 8; i++ )
			if (!dataHeader.child(i).text().equals(correctHeaders[i])){
				System.err.println("HTML might not work !");
				return;
			}
	}

	 private static void processCoverItem(CoverPlan plan, Element dataRow) throws IndexOutOfBoundsException{

		plan.coverItems.add(new CoverItem.Builder()
				 .setClass(dataRow.child(0).text())
				 .setHour(dataRow.child(1).text())
				 .setSubject(dataRow.child(2).text())
				 .setRoom(dataRow.child(3).text())
				 .setAnnotation(dataRow.child(4).text())
				 .setRelocated(dataRow.child(5).text())
				 .isNewEntry(dataRow.child(6).text().equals("X"))
				 .isCanceled(dataRow.child(7).text().equals("X"))
				 .build());

	 }

	 private static void processLastCoverPlanUpdate(CoverPlan coverplan, Element mon_head){
		 
		 Elements es = mon_head.children();
		 String info = es.text();
		 final String split = "Stand:";
		 coverplan.lastUpdate = info.substring(info.indexOf(split)+split.length()).trim();

	 }

	 private static void processDailyInfo(Elements tElements, CoverPlan cp){


		 Elements tableHeaderElements = tElements.select("tbody tr th");

		 for (int i = 0; i < tableHeaderElements.size(); i++) {
			 cp.dailyInfoHeader = tableHeaderElements.get(i).text();
		 }

		 Elements tableRowElements = tElements.select(":not(thead) tr");

		 for (int i = 0; i < tableRowElements.size(); i++) {
			 Element row = tableRowElements.get(i);
			 Elements rowItems = row.select("td");
			 StringBuilder r = new StringBuilder();
			 for (int j = 0; j < rowItems.size(); j++) {
				if(j!=0)
					r.append(" ");
			 	r.append(rowItems.get(j).text());
			 }
			 cp.dailyInfoRows.add(r.toString());

		 }


	 }

}
