package de.kgs.vertretungsplan.coverPlan;

public class CoverItem {

    private String mClass;
    private String mHour;
    private String mSubject;
    private String mRoom;

    private String mAnnotation;
    private String mRelocated;

    private boolean mNewEntry;
    private boolean mCanceled;

    private CoverItem() {
    }

    public String getTargetClass() {
        return this.mClass;
    }

    public String getHour() {
        return this.mHour;
    }

    public String getSubject() {
        return this.mSubject;
    }

    public String getRoom() {
        return this.mRoom;
    }

    public String getAnnotation() {
        return this.mAnnotation;
    }

    public String getRelocated() {
        return this.mRelocated;
    }

    public boolean isNewEntry() {
        return this.mNewEntry;
    }

    public boolean isCanceled() {
        return this.mRoom.equals("---");
    }

    public static class Builder {

        private String mClass;
        private String mHour;
        private String mSubject;
        private String mRoom;

        private String mAnnotation;
        private String mRelocated;
        private boolean mNewEntry;
        private boolean mCanceled;

        public Builder setClass(String vClass) {
            this.mClass = vClass;
            return this;
        }

        public Builder setHour(String vHour) {
            this.mHour = vHour;
            return this;
        }

        public Builder setSubject(String vSubject) {
            this.mSubject = vSubject;
            return this;
        }

        public Builder setRoom(String vRoom) {
            this.mRoom = vRoom;
            return this;
        }

        public Builder setAnnotation(String vAnnotation) {
            this.mAnnotation = vAnnotation;
            return this;
        }

        public Builder setRelocated(String vRelocated) {
            this.mRelocated = vRelocated;
            return this;
        }

        public Builder isNewEntry(boolean vNewEntry) {
            this.mNewEntry = vNewEntry;
            return this;
        }

        public Builder isCanceled(boolean vCanceled) {
            this.mCanceled = vCanceled;
            return this;
        }

        public CoverItem build() {
            CoverItem item = new CoverItem();
            item.mClass = this.mClass;
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
}
