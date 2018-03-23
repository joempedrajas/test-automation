package com.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestResult;

import com.automationlabs.rest.RestCredentials;
import com.automationlabs.rest.RestProperties;
import com.automationlabs.rest.RestUtils;
import com.automationlabs.rest.TestNGHelper;
import com.automationlabs.rest.annotation.TestCase;
import com.automationlabs.rest.jira.JiraCredentialsException;
import com.automationlabs.rest.jira.JiraRest;
import com.automationlabs.rest.jira.TransitionStatus;
import com.automationlabs.rest.jira.config.JiraPropertyKeys;
import com.automationlabs.rest.jira.datamodel.basic.Fields;
import com.automationlabs.rest.jira.datamodel.basic.NTPFields;
import com.automationlabs.rest.jira.datamodel.basic.child.ChildKey;
import com.automationlabs.rest.jira.datamodel.basic.child.ChildName;
import com.automationlabs.rest.jira.datamodel.basic.child.CustomField;
import com.automationlabs.rest.jira.datamodel.basic.child.NestedCustomField;
import com.automationlabs.rest.jira.datamodel.xray.Step;
import com.automationlabs.rest.jira.datamodel.xray.XrayFields;
import com.automationlabs.rest.jira.plugins.XrayRest;
import com.automationlabs.rest.jira.plugins.XrayStatus;
import com.automationlabs.rest.jira.request.CreateIssueRequest;
import com.automationlabs.rest.utils.file.ExcelFile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;


public class RestUtility {

	private static final Logger log = Logger.getLogger(RestUtility.class);
	private static final String ERROR_JSONPATH = "errors";

	private RestUtility() {
	}

	public static String createExecution(String summary, String description) {
		String host = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_BASEURL);

