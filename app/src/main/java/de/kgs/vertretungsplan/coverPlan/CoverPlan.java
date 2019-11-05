package de.kgs.vertretungsplan.coverPlan;

import android.support.annotation.NonNull;

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

			// Blacklist - Those elements will never be shown
			if(c.getTargetClass().matches(".*(A15|Pers.|Ber.|Sdm|Aufsicht).*"))
				continue;

			// Check if item is a type of class and doesn't match to the selected class id ( Constant / Do not change )
			if((c.getTargetClass().matches(".*(5|6|7|8|9|10|J1|J2).*") && !c.getTargetClass().contains(class_id)))
				continue;

			cItems.add(c);

		}

		return cItems;

	}

	@NonNull
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
