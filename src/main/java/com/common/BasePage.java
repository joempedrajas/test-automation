package com.common;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.utils.ConfigPropertyKeys;
import com.utils.ConfigurationProperties;
import com.utils.Locator;
import com.utils.Utils;

public class BasePage {

	public static final Logger log = Logger.getLogger(BasePage.class);
	public static final String SCROLLMIDSCRIPT = "var height = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);var element = arguments[0].getBoundingClientRect().top;window.scrollBy(0, element-(height/2));";
	public static final String BASE_URL = (System.getProperty(ConfigPropertyKeys.ENVIRONMENT) == null)
			? ConfigurationProperties.getStringProperty(ConfigPropertyKeys.BASE_URL)
			: Environment.valueOf(System.getProperty(ConfigPropertyKeys.ENVIRONMENT)).toString();
	protected static final long DEFAULT_TIMEOUT = ConfigurationProperties
			.getLongProperty(ConfigPropertyKeys.DEFAULT_WAIT);

	private static WebDriver staticDriver;

	protected WebDriver driver;
	protected WebDriverWait wait;

	// DOM constants found in Visual Design
	protected static final String ACCORDION_ATTRIBUTE_NAME = "aria-expanded";
	protected static final String WEBELEMENT_ATTRIBUTE_CLASS = "class";
	protected static final String WEBELEMENT_CLASS_DISABLED = "disabled";

	@FindBy(xpath = "//h1[starts-with(@id,\"pageTitle\")]")
	protected WebElement formHeader;

	public BasePage(WebDriver driver) {
		this.driver = driver;
		wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
		PageFactory.initElements(driver, this);
	}

	public boolean isElementPresent(By locator) {
		log.info("checking if locator " + locator + " is present.");
		return !driver.findElements(locator).isEmpty();
	}

	public boolean isElementPresent(String locator, Locator type) {
		log.info("checking if locator " + locator + " is present.");
		return !driver.findElements(Utils.getLocator(locator, type)).isEmpty();
	}

	public boolean isClickable(String locator, Locator type) {
		try {

			log.info("Waiting for element to be clickable. " + locator);
			wait.until(ExpectedConditions.elementToBeClickable(Utils.getLocator(locator, type)));
			log.info(locator + " is clickable.");
		} catch (TimeoutException t) {
			log.warn(locator + " is not clickable");
			return false;
		}
		return true;
	}

	protected boolean isClickable(WebElement element) {
		try {
			log.info("Waiting for element to be clickable. " + element);
			wait.until(ExpectedConditions.elementToBeClickable(element));
			log.info(element + " is clickable.");
		} catch (TimeoutException t) {
			log.warn(element + " is not clickable");
			return false;
		}
		return true;

	}

	protected boolean isToolTipDisplayed(By locator) {
		try {
			log.info("Waiting for element to be visible. " + locator);
			wait.until((ExpectedConditions.visibilityOfElementLocated(locator)));
			log.info(locator + "is visible.");
		} catch (TimeoutException t) {
			log.warn(locator + "is not visible");
			return false;
		}
		return true;
	}

	protected boolean isToolTipDisplayed(WebElement element) {
		try {
			log.info("Waiting for element to be visible. " + element);
			wait.until((ExpectedConditions.visibilityOfElementLocated((By) element)));
			log.info(element + "is visible.");
		} catch (TimeoutException t) {
			log.warn(element + "is not visible");
			return false;
		}
		return true;
	}

	public boolean isSelected(String locator, Locator type) {
		try {
			return driver.findElement(Utils.getLocator(locator, type)).isSelected();
		} catch (NoSuchElementException e) {
			log.error(e.getMessage());
			return false;
		}
	}

	public boolean isEnabled(String locator, Locator type){
		return driver.findElement(Utils.getLocator(locator, type)).isEnabled();
	}

	public boolean isReadOnly(String locator, Locator type){
		String state = driver.findElement(Utils.getLocator(locator, type)).getAttribute("readonly");
		return (state == null)? false: true;
	}

	protected void goTo() {
		driver.get(BASE_URL);
	}

	protected void waitForVisibilityOf(WebElement element) {
		wait.until(ExpectedConditions.visibilityOf(element));
		log.info("Found Element: " + element);
	}

