package com.utils;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

public final class ConfigurationProperties {

    private static Configuration INSTANCE;
    final static Logger LOG = Logger.getLogger(ConfigurationProperties.class);

    public static String getStringProperty(String key) {
        String value = null;
        if(null == INSTANCE) {
            try {
                INSTANCE = new PropertiesConfiguration("config.properties");
            } catch (ConfigurationException e) {
                e.printStackTrace();
                LOG.info(e.toString());
            }
        }
        value = INSTANCE.getString(key, null);
        return value;
    }

    public static String getStringProperty(String propertyFile, String key){
    	try {
			return new PropertiesConfiguration(propertyFile).getString(key);
		} catch (ConfigurationException e) {
			LOG.error(e.getMessage());
			return null;
		}
    }

    public static long getLongProperty(String key) {
        long value = 0;
        if(null == INSTANCE) {
            try {
                INSTANCE = new PropertiesConfiguration("config.properties");
            } catch (ConfigurationException e) {
                e.printStackTrace();
                LOG.info(e.toString());
            }
        }
        value = INSTANCE.getLong(key, 0);
        return value;
    }

    private ConfigurationProperties(){}

}
