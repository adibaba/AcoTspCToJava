package de.adrianwilke.acotspctojava;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CodeFile {

    private File sourceFile;

    private final String LS = System.getProperty("line.separator");
    private final boolean ADD_COMMENT_TO_CHANGES = false;

    public Map<String, String> constantsMap = new HashMap<String, String>();
    public Map<String, String> variablesMap = new HashMap<String, String>();
    public Map<String, String> methodsMap = new HashMap<String, String>();

    public CodeFile(File sourceFile) {
	this.sourceFile = sourceFile;
    }

    public String getFileName() {
	return sourceFile.getName();
    }

    public String getId() {
	Map<String, String> map = new HashMap<String, String>();
	map.put("unix_timer.c", "Timer");
	map.put("parse.c", "Parse");
	map.put("utilities.c", "Utilities");
	map.put("InOut.c", "InOut");
	map.put("dos_timer.c", "DosTimer");
	map.put("ants.c", "Ants");
	map.put("TSP.c", "Tsp");
	map.put("acotsp.c", "AcoTsp");
	map.put("ls.c", "LocalSearch");
	if (map.containsKey(getFileName())) {
	    return map.get(getFileName());
	} else {
	    return getFileName();
	}
    }

    public String export(File targetDirectory) throws IOException {
	if (!targetDirectory.isDirectory())
	    throw new IOException("Not a directory: " + targetDirectory);
	if (!targetDirectory.canWrite())
	    throw new IOException("Not writable: " + targetDirectory);
	File targetFile = new File(targetDirectory.getPath() + File.separator + getId() + ".java");

	List<String> lines = Files.getUTF8TextFileContent(sourceFile.getAbsolutePath());

	lines.set(0, getHeader() + lines.get(0));

	for (int l = 0; l < lines.size(); l++) {
	    String line = lines.get(l);
	    lines.set(l, parseLine(line));
	}

	lines.add("}");

	Files.writeUTF8TextFileContent(targetFile.getAbsolutePath(), lines);
	return targetFile.getAbsolutePath();
    }

    private String getHeader() {
	String header = "package de.adrianwilke.acotspjava;" + LS;
	header += "public class " + getId() + " {" + LS;

	// Class specific

	if (getId().equals("Ants")) {
	    header += "class ant_struct {" + LS;
	    header += " int [] tour;" + LS;
	    header += " boolean [] visited;" + LS;
	    header += " int tour_length;" + LS;
	    header += "}" + LS;
	} else if (getId().equals("Tsp")) {
	    header += "	class point {" + LS;
	    header += "	    double x;" + LS;
	    header += "	    double y;" + LS;
	    header += "	  }" + LS;
	    header += "" + LS;
	    header += "	  class problem {" + LS;
	    header += "	    String   name;      	     /* instance name */" + LS;
	    header += "	    String   edge_weight_type; /* selfexplanatory */" + LS;
	    header += "	    int      optimum;          /* optimal tour length if known, otherwise a bound */" + LS;
	    header += "	    int      number_of_cities; /* number of cities */" + LS;
	    header += "	    int      n_near;           /* number of nearest neighbors */" + LS;
	    header += "	    point [] nodeptr;          /* array of structs containing coordinates of nodes */" + LS;
	    header += "	    int [][] distance;	     /* distance matrix: distance[i][j] gives distance */" + LS;
	    header += "	    int [][]     nn_list;      /* nearest neighbor list; contains for each node i a sorted list of n_near nearest neighbors */"
		    + LS;
	    header += "	  }" + LS;
	}

	return header;
    }

    private String parseLine(String line) {

	String originalLine = line;
	String lineTrim = line.trim();
	String lineComment = " // XXX";

	// C specific

	if (lineTrim.startsWith("#include")) {
	    line = "";
	} else if (lineTrim.startsWith("free(")) {
	    line = "";
	} else if (lineTrim.startsWith("TRACE")) {
	    line = "// " + line;
	} else if (lineTrim.startsWith("DEBUG")) {
	    line = "// " + line;
	} else if (lineTrim.startsWith("#ifndef")) {
	    line = "// " + line;
	} else if (lineTrim.startsWith("#define")) {
	    line = "// " + line;
	} else if (lineTrim.startsWith("# define")) {
	    line = "// " + line;
	} else if (line.startsWith("void ") && line.contains("(")) {
	    line = "static " + line;
	}

	// C specific

	line = line.replaceAll("->", ".");

	// Types

	line = line.replaceAll("unsigned int", "int");
	line = line.replaceAll("long int \\*", "int []");
	line = line.replaceAll("long int", "int ");
	line = line.replaceAll("static double   \\*", "double []");
	line = line.replaceAll("\\( void \\)", "()");
	line = line.replaceAll("\\(void\\)", "()");

	// Functions

	line = line.replaceAll("fprintf\\(", "System.out.println(");
	line = line.replaceAll("printf\\(", "System.out.println(");
	// line = line.replaceAll("TRUE", "true");
	// line = line.replaceAll("FALSE", "false");

	// Code specific

	// line = line.replaceAll("ant\\[", "Ants.ant[");
	// line = line.replaceAll("\\&Ants.ant\\[", "Ants.ant[");
	// line = line.replaceAll("ant\\_struct \\*", "ant_struct ");

	// Methods

	for (String key : methodsMap.keySet()) {
	    String id = methodsMap.get(key);
	    if (!id.equals(getId())) {
		line = line.replaceAll(Pattern.quote(key + "\\("), id + "." + key + "(");
	    }
	}

	// Constants

	for (String key : constantsMap.keySet()) {
	    String id = constantsMap.get(key);
	    if (!id.equals(getId())) {
		line = line.replaceAll(Pattern.quote(key), id + "." + key);
	    }
	}

	// Variables

	for (String varPref : getVariablePrefixes()) {
	    if (line.startsWith(varPref)) {
		line = "static " + line;
		lineComment += " varPref->static";
	    }
	}

	for (String key : variablesMap.keySet()) {
	    String id = variablesMap.get(key);
	    if (!id.equals(getId())) {
		line = line.replaceAll(Pattern.quote(key), id + "." + key);
	    }
	}

	if (ADD_COMMENT_TO_CHANGES && !line.equals(originalLine)) {
	    line = line + lineComment;
	}

	// File specific

	if (getId().equals("Ants")) {
	    if (line.equals("ant_struct *ant_colony;")) {
		line = "static ant_struct ant_colony[];";
	    } else if (line.equals("ant_struct *best_so_far_ant;")) {
		line = "static ant_struct best_so_far_ant;";
	    } else if (line.equals("ant_struct *restart_best_ant;")) {
		line = "static ant_struct restart_best_ant;";
	    } else if (line.equals("static double   **pheromone;")) {
		line = "static double   pheromone[][];";
	    } else if (line.equals("static double   **total;")) {
		line = "static double   total[][];";
	    } else if (line.equals("static double   *prob_of_selection;")) {
		line = "static double   prob_of_selection[];";
	    }
	} else if (getId().equals("Tsp")) {
	    if (line.equals("struct problem instance;")) {
		line = "static problem instance;";
	    }
	}

	return line;
    }

    private List<String> getVariablePrefixes() {
	List<String> prefixes = new LinkedList<String>();
	prefixes.add("int ");
	prefixes.add("double ");
	return prefixes;
    }

}
