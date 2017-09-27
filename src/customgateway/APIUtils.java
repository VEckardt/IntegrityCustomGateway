/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package customgateway;

import com.mks.api.Command;
import com.mks.api.MultiValue;
import com.mks.api.Option;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import com.ptc.services.common.api.IntegrityAPI;
import static customgateway.CustomGatewayController.log;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jacek
 */
public class APIUtils {

    private List<String> itemsFromRelationship = new LinkedList<>();

    /**
     * getNewDocIdList - Returns the Doc ID List from Test Objectives and Childs
     * developed for KION
     *
     * @param imSession
     * @param issueOrDocID
     * @return
     */
    public static String getNewDocIdList(IntegrityAPI imSession, String issueOrDocID) {
        String newDocIdList = "";

        APIUtils apiUtils = new APIUtils();
        APIUtils apiUtils2 = new APIUtils();
        List<String> testObjectives = new LinkedList<>();
        List<String> tests = new LinkedList<>();
        List<String> typesFilter = new LinkedList<>();
        List<String> traverseFields = new LinkedList<>();
        try {
            traverseFields.add("Test Objectives");      //getting all Test Objectives
            typesFilter.add("Test Objective");
            testObjectives = apiUtils.getItemsViaRelationship(issueOrDocID, traverseFields, typesFilter, imSession);

            traverseFields.clear();
            typesFilter.clear();

            traverseFields.add("Tests");                //getting all Test Suites
            traverseFields.add("Test Objectives");
            typesFilter.add("Test Suite");
            tests = apiUtils2.getItemsViaRelationship(issueOrDocID, traverseFields, typesFilter, imSession);
        } catch (APIException ex) {
            Logger.getLogger(CustomGatewayController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        log("Test Objectives Via 'Test Objectives' Rel: " + testObjectives, 2);
        log("Test Suites Via 'Tests' Rel: " + tests, 2);

        // String testObjectiveList = "";
        // for (String to : testObjectives) {
        // 
        // }
        // remove duplicates
        Set<String> testSuitesIds = new LinkedHashSet<>(tests);
        log("DISTING Test Suites Via Rel: " + testSuitesIds, 2);

        Set<String> testObjectivesIds = new LinkedHashSet<>(testObjectives);
        log("DISTING Test Objectives Via Rel: " + testObjectivesIds, 2);

        // String objectiveList = new String();
        // for (String testObjective : testObjectivesIds) {
        //     objectiveList += testObjective + ",";
        // }
        for (String testSuite : testSuitesIds) {
            newDocIdList += testSuite + " ";
        }

        for (String testObjective : testObjectivesIds) {
            newDocIdList += testObjective + " ";
        }
        log("newDocIdList " + newDocIdList, 2);
        return newDocIdList;
    }

    /**
     * getItemsViaRelationship
     *
     * @param id
     * @param traverseFields
     * @param typesFilter
     * @param imSession
     * @return
     * @throws APIException
     */
    public List<String> getItemsViaRelationship(String id, List<String> traverseFields, List<String> typesFilter, IntegrityAPI imSession) throws APIException {
        itemsFromRelationship.clear();
        List<String> fields = new LinkedList<>();
        fields.add("ID");
        fields.add("Type");
        for (String traverseField : traverseFields) {
            if (!fields.contains(traverseField)) {
                fields.add(traverseField);
            }
        }

        Command cmd = new Command(Command.IM, "relationships");
        MultiValue mvTraverseFields = new MultiValue(",");

        for (String tF : traverseFields) {
            mvTraverseFields.add(tF);
        }
        cmd.addOption(new Option("traverseFields", mvTraverseFields));
        MultiValue mv = new MultiValue(",");
        for (String field : fields) {
            mv.add(field);
        }
        cmd.addOption(new Option("fields", mv));
        cmd.addSelection(id);

        Response result = imSession.executeCmd(cmd);
        WorkItemIterator wit = result.getWorkItems();

        while (wit.hasNext()) {
            WorkItem wi = wit.next();
            String wiId = wi.getField("ID").getValueAsString();
            String wiType = wi.getField("Type").getValueAsString();
            System.out.println("ID: " + wiId + " Type: " + wiType);

            if (typesFilter == null || typesFilter.isEmpty()) {
                itemsFromRelationship.add(wiId);
            } else if (typesFilter.contains(wiType)) {
                if (typesFilter.contains(wiType)) {
                    itemsFromRelationship.add(wiId);
                }
            }
            /*
             for (String traverseField : traverseFields) {
             System.out.println("[ApiUtils] --> traverseField: " + traverseField );
             Field field = wi.getField(traverseField);
             ItemListImpl ili = (ItemListImpl) field.getValue();
             if (ili != null) {
             digDeeper(ili, traverseFields, typesFilter);
             } else {
             System.out.println("[ApiUtils] --> ili was null");
             }
             }*/

        }

        return itemsFromRelationship;
    }

//    private void digDeeper(ItemListImpl ili, List<String> traverseFields, List<String> typesFilter) {
//        Iterator<ItemImpl> iiter = ili.getItems();
//        if (iiter == null) {
//            System.out.println("[ApiUtils] --> iiter was null");
//            return;
//        }
//        while (iiter.hasNext()) {
//            ItemImpl ii = iiter.next();
//            String id = ii.getId();
//            String type = ii.getField("Type").getValueAsString();
//            if (type == null) {
//                System.out.println("[ApiUtils] --> type was null");
//                return;
//            }
//
//            if (id == null) {
//                System.out.println("[ApiUtils] --> id was null");
//                return;
//            } else {
//                System.out.println("ID: " + id + " TYPE: " + type);
//            }
//
//            if (typesFilter == null || typesFilter.isEmpty()) {
//                itemsFromRelationship.add(id);
//            } else if (typesFilter.contains(type)) {
//                itemsFromRelationship.add(id);
//            }
//
//            for (String traverseField : traverseFields) {
//                Field relation = ii.getField(traverseField);
//                ItemListImpl rili = (ItemListImpl) relation.getValue();
//                if (rili != null) {
//                    digDeeper(rili, traverseFields, typesFilter);
//                }
//            }
//        }
//    }
}
