/*
 * Copyright:      Copyright 2015 (c) Parametric Technology GmbH
 * Product:        PTC Integrity Lifecycle Manager
 * Author:         V. Eckardt, Senior Consultant ALM
 * Purpose:        Custom Developed Code
 * **************  File Version Details  **************
 * Revision:       $Revision: 1.8 $
 * Last changed:   $Date: 2016/03/17 20:24:15CET $
 */
package customgateway;

import com.ptc.services.common.api.IntegrityAPI;
import com.ptc.services.common.api.IntegrityMessages;
import com.ptc.services.common.tools.FileUtils;
import static com.ptc.services.common.tools.FileUtils.updateContentAndGeneratePDF;
import com.ptc.services.common.tools.OSCommandHandler;
import java.io.File;
import javafx.concurrent.Task;
import jfx.messagebox.MessageBox;

/**
 *
 * @author veckardt
 */
public class GenerateOutputFile extends Task<Void> {

    private final IntegrityAPI imSession;
    private final String gatewayConfig;
    private final String docID;
    // private final String filePath;
    private final String filePathAndName;
    // private final String fileName;
    private Boolean openInWord = false;
    private final Boolean showParentage;
    private final Boolean cbSubstituteParameters;
    private Boolean generatePDF = false;
    private Boolean leaveOpen = false;
    private final CustomGatewayProperties cgp;
    private final String docFilter;
    private final String label;

    IntegrityMessages MC = new IntegrityMessages(CustomGateway.class);

    private static final String[] resultFilter = {""};

    public GenerateOutputFile(IntegrityAPI imSession, String gatewayConfig, String docID, String filePath,
            String fileName,
            Boolean openInWord,
            Boolean showParentage,
            Boolean generatePDF,
            Boolean cbSubstituteParameters,
            Boolean leaveOpen,
            String label,
            CustomGatewayProperties cgp, String docFilter) {
        this.imSession = imSession;
        this.gatewayConfig = gatewayConfig;
        this.docID = docID;
        // this.filePath = filePath;
        this.openInWord = openInWord;
        this.showParentage = showParentage;
        this.cgp = cgp;
        this.docFilter = docFilter;
        this.generatePDF = generatePDF;
        // this.fileName = fileName;
        this.filePathAndName = filePath + fileName;
        this.cbSubstituteParameters = cbSubstituteParameters;
        this.leaveOpen = leaveOpen;
        this.label = label;
    }

    public void generate() {
        Boolean canContinue = true;

//        if (generatePDF) {
//            System.getenv().put("GENERATEPDF", "true");
//        }
        updateProgress(1, 20);

//        String[] cmdArray = new String[]{"export",
//            "--config=" + gatewayConfig + "",
//            "--silent",
//            imSession.getLoginData().split(" ")[0],
//            imSession.getLoginData().split(" ")[1],
//            imSession.getLoginData().split(" ")[2],
//            "--file=" + filePathAndName + "",
//            docID
//        };
        // com.mks.gateway.App.main(new String[]{"import"});
        String command = "Gateway export " + imSession.getLoginData();
        command = command + " --config=\"" + gatewayConfig + "\" --silent " + (showParentage ? " --showParentage " : "");
        if (docFilter != null && !docFilter.isEmpty()) {
            command = command + " --filterQueryDefinition=\"" + docFilter + "\" ";
        }
        if (!label.contentEquals(CustomGatewayController.currentTag)) {
            command = command + " --asOf=\"label:" + label + "\" ";
        }
        command = command + " --file=\"" + filePathAndName + "\" " + docID;
        log(command, 1);
        // define file
        File f = new File(filePathAndName);

        if (canContinue) {
            // begin gateway export
            updateProgress(-1, 0);

//            if (cbSubstituteParameters) {
//                // im setprefs --command=viewsegment --save substituteParams=true 
//                Command cmd = new Command(Command.IM, "setprefs");
//                cmd.addOption(new Option("command", "viewsegment"));
//                cmd.addOption(new Option("save"));
//                cmd.addSelection("substituteParams=true");
//                try {
//                    imSession.executeCmd(cmd);
//                } catch (APIException ex) {
//                    Logger.getLogger(GenerateOutputFile.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                // im setprefs --command=viewsegment --save substituteParams=true 
//                cmd = new Command(Command.IM, "setprefs");
//                cmd.addOption(new Option("command", "issues"));
//                cmd.addOption(new Option("save"));
//                cmd.addSelection("substituteParams=true");
//                try {
//                    imSession.executeCmd(cmd);
//                } catch (APIException ex) {
//                    Logger.getLogger(GenerateOutputFile.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
            OSCommandHandler osh = new OSCommandHandler(resultFilter);
            // if (cbSubstituteParameters) {
            // im setprefs --command=viewsegment --save substituteParams=false 
//            Command cmd = new Command(Command.IM, "setprefs");
//            cmd.addOption(new Option("command", "viewsegment"));
//            cmd.addOption(new Option("save"));
//            cmd.addSelection("substituteParams=false");
//            try {
//                imSession.executeCmd(cmd);
//            } catch (APIException ex) {
//                Logger.getLogger(GenerateOutputFile.class.getName()).log(Level.SEVERE, null, ex);
//            }
            // }
            // com.mks.gateway.App.main(cmdArray);
            // int retCode = 0;

            int retCode = osh.executeCmd(command, true);
            log("After gateway command, result is: " + retCode, 2);
            // updateProgress(18, 20);
            // leaveOpen = (cbKeepOpen != null ? cbKeepOpen.isSelected() : false);

            if (retCode == 0 && f.exists()) {

                // openInWord = (CustomGatewayController.openInWord != null ? CustomGatewayController.openInWord.isSelected() : false);
                // generatePDF = (CustomGatewayController.cbGeneratePDF != null ? CustomGatewayController.cbGeneratePDF.isSelected() : false);
                if (generatePDF) {
                    updateContentAndGeneratePDF(true, filePathAndName);
                    log("After powershell command, result is: " + retCode, 2);
                    if (!openInWord) {
                        FileUtils.openWindowsFile("PDF", filePathAndName.replace(".docx", ".pdf").replace(".xlsx", ".pdf"));
                    }
                } else {
                    log("Generate PDF not selected.", 2);
                }
                // updateProgress(19, 20);

                // openInWord = true;
                if (openInWord) {
                    // Content Section already updated? Only if not do it here
                    if (!generatePDF) {
                        // if (true) {
                        updateContentAndGeneratePDF(false, filePathAndName);
                    }
                    updateProgress(20, 20);
                    FileUtils.openWindowsFile("Microsoft Office", filePathAndName);
                } else {
                    updateProgress(20, 20);
                    log("Open in Microsoft Office not selected.", 2);
                }
                CustomGatewayController.timeline.stop();

                if (!leaveOpen) {
                    System.exit(0);
                }
            } else {
                updateProgress(0, 20);
                MessageBox.show(CustomGateway.stage,
                        MC.getMessage("GATEWAY_ERROR").replace("&1", String.valueOf(retCode)),
                        "Gateway Error",
                        MessageBox.ICON_ERROR | MessageBox.OK);
                if (!leaveOpen) {
                    System.exit(3);
                }
            }
        }
    }

