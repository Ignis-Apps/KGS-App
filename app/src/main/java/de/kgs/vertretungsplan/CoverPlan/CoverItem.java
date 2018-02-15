package de.kgs.vertretungsplan.CoverPlan;

public class CoverItem {

	public String Class;			 // Klasse die vom Entfall betroffen ist
	public String Hour;				 // Welche Stunden sin betroffen
	public String Dropped;			 // Entf�llt die Stunde
	public String Fach;				 // Vertretende Fach
	public String Room;				 // Raum der Vertretungsstunde
	public String Annotation;		 // Bemerkung zur Vertretung
	public String Ver_From;			 // Verlegt von
	public String Annotation_Lesson; // Bemerkung zum Unterricht
	
	
	public String toString(){
		
		String Output = "Klasse : " + Class + "\nZu entfallende Stunde : " + Hour + "\nEntfaellt : " + Dropped + "\nFach : " + Fach ;
		Output += "\nRaum :  " + Room + "\nBemerkung : " + Annotation + "\nVerlegt von : " + Ver_From + "\nBemerkung zum Unterricht : " + Annotation_Lesson;
		return Output;
		
	}
	
	public boolean getsDropped(){
		return Dropped.equals("x");
	}
}