	protected WebElement waitForClickabilityOf(WebElement element) {
		log.info("Waiting for element to be clickable. " + element);
		return wait.until(ExpectedConditions.elementToBeClickable(element));
	}

	protected WebElement waitForClickabilityOfLocator(By locator) {
		log.info("Waiting for locator to be clickable. " + locator);
		return wait.until(ExpectedConditions.elementToBeClickable(locator));
	}

	protected WebElement waitFor(WebElement element) {
		WebElement returnElement;

		try {
			returnElement = wait.until(ExpectedConditions.visibilityOf(element));
		} catch (StaleElementReferenceException e) {
			log.error("2nd wait for element " + element + " after error ", e);
			returnElement = wait.until(ExpectedConditions.visibilityOf(element));
		} catch (Exception e) {
			log.error("3rd wait for element " + element + " after error ", e);
			returnElement = wait.until(ExpectedConditions.visibilityOf(element));
		}

		return returnElement;
	}

//	protected WebElement waitFor(By locator){
//    	log.info("Waiting for Locator to be visible : " + locator);
//    	return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
//    }
//
//    protected WebElement waitForPresence(By locator){
//    	log.info("Waiting for Locator to be present : " + locator);
//    	return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
//    }


    public String getTextInTableCell(By locator, int row){
    	return driver.findElements(locator).get(row-1).getText();
    }

	protected void waitForPageToLoad() {
		log.info("Waiting for DOM to be ready.");
		wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				String javaScript = "return document.readyState";
				return ((JavascriptExecutor) driver).executeScript(javaScript).toString().equals("complete");
			}
		});
	}

	protected void waitForJQueryToLoad() {
		wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				String jQueryScript = "return jQuery.active";
				try {
					return ((JavascriptExecutor) driver).executeScript(jQueryScript).toString().equals("0");
				} catch (Exception e) {
					log.error("JQuery check failed.", e);
					return false;
				}
			}
		});
		log.info("JQuery check true.");
	}

	protected void waitForVisibilityOfElementWhileScrolling(final WebElement element) {
		log.info("Scrolling to Element: " + element);
		wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				String scrollDown = "window.scrollTo(document.body.scrollHeight,0)";
				String scrollUp = "window.scrollTo(0, document.body.scrollHeight)";
				if (element.isDisplayed()) {
					return true;
				} else {
					log.info("Element not initially found.");
					boolean notFound = true;
					do {
						log.info("Scrolling..");
						((JavascriptExecutor) driver).executeScript(scrollDown);
						((JavascriptExecutor) driver).executeScript(scrollUp);
						if (element.isDisplayed()) {
							log.info("Element found!");
							notFound = false;
						}
					} while (notFound);
				}
				return true;
			}
		});
	}

	public void copyToClipboard(String text) {
		StringSelection stringSelection = new StringSelection(text);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}

