package io.mosip.ivv.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.io.mosip.ivv.core.policies.AssertionPolicy;
import main.java.io.mosip.ivv.core.policies.ErrorPolicy;
import main.java.io.mosip.ivv.core.structures.*;
import main.java.io.mosip.ivv.core.utils.Utils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;

public class Parser implements ParserInterface {

    private String PERSONA_SHEET = "";
    private String SCENARIO_SHEET = "";
    private String MACROS_SHEET = "";
    private String DOCUMENTS_SHEET = "";
    private String DOCUMENT_DATA_PATH = "";
    Properties properties = null;

    public Parser(String USER_DIR, String CONFIG_FILE){
        properties = Utils.getProperties(CONFIG_FILE);
        this.PERSONA_SHEET = USER_DIR+properties.getProperty("PERSONA_SHEET");
        this.SCENARIO_SHEET = USER_DIR+properties.getProperty("SCENARIO_SHEET");
        this.MACROS_SHEET = USER_DIR+properties.getProperty("MACROS_SHEET");
        this.DOCUMENTS_SHEET = USER_DIR+properties.getProperty("DOCUMENTS_SHEET");
        this.DOCUMENT_DATA_PATH = USER_DIR+properties.getProperty("DOCUMENT_DATA_PATH");
    }

    private void checkConfig(){
//        File f = new File(pdoc.path);
//        if(f.exists() && !f.isDirectory()) {
//            documents.add(pdoc);
//        }
    }

    public String getString(ArrayList<Scenario> scenarios){
        String listString = "";
        for (Scenario s : scenarios)
        {
            listString += "Scenario "+s.name+ "\n";
            listString += ToStringBuilder.reflectionToString(s) + "\n";
            listString += "Steps: "+ "\n";
            for (Scenario.Step st : s.steps) {
                listString += ToStringBuilder.reflectionToString(st) + "\t";
            }
            listString += "\n";
            listString += "Data: "+ "\n";
            listString += ToStringBuilder.reflectionToString(s.data) + "\n";
            listString += "Persona: "+ "\n";
            for (Persona p : s.data.persons) {
                listString += ToStringBuilder.reflectionToString(p) + "\n";
            }
        }
        return listString;
    }

