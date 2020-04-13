package de.kgs.vertretungsplan.coverPlan;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.kgs.vertretungsplan.singetones.ApplicationData;

public class CoverPlan {

    // Legacy ( cant change them now since i have no test data and they are used for saving )
    private String title;
    private String lastUpdate;
    private String dailyInfoHead;
    private List<CoverItem> coverItems = new ArrayList<>();
    private List<String> dailyInfoBody = new ArrayList<>();

    // New Values ( Those get parsed from the legacy ones, but they are not involved in the save process)
    private String affectedWeekday;         // ( Montag, Dienstag, ... )
    private String dailyMessageTitle;
    private String dailyMessageText;
    private String lastUpdateText;


    private CoverPlan() {
    }

    public String getTitle() {
        return title;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public String getDailyInfoHead() {
        return dailyInfoHead;
    }

    public List<String> getDailyInfoBody() {
        return dailyInfoBody;
    }

    public CoverItem[] getCoverItems() {
        return coverItems.toArray(new CoverItem[0]);
    }


    public List<CoverItem> getCoverItemsFiltered() {

        ApplicationData data = ApplicationData.getInstance();
        return getCoverItems(data.getCurrentGrade(), data.getCurrentGradeSubClass());

    }

    public String getWeekDay() {
        return affectedWeekday;
    }

    public String getLastUpdateText() {
        return lastUpdateText;
    }

    public String getNavigationText() {
        return affectedWeekday + ", " + title.split(" ")[0];
    }

    public List<CoverItem> getCoverItems(Grade grade, GradeSubClass gradeSubClass) {
        List<CoverItem> items = new LinkedList<>();
        for (CoverItem cItem : this.coverItems) {

            if (cItem.getTargetClass().matches(".*(A15|Pers.|Ber.|Sdm|Aufsicgt).*"))
                continue;

            if (!cItem.getTargetClass().contains(grade.getGradeInitials()))
                continue;

            if (grade.hasSubClasses() && !cItem.getTargetClass().contains(gradeSubClass.getClassInitials()))
                continue;

            items.add(cItem);

        }
        return items;
    }

    public String getDailyInfoMessage() {

        StringBuilder out = new StringBuilder();
        for (String s : dailyInfoBody) {
            out.append(s);
            if (this.dailyInfoBody.size() > 1) {
                out.append("\n");
            }
        }
        return out.toString();
    }

    public static class Builder {

        private String title;
        private String lastUpdate;
        private String dailyInfo;
        private List<CoverItem> items;
        private List<String> dailyInfoBody;

        private static String getWeekDay(String title) {
            return title.split(" ")[1].replace(",", "");
        }

        private static String getLastUpdateText(String lastUpdate) {
            String str = "Fehler";
            DateFormat sourceFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
            DateFormat targetFormatToolbar = new SimpleDateFormat("'Stand:' d. MMMM | HH:mm", Locale.GERMANY);
            try {
                Date date = sourceFormat.parse(lastUpdate);
                return targetFormatToolbar.format(date != null ? date : str);
            } catch (ParseException e) {
                return str;
            }
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setLastUpdate(String lastUpdate) {
            this.lastUpdate = lastUpdate;
            return this;
        }

        public Builder setCoverItems(List<CoverItem> items) {
            this.items = items;
            return this;
        }

        public Builder setDailyInfoBody(List<String> dailyInfoBody) {
            this.dailyInfoBody = dailyInfoBody;
            return this;
        }

        public Builder setDailyInfo(String dailyInfo) {
            this.dailyInfo = dailyInfo;
            return this;
        }

        public CoverPlan build() {
            CoverPlan coverPlan = new CoverPlan();
            coverPlan.title = title;
            coverPlan.lastUpdate = lastUpdate;
            coverPlan.coverItems = items;
            coverPlan.dailyInfoHead = dailyInfo;
            coverPlan.dailyInfoBody = dailyInfoBody;

            coverPlan.affectedWeekday = getWeekDay(title);
            coverPlan.lastUpdateText = getLastUpdateText(lastUpdate);
            return coverPlan;
        }


    }

}
