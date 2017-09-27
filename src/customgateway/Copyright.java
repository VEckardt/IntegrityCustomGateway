/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package customgateway;

/**
 *
 * @author veckardt
 */
public class Copyright {

    public static final String COPYRIGHT = "(c)";
    public static String copyright = "Copyright " + COPYRIGHT + " 2013, 2014, 2015 PTC Inc.";
    public static String copyrightHtml = "Copyright &copy; 2013, 2014, 2015 PTC Inc.";
    public static String programName = "Integrity Custom Gateway";
    public static String programVersion = "0.9.4";
    public static String author = "Author: Volker Eckardt";
    public static String email = "email: veckardt@ptc.com";

    public static void write() {
        System.out.println("* " + programName + " - Version " + programVersion);
        System.out.println("* A utility to enhance the Export Gateway for Integrity");
        System.out.println("* Tested with Integrity 10.4, 10.6 and 10.9");
        System.out.println("*");
        System.out.println("* " + copyright);
        System.out.println("* " + author + ", " + email + "\n");
    }

    // public static void usage() {
    //     System.out.println("*");
    //     System.out.println("* Usage: ");
    //     System.out.println("*   <path-to-javaw>\\javaw -jar <path-to-jar>\\IntegrityCustomGateway.jar");
    //     System.out.println("* Example:");
    //     System.out.println("*   C:\\Program Files\\Java\\jdk1.7.0_21\\bin\\javaw -jar C:\\IntegrityClient10\\lib\\IntegrityCustomGateway.jar");
    //     System.out.println("* Additional Notes:");
    //     System.out.println("*   - a configuration file 'CustomGateway.properties' can be used to specify default values");
    //     System.out.println("*   - a log file is created in directory '%temp%', the filename is 'IntegrityCustomGateway_YYYY-MM-DD.log'");
    //     System.out.println("*");
    // }
}
