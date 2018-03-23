package com.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.Augmenter;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Utils {

    final static Logger log = Logger.getLogger(Utils.class);

	public static By getLocator(String locator, Locator type){
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
		return loc;
	}

	public static String getValueFromObject(int index, Object[] dataRow){
		return dataRow[index].toString();
	}

	/**
	 *
	 * @param format - ex. "yyyy-MM-dd_HH.mm.ss.SS"
	 * @return String date format
	 */
	public static String getCurrentDate(String format){
		SimpleDateFormat date = new SimpleDateFormat(format);
		return date.format(Calendar.getInstance().getTime());
	}

	public static String getSimpleDateFormat(Date date, String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static String getMachineName(){
		String machine = null;
		try
		{
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    machine = addr.getHostName();
		}
		catch (Exception e)
		{
			log.warn(e.getMessage());
		}
		return machine;
	}

    public static String objectToString(Object[][] o){
        StringBuffer buff = new StringBuffer();
        for(int i = 0; i < o.length ; i++){
            buff.append("\n" + objectToString(o[i]));
        }
        return buff.toString();
    }

    public static String objectToString(Object[] o){
        StringBuffer buff = new StringBuffer();
        for(int i=0; i < o.length; i++){
            buff.append(o[i].toString() + " | ");
        }
        return buff.toString();
    }

    public static Object[][] objectToMap(Object[][] o, List<String> headers){
    	Object[][] oNew = new Object[o.length][1];
    	for(int i=0; i < o.length ; i++){
    		Map<String,String> map = new HashMap<String,String>();
    		for(int j = 0; j < headers.size(); j++){
    			map.put(headers.get(j), o[i][j].toString());
    		}
    		oNew[i][0] = map;
    	}
    	return oNew;
    }

    public static Map<String,String> toMap(List<String> headers, List<String> datas){
    	Map<String,String> mapData = new HashMap<>();
    	for(int i = 0; i < headers.size(); i++){
    		if(i < datas.size())
    			mapData.put(headers.get(i), datas.get(i));
    		else
    			mapData.put(headers.get(i), "");
    	}
    	return mapData;
    }

    public static Map<String, Object> toMapObject(List<Object> headers, List<Object> datas){
    	Map<String,Object> mapData = new HashMap<>();
    	for(int i = 0; i < headers.size(); i++){
    		mapData.put(headers.get(i).toString(), datas.get(i));
    	}
    	return mapData;
    }

    /**
     * Captures screenshot on the Browser
     * @param driver - WebDriver
     * @param path - location (ex. "C:\\screenshot\\" or target/screenshot/)
     * @param filename - ex. "screenshot1.png" or "screenshot1"
     */
    public static void captureScreenshot(WebDriver driver, String path,  String filename) {
        try {
            TakesScreenshot capture = (TakesScreenshot) driver;
            File screenshot = capture.getScreenshotAs(OutputType.FILE);
            if (!path.endsWith(File.separator))
                path = path + File.separator;
            if (!filename.endsWith(".png"))
                filename = filename + ".png";
            File file = new File(path + filename);
            file.getParentFile().mkdirs();
            FileUtils.copyFile(screenshot, file);
            log.info("Captured screenshot | " + path + filename);
        } catch(NoSuchWindowException nswe){
        	log.error("Unable to Capture screenshot. Browser window is closed.", nswe);
        } catch(WebDriverException wde){
        	log.error("Unable to Capture Screenshot.",wde);
        } catch(IOException ioe){
        	log.error("Unable to copy file to " + path + " | " + filename,ioe);
        }catch (Exception e){
        	log.error("",e);
        }
    }

    public static void augmentedScreenshot(WebDriver driver, String path,  String filename){
    	log.info("Capture Screenshot Aug");
    	captureScreenshot(new Augmenter().augment(driver), path, filename);
    }

    public static void captureScreenshot(WebDriver driver,  String filename) {
        TakesScreenshot capture = (TakesScreenshot) driver;
        File screenshot = capture.getScreenshotAs(OutputType.FILE);

        String path = "target/screenshot_" + getCurrentDate("yyyy.MM.dd") + "/";

        if (!filename.endsWith(".png"))
            filename = filename + ".png";
        File file = new File(path + filename);
        file.getParentFile().mkdirs();
        try {
            FileUtils.copyFile(screenshot, file);
        } catch (IOException e) {
            System.out.println("Unable to copy screenshot " + file.getName() + "  to " + file.getAbsolutePath());
            System.out.println(e.getMessage());
        }

    }

    public static String[] objectArrayToString(Object[] obj){
        String[] stringArray = Arrays.copyOf(obj, obj.length, String[].class);
        return stringArray;
    }

    public static boolean deleteFile(String filePath){
		return isFileExist(filePath)? new File(filePath).delete(): false;
	}

	private static boolean isFileExist(String filePath){
		return new File(filePath).exists();
	}

	public static String createPayload(String payload, Map<String, String> datas){
		String createdPayload = payload;
		for(Map.Entry<String, String> data: datas.entrySet()){
			createdPayload = createdPayload.replace("${" + data.getKey() + "}", data.getValue());
		}
		return createdPayload;
	}

	public static String readFileString(String filePath) throws IOException{
		Path path = Paths.get(filePath, new String[0]);
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}

	public static String readXpath(String xmlBody, String xpathExpression) throws Exception{
		try{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xmlBody));
			Document doc = builder.parse(is);
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			String result = xpath.compile(xpathExpression).evaluate(doc);
			log.info(xpathExpression);
			log.info(result);
			return result;
		}catch(XPathExpressionException|ParserConfigurationException|IOException|SAXException xe){
			log.error(xe.getMessage());
			throw xe;
		}catch(Exception e){
			throw e;
		}
	}

	public static String resultSetToString(ResultSet set){
		try{
			StringBuilder build = new StringBuilder();
			while(set.next()){
				for(int i = 1; i <= set.getMetaData().getColumnCount(); i++)
					build.append(set.getString(i) + "\t");
				build.append("\n");
			}
			return build.toString();
		}catch(SQLException e){
			log.error(e.getMessage(), e);
			return null;
		}
	}
}
