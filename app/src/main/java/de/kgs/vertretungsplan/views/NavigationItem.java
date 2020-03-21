package de.kgs.vertretungsplan.views;

import de.kgs.vertretungsplan.R;

public enum NavigationItem {

    BLACK_BOARD(R.id.nav_black_board),

    COVER_PLAN_TODAY(R.id.nav_today),

    COVER_PLAN_TOMORROW(R.id.nav_tomorrow),

    NEWS(R.id.nav_school_website_news),

    APPOINTMENTS(R.id.nav_school_website_events),

    PRESS(R.id.nav_school_website_press),

    CANTEEN_PLAN(R.id.nav_school_mensa),

    WEBSITE(R.id.nav_school_website),

    MOODLE(R.id.nav_moodle),

    NEWSLETTER(R.id.nav_school_newsletter);

    private final int resourceId;

    NavigationItem(int resourceId2) {
        this.resourceId = resourceId2;
    }

    public static NavigationItem getNavigationItemById(int resourceId) {

        for (NavigationItem e : values()) {
            if (e.resourceId == resourceId) {
                return e;
            }
        }
        throw new AssertionError("Enum not implemented !");
    }
}
