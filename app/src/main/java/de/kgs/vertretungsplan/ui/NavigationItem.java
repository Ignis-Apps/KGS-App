package de.kgs.vertretungsplan.ui;

import de.kgs.vertretungsplan.R;

public enum NavigationItem {

    BLACK_BOARD(R.id.nav_black_board, true),

    COVER_PLAN_TODAY(R.id.nav_today, true),

    COVER_PLAN_TOMORROW(R.id.nav_tomorrow, true),

    NEWS(R.id.nav_school_website_news, false),

    APPOINTMENTS(R.id.nav_school_website_events, false),

    PRESS(R.id.nav_school_website_press, false),

    CANTEEN_PLAN(R.id.nav_school_mensa, false),

    WEBSITE(R.id.nav_school_website, false),

    MOODLE(R.id.nav_moodle, false),

    NEWSLETTER(R.id.nav_school_newsletter, false),

    STUDENT_NEWS_PAPER(-1, false);

    private final int resourceId;
    private final boolean onPageViewer;

    NavigationItem(int resourceId, boolean onPageViewer) {
        this.resourceId = resourceId;
        this.onPageViewer = onPageViewer;
    }

    public static NavigationItem getNavigationItemById(int resourceId) {

        for (NavigationItem e : values()) {
            if (e.resourceId == resourceId) {
                return e;
            }
        }
        throw new AssertionError("Enum not implemented !");
    }

    /**
     * Returns whether the enum is an element of the viewpager or not
     *
     * @return True if enum is element. False otherwise
     */
    public boolean isOnPageViewer() {
        return onPageViewer;
    }
}
