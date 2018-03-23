package com.utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.xml.XmlSuite;

import com.automationlabs.rest.RestUtils;
import com.automationlabs.rest.TestNGHelper;
import com.automationlabs.rest.annotation.TestCase;
import com.automationlabs.rest.jira.JiraCredentialsException;
import com.automationlabs.rest.jira.datamodel.xray.Step;
import com.automationlabs.rest.jira.plugins.XrayStatus;
import com.utils.TestConstants;

public class TestListener extends TestListenerAdapter implements IInvokedMethodListener, ISuiteListener, IReporter {

	private static final Logger log = Logger.getLogger(TestListener.class);
//	private static final String ANNOTATION_ERROR_MESSAGE = "Test Case Annotation Error.";
	private static final String STEP_SEPARATOR = "Step ";

	// Test Listener
	// ---------------------------------------------------------------------------

	@Override
	public void onTestStart(ITestResult result) {
		log.info(TestConstants.LOG_SEPARATOR);
		log.info(" T E S T   N A M E : " + result.getMethod().getXmlTest().getName() + " Started.");
		log.info(TestConstants.LOG_SEPARATOR);
	}

	@Override
	public void onFinish(ITestContext context) {
		log.info(TestConstants.LOG_SEPARATOR);
		log.info(" T E S T   N A M E : " + context.getCurrentXmlTest().getName() + " Completed.");
		log.info(TestConstants.LOG_SEPARATOR);
	}

	@Override
	public void onTestFailure(ITestResult result) {
		String comment = (result.getThrowable().getMessage()==null)?result.getThrowable().toString():result.getThrowable().getMessage();
		try {
			log.info(TestNGHelper.getTestResultAnnotation(result, TestCase.class).id() + " : Failed | " + comment);
		} catch (NoSuchMethodException | SecurityException e) {
			log.error(e.getMessage());
		} catch (Exception e){
			log.error(e.getMessage());
		}
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		String comment = result.getMethod().getMethodName() + " is skipped. Depends on : " + Arrays.toString(result.getMethod().getMethodsDependedUpon());
		try {
			log.info(TestNGHelper.getTestResultAnnotation(result, TestCase.class).id() + " : Skipped | " + comment);
		} catch (NoSuchMethodException | SecurityException e) {
			log.error(e.getMessage());
		}
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		try {
			log.info(TestNGHelper.getTestResultAnnotation(result, TestCase.class).id() + " : Passed | Execution Successful");
		} catch (NoSuchMethodException | SecurityException e) {
			log.error(e.getMessage());
		} catch (Exception e){
			log.error(e.getMessage());
		}
	}

	// Method Listener
	// ---------------------------------------------------------------------------

	@Override
	public void afterInvocation(IInvokedMethod arg0, ITestResult arg1) {
		log.info("Invoked Method : " + arg0.getTestMethod().getMethodName() + " completed.");
	}

	@Override
	public void beforeInvocation(IInvokedMethod arg0, ITestResult arg1) {
		log.info("Invoked Method : " + arg0.getTestMethod().getMethodName() + " started.");
	}

	// Suite Listener
	// ---------------------------------------------------------------------------

	@Override
	public void onFinish(ISuite arg0) {
		log.info("S U I T E   N A M E : " + arg0.getName() + " completed.");
		try {
			log.info("Copy logs into specific folder.");
			File logs = new File(System.getProperty("user.dir") + "/target/" + TestConstants.TEST_AUTOMATION_LOGS);
			FileUtils.copyFile(logs, new File(arg0.getOutputDirectory() + "/" + TestConstants.TEST_AUTOMATION_LOGS));
		} catch (IOException e) {
			log.warn("Unable to copy log file. " + e.getMessage());
		}
		log.info(TestConstants.SUITE_SEPARATOR);
	}

	@Override
	public void onStart(ISuite arg0) {
		log.info("S U I T E   N A M E : " + arg0.getName() + " started.");
	}

	// ---------------------------------------------------------------------------------

	@Override
	public void generateReport(List<XmlSuite> arg0, List<ISuite> suites, String arg2) {
		log.info(TestConstants.LOG_SEPARATOR);
		log.info(" R E P O R T E R   L I S T E N E R ");
		log.info(TestConstants.LOG_SEPARATOR);


		try{
			Set<ITestResult> failures = new HashSet<>();
			Set<ITestResult> skips = new HashSet<>();
			Set<ITestResult> passes = new HashSet<>();
			Set<ITestResult> all = new HashSet<>();
			for(ISuite suite: suites){
				log.info("Suite : " + suite.getName());
				for(ISuiteResult tests : suite.getResults().values()){
					failures.addAll(tests.getTestContext().getFailedTests().getAllResults());
					skips.addAll(tests.getTestContext().getSkippedTests().getAllResults());
					passes.addAll(tests.getTestContext().getPassedTests().getAllResults());
				}
			}

			all.addAll(passes);
			all.addAll(skips);
			all.addAll(failures);

			log.info("Failure Count : " + failures.size());
			log.info("Skipped Count : " + skips.size());
			log.info("Passed Count  : " + passes.size());
			log.info("Total Count   : " + all.size());

			boolean jiraEnabled = (System.getProperty(ConfigPropertyKeys.JIRA_CREATE_EXECUTION) == null) ?
					Boolean.parseBoolean(ConfigurationProperties.getStringProperty(ConfigPropertyKeys.JIRA_CREATE_EXECUTION)) :
					Boolean.parseBoolean(System.getProperty(ConfigPropertyKeys.JIRA_CREATE_EXECUTION));

			if(Boolean.parseBoolean(System.getProperty(ConfigPropertyKeys.JIRA_DEFECT_LOGGING))){
				log.info("Defect Logging enabled.");
				logDefect(failures, skips);
			}
			else
				log.info("Defect Logging disabled.");

			if(jiraEnabled){
				log.info("JIRA status update enabled.");
				String key = createExecution(all);
				updateTestCases(key, all);
			}
			else
				log.info("JIRA status update disabled.");
		}
		catch(JiraCredentialsException jce){
			log.error("Jira Integration error.", jce);
		}
		catch(NoSuchMethodException nsme){
			log.error("Method not found.", nsme);
		}
		catch(Exception e){
			log.error("Unknown Error. ", e);
		}
		log.info(TestConstants.LOG_SEPARATOR);
	}

