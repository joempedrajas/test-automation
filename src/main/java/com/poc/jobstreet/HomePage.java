package com.poc.jobstreet;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import com.common.BasePage;
import com.poc.jobstreet.loc.HomePageLoc;

public class HomePage extends BasePage {
	protected static final Logger LOG = Logger.getLogger(HomePage.class);
	private static final String PAGE_TITLE = "";
 	private static final String FORM_TITLE = "";
 	private static final String PATH = "https://www.jobstreet.com.ph/";


	public HomePage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	public void goTo(){
		driver.get(PATH);
		waitForPageToLoad();
	}



	public boolean isOnHomePage(){
		return driver.getCurrentUrl().equalsIgnoreCase("https://www.jobstreet.com.ph/");
	}

	public boolean isDisplayedLoginLink(){
		WebElement element = waitFor(By.id(HomePageLoc.LOGIN_LINK_ID));
		return element.isDisplayed();
	}

	public boolean isDisplayedSignUpLink(){
		WebElement element = waitFor(By.id(HomePageLoc.SIGNUP_LINK_ID));
		return element.isDisplayed();
	}

	public void clickSignUpLink(){
		WebElement element = waitFor(By.id(HomePageLoc.SIGNUP_LINK_ID));
		element.click();
	}


	public void signUp(String firstName, String lastName, String email, String password){
		clickSignUpLink();
		enter_SignUpFirstName(firstName);
		enter_SignUpLastName(lastName);
		enter_SignUpEmail(email);
		enter_SignUpPassword(password);
		click_SignUpButton();
	}

	public void enter_SignUpFirstName(String firstName) {
		WebElement element = waitFor(By.id(HomePageLoc.MODAL_FIRSTNAME_INPUT_ID));
		element.sendKeys(firstName);
	}

	public void enter_SignUpLastName(String lastName) {
		WebElement element = waitFor(By.id(HomePageLoc.MODAL_LASTNAME_INPUT_ID));
		element.sendKeys(lastName);
	}

	public void enter_SignUpEmail(String email) {
		WebElement element = waitFor(By.id(HomePageLoc.MODAL_EMAIL_INPUT_ID));
		element.sendKeys(email);
	}

	public void enter_SignUpPassword(String password) {
		WebElement element = waitFor(By.id(HomePageLoc.MODAL_PASSWORD_INPUT_ID));
		element.sendKeys(password);
	}

	public void click_SignUpButton() {
		WebElement element = waitFor(By.id(HomePageLoc.MODAL_SIGNUP_BTN_ID));
		element.click();
	}



	public void login(String email, String password){
		clickLoginLink();
		enter_LoginEmail(email);
		enter_LoginPassword(password);
		clickLoginBtn();
	}

	public boolean isOnHomeIndexPage(){
		return driver.getCurrentUrl().contains("https://myjobstreet.jobstreet.com.ph/home/index.php");
	}

	public void clickLoginLink(){
		WebElement element = waitFor(By.id(HomePageLoc.LOGIN_LINK_ID));
		element.click();
	}

	public void clickLoginBtn(){
		WebElement element = waitFor(By.id(HomePageLoc.MODAL_LOGIN_BTN_ID));
		element.click();
	}


	public void enter_LoginEmail(String email) {
		WebElement element = waitFor(By.id(HomePageLoc.MODAL_LOGIN_EMAIL_INPUT_ID));
		element.sendKeys(email);
	}

	public void enter_LoginPassword(String password) {
		WebElement element = waitFor(By.id(HomePageLoc.MODAL_LOGIN_PASSOWORD_INPUT_ID));
		element.sendKeys(password);
	}



}
