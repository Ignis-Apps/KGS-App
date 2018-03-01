package de.kgs.vertretungsplan.coverPlan;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class CoverPlanAnalyser {

	
	CoverPlan getCoverPlan(Document document) throws Exception{
		
		CoverPlan coverPlan = new CoverPlan();
		Elements elements = document.getAllElements();

		int infoIndex=0;

		for(Element e : elements ){
			  		
			CoverItem cItem = null;
			  
			switch (e.className()) {

				case "info":
					if(infoIndex==0){
						getDailyInfos(e.getAllElements(),coverPlan);
						infoIndex++;
					}
					break;

				case "mon_title":
					coverPlan.title = e.ownText();
					break;
					
				case "mon_head":
					coverPlan.lastUpdate = getLastCoverPlanUpdate(e);
					break;

				case "list odd":				
					cItem = getCoverItem(e);
					break;
					
				case "list even":			
					cItem = getCoverItem(e);
					break;

			}
			
			if(cItem!=null)
				coverPlan.coverItems.add(cItem);

		}

		return coverPlan;
	}
	


	 private CoverItem getCoverItem(Element list){
		  
		  int index = 0;
  
		  CoverItem tableObject = new CoverItem();
		  
		  for(Element el: list.children()){
			  
			  Element e = el;
			  // Check if it has a span child
			  if(el.children()!=null){
			  	Elements childs = e.children();
			  	if(childs.size()>0){
			  		e = childs.get(0);
			  	}
			  }

			  switch (index){
			  case 0:
			  	  // works best with child
				  tableObject.Class = e.ownText();
				  break;
			  case 1:
			  	 // works best with child
				  tableObject.Hour = e.ownText();
				  break;	
			  case 2:
				  // works best with child
				  tableObject.Dropped = e.ownText();
				  break;
			  case 3:
			  		// works best with root
				  tableObject.Fach = el.text();
				  break;
			  case 4:
			  	  // Works best with root
				  tableObject.Room = el.text();
				  break;
			  case 5:
				  tableObject.Annotation = e.ownText();
				  break;
			  case 6:
				  tableObject.Ver_From = e.ownText();
				  break;
			  case 7:
				  tableObject.Annotation_Lesson = e.ownText();
				  break;
			  }
			  
			  index++;
			  
		  }
		  if(tableObject.Class!=null){
			  return tableObject;
		  }
		  
		  return null;
	 }
	
	 private String getLastCoverPlanUpdate(Element mon_head){
		 
		 Elements es = mon_head.children();
		 
		 String info = es.text();
		 String oString = splitAfterWord(info, "Stand:");
		 return oString.trim();
		 
	 }

	 private void getDailyInfos(Elements tElements,CoverPlan cp){


		 Elements tableHeaderEles = tElements.select("tbody tr th");

		 for (int i = 0; i < tableHeaderEles.size(); i++) {
			 cp.dailyInfoHeader = tableHeaderEles.get(i).text();
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
	 
	 private String splitAfterWord(String text, @SuppressWarnings("SameParameterValue") String split){
		 
		 StringBuilder output = new StringBuilder();
		 char[] t = text.toCharArray();
		 char[] cs= split.toCharArray();
		 int mCounter = cs.length;
		 
		 for( char c:t){

			 if(mCounter!=0){
				 if(c == cs[cs.length-mCounter]){
					 mCounter--;
				 }else {
					 mCounter = cs.length;
				 }
			 }else {
				output.append(c);
			}
			  
		 }
		 
		 return output.toString();
		 
	 }
	 
}
