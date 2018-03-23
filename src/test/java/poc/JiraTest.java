package poc;

import java.util.Scanner;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.automationlabs.rest.RestCredentials;
import com.automationlabs.rest.RestProperties;
import com.automationlabs.rest.jira.JiraRest;
import com.automationlabs.rest.jira.config.JiraPropertyKeys;

public class JiraTest {

	private static final Logger log = Logger.getLogger(JiraTest.class);

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		String user = sc.nextLine();
		String pass = sc.nextLine();

		RestCredentials cred = new RestCredentials("http://localhost:8080", user, pass);
		JiraRest jira = new JiraRest(cred);
		log.info("RES : " + jira.viewIssueBasic("JOB-2"));
	}

	@Test
	public void jenkinsJiraTest(){
		String JIRA_URL = RestProperties.getStringProperty(JiraPropertyKeys.JIRA_BASEURL);
		RestCredentials cred = new RestCredentials(JIRA_URL, System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_USER), System.getProperty(JiraPropertyKeys.JIRA_CREDENTIALS_PASS));
		JiraRest jira = new JiraRest(cred);
		log.info(jira.viewIssueBasic("JOB-2"));
	}

}
