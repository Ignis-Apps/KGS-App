package de.kgs.vertretungsplan.coverPlan;

import androidx.annotation.NonNull;

public class CoverItem {

	private String mClass;		// Betroffene Klasse
	private String mHour;		// Betroffene Schulstunde/n
	private String mSubject; 	// Betroffenes Fach
	private String mRoom;		// Raum in dem es stattfindet
	private String mAnnotation;	// Anmerkung
	private String mRelocated;	// Verlegt von
	private boolean mNewEntry;	// Ist ein neuer Eintrag
	private boolean mCanceled;	// Fällt aus;

	public static class Builder{

		private String mClass;
		private String mHour;
		private String mSubject;
		private String mRoom;
		private String mAnnotation;
		private String mRelocated;
		private boolean mNewEntry;
		private boolean mCanceled;

		public Builder(){

		}

		public Builder setClass(String vClass){
			this.mClass = vClass;
			return this;
		}

		public Builder setHour(String vHour){
			this.mHour = vHour;
			return this;
		}

		public Builder setSubject(String vSubject){
			this.mSubject = vSubject;
			return this;
		}

		public Builder setRoom(String vRoom){
			this.mRoom = vRoom;
			return this;
		}

		public Builder setAnnotation(String vAnnotation){
			this.mAnnotation = vAnnotation;
			return this;
		}

		public Builder setRelocated(String vRelocated){
			this.mRelocated = vRelocated;
			return this;
		}

		public Builder isNewEntry(boolean vNewEntry){
			this.mNewEntry = vNewEntry;
			return this;
		}

		public Builder isCanceled(boolean vCanceled){
			this.mCanceled = vCanceled;
			return this;
		}

		public CoverItem build(){

			CoverItem item = new CoverItem();
			item.mClass	= this.mClass;
			item.mHour = this.mHour;
			item.mSubject = this.mSubject;
			item.mRoom = this.mRoom;
			item.mAnnotation = this.mAnnotation;
			item.mRelocated = this.mRelocated;
			item.mNewEntry = this.mNewEntry;
			item.mCanceled = this.mCanceled;

			return item;

		}

	}

	private CoverItem(){

	}

	// getClass() ist reserviert :/
	public String getTargetClass(){
		return mClass;
	}

	public String getHour(){
		return mHour;
	}

	public String getSubject(){
		return mSubject;
	}

	public String getRoom(){
		return mRoom;
	}

	public String getAnnotation(){
		return mAnnotation;
	}

	public String getRelocated(){
		return mRelocated;
	}

	public boolean isNewEntry(){
		return mNewEntry;
	}

	public boolean isCanceled(){
		// return mCanceled;	TODO Es tut zwar was es soll, aber vileicht nimmt man den Wert aus der (Entfällt) Spalte
		return mRoom.equals("---");
	}

	@NonNull
	public String toString(){

		return "Klasse : " + mClass + "\n" +
				"Betroffene Stunde : " + mHour + "\n" +
				"Betroffenes Fach : " + mSubject + "\n" +
				"Raum : " + mRoom + "\n" +
				"Verlegt von : " + mRelocated + "\n" +
				"Ist ein neuer Eintrag : " + mNewEntry + "\n" +
				"Entfällt : " + mCanceled + "\n";


	}

}
