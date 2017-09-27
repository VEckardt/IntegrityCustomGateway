/*
 * Copyright:      Copyright 2017 (c) Parametric Technology GmbH
 * Product:        PTC Integrity Lifecycle Manager
 * Author:         Volker Eckardt, Principal Consultant ALM
 * Purpose:        Custom Developed Code
 * **************  File Version Details  **************
 * Revision:       $Revision$
 * Last changed:   $Date$
 */
package customgateway;

import com.ptc.services.common.tools.EnvUtil;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.System.out;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author veckardt
 */
public class ReadXMLConfig {

    String configList = "";
    String filename = "Custom-Gateway-Configurations.xml";

    
//<?xml version="1.0" encoding="UTF-8"?>
//<configurations version="1.3">
//   <!-- configs are used for the Custom Gateway Export process -->
//   <!-- see IntegrityCustomGateway -->
//      <typedef type="Requirement Document">
//         <configs>
//            <config>12-Dynamic-Document-Landscape</config>
//            <config>20-Sample MS Word Exporter -- with TOC page</config>
//            <config>17-Admin Export</config>
//            <config>16-Trace Document</config>
//            <config>21-Admin Setup</config>
//            <config>01-Compliance Matrix (Excel)</config>
//            <config>01-Compliance Matrix Overview (Excel)</config>
//         </configs>
//      </typedef>
//</configurations>    
    
//    public static void main(String[] args) {
//        try {
//            new ReadXMLConfig().readConfigXML("Requirement Document");
//            out.println("configList: '" + getConfigList() + "'");
//        } catch (Exception e) {
//            out.println("Exception:");
//            e.printStackTrace();
//        }
//    }
    public ReadXMLConfig(String typeName) throws MalformedURLException, FileNotFoundException, IOException, Exception {
        URL url = new URL("http://" + EnvUtil.getHostName() + ":" + EnvUtil.getPort() + "/" + filename);
        URLConnection connection = url.openConnection();

        Document doc = parseXML(connection.getInputStream());
        NodeList descNodes = doc.getElementsByTagName("typedef");

        // System.out.println(descNodes.getLength());
        for (int i = 0; i < descNodes.getLength(); i++) {
            Element eElement = (org.w3c.dom.Element) descNodes.item(i);
            if (descNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                if (typeName.contentEquals(eElement.getAttribute("type"))) {
                    out.println("Type: " + eElement.getAttribute("type"));
                    for (int j = 0; j < eElement.getElementsByTagName("config").getLength(); j++) {
                        String config = eElement.getElementsByTagName("config").item(j).getTextContent();
                        out.println("  Config: " + config);
                        configList += configList.isEmpty() ? config : "," + config;
                    }
                }
            }

            // System.out.println(child.getNodeName() + " - " + child.getTextContent());
        }
    }

    private Document parseXML(InputStream stream)
            throws Exception {
        DocumentBuilderFactory objDocumentBuilderFactory = null;
        DocumentBuilder objDocumentBuilder = null;
        Document doc = null;
        try {
            objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
            objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();

            doc = objDocumentBuilder.parse(stream);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw ex;
        }

        return doc;
    }

    public String getConfigList() {
        return configList;
    }
}