		RestCredentials cred = new RestCredentials(host, System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_USER),
				System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_PASS));
		XrayRest xray = new XrayRest(cred);

		ChildKey project = new ChildKey(RestProperties.getStringProperty(JiraPropertyKeys.XRAY_ISSUE_KEY));
		ChildName issuetype = new ChildName(
				RestProperties.getStringProperty(JiraPropertyKeys.XRAY_ISSUE_TYPE_EXECUTION));
		XrayFields fields = new XrayFields(project, summary, description, issuetype, null, null);
		CreateIssueRequest body = new CreateIssueRequest(fields);

		String createIssueResponse = xray.createIssueBasic(body);
		log.info("Create Issue response: " + createIssueResponse);
		return RestUtils.readJsonPath(createIssueResponse, "$..key").get(0).toString();
	}

	public static int addRun(String issueKey, String testCaseKey) {
		String host = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_BASEURL);

		RestCredentials cred = new RestCredentials(host, System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_USER), System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_PASS));
		XrayRest xray = new XrayRest(cred);
		List<String> addKeys = new ArrayList<>();
		addKeys.add(testCaseKey);
		xray.editTestExecution(issueKey, addKeys, null);
		return Integer.parseInt(RestUtils.readJsonPath(xray.getTestExecution(issueKey), "$[?(@.key=='" + testCaseKey + "')].id").get(0).toString());
	}

	public static List<Integer> getRunStepIDs(String json){
		return RestUtils.jsonToObject(RestUtils.readJsonPath(json, "$..steps[*].id").toString(), new TypeToken<ArrayList<Integer>>(){}.getType());
	}
	public static List<Integer> getRunStepIndexes(String json){
		return RestUtils.jsonToObject(RestUtils.readJsonPath(json, "$..steps[*].index").toString(), new TypeToken<ArrayList<Integer>>(){}.getType());
	}

	public static String getTestRun(int runId){
		String host = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_BASEURL);

		RestCredentials cred = new RestCredentials(host, System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_USER), System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_PASS));
		XrayRest xray = new XrayRest(cred);
		return xray.getTestRun(runId);
	}

	public static String addRuns(String issueKey, List<String> testCaseKeys){
		String host = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_BASEURL);

		RestCredentials cred = new RestCredentials(host, System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_USER), System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_PASS));
		XrayRest xray = new XrayRest(cred);
		return xray.editTestExecution(issueKey, testCaseKeys, null);
	}

	public static void updateRunAndTestCase(int id, ITestResult result, String comment, List<String> defects,
			List<File> addEvidences, List<File> removeEvidences, String fileContentType, List<Step> steps) throws Exception{
		try {
			String host = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_BASEURL);

			RestCredentials cred = new RestCredentials(host, System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_USER),
					System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_PASS));
			XrayRest xray = new XrayRest(cred);

			xray.updateTestRun(id, testNGToXrayStatus(result.getStatus()), comment, defects, addEvidences, removeEvidences, fileContentType, steps);
			xray.doTransition(TestNGHelper.getTestResultAnnotation(result, TestCase.class).jiraKey(), testNGToTransitionStatus(result.getStatus()));
		} catch (Exception e) {
			throw e;
		}
	}

	public static String getExecutions(String execKey){
		String host = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_BASEURL);
		RestCredentials cred = new RestCredentials(host, System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_USER), System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_PASS));
		XrayRest xray = new XrayRest(cred);
		return xray.getTestExecution(execKey);
	}

	//---------------------------------------------

	public static void writeFailureTestToExcel(Set<ITestResult> failures) {
		log.info("Writing failed test info to excel.");
		String filePath = System.getProperty(TestConstants.TEST_REPORTS_DIRECTORY) + File.separator + RestProperties.getStringProperty(JiraPropertyKeys.EXCEL_ISSUES_FILE);
		if(Utils.deleteFile(filePath))
			log.info("File deleted - " + filePath);
		try {
			String[] item;
			ExcelFile file = new ExcelFile(filePath);
			String sheetName = RestProperties.getStringProperty(JiraPropertyKeys.EXCEL_ISSUES_SHEET1);
			file.createNewSheet(sheetName);
			for (ITestResult failed : failures) {
				String tcId = TestNGHelper.getTestResultAnnotation(failed, TestCase.class).id();
				TestConstants.TC_MODULE_MAP.put(tcId, failed.getTestContext().getCurrentXmlTest().getParameter(TestConstants.TEST_MODULE_PARAM_KEY));
				TestConstants.TC_APP_MAP.put(tcId, failed.getTestContext().getCurrentXmlTest().getParameter(TestConstants.TEST_APPLICATION_PARAM_KEY));
				item = new String[] {
						failed.getMethod().getMethodName(),
						tcId,
						failed.getThrowable().toString()};
				file.writeToLastRow(sheetName, item);
			}
			file.writeAndClose();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static void writeSkippedTestToExcel(Set<ITestResult> skips){
		log.info("Writing skipped test info to excel.");
		String excelpath = System.getProperty(TestConstants.TEST_REPORTS_DIRECTORY) + File.separator + RestProperties.getStringProperty(JiraPropertyKeys.EXCEL_ISSUES_FILE);
		String sheet1 = RestProperties.getStringProperty(JiraPropertyKeys.EXCEL_ISSUES_SHEET1);
		try {
			String[] item;
			ExcelFile file = new ExcelFile(excelpath);
			List<String> failedTestCases = file.getAllDataInColumn(sheet1, 1);
			List<String> failedErrors = file.getAllDataInColumn(sheet1, 2);
			Map<String, String> excelErrors = Utils.toMap(failedTestCases, failedErrors);

			for (ITestResult skip : skips) {

				String[] depend = skip.getMethod().getMethodsDependedUpon();
				log.info(TestNGHelper.getTestResultAnnotation(skip, TestCase.class).id() + " depends on " + Arrays.toString(depend));
				String error = null;
				if(depend.length > 0){
					String[] methodPath = depend[0].split("\\.");
					error = excelErrors.get(TestNGHelper.getMethodAnnotation(skip.getTestClass().getRealClass(), methodPath[methodPath.length-1], TestCase.class).id());
				}

				String tcId = TestNGHelper.getTestResultAnnotation(skip, TestCase.class).id();
				TestConstants.TC_MODULE_MAP.put(tcId, skip.getTestContext().getCurrentXmlTest().getParameter(TestConstants.TEST_MODULE_PARAM_KEY));
				TestConstants.TC_APP_MAP.put(tcId, skip.getTestContext().getCurrentXmlTest().getParameter(TestConstants.TEST_APPLICATION_PARAM_KEY));

				item = new String[] {
						skip.getMethod().getMethodName(),
						tcId,
						error };
				file.writeToLastRow(sheet1, item);
			}
			file.writeAndClose();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void logIssueToJiraLocal() throws JiraCredentialsException{
		String excelpath = System.getProperty(TestConstants.TEST_REPORTS_DIRECTORY) + File.separator + RestProperties.getStringProperty(JiraPropertyKeys.EXCEL_ISSUES_FILE);
		String sheet1 = RestProperties.getStringProperty(JiraPropertyKeys.EXCEL_ISSUES_SHEET1);
		String sheet2 = RestProperties.getStringProperty(JiraPropertyKeys.EXCEL_ISSUES_SHEET2);

		try {
			ExcelFile file = new ExcelFile(excelpath);
			List<String> errors = file.getAllDataInColumn(0, 2);
			Set<String> uniqueFailure = new HashSet<>(errors);
			file.createNewSheet(sheet2);
			file.writeToLastRow(sheet2, new String[] { "Summary", "Description", "JIRA Defect","Test Case ID" });

			String baseURL = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_BASEURL);
			String user = System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_USER);
			String pass = System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_PASS);

			if(user == null || pass == null)
				throw new JiraCredentialsException();

			JiraRest rest = new JiraRest(new RestCredentials(baseURL,user,pass));

			for (String item : uniqueFailure) {
				try{
					String summary = createSummary(item);
					List<String> testCases = file.getAllDataWithValueInColumn(sheet1, 2, item, 1);
					String description = createDescription(item, testCases.toString());
					String ticket;

					CreateIssueRequest req = createIssueRequestModel(summary, description);

					String res = rest.createIssueBasic(req);
					log.info("Log to Jira response: " + res);
					if(isJsonPathExist(res,ERROR_JSONPATH))
						ticket = JsonPath.read(res, ERROR_JSONPATH).toString();
					else
						ticket = JsonPath.read(res, "key").toString();
					for(String testCase: testCases){
						file.writeToLastRow(sheet2, new String[]{summary, description, ticket, testCase});
					}
				}catch(Exception e){
					log.info("Error logging defect.", e);
				}
			}

			file.writeAndClose();
		}catch(JiraCredentialsException jce){
			throw jce;
		}catch(Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static void logIssueToJiraNTP() throws JiraCredentialsException{
		String excelpath = System.getProperty(TestConstants.TEST_REPORTS_DIRECTORY) + File.separator + RestProperties.getStringProperty(JiraPropertyKeys.EXCEL_ISSUES_FILE);
		String sheet1 = RestProperties.getStringProperty(JiraPropertyKeys.EXCEL_ISSUES_SHEET1);
		String sheet2 = RestProperties.getStringProperty(JiraPropertyKeys.EXCEL_ISSUES_SHEET2);

		String projectPhase = ConfigurationProperties.getStringProperty(ConfigPropertyKeys.JIRA_DEFECT_PROJECT_PHASE);
		String testStage = ConfigurationProperties.getStringProperty(ConfigPropertyKeys.JIRA_DEFECT_TEST_STAGE);

		try {
			ExcelFile file = new ExcelFile(excelpath);
			List<String> errors = file.getAllDataInColumn(0, 2);
			Set<String> uniqueFailure = new HashSet<>(errors);
			file.createNewSheet(sheet2);
			file.writeToLastRow(sheet2, new String[] { "Summary", "Description", "JIRA Defect","Test Case ID" });

			String baseURL = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_BASEURL);
			String user = System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_USER);
			String pass = System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_PASS);

			if(user == null || pass == null)
				throw new JiraCredentialsException();

			JiraRest rest = new JiraRest(new RestCredentials(baseURL,user,pass));

			for (String item : uniqueFailure) {
				try{
					String summary = createSummary(item);
					List<String> testCases = file.getAllDataWithValueInColumn(sheet1, 2, item, 1);
					String description = createDescription(item, testCases.toString());
					String ticket;

					String application = TestConstants.TC_APP_MAP.get(testCases.get(0));
					String module = TestConstants.TC_MODULE_MAP.get(testCases.get(0));

					CreateIssueRequest req = createIssueRequestModel(summary, description, projectPhase, testStage, application, module);

					String res = rest.createIssueBasic(req);
					log.info("Log to Jira response: " + res);
					if(isJsonPathExist(res,ERROR_JSONPATH))
						ticket = JsonPath.read(res, ERROR_JSONPATH).toString();
					else
						ticket = JsonPath.read(res, "key").toString();
					for(String testCase: testCases){
						file.writeToLastRow(sheet2, new String[]{summary, description, ticket, testCase});
					}
				}catch(Exception e){
					log.info("Error logging defect.", e);
				}
			}

			file.writeAndClose();
		}catch(JiraCredentialsException jce){
			throw jce;
		}catch(Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static Map<String, String> getLoggedDefects(){
		String excelpath = System.getProperty(TestConstants.TEST_REPORTS_DIRECTORY) + File.separator + RestProperties.getStringProperty(JiraPropertyKeys.EXCEL_ISSUES_FILE);
		String sheet2 = RestProperties.getStringProperty(JiraPropertyKeys.EXCEL_ISSUES_SHEET2);
		Map<String, String> sirs = new HashMap<>();

		try {
			ExcelFile file = new ExcelFile(excelpath);
			List<String> testcases = file.getAllDataInColumn(sheet2, 3);
			List<String> defects = file.getAllDataInColumn(sheet2, 2);
			sirs = Utils.toMap(testcases, defects);
		} catch (IOException e) {
			log.error("Error reading logged defects from excel.", e);
		}

		return sirs;
	}

	private static boolean isJsonPathExist(String json, String path){
		try{
			JsonPath.read(json, path);
			return true;
		}catch(PathNotFoundException pnfe){
			log.warn(pnfe.getMessage());
			return false;
		}
	}

	public static CreateIssueRequest createIssueRequestModel(String summary, String description, String projectPhase, String testStage,
			String application, String module){

		String projectKey = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_ISSUE_KEY);
		String issueAssignee = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_ISSUE_ASSIGNEE);
		String issueType = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_ISSUE_TYPE);
		String issueEnvironment = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_ISSUE_ENVIRONMENT);

		NTPFields fields = new NTPFields(
				new ChildKey(projectKey),
				summary,
				description,
				new ChildName(issueType),
				issueEnvironment,
				new ChildKey(issueAssignee),
				new CustomField(projectPhase),
				new CustomField(testStage),
				new NestedCustomField(application, new CustomField(module))
		);
		return new CreateIssueRequest(fields);
	}

	public static CreateIssueRequest createIssueRequestModel(String summary, String description){
		String projectKey = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_ISSUE_KEY);
		String issueAssignee = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_ISSUE_ASSIGNEE);
		String issueType = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_ISSUE_TYPE);
		String issueEnvironment = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_ISSUE_ENVIRONMENT);

		Fields fields = new Fields(new ChildKey(projectKey), summary, description, new ChildName(issueType), issueEnvironment, new ChildKey(issueAssignee));
		return new CreateIssueRequest(fields);

	}

	public static String createSummary(String text){
		if(text == null)
			return "null";
		else if(text.length() >= 255)
			return text.split(":")[0];
		else
			return text.replace("\n", " ");
	}

	public static String createDescription(String error, String testCases){
		return "Error Description:\n" +
				error + "\n\nAffected Test Cases:\n" + testCases;
	}

	public static String objectToJsonString(Object obj){
		Gson gson = new Gson();
		return gson.toJson(obj);
	}

	public static String objectToJsonStringExpose(Object obj){
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		return gson.toJson(obj);
	}
	//---------------------------------------------

	@Deprecated
	public static void updateStatus(ITestContext context) {

		boolean enableTestRailUpdate = (System.getProperty("enableTestUpdate") != null)
				? Boolean.parseBoolean(System.getProperty("enableTestUpdate"))
				: Boolean.parseBoolean(ConfigurationProperties.getStringProperty(ConfigPropertyKeys.RAIL_ENABLE));

		if (enableTestRailUpdate) {
			log.info(TestConstants.LOG_SEPARATOR);
			log.info(" Test Status Update");
			log.info(TestConstants.LOG_SEPARATOR);
			log.info(TestConstants.LOG_SEPARATOR);
			log.info(" Test Status Update Done");
			log.info(TestConstants.LOG_SEPARATOR);
		} else
			log.info("Test Cases status update disabled.");
	}

	public static XrayStatus testNGToXrayStatus(int status){
		switch(status){
			case ITestResult.SUCCESS:
				return XrayStatus.PASS;
			case ITestResult.FAILURE:
				return XrayStatus.FAIL;
			case ITestResult.SKIP:
				return XrayStatus.ABORTED;
			default:
				return XrayStatus.EXECUTING;
		}
	}

	public static TransitionStatus testNGToTransitionStatus(int status){
		switch(status){
			case ITestResult.SUCCESS:
				return TransitionStatus.PASSED_ID;
			case ITestResult.FAILURE:
				return TransitionStatus.FAILED_ID;
			case ITestResult.SKIP:
				return TransitionStatus.SKIPPED_ID;
			default:
				return TransitionStatus.NOT_STARTED_ID;
		}
	}

	@SuppressWarnings("unchecked")
	public static String determineTestCaseComment(ITestResult result, XrayStatus status) throws NoSuchMethodException {
		String comment;
		switch(status){
			case PASS:
				comment = "Success";
				break;
			case FAIL:
				String message = (result.getThrowable().getMessage()==null)?result.getThrowable().toString():result.getThrowable().getMessage();
				comment = createSummary(message);
				break;
			case ABORTED:
				comment = (result.getMethod().getMethodsDependedUpon().length == 0)?
						"Configuration Failed":
						"Depends on " +
						TestNGHelper.getMethodAnnotation(result.getTestClass().getRealClass(), result.getMethod().getMethodsDependedUpon()[0].split("\\.")[result.getMethod().getMethodsDependedUpon()[0].split("\\.").length - 1], TestCase.class).jiraKey();
				break;
			default:
				comment = "Unable to update status. Please check test-output of Selenium Automation.";
		}
		return comment;
	}

	public static String getDefectAssociated(ITestResult result, XrayStatus status, Map<String, String> sirs) throws NoSuchMethodException {
		if(status == XrayStatus.FAIL || status == XrayStatus.ABORTED)
			return sirs.get(TestNGHelper.getTestResultAnnotation(result, TestCase.class).id());
		return null;
	}


}
