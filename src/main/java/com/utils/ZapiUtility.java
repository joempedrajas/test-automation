package com.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class ZapiUtility {
	private static final Logger LOGGER = Logger.getLogger(ZapiUtility.class);


	public static void main(String args[]) {
			ZapiUtility zap = new ZapiUtility();
			String id  = zap.createTestCycles();
			zap.addTestsToCycle(id);
	}



	public String createTestCycles()  {


		DateFormat dtf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date date = new Date();
		String runDate = dtf.format(date);


		/** Declare the Variables here **/
		String usernameAndPassword = "joe.pedrajas:yoazakura";
		String url = "http://localhost:8080";
		Base64 ed = new Base64();
		String auth = new String(ed.encode(usernameAndPassword.getBytes()));
		int projectId = 10001;
		int versionId = 10000;
		String cycleName = "Test Cycle -- API DEMO " + runDate;
		String cycleDescription = "Created by ZAPI CLOUD API";
		String createCycleUri = url + "/rest/zapi/latest/cycle";

		/** Cycle Object created - DO NOT EDIT **/
		JSONObject createCycleObj = new JSONObject();
		createCycleObj.put("name", cycleName);
		createCycleObj.put("description", cycleDescription);
		createCycleObj.put("startDate", "10/Aug/18");
		createCycleObj.put("projectId", projectId);
		createCycleObj.put("versionId", versionId);

		StringEntity cycleJSON = null;
		try {
			cycleJSON = new StringEntity(createCycleObj.toString());
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		/** Request **/
		HttpResponse response = null;
		HttpClient restClient = HttpClientBuilder.create().build();
		URI uri = null;
		try {
			uri = new URI(createCycleUri);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		HttpPost createCycleReq = new HttpPost(uri);
		createCycleReq.addHeader("Content-Type", "application/json");
		createCycleReq.addHeader("Authorization", "Basic " + auth);
		createCycleReq.setEntity(cycleJSON);

		try {
			response = restClient.execute(createCycleReq);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int statusCode = response.getStatusLine().getStatusCode();
		LOGGER.info("Status code: " + statusCode);
		LOGGER.info("Response : " + response.toString());

		String cycleId = "-1";
		String string = null;
		if (statusCode >= 200 && statusCode < 300) {
			HttpEntity entity = response.getEntity();
			try {
				string = EntityUtils.toString(entity);
				JSONObject cycleObj = new JSONObject(string);
				cycleId = cycleObj.getString("id");
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			try {
				throw new ClientProtocolException("Unexpected response status: " + statusCode);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		}
		return cycleId;
	}

	public String addTestsToCycle(String cycleID) {

		/** Declare the Variables here **/
		String usernameAndPassword = "joe.pedrajas:yoazakura";
		String url = "http://localhost:8080";
		Base64 ed = new Base64();
		String auth = new String(ed.encode(usernameAndPassword.getBytes()));
		int projectId = 10001;
		int versionId = 10000;
		String addTestUrl = url + "/rest/zapi/latest/execution/addTestsToCycle/";

		String[] issues = {"JOB-2"}; //Issue Id's to be added to Test Cycle, add more issueKeys separated by comma

		JSONObject addTestsObj = new JSONObject();
		addTestsObj.put("issues", issues);
		addTestsObj.put("method", "1");
		addTestsObj.put("projectId", projectId);
		addTestsObj.put("versionId", versionId);
		addTestsObj.put("cycleId", cycleID);

		StringEntity addTestsJSON = null;
		try {
			addTestsJSON = new StringEntity(addTestsObj.toString());
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}


		HttpResponse response = null;
		HttpClient restClient = HttpClientBuilder.create().build();

		HttpPost addTestsReq = new HttpPost(addTestUrl);
		addTestsReq.addHeader("Content-Type", "application/json");
		addTestsReq.addHeader("Authorization", "Basic "+ auth);
		addTestsReq.setEntity(addTestsJSON);

		try {
			response = restClient.execute(addTestsReq);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int statusCode = response.getStatusLine().getStatusCode();
		System.out.println(statusCode);
		System.out.println(response.toString());
		String string = null;
		if (statusCode >= 200 && statusCode < 300) {
			HttpEntity entity = response.getEntity();
			try {
				string = EntityUtils.toString(entity);
				System.out.println(string);
				JSONObject cycleObj = new JSONObject(entity);
				System.out.println(cycleObj.toString());
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else {
			try {
				throw new ClientProtocolException("Unexpected response status: " + statusCode);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			}
		}
		return string;
	}
}