    @Override
    protected Void call() {
        generate();
        return null;
    }

    private void log(String text, int level) {
        CustomGatewayController.log(text, level);
    }

//    public void updateContentAndGeneratePDF(Boolean genPDF, String filePathAndPath) {
//
//        if (filePathAndPath.endsWith(".docx")) {
//
//            try {
//
//                File pdfFile = new File(filePathAndPath.replace(".docx", ".pdf"));
//                if (genPDF) {
//                    if (pdfFile.exists()) {
//                        pdfFile.delete();
//                    }
//                }
//
//                String[] cmds = new String[5];
//                cmds[0] = "powershell.exe";
//                cmds[1] = "-executionpolicy";
//                cmds[2] = "ByPass";
//                cmds[3] = "-Command";
//                cmds[4] = "$filename = '" + filePathAndPath + "';"
//                        + "$word_app = New-Object -ComObject Word.Application; "
//                        + "$document = $word_app.Documents.Open($filename); "
//                        + "if ($document.TablesOfContents -ne $null) {$document.TablesOfContents.item(1).Update();};";
//                if (genPDF) {
//                    cmds[4] = cmds[4]
//                            + "$pdf_filename = '" + filePathAndPath.replace(".docx", ".pdf") + "';"
//                            + "$document.SaveAs([ref] $pdf_filename, [ref] 17);";
//
//                }
//                cmds[4] = cmds[4] + "$document.Close();";
//                cmds[4] = cmds[4] + "$word_app.quit();";
//                Runtime runtime = Runtime.getRuntime();
//                Process proc = runtime.exec(cmds);
//                proc.getOutputStream().close();
//                proc.waitFor();
//
//                int retCode = proc.exitValue();
//                InputStream inputstream = proc.getErrorStream();
//                InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
//                BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
//                String line;
//                while ((line = bufferedreader.readLine()) != null) {
//                    // System.out.println(line);
//                    log(line, 2);
//                }
//                if (genPDF) {
//                    if (pdfFile.exists()) {
//                        log("New PDF File '" + pdfFile.getAbsolutePath() + " generated.", 1);
//                    } else {
//                        log("Unable to generate PDF File '" + pdfFile.getAbsolutePath(), 1);
//                    }
//                }
//
//            } catch (InterruptedException ex) {
//                log("ERROR: " + ex.getMessage(), 2);
//            } catch (IOException ex) {
//                log("ERROR: " + ex.getMessage(), 2);
//            }
//        } else {
//            log("INFO: PDF Generation is only supported for Word Files.", 2);
//        }
//    }
}
