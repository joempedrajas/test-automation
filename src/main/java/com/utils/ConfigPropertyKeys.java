package com.utils;

public final class ConfigPropertyKeys {
	public static final String BASE_URL = "application.baseurl";
    public static final String BASE_DEV_URL = "application.dev.baseurl";
    public static final String BASE_UT_URL = "application.ut.baseurl";
    public static final String BASE_SIT_URL = "application.sit.baseurl";


    public static final String DEFAULT_TEST_DIR = "application.default.test.dir";
    public static final String STEP_SCREENSHOT_LOCATION = "step.screenshot.location";

    // Web Service
    public static final String API_BASE_URL = "api.baseurl";
    public static final String API_DEV_BASE_URL = "api.dev.baseurl";
    public static final String API_UT_BASE_URL = "api.ut.baseurl";
    public static final String API_SIT_BASE_URL = "api.sit.baseurl";

    public static final String JAVA_BASE_URL = "java.baseurl";
    public static final String JAVA_DEV_BASE_URL = "java.dev.baseurl";
    public static final String JAVA_UT_BASE_URL = "java.ut.baseurl";
    public static final String JAVA_SIT_BASE_URL = "java.sit.baseurl";

    // Driver Options
    public static final String DEFAULT_WAIT = "driver.wait.default.seconds";
    public static final String DEFAULT_BROWSER = "driver.browser.default";
    public static final String DRIVER_BROWSER_FIREFOX_PROFILE = "driver.browser.firefox.profile";

    //DataBase
    public static final String DB_HOST = "database.host";
    public static final String DB_SERVICE = "database.service";
    public static final String DB_USER = "database.user";
    public static final String DB_PW = "database.pass";
    public static final String DRIVERS_CHROME_LOCATION = "driver.chrome.webdriver";
	public static final String DRIVERS_IE_LOCATION = "driver.ie.webdriver";
	public static final String DRIVERS_FIREFOX_LOCATION = "driver.firefox.webdriver";
	public static final String DRIVERS_EDGE_LOCATION = "driver.edge.webdriver";
	public static final String DRIVERS_OPERA_LOCATION = "driver.opera.webdriver";
	public static final String DRIVERS_OPERA_BINARY_LOCAL = "opera.binary.local";
	public static final String DRIVERS_OPERA_BINARY_GRID = "opera.binary.grid";
	public static final String DRIVERS_GRID_ENABLE = "drivers.grid.enable";
	public static final String DRIVERS_GRID_HUB = "drivers.grid.hub";
	public static final String DRIVERS_GRID_PLATFORM = "drivers.grid.platform";

	// System Properties
	public static final String RAIL_ENABLE = "rail.enable.update";
	public static final String JIRA_DEFECT_LOGGING = "jira.defect.logging";
	public static final String JIRA_CREATE_EXECUTION = "jira.create.execution";
	public static final String JIRA_EXECUTION_SUMMARY = "jira.execution.summary";
	public static final String ENVIRONMENT = "environment";

	public static final String JIRA_DEFECT_PROJECT_PHASE = "jira.defect.project.phase";
	public static final String JIRA_DEFECT_TEST_STAGE = "jira.defect.test.stage";

	//TestNG
	public static final String TESTNG_SUITE_WRAPPER = "suite.wrapper.name";
}
