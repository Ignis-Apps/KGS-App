package de.kgs.vertretungsplan.broadcaster;


public enum BroadcastEvent {

    /**
     * This event should be called whenever the current page changes (blackboard, today, tomorrow)
     */
    CURRENT_PAGE_CHANGED,

    /**
     * This event should be called whenever the selected grade changes
     */
    CURRENT_GRADE_CHANGED,

    /**
     * This event should be called whenever the selected class changes
     */
    CURRENT_CLASS_CHANGED,

    /**
     * This event should be called whenever the loader provided fresh data in the singleton
     */
    DATA_PROVIDED,

    /**
     * This event should be called whenever a menu item gets selected
     */
    CURRENT_MENU_ITEM_CHANGED,

    /**
     * This event should be called whenever the app requests new data from the loader
     */
    REQUEST_DATA_RELOAD,

    /**
     * This event should be called when the internal browser view gets closed
     */
    // TODO implement it !
    INTERNAL_BROWSER_CLOSED,
}
