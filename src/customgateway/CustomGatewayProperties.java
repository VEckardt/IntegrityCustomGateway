/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package customgateway;

// import static com.mks.gateway.driver.word.exporter.WordTransformer.TEMPLATE_NAME;
import com.ptc.services.common.api.ApplicationProperties;

/**
 *
 * @author veckardt
 */
public final class CustomGatewayProperties extends ApplicationProperties {

    public static String fldSummary;
    public static String fldType;
    public static String fldRevision;
    public static String propGatewayConfigurations;
    public static String propGatewayConfigTestObjectiveProtocol;
    public static String defaultGatewayConfigTestObjectiveProtocol;

    // Other Properties
    public static String defaultGatewayConfig;
    public String gatewayExportPath;
    public static String propShowParentage;
    public String keepOpen;
    public String fileNameConvention;

    // Constructor
    public CustomGatewayProperties() {
        // loadProperties();
        super(CustomGateway.class);
        // List of Field Names used in version with Testting
        // Field Names
        fldSummary = getProperty("fldSummary", "Summary");
        fldType = getProperty("fldType", "Type");
        fldRevision = getProperty("fldRevision", "Type");
        // Config Property
        propGatewayConfigurations = getProperty("propGatewayConfigurations", "Gateway.Configurations");
        propGatewayConfigTestObjectiveProtocol = getProperty("propGatewayConfigTestObjectiveProtocol", "Gateway.Config.Test.Objective.Protocol");
        propShowParentage = getProperty("propShowParentage", "false");
        // Other Properties
        defaultGatewayConfig = getProperty("defaultGatewayConfig", "Please Select One");
        defaultGatewayConfigTestObjectiveProtocol = getProperty("defaultGatewayConfigTestObjectiveProtocol", "Test Objective Protocol");
        keepOpen = getProperty("keepOpen", "true");
        gatewayExportPath = getProperty("gatewayExportPath", "C:\\IntegrityWordExport\\");
        // fileNameConvention = getProperty("fileNameConvention", "<%Summary%>_<%Type%>_<%ID%>.docx");
        fileNameConvention = getProperty("fileNameConvention", "<%Type%>_<%ID%>.docx");
    }

    public void setGatewayExportPath(String gatewayExportPath) {
        this.gatewayExportPath = gatewayExportPath;
    }
}
