package com.test.poc.jobstreetSIT;

import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.common.BasePage;
import com.poc.jobstreet.HomePage;
import com.test.common.BaseTest;
import com.test.utils.DataReaderUtil;


public class HomePageTest extends BaseTest{

	// TEST VARS

	private static  String PATH = ("src/test/resources/datasheet/HomePage_POC.xlsx");
	private static  String SHEET = ("Datasheet");
	private static  String CROSSSHEET = ("Cross Scripts Data");
	private HomePage homepage;
	private SoftAssert s;
	private DataReaderUtil dataReader = new DataReaderUtil(PATH, SHEET, CROSSSHEET);

	private String readDataForMethod(String colName) throws Exception{
		if(StringUtils.isEmpty(colName)) {
			colName = "Field name^EXP";
		}
		String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
		String formattedMethodName = methodName.substring(0, methodName.lastIndexOf('_')).replace(('_'), ('.')) + methodName.substring(methodName.lastIndexOf('_'));
		return dataReader.getData(formattedMethodName, colName);

	}

	@BeforeClass
	public void classInitialization() throws Exception {
		homepage = new HomePage(driver);
		dataReader = new DataReaderUtil(PATH, SHEET, CROSSSHEET);
		driver.get(BasePage.getBaseURL());

	}

	@BeforeMethod
	private void initAssert() {
		s = new SoftAssert();
	}

	@BeforeGroups(groups = "HomePage")
	public void goToHomePage(){
		homepage.goTo();
	}

	@Test(groups = "HomePage", description = "Access home page")
	@Parameters("displayName")
	public void POC_JOBSTREET_HOMEPAGE_1_001_001(String displayName) throws Exception {
		s.assertTrue(homepage.isOnHomePage());
	}

	@Test(groups = "HomePage", description = "Verify login link is displayed")
	public void POC_JOBSTREET_HOMEPAGE_1_001_002() throws Exception {
		s.assertTrue(homepage.isDisplayedLoginLink());
	}

	@Test(groups = "HomePage", description = "Verify SignUp link is displayed")
	public void POC_JOBSTREET_HOMEPAGE_1_001_003() throws Exception {
		s.assertTrue(homepage.isDisplayedSignUpLink());
	}
	
	@Test(groups = "HomePage", description = "Access home page")
	public void POC_JOBSTREET_HOMEPAGE_1_001_004(String displayName) throws Exception {
		s.assertTrue(homepage.isOnHomePage());
	}

//	@Test(groups = "HomePage", description = "Verify if SignUp successful")
//	public void POC_JOBSTREET_HOMEPAGE_1_001_004() throws Exception {
//		String firstName = readDataForMethod("FirstName^IN");
//		String lastName = readDataForMethod("LastName^IN");
//		String email = readDataForMethod("Email^IN");
//		String password = readDataForMethod("Password^IN");
//		homepage.signUp(firstName, lastName, email, password);
//		s.assertTrue(false);
//		// code for successful registration
//		//s.assertTrue(homepage.isDisplayedSignUpLink());
//	}

//	@Test(groups = "HomePage", description = "Verify if Login successful")
//	public void POC_JOBSTREET_HOMEPAGE_1_001_005() throws Exception {
//		String email = readDataForMethod("Email^IN");
//		String password = readDataForMethod("Password^IN");
//		homepage.login(email, password);
//		s.assertTrue(homepage.isOnHomeIndexPage());
//		// code for successful registration
//		//s.assertTrue(homepage.isDisplayedSignUpLink());
//	}


}
