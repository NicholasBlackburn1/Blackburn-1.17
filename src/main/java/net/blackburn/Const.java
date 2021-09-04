
/***
 * this is were  idefine all consts for my additions to mc 
 */
package net.blackburn;

import org.apache.logging.log4j.LogManager;

public class Const {
    
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
   
    // this is the logo dimention on the loading screen
    public static int px =  160;
    public static int py = 50;
    public static int  pTextureWidth = 120;
    public static int pTextureHeight= 120;

    public static int pWidth =  120;
    public static int pHight = 120;

    public static int pUOffset = 0 ;
    public static float pVOffset = 0;

    public static String RELEASE = "DEBUG";
    public static String VERSION = "1.17.1-Blackburn";
    public static String Date = "Saterday, Sept 4";



    public static void dbg(String s)
    {
        LOGGER.info("[Blackburn] " + s);
    }

    public static void warn(String s)
    {
        LOGGER.warn("[Blackburn] " + s);
    }

    public static void warn(String s, Throwable t)
    {
        LOGGER.warn("[Blackburn] " + s, t);
    }

    public static void error(String s)
    {
        LOGGER.error("[Blackburn] " + s);
    }

    public static void error(String s, Throwable t)
    {
        LOGGER.error("[Blackburn] " + s, t);
    }

    public static void log(String s)
    {
        dbg(s);
    }

}
