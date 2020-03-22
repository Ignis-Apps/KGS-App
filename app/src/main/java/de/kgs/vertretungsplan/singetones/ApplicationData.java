package de.kgs.vertretungsplan.singetones;

import android.content.Context;
import android.content.SharedPreferences;

import de.kgs.vertretungsplan.coverPlan.CoverPlan;
import de.kgs.vertretungsplan.coverPlan.Grade;
import de.kgs.vertretungsplan.coverPlan.GradeSubClass;
import de.kgs.vertretungsplan.views.NavigationItem;

/**
 * This singleton contains values that are frequently changed.
 */
public class ApplicationData {

    private static final String PREFERENCE_NAME = "application_data";
    private static final ApplicationData instance = new ApplicationData();

    private CoverPlan coverPlanToday;
    private CoverPlan coverPlanTomorrow;
    private Grade currentGrade;
    private GradeSubClass currentGradeSubClass;
    private NavigationItem currentNavigationItem;

    private int currentlySelectedViewPage;

    private ApplicationData() {
    }

    public static ApplicationData getInstance() {

        if (instance == null)
            throw new AssertionError("ApplicationData has been deleted !");

        return instance;

    }

    public void loadData(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        currentGrade = Grade.getGradeByGradeLevel(preferences.getInt("grade_level", 0));
        currentGradeSubClass = GradeSubClass.getClassByClassLevel(preferences.getInt("grade_sub_class", 0));

    }

    public void saveData(Context context) {

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt("grade_level", currentGrade.getGradeLevel());
        editor.putInt("grade_sub_class", currentGradeSubClass.getClassLevel());
        editor.apply();

    }


    public void setCoverPlanToday(CoverPlan coverPlanToday) {
        this.coverPlanToday = coverPlanToday;
    }

    public void setCoverPlanTomorrow(CoverPlan coverPlanTomorrow) {
        this.coverPlanTomorrow = coverPlanTomorrow;
    }

    public void setCurrentGrade(Grade currentGrade) {
        this.currentGrade = currentGrade;
    }

    public void setCurrentGradeSubClass(GradeSubClass currentGradeSubClass) {
        this.currentGradeSubClass = currentGradeSubClass;
    }

    public void setCurrentNavigationItem(NavigationItem currentNavigationItem) {
        this.currentNavigationItem = currentNavigationItem;
    }

    public void setCurrentlySelectedViewPage(int currentlySelectedViewPage) {
        this.currentlySelectedViewPage = currentlySelectedViewPage;
    }

    public CoverPlan getCoverPlanToday() {
        return coverPlanToday;
    }

    public CoverPlan getCoverPlanTomorrow() {
        return coverPlanTomorrow;
    }

    public Grade getCurrentGrade() {
        return currentGrade;
    }

    public GradeSubClass getCurrentGradeSubClass() {
        return currentGradeSubClass;
    }

    public NavigationItem getCurrentNavigationItem() {
        return currentNavigationItem;
    }

    public int getCurrentlySelectedViewPage() {
        return currentlySelectedViewPage;
    }
}
