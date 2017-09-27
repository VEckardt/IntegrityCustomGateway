/*
 * Copyright:      Copyright 2015 (c) Parametric Technology GmbH
 * Product:        PTC Integrity Lifecycle Manager
 * Author:         V. Eckardt, Senior Consultant ALM
 * Purpose:        Custom Developed Code
 * **************  File Version Details  **************
 * Revision:       $Revision: 1.10 $
 * Last changed:   $Date: 2016/03/17 20:09:59CET $
 */
package customgateway;

import com.mks.api.Command;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.impl.ItemImpl;
import com.mks.api.response.impl.ItemListImpl;
import com.ptc.services.common.api.IntegrityAPI;
import com.ptc.services.common.api.IntegrityDocBaseline;
import com.ptc.services.common.api.IntegrityDocBaselineList;
import com.ptc.services.common.api.IntegrityMessages;
import com.ptc.services.common.tools.EnvUtil;
import com.ptc.services.common.tools.FileUtils;
import static com.ptc.services.common.tools.StringUtils.strip;
import static com.ptc.services.common.tools.StringUtils.toUpperCase;
import static customgateway.APIUtils.getNewDocIdList;
import static customgateway.CustomGatewayProperties.defaultGatewayConfigTestObjectiveProtocol;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.System.out;
import java.net.URL;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import jfx.messagebox.MessageBox;

/**
 * FXML Controller class
 *
 * @author veckardt
 */
public class CustomGatewayController implements Initializable {

    @FXML
    ChoiceBox cbConfig, cbLabel;
    @FXML
    Label lDocument;
    @FXML
    Label lDirectoryName;
    @FXML
    Label clock;
    @FXML
    Label lHostAndPort;
    @FXML
    public CheckBox openInWord;
    @FXML
    public CheckBox setAsDefault;
    @FXML
    public CheckBox showParentage;
    @FXML
    public CheckBox cbGeneratePDF;
    @FXML
    public CheckBox cbSubstituteParameters;
    @FXML
    public CheckBox cbKeepOpen;
    @FXML
    private ProgressBar progressBar;

    private static final Map<String, String> env = System.getenv();
    private static final IntegrityAPI imSession = new IntegrityAPI(env, "IntegrityCustomGateway");
    private String itemType = "";
    private CustomGatewayProperties props = new CustomGatewayProperties();
    private static String fileName = "TestProtocol";
    private String gatewayConfig = "";
    private String gatewayConfigTestObjectiveProtocol = "";
    private String issueOrDocID = env.get("MKSSI_ISSUE0");
    private String issueOrDocIDList = "";
    private int numIssuesSelected = 1;
    private final String currentDocFilter = env.get("ILMIM_DOC_FILTER");
    IntegrityMessages MC = new IntegrityMessages(CustomGateway.class);
    static Timeline timeline;
    WorkItem wiDoc = null;
    public static final String currentTag = "* current *";

    @FXML
    private void cancelAction(ActionEvent event) throws APIException, IOException {
        imSession.terminate();
        System.exit(0);
    }

    @FXML
    private void openInMsWordAction(ActionEvent event) throws APIException, IOException {
        imSession.terminate();
        System.exit(0);
    }

    @FXML
    private void updateFileName(WorkItem wiDoc) {
        String value;
        fileName = props.fileNameConvention;
        for (String part : props.fileNameConvention.split("[<>]")) {
            // log("part: " + part, 3);
            if (part.startsWith("%")) {
                if (part.replace("%", "").toLowerCase().contentEquals("id")) {
                    value = issueOrDocID;
                } else {
                    // log ("")
                    value = strip(toUpperCase(wiDoc.getField(part.replace("%", "")).getValueAsString() + ""));
                }
                fileName = fileName.replace("<" + part + ">", value);
            }
        }
        if (cbLabel.getValue().toString().contentEquals(currentTag)) {
            fileName = fileName.replace(".docx", "." + getFileExtension(cbConfig.getValue().toString()));
        } else {
            fileName = fileName.replace(".docx", "_" + cbLabel.getValue().toString().replaceAll("[^a-zA-Z0-9_.]", "") + "." + getFileExtension(cbConfig.getValue().toString()));
        }

        // special handling if multiple items are selected
        if (numIssuesSelected > 1) {
            int dotPos = fileName.indexOf(".");
            fileName = wiDoc.getField("Type").getValueAsString().replaceAll("[^a-zA-Z0-9_.]", "") + "s" + fileName.substring(dotPos);
        }

        lDocument.setText(fileName);
    }

