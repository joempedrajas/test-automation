package com.utils;

import java.util.HashMap;
import java.util.Map;

public class TestConstants {

	public static final String TEST_CASE_ID_ATTRIBUTE_KEY = "test-case-id";
	public static final String TEST_FLOW_ID_PARAMETER_KEY = "test-flow-id";
	public static final String TEST_DATA_PATH_PARAM_KEY = "test-data-path";
	public static final String TEST_DATA_SHEET_PARAM_KEY = "test-data-sheet";
	public static final String TEST_DATA_PREP_SHEET_PARAM_KEY = "test-data-prep-sheet";
	public static final String TEST_MODULE_PARAM_KEY = "test-module";
	public static final String TEST_APPLICATION_PARAM_KEY = "test-application";

	public static final String TEST_REPORTS_DIRECTORY = "test-report-directory";
	public static final String TEST_AUTOMATION_LOGS = "testautomationlogs.log";
	public static final String LOG_SEPARATOR = "-------------------------------------------------------";
	public static final String SUITE_SEPARATOR = "=======================================================";
	protected static final Map<String, String> TC_MODULE_MAP = new HashMap<>();
	protected static final Map<String, String> TC_APP_MAP = new HashMap<>();

	private TestConstants(){}

}
