package io.mosip.ivv.parser.Utils;

import main.java.io.mosip.ivv.core.policies.AssertionPolicy;
import main.java.io.mosip.ivv.core.policies.ErrorPolicy;
import main.java.io.mosip.ivv.core.structures.Scenario;
import main.java.io.mosip.ivv.core.utils.Utils;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class StepParser {

    public static Scenario.Step parse(String cell){
        String[] assertKeys = new String[]{"Assert", "assert", "ASSERT"};
        String[] errorKeys = new String[]{"Error", "error", "ERROR"};
        ArrayList<Scenario.Step.Assert> asserts = new ArrayList<>();
        ArrayList<Scenario.Step.Error> errors = new ArrayList<>();
        ArrayList<String> parameters = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();
        Scenario.Step step = new Scenario.Step();
        Pattern pattern = Pattern.compile("\\.");
        String[] str_split = pattern.split(cell);
        for( int i = 0; i < str_split.length; i++) {
            String func = str_split[i];
            if(i==0){
                String name_variant = Utils.regex("(\\w*)\\(", func);
                String[] nv_split = name_variant.split("\\_");
                step.setName(nv_split[0]);
                if(nv_split.length>1){
                    step.setVariant(nv_split[1]);
                }else{
                    step.setVariant("DEFAULT");
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
                        as.parameters.add(AssertionPolicy.valueOf(param_array[z]));
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
                        er.parameters.add(ErrorPolicy.valueOf(param_array[z]));
                    }
                    errors.add(er);
                }
            }
        }
        step.setParameters(parameters);
        step.setAsserts(asserts);
        step.setErrors(errors);
        step.setIndex(indexes);
        return step;
    }
}
