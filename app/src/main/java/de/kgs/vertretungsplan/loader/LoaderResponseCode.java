package de.kgs.vertretungsplan.loader;

public enum LoaderResponseCode {

    /**
     * Indicates that the given username/password is wrong
     */
    LOGIN_REQUIRED,

    /**
     * Indicates that an error occurred during the load process (data corrupted, network error, ...)
     */
    ERROR,

    /**
     * Indicates that the device is currently not connected to the internet and there are no data
     */
    NO_INTERNET_NO_DATA_SET,

    /**
     * Indicates that the latest data could be downloaded
     */
    LATEST_DATA_SET,

    /**
     * Indicates that that the device is not connected to the internet but there are cached data
     */
    NO_INTERNET_DATA_SET_EXISTS,

    /**
     * Indicates that the requested data is not provided by moodle ( server down / file not uploaded)
     */
    COVER_PLAN_NOT_PROVIDED

}
