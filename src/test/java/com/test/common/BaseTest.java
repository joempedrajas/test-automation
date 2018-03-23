package com.test.common;

import java.io.File;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.UnsupportedCommandException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.interactions.Actions;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import com.automationlabs.rest.Rest;
import com.test.utils.DataReaderUtils;
import com.utils.ConfigPropertyKeys;
import com.utils.ConfigurationProperties;
import com.utils.DBQueryUtil;
import com.utils.DriverFactory;
import com.utils.Locator;
import com.utils.TestConstants;
import com.utils.Utils;
import com.utils.DriverFactory.DriverTypes;

/**
 * Extends to this class and add test.
 * Use methods as Before or After TestNG Annotations
 *
 */
public class BaseTest {

	protected String baseurl = ConfigurationProperties.getStringProperty(ConfigPropertyKeys.BASE_URL);

	final static Logger log = Logger.getLogger(BaseTest.class);
	protected WebDriver driver;
	protected Rest rest;
	protected DBQueryUtil dbquery;

	protected String path;
    protected String fileName;
    protected String className;
    protected long exeInstance = System.currentTimeMillis();


	/**
	 * Creates WebDriver, call this from method annotated by 'Before' annotation.
	 * @param context
	 * @throws MalformedURLException
	 */
	@BeforeClass(alwaysRun = true)
	public void setUp(ITestContext context) throws MalformedURLException {
		log.info("-----------------------------------------------------");
		String browser = (System.getProperty("driverBrowser") == null)
				? ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DEFAULT_BROWSER)
				: System.getProperty("driverBrowser");
		boolean gridEnable = (System.getProperty("enableGrid") == null)
				? Boolean.valueOf(ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DRIVERS_GRID_ENABLE))
				: Boolean.valueOf(System.getProperty("enableGrid"));



		driver = DriverFactory.createDriver(gridEnable, DriverTypes.valueOf(browser));
		driver.manage().timeouts().implicitlyWait(ConfigurationProperties.getLongProperty(ConfigPropertyKeys.DEFAULT_WAIT), TimeUnit.SECONDS);
//		driver.manage().timeouts().pageLoadTimeout(ConfigurationProperties.getLongProperty(ConfigPropertyKeys.DEFAULT_WAIT), TimeUnit.SECONDS);
		try{
			driver.manage().window().maximize();
		}catch(WebDriverException wde){
			log.error(wde.getMessage());
		}
		log.info("Driver : " + browser);
		log.info("Grid Execution : " + gridEnable);
		System.setProperty(TestConstants.TEST_REPORTS_DIRECTORY, new File(context.getOutputDirectory()).getParent());

	}

	public void setupDBConnection(){
		dbquery = new DBQueryUtil(ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DB_HOST) + ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DB_SERVICE), ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DB_USER), ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DB_PW));
	}

	public void closeDBConnection(){
		dbquery.closeConnection();
	}
	/**
	 * Takes screenshot in WebDriver. Call this from method annotated by 'After' annotation.
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void takeScreenshot(ITestResult result) {
		if (driver != null) {
			String path = result.getTestContext().getOutputDirectory() + File.separator;
			String dataRowNo = (result.getAttribute(TestConstants.TEST_CASE_ID_ATTRIBUTE_KEY) != null)
					? ("[" + result.getAttribute(TestConstants.TEST_CASE_ID_ATTRIBUTE_KEY) + "]").toString() : "";
			String filename = dataRowNo + result.getMethod().getMethodName();
			if (result.isSuccess()) {
				path = path + "Passed" + File.separator + result.getTestContext().getName();
			} else {
				path = path + "Failed" + File.separator + result.getTestContext().getName();
			}
			Utils.captureScreenshot(driver, path, filename);
		}
	}

	/**
	 * Closes the driver. Call this from method annotated by 'After' annotation.
	 */
	@AfterClass(alwaysRun = true)
	public void tearDown() {
		if (driver != null) {
			try{
				driver.quit();
			}catch(Exception e){
				log.warn(e.getMessage());
				driver.close();
			}
		}
	}

	/**
	 * Call this method in Test Class data provider method
	 * @param context
	 * @param path
	 * @param sheet
	 * @return
	 * @throws Exception
	 */

	/**
    public void takeScreenshot(ITestResult result) {
	    if(driver != null){
	        className = this.getClass().getSimpleName();
	        path = "target/screenshot_" + getCurrentDate("yyyy.MM.dd") + "/" + className + exeInstance;
	        fileName = result.getMethod().getMethodName() ;
	        Utils.captureScreenshot(driver, path, fileName);
	    }
    }
    */

    public void takeStepScreenshot(String result) {
	    if(driver != null){
	        className = this.getClass().getSimpleName();
	        path = "target/screenshot_" + getCurrentDate("yyyy.MM.dd") + "/" + className + exeInstance;
	        fileName = result;
	        Utils.captureScreenshot(driver, path, fileName);
	    }
    }
	public Object[][] getTestData(ITestContext context, String path, String sheet) throws Exception{
		String testFlowId = context.getCurrentXmlTest().getParameter(TestConstants.TEST_FLOW_ID_PARAMETER_KEY);
		Reporter.log("Test Flow ID : " + testFlowId);
		return DataReaderUtils.getTestFlowData(path, sheet, testFlowId);
	}

	public void stepScreenshot(String testCaseId, int id){
		String path = System.getProperty(TestConstants.TEST_REPORTS_DIRECTORY);
		Utils.captureScreenshot(driver, path, testCaseId + " " + id + ".png");
	}

	public void stepScreenshot(String testCaseId, String postfix){
		String path = System.getProperty(TestConstants.TEST_REPORTS_DIRECTORY);
		Utils.captureScreenshot(driver, path, testCaseId + " " + postfix + ".png");
	}

	public void stepScreenshot(String testCaseId, String postfix, String locator, Locator type){
		try{
			new Actions(driver).moveToElement(driver.findElement(Utils.getLocator(locator, type))).build().perform();
			stepScreenshot(testCaseId, postfix);
		}catch(UnsupportedCommandException uce){
			log.warn("Action move to element is not supported with this browser and driver version. " + uce.getMessage());
			stepScreenshot(testCaseId, postfix);
		}catch(Exception e){
			log.warn("Unable to capture screenshot to element. " + e.getMessage());
		}
	}

	public void stepScreenshot(String testCaseId, String postfix, By locator){
		try{
			new Actions(driver).moveToElement(driver.findElement(locator)).build().perform();
			stepScreenshot(testCaseId, postfix);
		}catch(UnsupportedCommandException uce){
			log.warn("Action move to element is not supported with this browser and driver version. " + uce.getMessage());
			stepScreenshot(testCaseId, postfix);
		}catch(Exception e){
			log.warn("Unable to capture screenshot to element. " + e.getMessage());
		}
	}

	  public static String getCurrentDate(String format){
	        SimpleDateFormat date = new SimpleDateFormat(format);
	        return date.format(Calendar.getInstance().getTime());
	    }
}
