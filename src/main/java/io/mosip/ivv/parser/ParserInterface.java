package io.mosip.ivv.parser;

import main.java.io.mosip.ivv.core.structures.Persona;
import main.java.io.mosip.ivv.core.structures.ProofDocument;
import main.java.io.mosip.ivv.core.structures.Scenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface ParserInterface {

    ArrayList<Persona> getPersonas();

    ArrayList<Scenario> getScenarios();

    ArrayList<ProofDocument> getDocuments();

    HashMap<String, String> getGlobals();

}
