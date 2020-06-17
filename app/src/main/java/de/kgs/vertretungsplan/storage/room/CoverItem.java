package de.kgs.vertretungsplan.storage.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cover_item_table")
public class CoverItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "target_class")
    private String grade;
    private String hour;
    private String subject;
    private String room;

    private String annotation;
    private String relocated;

    private boolean recentlyUpdated;
    private boolean canceled;

    @ColumnInfo(name = "target_day")
    private String day;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getRelocated() {
        return relocated;
    }

    public void setRelocated(String relocated) {
        this.relocated = relocated;
    }

    public boolean isRecentlyUpdated() {
        return recentlyUpdated;
    }

    public void setRecentlyUpdated(boolean recentlyUpdated) {
        this.recentlyUpdated = recentlyUpdated;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
