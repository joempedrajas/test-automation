package com.utils;

import java.util.Set;

import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.uncommons.reportng.ReportNGUtils;

public class CustomReportNGUtils extends ReportNGUtils {

    public int getCountFromSet(int testDataId, Set<ITestResult> test){
        int count = 0;
        for(ITestResult result: test){
            if(null != result.getAttribute("test-data-id") && Integer.parseInt(result.getAttribute("test-data-id").toString()) == testDataId)
                count++;
        }
        return count;
    }

    public long getDuration(ISuite suite){
        long duration = 0;
        for(ISuiteResult testResult: suite.getResults().values()){
            duration += getDuration(testResult.getTestContext());
        }
        return duration;
    }

    public int add(int value1 , long value2){
        return (int) ((long)value1 + value2);
    }

}
