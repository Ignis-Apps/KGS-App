package de.kgs.vertretungsplan.coverPlan;

import java.util.ArrayList;
import java.util.List;

public class CoverPlan {

	public String title;
	public String lastUpdate;
	public String dailyInfoHeader = ""; // Sollte nie mehr als einen geben
	public List<String> dailyInfoRows = new ArrayList<>();
	public List<CoverItem> coverItems = new ArrayList<>();
	
	public List getCoverItemsForClass(String class_id){
		
		List<CoverItem> cItems = new ArrayList<>();
		for(CoverItem c:coverItems){

			if(!(class_id.isEmpty() || c.Class.contains("5") || c.Class.contains("6") || c.Class.contains("7") ||
				c.Class.contains("8") || c.Class.contains("9") || c.Class.contains("10") || c.Class.contains("J1") ||
				c.Class.contains("J2")|| c.Class.contains("A15")|| c.Class.contains("Pers.")|| c.Class.contains("Ber.")||
				c.Class.contains("Sdm")))
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

		String out = "";
		out+=title;
		out+="\nZuletzt Aktualiesiert"+lastUpdate;
		out+="\nHeaders : " + dailyInfoHeader;
		//for(String h:dailyInfoHeaders){out+=" "+h;}

		out+="\nRows : ";
		for(String r:dailyInfoRows){
			out+=" "+r;
		}

		for(CoverItem c:coverItems){
			out+="\n-------------------------------------";
			out+="\n"+c.toString();
			out+="\n-------------------------------------";
		}
		return out;
	}

	public void log(){

		System.out.println(toString());

		/*
		System.out.println(title);
		System.out.println("Zuletzt Aktualiesiert : " + lastUpdate);
		
		for(CoverItem c:coverItems){
			
			System.out.println("-------------------------------------");
			System.out.println("Klasse: " +c.Class );
			System.out.println("Stunde: " +c.Hour );
			System.out.println("Entfaellt: " +c.Dropped );
			System.out.println("Fach: " +c.Fach );
			System.out.println("Raum: " +c.Room );
			System.out.println("Bemerkung zur Vertretung: " +c.Annotation );
			System.out.println("Verlegt von: " +c.Ver_From );
			System.out.println("Bemerkung zum Unterricht: " +c.Annotation_Lesson );
			System.out.println("-------------------------------------");
			
		}
		*/
	}

	public CoverItem[] getCoverItems(){

		CoverItem[] c = new CoverItem[coverItems.size()];

		for (int i=0;i<coverItems.size();i++){
			c[i] = coverItems.get(i);
		}

		return c;

	}

	public String getDailyInfoMessage(){
		String out = "";
		for(String s:dailyInfoRows){
			out+=s;
			if(dailyInfoRows.size()>1){
				out+="\n";
			}
		}
		return out;
	}
	
}
