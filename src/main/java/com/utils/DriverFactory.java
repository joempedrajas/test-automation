package com.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public final class DriverFactory {

	static final Logger log = Logger.getLogger(DriverFactory.class);

	public enum DriverTypes {
		IE, CHROME, FIREFOX, EDGE, OPERA;

		private String browser;

		@Override
		public String toString(){
			return browser;
		}
	}

	public static WebDriver createDriver(boolean isGrid, DriverTypes type) throws MalformedURLException {
		WebDriver generatedWebDriver;
		switch (type) {
		case IE:
			System.setProperty("webdriver.ie.driver", ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DRIVERS_IE_LOCATION));
			DesiredCapabilities cap = DesiredCapabilities.internetExplorer();
			cap.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
			cap.setCapability("ie.ensureCleanSession", true);
			cap.setCapability("takesScreenshot", true);
			generatedWebDriver = isGrid ? createRemoteDriver(cap) : new InternetExplorerDriver();
			break;
		case CHROME:
			System.setProperty("webdriver.chrome.driver", ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DRIVERS_CHROME_LOCATION));
			generatedWebDriver = isGrid ? createRemoteDriver(DesiredCapabilities.chrome()) : new ChromeDriver();
			break;
		case FIREFOX:
			System.setProperty("webdriver.gecko.driver", ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DRIVERS_FIREFOX_LOCATION));
			generatedWebDriver = isGrid ? createRemoteDriver(DesiredCapabilities.firefox()) : new FirefoxDriver();
			break;
		case EDGE:
			System.setProperty("webdriver.edge.driver", ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DRIVERS_EDGE_LOCATION));
			generatedWebDriver = isGrid ? createRemoteDriver(DesiredCapabilities.edge()) : new EdgeDriver();
			break;
		case OPERA:
			System.setProperty("webdriver.opera.driver", ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DRIVERS_OPERA_LOCATION));
			OperaOptions option = new OperaOptions();
			option.setBinary(new File(ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DRIVERS_OPERA_BINARY_LOCAL)));
			DesiredCapabilities operaCap = DesiredCapabilities.operaBlink();
			Map<String, Object> map = new HashMap<>();
			map.put("args", new ArrayList<>());
			map.put("extensions", new ArrayList<>());
			map.put("binary", ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DRIVERS_OPERA_BINARY_GRID));
			operaCap.setCapability("operaOptions", map);
			generatedWebDriver = isGrid ? createRemoteDriver(operaCap) : new OperaDriver(option);
			break;
		default:
			generatedWebDriver = new HtmlUnitDriver();
			break;
		}
		return generatedWebDriver;
	}

	public static RemoteWebDriver createRemoteDriver(DesiredCapabilities cap) throws MalformedURLException {
		String platform = (System.getProperty("gridPlatform") == null)
				? ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DRIVERS_GRID_PLATFORM)
				: System.getProperty("gridPlatform");
		cap.setCapability("platform", platform);
		cap.setAcceptInsecureCerts(true);
		log.info("Remote Driver Capabilities : \n" + cap.toString());
		URL url = new URL(ConfigurationProperties.getStringProperty(ConfigPropertyKeys.DRIVERS_GRID_HUB));
		log.info("Remote Grid Hub : " + url.toString());
		return new RemoteWebDriver(url, cap);
	}

}
