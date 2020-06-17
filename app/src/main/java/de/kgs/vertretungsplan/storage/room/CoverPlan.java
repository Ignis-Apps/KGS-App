package de.kgs.vertretungsplan.storage.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cover_plan_meta_data_table")
public class CoverPlan {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "target_day")
    private String day;
    private String title;
    private String lastUpdate;
    private String dailyInfoHead;
    private String dailyInfoBody;
    private String affectedWeekDay;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getDailyInfoHead() {
        return dailyInfoHead;
    }

    public void setDailyInfoHead(String dailyInfoHead) {
        this.dailyInfoHead = dailyInfoHead;
    }

    public String getDailyInfoBody() {
        return dailyInfoBody;
    }

    public void setDailyInfoBody(String dailyInfoBody) {
        this.dailyInfoBody = dailyInfoBody;
    }

    public String getAffectedWeekDay() {
        return affectedWeekDay;
    }

    public void setAffectedWeekDay(String affectedWeekDay) {
        this.affectedWeekDay = affectedWeekDay;
    }
}
