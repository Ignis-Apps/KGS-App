package de.kgs.vertretungsplan.coverPlan;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.kgs.vertretungsplan.views.Grade;
import de.kgs.vertretungsplan.views.GradeSubClass;


public class CoverPlan {

    public String title;
    public String lastUpdate;
    public String dailyInfoHeader = "";

    List<CoverItem> coverItems = new ArrayList<>();
    List<String> dailyInfoRows = new ArrayList<>();

    public List<CoverItem> getCoverItems(Grade grade, GradeSubClass gradeSubClass) {
        List<CoverItem> items = new LinkedList<>();
        for (CoverItem cItem : this.coverItems) {

            if (cItem.getTargetClass().matches(".*(A15|Pers.|Ber.|Sdm|Aufsicgt).*"))
                continue;

            if (!cItem.getTargetClass().contains(grade.getGradeInitials()))
                continue;

            if(!cItem.getTargetClass().contains(gradeSubClass.getClassInitials()))
                continue;

            items.add(cItem);

        }
        return items;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(this.title);
        out.append("\nZuletzt Aktualiesiert");
        out.append(this.lastUpdate);
        out.append("\nHeaders : ");
        out.append(this.dailyInfoHeader);
        out.append("\nRows : ");
        for (String r : this.dailyInfoRows) {
            out.append(" ");
            out.append(r);
        }
        for (CoverItem c : this.coverItems) {
            String str = "\n-------------------------------------";
            out.append(str);
            out.append("\n");
            out.append(c.toString());
            out.append(str);
        }
        return out.toString();
    }

    /* access modifiers changed from: 0000 */
    public CoverItem[] getCoverItems() {
        CoverItem[] c = new CoverItem[this.coverItems.size()];
        for (int i = 0; i < this.coverItems.size(); i++) {
            c[i] = (CoverItem) this.coverItems.get(i);
        }
        return c;
    }

    public String getDailyInfoMessage() {
        StringBuilder out = new StringBuilder();
        for (String s : this.dailyInfoRows) {
            out.append(s);
            if (this.dailyInfoRows.size() > 1) {
                out.append("\n");
            }
        }
        return out.toString();
    }
}