//	protected void moveToElement(WebElement element) {
//		waitForVisibilityOf(element);
//		log.info("Moving to element: " + element);
//		String scrollToMiddle = "var height = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
//				+ "var element = arguments[0].getBoundingClientRect().top;" + "window.scrollBy(0, element-(height/2));";
//		((JavascriptExecutor) driver).executeScript(scrollToMiddle, element);
//	}

	protected void selectFromDropdown(WebElement dropdown, String dropdownName, String dropdownOption) {
		new Select(dropdown).selectByVisibleText(dropdownOption);
		log.info("Selecting " + dropdownName + ": \"" + dropdownOption + "\"");
	}

	protected void selectFromDropdownByIndex(WebElement dropdown, String dropdownName, String dropdownIndex) {
		int index = Integer.parseInt(dropdownIndex);
		new Select(dropdown).selectByIndex(index);
		log.info("Selecting by Index " + dropdownName + ": Index[" + index + "]");
	}

	protected void selectFromDropdownByValue(WebElement dropdown, String dropdownName, String dropdownValue) {
		new Select(dropdown).selectByValue(dropdownValue);
		log.info("Selecting by Value " + dropdownName + ": \"" + dropdownValue + "\"");
	}

	protected List<String> getTextValuesOfWebElementList(List<WebElement> list) {
		List<String> listValues = new ArrayList<>();
		for (WebElement item : list) {
			listValues.add(item.getText());
		}
		return listValues;
	}

	public String getTextByLocator(By locator) {
		try {
			wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		} catch (TimeoutException te) {
			log.warn("Waiting for " + locator, te);
		}
		return driver.findElement(locator).getText();
	}

	public String getTextByLocator(String locator, Locator type) {
		By loc;
		switch (type) {
		case CLASS_NAME:
			loc = By.className(locator);
			break;
		case CSS_SELECTOR:
			loc = By.cssSelector(locator);
			break;
		case ID:
			loc = By.id(locator);
			break;
		case LINK_TEXT:
			loc = By.linkText(locator);
			break;
		case NAME:
			loc = By.name(locator);
			break;
		case PARTIAL_LINK_TEXT:
			loc = By.partialLinkText(locator);
			break;
		case XPATH:
			loc = By.xpath(locator);
			break;
		default:
			return null;
		}
		log.info("Get Text by Locator : " + locator);
		return wait.until(ExpectedConditions.presenceOfElementLocated(loc)).getText();
	}

	public String getTextInTableCell(String locator, Locator type, int row) {
		return driver.findElements(Utils.getLocator(locator, type)).get(row - 1).getText();
	}

	protected String getSelectedIndexFromDropdown(WebElement dropdownElement) {
		waitForVisibilityOf(dropdownElement);
		Select dropdown = new Select(dropdownElement);
		String selectedValue = dropdown.getFirstSelectedOption().getText();
		log.info("Selected value : " + selectedValue);
		List<WebElement> options = dropdown.getOptions();
		String indexSelected = "";
		for (int index = 0; index < options.size(); index++) {
			if (options.get(index).getText().equals(selectedValue)) {
				indexSelected = Integer.toString(index);
				break;
			}
		}
		return indexSelected;
	}

	protected String getSelectedValueFromDropdown(WebElement dropdownElement) {
		waitForVisibilityOf(dropdownElement);
		Select dropdown = new Select(dropdownElement);
		log.info("Selected value : " + dropdown.getFirstSelectedOption().getAttribute("value"));
		return dropdown.getFirstSelectedOption().getAttribute("value");
	}


	/**
	 * Get Element attribute value. Useful for textbox field which does not
	 * return text after calling getText method.
	 *
	 * @param locator
	 * @param locator
	 * @return
	 */
	public String getElementValue(String locator, Locator locatorType) {
		String value = null;
		switch (locatorType) {
		case ID:
			value = getElementValue(By.id(locator));
			break;
		case NAME:
			value = getElementValue(By.name(locator));
			break;
		case CLASS_NAME:
			value = getElementValue(By.className(locator));
			break;
		case CSS_SELECTOR:
			value = getElementValue(By.cssSelector(locator));
			break;
		case LINK_TEXT:
			value = getElementValue(By.linkText(locator));
			break;
		case PARTIAL_LINK_TEXT:
			value = getElementValue(By.partialLinkText(locator));
			break;
		case XPATH:
			value = getElementValue(By.xpath(locator));
			break;
		default:
			log.error("Undefined locator type " + locatorType);
		}
		return value;
	}

	private String getElementValue(By locator) {
		log.info("Element value of : " + locator);
		return driver.findElement(locator).getAttribute("value");
	}

	protected String getElementAttribute(WebElement element, String attribute) {
		String value = element.getAttribute(attribute);
		log.info("Element attribute value " + attribute + "=" + value + " : " + element);
		return value;
	}

	public String getElementAttributeBylocator(String locator, Locator type, String attribute) {
		return getElementAttribute(getElementByLocator(locator, type), attribute);
	}

	private WebElement getElementByLocator(String locator, Locator type) {
		WebElement value = null;
		switch (type) {
		case ID:
			value = driver.findElement(By.id(locator));
			break;
		case NAME:
			value = driver.findElement(By.name(locator));
			break;
		case CLASS_NAME:
			value = driver.findElement(By.className(locator));
			break;
		case CSS_SELECTOR:
			value = driver.findElement(By.cssSelector(locator));
			break;
		case LINK_TEXT:
			value = driver.findElement(By.linkText(locator));
			break;
		case PARTIAL_LINK_TEXT:
			value = driver.findElement(By.partialLinkText(locator));
			break;
		case XPATH:
			value = driver.findElement(By.xpath(locator));
			break;
		default:
			log.error("Undefined locator type " + type);
		}
		return value;
	}

	protected void click(WebElement element) {
		waitForClickabilityOf(element);
		element.click();
		log.info("Element clicked.");
	}

	/**
	 * Alternative to click(WebElement element) method. Performs click using
	 * javascript commands
	 *
	 * @param element
	 */
	protected void clickJS(WebElement element) {
		waitForClickabilityOf(element);
		JavascriptExecutor executor = (JavascriptExecutor) driver;
		executor.executeScript("arguments[0].click();", element);
	}

	protected void enterText(WebElement element, String text) {
		element.sendKeys(text);
		log.info("Element " + element + " is populated");
	}

	protected void enterDate(WebElement element, String date) {
		element.click();
		element.click();
		element.sendKeys(date.replace("/", ""));
		log.info("Date entered");
	}

	protected void scrollToTop() {
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0)");
		log.info("Scroll to top of the page.");
	}

	protected String getPageHeaderName() {
		return formHeader.getText();
	}

	protected void checkCheckbox(WebElement element) {
		if (!element.isSelected()) {
			element.click();
			log.info("Element is marked as check : " + element);
		} else
			log.warn("Element is already selected : " + element);
	}

	protected void uncheckCheckbox(WebElement element) {
		if (element.isSelected()) {
			element.click();
			log.info("Element is unchecked : " + element);
		} else
			log.warn("Element is not selected : " + element);
	}

	public static WebDriver getStaticDriver() {
		return staticDriver;
	}

	protected static void setStaticDriver(WebDriver driver) {
		BasePage.staticDriver = driver;
	}

	protected void hoverToElement(WebElement element) {
		Actions action = new Actions(driver);
		action.moveToElement(element).build().perform();
	}

	enum Environment {

		DEV(ConfigurationProperties.getStringProperty(ConfigPropertyKeys.BASE_DEV_URL)),
		UT(ConfigurationProperties.getStringProperty(ConfigPropertyKeys.BASE_UT_URL)),
		SIT(ConfigurationProperties.getStringProperty(ConfigPropertyKeys.BASE_SIT_URL)),
		DEFAULT(ConfigurationProperties.getStringProperty(ConfigPropertyKeys.BASE_URL));

		private String baseURL;

		private Environment(String baseURL) {
			this.baseURL = baseURL;
		}

		@Override
		public String toString() {
			return baseURL;
		}
	}

    /** get the number of indexes in dropdown list
     * @param element	WebElement
     */
    protected int getNumOfListOptions(WebElement element){
    	Select drpdown = new Select(element);
    	return drpdown.getOptions().size();
    }

	public static String getBaseURL() {
		return ConfigurationProperties
				.getStringProperty(ConfigPropertyKeys.BASE_URL);
	}

	// from Helper

	public static void waitForPageToLoad(WebDriver driver) {
		log.info("Waiting for DOM to be ready.");
		log.info("URL: " + driver.getCurrentUrl());
		new WebDriverWait(driver, DEFAULT_TIMEOUT).until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				String javaScript = "return document.readyState";
				return ((JavascriptExecutor) driver).executeScript(javaScript).toString().equals("complete");
			}
		});
	}

	public WebElement waitFor(By locator){
		WebElement element = waitForPresence(locator);
		moveToElement(element);
		log.info("Waiting for Locator to be visible : " + locator);
		return (new WebDriverWait(driver, DEFAULT_TIMEOUT)).until(ExpectedConditions.visibilityOf(element));
	}

	public WebElement waitForPresence(By locator){
		log.info("Waiting for Locator to be present : " + locator);
		return (new WebDriverWait(driver, DEFAULT_TIMEOUT)).until(ExpectedConditions.presenceOfElementLocated(locator));
	}

	public void moveToElement(WebElement element) {
		// waitForVisibilityOf(driver,element); -- We move so we can check
		// visibility afterwards.
		log.info("Moving to element: " + element);
		String scrollToMiddle = "var height = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
				+ "var element = arguments[0].getBoundingClientRect().top;" + "window.scrollBy(0, element-(height/2));";
		((JavascriptExecutor) driver).executeScript(scrollToMiddle, element);
	}
}
