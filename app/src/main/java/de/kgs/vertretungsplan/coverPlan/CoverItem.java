package de.kgs.vertretungsplan.coverPlan;

public class CoverItem {

	public String Class;			 // Klasse die vom Entfall betroffen ist
	public String Hour;				 // Welche Stunden sin betroffen
	public String Fach;				 // Vertretende Fach
	public String Room;				 // Raum der Vertretungsstunde
	public String Annotation;		 // Bemerkung zur Vertretung
	public String Ver_From;			 // Verlegt von
	public String New; 				 // Neu (Ja/Nein)
	public String Entfall;			 // Entfall (Ja/Nein)
	
	
	public String toString(){
		
		String Output = "Klasse : " + Class + "\nZu entfallende Stunde : " + Hour + "\nFach : " + Fach ;
		Output += "\nRaum :  " + Room + "\nBemerkung : " + Annotation + "\nVerlegt von : " + Ver_From + "\nNeu : " + New + "\nEntfall : " + Entfall;
		return Output;
		
	}
	
	public boolean getsDropped(){
		return Room.equals("---");
	}
}
