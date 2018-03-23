package com.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.tools.generic.SortTool;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;
import org.uncommons.reportng.AbstractReporter;
import org.uncommons.reportng.ReportNGException;

public class SimpleHTMLReporter extends AbstractReporter{

	private static final String FRAMES_PROPERTY = "org.uncommons.reportng.frames";
    private static final String ONLY_FAILURES_PROPERTY = "org.uncommons.reportng.failures-only";
    private static final String TESTNG_TEMPLATES_PATH = "org/uncommons/reportng/templates/html/";
    private static final String DEMO_TEMPLATES_PATH = "report/templates/simplereport/";
    private static final String SIMPLE_REPORT_FILE = "SimpleHTMLReport.html";
    private static final String SUITES_KEY = "suites";
    private static final String ONLY_FAILURES_KEY = "onlyReportFailures";
    private static final String REPORT_DIRECTORY = "html";
    private static final String META_KEY = "meta";
    private static final String UTILS_KEY = "utils";
    private static final String MESSAGES_KEY = "messages";
    private static final ResourceBundle MESSAGES = ResourceBundle.getBundle("org.uncommons.reportng.messages.reportng", META.getLocale());
    private static final CustomReportNGUtils UTILS = new CustomReportNGUtils();
    private static final String ENCODING = "UTF-8";
    private static final String SORTER_KEY = "sorter";

	public SimpleHTMLReporter() {
		super(TESTNG_TEMPLATES_PATH);
	}

	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectoryName) {
		removeEmptyDirectories(new File(outputDirectoryName));

        boolean useFrames = System.getProperty(FRAMES_PROPERTY, "true").equals("true");
        boolean onlyFailures = System.getProperty(ONLY_FAILURES_PROPERTY, "false").equals("true");

        File outputDirectory = new File(outputDirectoryName, REPORT_DIRECTORY);
        outputDirectory.mkdirs();

        //check if suites count is more than 1 and running in a single suite that calls multiple suite
        //removes the last suite in suites so that it will not displayed in the report
        //if not removed, it will display in the report with zero test count
        if(suites.size() > 1 && suites.get(suites.size() - 1).getName().equals("allSuites")){
            suites.remove(suites.size() - 1);
        }

        try {
            createSimpleHTMLReport(suites, outputDirectory, !useFrames, onlyFailures);
        } catch (Exception ex) {
            throw new ReportNGException("Failed generating HTML report.", ex);
        }
	}

	private void createSimpleHTMLReport(List<ISuite> suites, File outputDirectory,
            boolean isIndex, boolean onlyFailures) throws Exception {
        VelocityContext context = createContext();
        context.put(SUITES_KEY, suites);
        context.put(ONLY_FAILURES_KEY, onlyFailures);
        context.put(SORTER_KEY, new SortTool());
        generateFile(new File(outputDirectory, SIMPLE_REPORT_FILE), SIMPLE_REPORT_FILE + TEMPLATE_EXTENSION, context, DEMO_TEMPLATES_PATH);
    }

    /**
     * DEMO
     * Overrides AbstractReporter.java createContext method
     * UTILS - change to CustomReportNGUtils object which extends ReportNGUtils class
     */
    @Override
    protected VelocityContext createContext() {
        VelocityContext context = new VelocityContext();
        context.put(META_KEY, META);
        context.put(UTILS_KEY, UTILS);
        context.put(MESSAGES_KEY, MESSAGES);
        return context;
    }

	/**
	 * Overload
	 * @param file
	 * @param templateName
	 * @param context
	 * @param path
	 * @throws Exception
	 */
	protected void generateFile(File file, String templateName, VelocityContext context, String path) throws Exception {
        Writer writer = new BufferedWriter(new FileWriter(file));
        try {
            Velocity.mergeTemplate(path + templateName, ENCODING, context, writer);
            writer.flush();
        } finally {
            writer.close();
        }
    }
}
