package dbs.smileytown.poc.utils;

import org.apache.log4j.Logger;

/**
 * Created by razelsoco on 28/1/16.
 */
public class STLogger {


    private static final Logger log = Logger.getLogger(STLogger.class);

    public static Logger getLogger(){
        return log;
    }

}
