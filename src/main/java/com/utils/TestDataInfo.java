package com.utils;

import org.apache.log4j.Logger;

public class TestDataInfo {
    public static String ID;
    public static int size = 0;

    private static final Logger log = Logger.getLogger(TestDataInfo.class);

    private TestDataInfo(){}

    public static String getCurrentID() {
        return ID;
    }

    public static int getTestDataSize(){
        return size;
    }

    public static void setCurrentID(String id) {
        ID = id;
        size++;
        log.info("Running : " + id);
    }

    public static void resetSize(){
        size = 0;
    }
}
