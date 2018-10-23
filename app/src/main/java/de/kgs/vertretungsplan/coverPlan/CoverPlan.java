package de.kgs.vertretungsplan.coverPlan;

import java.util.ArrayList;
import java.util.List;

public class CoverPlan {

	public String title;
	public String lastUpdate;
	public String dailyInfoHeader = "";
	List<String> dailyInfoRows = new ArrayList<>();
	List<CoverItem> coverItems = new ArrayList<>();
	
	public List<CoverItem> getCoverItemsForClass(String class_id){
		
		List<CoverItem> cItems = new ArrayList<>();
		for(CoverItem c:coverItems){

			if(!(class_id.isEmpty() || c.Class.contains("5") || c.Class.contains("6") || c.Class.contains("7") ||
				c.Class.contains("8") || c.Class.contains("9") || c.Class.contains("10") || c.Class.contains("J1") ||
				c.Class.contains("J2")|| c.Class.contains("A15")|| c.Class.contains("Pers.")|| c.Class.contains("Ber.")||
				c.Class.contains("Sdm") || c.Fach.contains("Aufsicht")))
			{
				cItems.add(c);
			}

			if(c.Class.contains(class_id)) {
				cItems.add(c);
			}
			
		}
		
		return cItems;
		
	}

	public String toString(){

		StringBuilder out = new StringBuilder();
		out.append(title);
		out.append("\nZuletzt Aktualiesiert").append(lastUpdate);
		out.append("\nHeaders : ").append(dailyInfoHeader);

		out.append("\nRows : ");
		for(String r:dailyInfoRows){
			out.append(" ").append(r);
		}

		for(CoverItem c:coverItems){
			out.append("\n-------------------------------------");
			out.append("\n").append(c.toString());
			out.append("\n-------------------------------------");
		}
		return out.toString();
	}

	CoverItem[] getCoverItems(){

		CoverItem[] c = new CoverItem[coverItems.size()];

		for (int i=0;i<coverItems.size();i++){
			c[i] = coverItems.get(i);
		}

		return c;

	}

	public String getDailyInfoMessage(){
		StringBuilder out = new StringBuilder();
		for(String s:dailyInfoRows){
			out.append(s);
			if(dailyInfoRows.size()>1){
				out.append("\n");
			}
		}
		return out.toString();
	}
	
}
