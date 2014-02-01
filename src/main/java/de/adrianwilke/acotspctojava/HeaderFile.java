package de.adrianwilke.acotspctojava;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HeaderFile {

    class Tupel {
	String v, c;

	public Tupel(String x, String y) {
	    this.v = x;
	    this.c = y;
	}
    }

    private File sourceFile;
    private Map<String, Tupel> constants = new HashMap<String, Tupel>();
    private List<String> variables = new LinkedList<String>();
    private List<String> methods = new LinkedList<String>();

    public HeaderFile(File sourceFile) {
	this.sourceFile = sourceFile;
    }

    public HeaderFile() {
    }

    public String getId() {
	Map<String, String> map = new HashMap<String, String>();
	map.put("timer.h", "Timer");
	map.put("parse.h", "Parse");
	map.put("utilities.h", "Utilities");
	map.put("InOut.h", "InOut");
	map.put("ants.h", "Ants");
	map.put("TSP.h", "Tsp");
	map.put("ls.h", "LocalSearch");
	if (map.containsKey(getFileName())) {
	    return map.get(getFileName());
	} else {
	    return getFileName();
	}
    }

    private String removeSpecialChars(String string) {
	return string.replaceAll("[^0-9a-zA-Z_]", "");
    }

    public void parse() throws IOException {
	List<String> lines = Files.getUTF8TextFileContent(sourceFile.getAbsolutePath());
	for (String line : lines) {

	    // Constants

	    if (line.startsWith("#define") && !line.contains("(")) {
		String[] strings = line.split("\\s{1,}");
		String name = strings[1];
		String value = strings[2];
		String comment = "";
		if (strings.length > 3) {
		    int index1 = line.indexOf(strings[1]) + strings[1].length() + 1;
		    int index2 = line.indexOf(strings[2]) + strings[2].length() + 1;

		    if (line.contains("/*")) {
			comment = line.substring(index2).trim();
		    } else {
			value = line.substring(index1).trim();
		    }
		}
		constants.put(removeSpecialChars(name), new Tupel(value, comment));
		continue;
	    }

	    // Variables

	    for (String varPref : getVariablePrefixes()) {
		if (line.startsWith(varPref) && line.contains(";") && !line.contains("(")) {
		    String noPref = line.substring(varPref.length());
		    String name = noPref.substring(0, noPref.indexOf(";"));
		    if (name.contains("[")) {
			name = name.substring(0, name.indexOf("[") + 1);
		    }
		    variables.add(removeSpecialChars(name));
		    continue;
		}
	    }
	    Collections.sort(variables, new Comparator<String>() {
		public int compare(String o1, String o2) {
		    return o2.length() - o1.length();
		}
	    });

	    // Methods

	    for (String mPref : getMethodPrefixes()) {
		if (line.startsWith(mPref) && line.contains(";") && line.contains("(")) {
		    String noPref = line.substring(mPref.length());
		    String name = noPref.substring(0, noPref.indexOf("("));
		    name = removeSpecialChars(name);
		    if (!getAcoTspFunctions().contains(name)) {
			methods.add(name);
		    }
		    continue;
		}
	    }
	    Collections.sort(methods, new Comparator<String>() {
		public int compare(String o1, String o2) {
		    return o2.length() - o1.length();
		}
	    });
	}
    }

    public String getFileName() {
	return sourceFile.getName();
    }

    public Map<String, Tupel> getConstants() {
	return constants;
    }

    public List<String> getVariables() {
	return variables;
    }

    public void addVariable(String variable) {
	variables.add(variable);
    }

    public List<String> getMethods() {
	return methods;
    }

    public void setMethods(List<String> methods) {
	this.methods = methods;
    }

    public List<String> getVariablePrefixes() {
	List<String> prefixes = new LinkedList<String>();
	prefixes.add("extern long int ");
	prefixes.add("extern double ");
	prefixes.add("extern char ");
	prefixes.add("extern FILE ");
	prefixes.add("extern int ");
	prefixes.add("long int ");
	prefixes.add("double ");
	return prefixes;
    }

    private List<String> getMethodPrefixes() {
	List<String> prefixes = new LinkedList<String>();
	prefixes.add("extern int ");
	prefixes.add("long int ");
	prefixes.add("double ");
	prefixes.add("void ");
	prefixes.add("int ");
	return prefixes;
    }

    public List<String> getAcoTspFunctions() {
	return Arrays.asList("acs_global_update", "as_update", "bwas_update", "construct_solutions", "eas_update",
		"init_try", "local_search", "mmas_update", "pheromone_trail_update", "ras_update",
		"search_control_and_statistics", "termination_condition", "update_statistics");
    }

}