	// Private Methods
	// ---------------------------------------------------------------------------

	private void logDefect(Set<ITestResult> failures, Set<ITestResult> skips) throws JiraCredentialsException{
		log.info("Log Defect ----------------");
		RestUtility.writeFailureTestToExcel(failures);
		RestUtility.writeSkippedTestToExcel(skips);
		RestUtility.logIssueToJiraNTP();
	}

	private String createExecution(Set<ITestResult> results) throws NoSuchMethodException{
		String date = new SimpleDateFormat("yyyyMMMdd-hh:mm:ss.S").format(new Date());

		String summary = (System.getProperty(ConfigPropertyKeys.JIRA_EXECUTION_SUMMARY)==null)?"EXECUTE-" + date:System.getProperty(ConfigPropertyKeys.JIRA_EXECUTION_SUMMARY);
		String machine;
		try {
			machine = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			log.error("Error getting local host name.",e);
			machine = "Unknown";
		}
		String description = 	"Selenium Test Execution" +
								"\nDate: " + date +
								"\nMachine: " + machine;
		String key = RestUtility.createExecution(summary, description);
		log.info("Create Execution Ticket : " + key);
		log.info(RestUtility.addRuns(key, TestNGHelper.getAllJiraKey(new ArrayList<>(results))));
		return key;
	}

	private void updateTestCases(String execKey, Set<ITestResult> results) throws Exception{
		try{
			log.info("Update Test Cases ----------------------------");
			String executions = RestUtility.getExecutions(execKey);

			Map<String, String> sirs;
			if(Boolean.parseBoolean(System.getProperty(ConfigPropertyKeys.JIRA_DEFECT_LOGGING)))
				sirs = RestUtility.getLoggedDefects();
			else
				sirs = null;

			for(ITestResult result: results){
				String jiraKey = TestNGHelper.getTestResultAnnotation(result, TestCase.class).jiraKey();
				String tcId = TestNGHelper.getTestResultAnnotation(result, TestCase.class).id();
				int id = Integer.parseInt(RestUtils.readJsonPath(executions, "$[?(@.key=='" + jiraKey + "')].id").get(0).toString());
				XrayStatus status = RestUtility.testNGToXrayStatus(result.getStatus());

				log.info("Result Details : " + jiraKey + " | " + tcId + " | " + status);

				String comment = RestUtility.determineTestCaseComment(result, status);
				String defect = (sirs == null)? null: RestUtility.getDefectAssociated(result, status, sirs);

				List<String> defects = new ArrayList<>();
				defects.add(defect);

				RestUtility.updateRunAndTestCase(id, result, comment, (defect == null)? null: defects, null, null, null, getStepFromResult(id, result));
			}
		}catch(Exception e){
			log.error("Update Test Case Error.", e);
			throw e;
		}
	}

	private List<Step> getStepFromResult(int runId, ITestResult result){
		if(result.getStatus() == ITestResult.FAILURE){
			List<Integer> stepIds = RestUtility.getRunStepIDs(RestUtility.getTestRun(runId));
			List<Step> steps = new ArrayList<>();
			List<Integer> failedStepIds = new ArrayList<>();

			if(result.getThrowable() != null && result.getThrowable().getMessage() != null && result.getThrowable().getMessage().contains(STEP_SEPARATOR)){
				String[] errors = result.getThrowable().getMessage().split(STEP_SEPARATOR);
				for(int i = 1; i < errors.length; i++){
					failedStepIds.add(stepIds.get(Integer.parseInt(errors[i].split(":")[0]) - 1));
				}
			}

			for(int stepId: stepIds){
				Step step = new Step();
				step.setId(stepId);
				if(result.getThrowable() != null && result.getThrowable().getMessage() != null)
					step.setStatus((result.getThrowable().getMessage().contains(STEP_SEPARATOR) && !failedStepIds.contains(stepId))? XrayStatus.PASS: XrayStatus.FAIL);
				else
					step.setStatus(XrayStatus.FAIL);
				steps.add(step);
			}
			return steps;
		}
		else
			return new ArrayList<>();
	}

}
