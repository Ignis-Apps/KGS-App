package de.kgs.vertretungsplan.coverPlan;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CoverPlanAnalyser {

	
	public CoverPlan getCoverPlan(Document document) throws Exception{
		
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
					
					String lastUpdated = getLastCoverPlanUpdate(e);
					coverPlan.lastUpdate = lastUpdated;
					
					break;
				
				case "list odd":				
					cItem = getCoverItem(e);
					break;
					
				case "list even":			
					cItem = getCoverItem(e);
					break;

				default:
					
					break;
			}
			
			if(cItem!=null){
				coverPlan.coverItems.add(cItem);
			}

		  }


		if(coverPlan.title == null){
            if(coverPlan.title.trim().isEmpty()){
                throw new Exception("Title dosen't exsist");
            }
		}

		return coverPlan;
		
	}
	


	 public CoverItem getCoverItem(Element list){
		  
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
	
	 public String getLastCoverPlanUpdate(Element mon_head){
		 
		 Elements es = mon_head.children();
		 
		 String info = es.text();
		 String oString = splitAfterWord(info, "Stand:");
		 return oString.trim();
		 
	 }

	 public void getDailyInfos(Elements tElements,CoverPlan cp){

		 //System.out.println(tElements);

		 Elements tableElements = tElements;
		 String headers = "Headers : ";
		 String rows = "Rows : ";

		 Elements tableHeaderEles = tableElements.select("tbody tr th");

		 System.out.println("tableHeaderEles.size()" +  tableHeaderEles.size());

		 for (int i = 0; i < tableHeaderEles.size(); i++) {
			 cp.dailyInfoHeader = tableHeaderEles.get(i).text();
		 }


		 Elements tableRowElements = tableElements.select(":not(thead) tr");

		 for (int i = 0; i < tableRowElements.size(); i++) {
			 Element row = tableRowElements.get(i);
			 Elements rowItems = row.select("td");
			 String r = "";
			 for (int j = 0; j < rowItems.size(); j++) {
				if(j!=0)
					r+=" ";
			 	r+=(rowItems.get(j).text());
			 }
			 cp.dailyInfoRows.add(r);

		 }

		// System.out.println("HEADERS : " + cp.dailyInfoHeaders.toArray().toString());
		 //System.out.println("ROWS : " + cp.dailyInfoRows.toArray().toString());

	 }
	 
	 public String splitAfterWord(String text, String split){
		 
		 String output = "";
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
				output+=c;
			}
			  
		 }
		 
		 return output;
		 
	 }
	 
}