    public ArrayList<Persona> getPersonas(){
        ArrayList data = fetchData();
        ArrayList<Persona> persona_list = new ArrayList();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            System.out.println("Parsing Persona: "+data_map.get("persona_class"));
            Persona main = new Persona();

            Persona.Person iam = new Persona.Person();
            /* persona definition */
            iam.personaDef.gender = PersonaDef.GENDER.valueOf(data_map.get("gender"));
            iam.personaDef.name = data_map.get("persona_class");
            iam.personaDef.residence_status = PersonaDef.RESIDENCE_STATUS.valueOf(data_map.get("residence_status"));
            iam.personaDef.age_group = PersonaDef.AGE_GROUP.valueOf("ADULT");
            iam.personaDef.role = PersonaDef.ROLE.valueOf("APPLICANT");
            /* persona */
            iam.name = data_map.get("name");
            iam.preffered_lang = data_map.get("preffered_lang");
            iam.default_lang = data_map.get("default_lang");
            iam.address_line_1 = data_map.get("address_line_1");
            iam.address_line_2 = data_map.get("address_line_2");
            iam.address_line_3 = data_map.get("address_line_3");
            iam.region = data_map.get("region");
            iam.province = data_map.get("province");
            iam.city = data_map.get("city");
            iam.postal_code = data_map.get("postal_code");
            iam.email = data_map.get("email");
            iam.phone = data_map.get("mobile");
            iam.date_of_birth = data_map.get("dob");
            iam.cnie_number = data_map.get("cnie");
            iam.registration_center_id = data_map.get("registration_center_id");

            /* attaching documents */
            if(data_map.get("group_name") == null || data_map.get("group_name").isEmpty()){
                main.group_name = data_map.get("group_name");
                main.persona_class = data_map.get("persona_class");
                main.addPerson(iam);
                persona_list.add(main);
            }else{
                Boolean group_exist = false;
                for(int i=0; i<persona_list.size();i++) {
                    if(persona_list.get(i).group_name.equals(data_map.get("group_name"))){
                        group_exist = true;
                        persona_list.get(i).persons.add(iam);
                    }
                }
                if(!group_exist){
                    main.group_name = data_map.get("group_name");
                    main.persona_class = data_map.get("persona_class");
                    main.addPerson(iam);
                    persona_list.add(main);
                }
            }
        }
        return persona_list;
    }

    public ArrayList<Scenario> getScenarios(){
        ArrayList data = fetchScenarios();
        ArrayList<Scenario> scenario_array = new ArrayList();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            System.out.println("Parsing Scenario: "+data_map.get("tc_no"));
            Scenario scenario = new Scenario();
            scenario.name = data_map.get("tc_no");
            scenario.description = data_map.get("description");
            scenario.persona_class = data_map.get("persona_class");
            scenario.persona = data_map.get("persona");
            scenario.group_name = data_map.get("group_name");
            scenario.flags = parseTags(data_map.get("tags"));
            scenario.steps = formatSteps(data_map);
            scenario_array.add(scenario);
        }
        System.out.println("total scenarios parsed: "+scenario_array.size());
        return scenario_array;
    }

    public ArrayList<ProofDocument> getDocuments(){
        ArrayList data = fetchDocuments();
        ArrayList<ProofDocument> documents = new ArrayList<>();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            ProofDocument pdoc = new ProofDocument();
            pdoc.doc_cat_code = ProofDocument.DOCUMENT_CATEGORY.valueOf(data_map.get("doc_cat_code"));
            pdoc.doc_type_code = data_map.get("doc_typ_code");
            pdoc.doc_file_format = data_map.get("doc_file_format");
            pdoc.tags = parseTags(data_map.get("tags"));
            pdoc.name = data_map.get("file");
            pdoc.path = DOCUMENT_DATA_PATH+data_map.get("file");
            File f = new File(pdoc.path);
            if(f.exists() && !f.isDirectory()) {
                documents.add(pdoc);
            }
        }
        System.out.println("total documents parsed: "+documents.size());
        return documents;
    }

    public HashMap<String, String> getGlobals(){
        ArrayList data = fetchMacros();
        HashMap<String, String> macros_map = new HashMap<>();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            macros_map.put(data_map.get("key"), data_map.get("value"));
        }
        System.out.println("total macros parsed: "+macros_map.size());
        return macros_map;
    }

    private ArrayList formatScenarios(ArrayList data){
        System.out.println("total scenarios found: "+data.size());
        ArrayList<Scenario> scenario_array = new ArrayList();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            System.out.println("Parsing Scenario: "+data_map.get("tc_no"));
            Scenario scenario = new Scenario();
            scenario.name = data_map.get("tc_no");
            scenario.description = data_map.get("description");
            scenario.persona_class = data_map.get("persona_class");
            scenario.persona = data_map.get("persona");
            scenario.group_name = data_map.get("group_name");
            scenario.flags = parseTags(data_map.get("tags"));
            scenario.steps = formatSteps(data_map);
            scenario_array.add(scenario);
        }
        System.out.println("total scenarios parsed: "+scenario_array.size());
        return scenario_array;
    }

    private HashMap<String, String> formatMacros(ArrayList data){
        System.out.println("total macros found: "+data.size());
        HashMap<String, String> macros_map = new HashMap<>();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            macros_map.put(data_map.get("key"), data_map.get("value"));
        }
        System.out.println("total macros parsed: "+macros_map.size());
        return macros_map;
    }

    private HashMap<String, ArrayList<ProofDocument>> formatDocuments(ArrayList data){
        System.out.println("total documents found: "+data.size());
        HashMap<String, ArrayList<ProofDocument>> document_map = new HashMap<>();
        ObjectMapper oMapper = new ObjectMapper();
        Iterator iter = data.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            HashMap<String, String> data_map = oMapper.convertValue(obj, HashMap.class);
            ProofDocument pdoc = new ProofDocument();
            if(document_map.get(data_map.get("doc_cat_code")) == null){
                document_map.put(data_map.get("doc_cat_code"), new ArrayList<>());
            }
            pdoc.doc_cat_code = ProofDocument.DOCUMENT_CATEGORY.valueOf(data_map.get("doc_cat_code"));
            pdoc.doc_type_code = data_map.get("doc_typ_code");
            pdoc.doc_file_format = data_map.get("doc_file_format");
            pdoc.tags = parseTags(data_map.get("tags"));
            pdoc.name = data_map.get("file");
            pdoc.path = DOCUMENT_DATA_PATH+data_map.get("file");
            File f = new File(pdoc.path);
            if(f.exists() && !f.isDirectory()) {
                document_map.get(data_map.get("doc_cat_code")).add(pdoc);
            }
        }
        System.out.println("total documents parsed: "+document_map.size());
        return document_map;
    }

    private ArrayList fetchData(){
        return Utils.csvToList(PERSONA_SHEET);
    }

    private ArrayList fetchScenarios(){
        return Utils.csvToList(SCENARIO_SHEET);
    }

    private ArrayList fetchDocuments(){
        return Utils.csvToList(DOCUMENTS_SHEET);
    }

    private ArrayList fetchMacros(){
        return Utils.csvToList(MACROS_SHEET);
    }

    private ArrayList<Scenario.Step> formatSteps(HashMap<String, String> data_map){
        ArrayList<Scenario.Step> steps = new ArrayList<Scenario.Step>();
        for (HashMap.Entry<String, String> entry : data_map.entrySet())
        {
            boolean isMatching = entry.getKey().contains("field");
            if(isMatching && entry.getValue() != null && !entry.getValue().isEmpty()){
                if(entry.getValue() != null && !entry.getValue().equals("")) {
                    steps.add(parseStep(entry.getValue()));
                }
            }
        }
        return steps;
    }

    private Scenario.Step parseStep(String data){
        String[] assertKeys = new String[]{"Assert", "assert", "ASSERT"};
        String[] errorKeys = new String[]{"Error", "error", "ERROR"};
        ArrayList<Scenario.Step.Assert> asserts = new ArrayList<>();
        ArrayList<Scenario.Step.Error> errors = new ArrayList<>();
        ArrayList<String> parameters = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();
        Scenario.Step step = new Scenario.Step();
        Pattern pattern = Pattern.compile("\\.");
        String[] str_split = pattern.split(data);
        for( int i = 0; i < str_split.length; i++) {
            String func = str_split[i];
            if(i==0){
                String name_variant = Utils.regex("(\\w*)\\(", func);
                String[] nv_split = name_variant.split("\\_");
                step.name = nv_split[0];
                if(nv_split.length>1){
                    step.variant = nv_split[1];
                }else{
                    step.variant = "DEFAULT";
                }
                String[] param_array = Pattern.compile("," ).split(Utils.regex("\\((.*?)\\)", str_split[i]).replaceAll("\\s+",""));
                for(int z=0; z<param_array.length;z++){
                    parameters.add(param_array[z]);
                }
                String[] index_array = Pattern.compile("," ).split(Utils.regex("\\[(.*?)\\]", str_split[i]).replaceAll("\\s+",""));
                for(int z=0; z<index_array.length;z++){
                    try {
                        indexes.add(Integer.parseInt(index_array[z]));
                    }catch (NumberFormatException e){

                    }

                }
            }
            for( int j = 0; j < assertKeys.length; j++) {
                String assert_type = Utils.regex("(\\w*)\\(", str_split[i]);
                String[] nv_split = assert_type.split("\\_");
                String name = nv_split[0];
                if(func.contains(assertKeys[j])){
                    Scenario.Step.Assert as = new Scenario.Step.Assert();
                    if(nv_split.length>1){
                        as.type = AssertionPolicy.valueOf(nv_split[1]);
                    }else{
                        as.type = AssertionPolicy.valueOf("DEFAULT");
                    }
                    String[] param_array = Pattern.compile("," ).split(Utils.regex("\\((.*?)\\)", func).replaceAll("\\s+",""));
                    for(int z=0; z<param_array.length;z++){
                        as.parameters.add(param_array[z]);
                    }
                    asserts.add(as);
                }
            }
            for( int j = 0; j < errorKeys.length; j++) {
                String error_type = Utils.regex("(\\w*)\\(", str_split[i]);
                String[] nv_split = error_type.split("\\_");
                String name = nv_split[0];
                if(func.contains(errorKeys[j])){
                    Scenario.Step.Error er = new Scenario.Step.Error();
                    if(nv_split.length>1){
                        er.type = ErrorPolicy.valueOf(nv_split[1]);
                    }else{
                        er.type = ErrorPolicy.valueOf("DEFAULT");
                    }
                    String[] param_array = Pattern.compile("," ).split(Utils.regex("\\((.*?)\\)", func).replaceAll("\\s+",""));
                    for(int z=0; z<param_array.length;z++){
                        er.parameters.add(param_array[z]);
                    }
                    errors.add(er);
                }
            }
        }
        step.parameters = parameters;
        step.asserts = asserts;
        step.errors = errors;
        step.index = indexes;
        return step;
    }

    private ArrayList<String> parseTags(String tags){
        ArrayList<String> tag = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\\," );
        String[] split = pattern.split(tags);
        for( int i = 0; i < split.length; i++) {
            tag.add(split[i].trim());
        }
        return tag;
    }

    public void debug(){
        String str = "loginwithemail(1, 2)";
        Pattern pattern = Pattern.compile("\\.");
        String[] str_split = pattern.split(str);
        System.out.println(str_split[0]);
//        System.out.println(UtilsA.regex("(\\w*)\\(", str));
    }
}