    private String getFileExtension(String config) {
        if (config.toLowerCase().contains("matrix") || config.toLowerCase().contains("excel") || config.toLowerCase().contains("xls")) {
            if (config.toLowerCase().contains("macro")) {
                return ("xlsm");
            } else {
                return ("xlsx");
            }
        }
        return ("docx");
    }

    @FXML
    private void bChangeDir() {
        final DirectoryChooser directoryChooser
                = new DirectoryChooser();
        File expPath = new File(props.gatewayExportPath);
        if (expPath.exists()) {
            directoryChooser.setInitialDirectory(new File(props.gatewayExportPath));
        }
        directoryChooser.setTitle("Select Export Directory");
        final File selectedDirectory
                = directoryChooser.showDialog(CustomGateway.stage);
        if (selectedDirectory != null) {
            props.setGatewayExportPath(selectedDirectory.getAbsolutePath() + "\\");
            lDirectoryName.setText(props.gatewayExportPath);
            // props.saveProperties();
        }
    }

    @FXML
    private void bOpenLogFile() {
        imSession.openLogFile();
    }

    @FXML
    private void bOpenOfficeFile() {
        FileUtils.openWindowsFile("Microsoft Office", props.gatewayExportPath + fileName);
    }

    @FXML
    private void generateAction(ActionEvent event) {
        // String filePath = WordExportProperties.gatewayExportPath + fileName;
        gatewayConfig = cbConfig.getValue().toString();
        if (setAsDefault != null && setAsDefault.isSelected()) {
            log("setAsDefault.isSelected(): " + setAsDefault.isSelected(), 2);
            // props.setProperty("gatewayConfig", gatewayConfig);
            props.put("gatewayConfig+" + itemType, gatewayConfig);
            // props.put("showParentage+" + itemType, showParentage.isSelected() ? "true" : "false");

            props.put("gatewayExportPath", props.gatewayExportPath);
            props.put("keepOpen", String.valueOf(cbKeepOpen.isSelected()));
            props.saveProperties();
            log("INFO: Properties " + props.getPropFile() + " saved.", 2);
        }
        timeline.playFromStart();
        generateFile(true);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        // openInWord.setDisable(false);

        lHostAndPort.setText(EnvUtil.getUser() + "@" + EnvUtil.getHostName() + ":" + EnvUtil.getPort());

        progressBar.setProgress(0);
        cbConfig.getItems().clear();
        cbLabel.getItems().clear();
        // VE: openInWord.setSelected(true);
        lDirectoryName.setText(props.gatewayExportPath);
        lDirectoryName.setTooltip(new Tooltip("Click to change the target directory"));
        lDocument.setTooltip(new Tooltip("Click to re-open the document"));

        if (props.keepOpen != null) {
            cbKeepOpen.setSelected(props.keepOpen.contentEquals("true"));
        }

        cbConfig.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateFileName(wiDoc);
            }

        });

        // final Label clock = new Label();
        final DateFormat format = DateFormat.getTimeInstance();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Calendar cal = Calendar.getInstance();
                clock.setText(format.format(cal.getTime()));
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        if (env.get("MKSSI_ISSUE0") == null && env.get("MKSSI_DOCUMENT") + "x" == null) {
            MessageBox.show(CustomGateway.stage,
                    "Please select one item to run the Custom Gateway!",
                    "Gateway Error 1",
                    MessageBox.ICON_ERROR | MessageBox.OK);
            System.exit(1);

        }
        if (imSession == null) {
            MessageBox.show(CustomGateway.stage,
                    "Invalid Integrity Session, please check your setup!",
                    "Gateway Error 3",
                    MessageBox.ICON_ERROR | MessageBox.OK);
            System.exit(3);
        }

        String gatewayConfigs = "";

        // determine the type of selection
        if (env.get("MKSSI_DOCUMENT") != null) {
            issueOrDocID = env.get("MKSSI_DOCUMENT");
            issueOrDocIDList = issueOrDocID;
        } else {
            numIssuesSelected = Integer.parseInt(env.get("MKSSI_NISSUE"));
            issueOrDocID = env.get("MKSSI_ISSUE0");
            for (int i = 0; i < numIssuesSelected; i++) {
                String issue = env.get("MKSSI_ISSUE" + i);
                issueOrDocIDList = issueOrDocIDList + " " + issue;
            }
        }

        // MKSSI_NISSUE // only for try-out reasons
        if (issueOrDocID.contentEquals("14047")) {
            issueOrDocID = "14041-1.0";
        }

        // String summary = "";
        try {
            // Determine the Summary and the Type
            // Command cmd = new Command(Command.IM, "issues");
            // cmd.addOption(new Option("fields", "MKSIssueLabels," + CustomGatewayProperties.fldSummary + "," + CustomGatewayProperties.fldType + "," + CustomGatewayProperties.fldRevision));
            Command cmd = new Command(Command.IM, "viewissue");
            cmd.addOption(new Option("showLabels"));

            cmd.addSelection(issueOrDocID);
            Response res = imSession.executeCmd(cmd);
            wiDoc = res.getWorkItems().next();
            // WorkItem wi = res.getWorkItem(issueOrDocID);

            IntegrityDocBaselineList dbl = new IntegrityDocBaselineList(wiDoc.getField("MKSIssueLabels"));
            cbLabel.getItems().add(currentTag);
            for (IntegrityDocBaseline db : dbl) {
                cbLabel.getItems().add(db.getLabel());
            }
            cbLabel.getSelectionModel().selectFirst();
            if (cbLabel.getItems().size() == 1) {
                cbLabel.setDisable(true);
            }
            itemType = wiDoc.getField(CustomGatewayProperties.fldType).getValueAsString();
            // summary = wiDoc.getField(CustomGatewayProperties.fldSummary).getString();

            log("INFO: currentDocFilter: " + currentDocFilter, 1);

            try {
                ReadXMLConfig xmlDef = new ReadXMLConfig(itemType);
                gatewayConfigs = xmlDef.getConfigList();
                out.println("INFO: ConfigList from XML: '" + xmlDef.getConfigList() + "'");
            } catch (FileNotFoundException ex) {
                out.println("NOT-FOUND: ConfigList from XML: ' " + ex.getMessage());
            }
            if (gatewayConfigs.isEmpty()) {
                // Get Type Property for propGatewayConfigurations
                gatewayConfigs = imSession.getTypePropertyValue(itemType, CustomGatewayProperties.propGatewayConfigurations);
                log("INFO: GatewayConfigs: " + gatewayConfigs, 1);
            }

            // Get Type Property for propGatewayConfigTestObjectiveProtocol
            gatewayConfigTestObjectiveProtocol = imSession.getTypePropertyValue(
                    itemType,
                    CustomGatewayProperties.propGatewayConfigTestObjectiveProtocol);

            if (gatewayConfigTestObjectiveProtocol.isEmpty()) {
                gatewayConfigTestObjectiveProtocol = defaultGatewayConfigTestObjectiveProtocol;
            }

            log("INFO: gatewayConfigTestObjectiveProtocol: " + gatewayConfigTestObjectiveProtocol, 1);

        } catch (APIException ex) {
            log("ERROR 2: " + ex.toString(), 1);
            Logger.getLogger(CustomGatewayController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            MessageBox.show(CustomGateway.stage,
                    MC.getMessage("OTHER_ERROR") + "\n(" + ex.getMessage() + ")",
                    "Gateway Error 2",
                    MessageBox.ICON_ERROR | MessageBox.OK);
            System.exit(2);
        } catch (Exception ex) {
            log("ERROR 3: " + ex.toString(), 1);
            Logger.getLogger(CustomGatewayController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            MessageBox.show(CustomGateway.stage,
                    MC.getMessage("OTHER_ERROR") + "\n(" + ex.getMessage() + ")",
                    "Gateway Error 3",
                    MessageBox.ICON_ERROR | MessageBox.OK);
            System.exit(2);
        }

        if (!gatewayConfigs.isEmpty()) {
            gatewayConfig = gatewayConfigs;

            // multiple configurations entered
            if (gatewayConfigs.contains(",")) {
                cbConfig.getItems().addAll(Arrays.asList(gatewayConfigs.split(",")));
                cbConfig.getSelectionModel().selectFirst();
                gatewayConfig = gatewayConfigs.split(",")[0];

                String defaultGatewayConfig = props.getProperty("gatewayConfig+" + itemType, CustomGatewayProperties.defaultGatewayConfig);

                for (int k = 1; k <= cbConfig.getItems().size(); k++) {
                    if (cbConfig.getItems().get(k - 1).toString().contentEquals(defaultGatewayConfig)) {
                        cbConfig.getSelectionModel().select(k - 1);
                        gatewayConfig = defaultGatewayConfig;
                        break;
                    }
                }
            }

        } else {
            gatewayConfig = CustomGatewayProperties.defaultGatewayConfig;
        }

        // Construct the file name
        // fileName = strip(toUpperCase(summary) + "_" + itemType) + "_" + issueOrDocID + ".xlsx";
        // updateFileName(wiDoc);
        // lDocument.setText(fileName);
        log("INFO: FileName: '" + fileName + "'", 1);

        // determine if the layout is a dynamic layout
        if (env.get("MKSSI_WINDOW") != null && env.get("MKSSI_WINDOW").contentEquals("documentview")) {
            log("INFO: List of selected columns: " + EnvUtil.getListOfColumns(), 1);
        }

        // log("cbConfig.getItems().size(): " + cbConfig.getItems().size(), 1);
        // if (cbConfig.getItems().size() < 2) {
        // generateFile(false);
        // }
        cbLabel.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> ov, String t, String t1) {
                updateFileName(wiDoc);
            }

        });

    }

    private String getPathAndFile() {
        return props.gatewayExportPath + fileName;
    }

    /*
     * Generates the word file and opens Word directly if requested 
     */
    private void generateFile(Boolean asTask) {
        log("issueOrDocID     => " + issueOrDocID, 2);
        log("issueOrDocIDList => " + issueOrDocIDList, 2);
        File path = new File(props.gatewayExportPath);
        if (!path.exists()) {
            //     path.mkdir();
            MessageBox.show(CustomGateway.stage,
                    "The directory " + props.gatewayExportPath + " does not exist.\n\nPlease create it first or choose another one by clicking \n"
                    + "at the directory name itself in the main application window.",
                    "Gateway Error 3",
                    MessageBox.ICON_ERROR | MessageBox.OK);

        } else {
            String filePathAndName = props.gatewayExportPath + fileName;
            Boolean canContinue = true;
            // define file
            File f = new File(filePathAndName);
            // try to delete an existing file
            if (f.exists()) {
                // prints
                log("INFO: File '" + filePathAndName + "' deleted: " + f.delete(), 1);

                if (f.exists()) {
                    MessageBox.show(CustomGateway.stage,
                            "Can not write the output file, please close it before continuing!",
                            "Gateway Error",
                            MessageBox.ICON_ERROR | MessageBox.OK);
                    // System.exit(4);
                    canContinue = false;
                }
            }

            if (canContinue) {
                String newDocIdList = "";
                // String testObjectivesChildsId = "";

                if (gatewayConfig.contentEquals(gatewayConfigTestObjectiveProtocol)) {
                    newDocIdList = getNewDocIdList(imSession, issueOrDocID);
                }

                try {
                    String newIssueOrDocIDList = "";
                    for (String issueID : issueOrDocIDList.split(" ")) {
                        WorkItem wi = imSession.getItemDetails2(issueID, "Type,Authorizes Changes To", null).next();
                        String type = wi.getField("Type").getValueAsString();
                        if (type.equals("Change Order")) {
                            log("Special Handling for Change Order activated!", 2);
                            newIssueOrDocIDList = newIssueOrDocIDList + " " + issueID + " " + wi.getField("Authorizes Changes To").getValueAsString().replace(",", " ");
                            log("issueOrDocID => " + issueOrDocID, 2);
                        }
                    }
                    if (!newIssueOrDocIDList.isEmpty()) {
                        issueOrDocIDList = newIssueOrDocIDList;
                    }

                } catch (APIException ex) {
                    Logger.getLogger(CustomGatewayController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }

                log("gatewayConfig: " + gatewayConfig, 1);

                GenerateOutputFile myTask = new GenerateOutputFile(
                        imSession,
                        gatewayConfig,
                        (newDocIdList.isEmpty() ? (issueOrDocIDList.isEmpty() ? issueOrDocID : issueOrDocIDList.trim()) : newDocIdList),
                        props.gatewayExportPath,
                        fileName,
                        openInWord.isSelected(),
                        (showParentage != null ? showParentage.isSelected() : false),
                        cbGeneratePDF.isSelected(),
                        (cbSubstituteParameters != null ? cbSubstituteParameters.isSelected() : false),
                        cbKeepOpen.isSelected(),
                        cbLabel.getValue().toString(),
                        props, currentDocFilter);
                if (asTask) {
                    progressBar.progressProperty().unbind();
                    progressBar.setProgress(0);
                    progressBar.progressProperty().bind(myTask.progressProperty());

                    Thread myTaskThread = new Thread(myTask);
                    myTaskThread.start();
                } else {
                    myTask.generate();
                }
            }
        }
    }

    public void getElementViaRelField(ItemListImpl ili, String relField, List<String> elements) {
        Iterator<ItemImpl> iiter = ili.getItems();
        while (iiter.hasNext()) {
            ItemImpl ii = iiter.next();
            ii.getField(relField);
        }
    }

    /*
     * Logging to the defined log file
     */
    public static void log(String text, int level) {
        imSession.log(text, level);
    }

//    private static File getNextFile(String datei) {
//        File dir = new File(datei); //erzeugt neues File-Objekt f�r das Verzeichnis.
//        String[] files = dir.list(); //liest alle Dateinamen des V. in ein Array.
//        Set<String> fileSet = new HashSet<>(); //Erzeugt neues HashSet, mit Strings als Elementen
//        fileSet.addAll(Arrays.asList(files));
//        int i;
//        for (i = 0; i < 99; i++) {
//            if (!fileSet.contains("DATEN" + i / 10 + "" + i % 10 + ".TXT")) //Wenn die Menge den Dateinamen noch nicht enth�lt
//            {
//                return new File(datei + "DATEN" + i / 10 + "" + i % 10 + ".TXT"); //neues File-Objekt mit diesem Dateinamen zur�ckgeben
//            }
//        }
//        return null; //Alle Dateiennamen(00-99) schon vorhanden? Pech gehabt...null zur�ckgeben
//    }
}
