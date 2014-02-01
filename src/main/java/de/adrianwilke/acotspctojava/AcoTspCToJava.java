package de.adrianwilke.acotspctojava;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.adrianwilke.acotspctojava.HeaderFile.Tupel;

public class AcoTspCToJava {

    // Configuration

    public final static String SOURCE_DIRECTORY = "";
    public final static String TARGET_DIRECTORY = "";
    public final static String OPTIONS_FILE = "parse.c";

    static final boolean PRINT_FILE_NAMES = false;
    static final boolean PRINT_PARSED_DATA = false;
    static final boolean DO_EXPORT = false;
    static final boolean PRINT_EXPORTED_FILES = false;
    static final boolean PARSE_OPTIONS = true;

    // Internal

    List<HeaderFile> headerFiles = new LinkedList<HeaderFile>();
    List<CodeFile> codeFiles = new LinkedList<CodeFile>();

    // Main program

    public static void main(String[] args) throws IOException {

	AcoTspCToJava main = new AcoTspCToJava();

	// Get files

	main.getHeaderFiles(AcoTspCToJava.SOURCE_DIRECTORY);
	main.getCodeFiles(AcoTspCToJava.SOURCE_DIRECTORY);
	if (PRINT_FILE_NAMES) {
	    main.printNamesOfHeaderFiles();
	    main.printNamesOfCodeFiles();
	}

	// Parse Header files

	main.parseHeaderFiles();
	for (HeaderFile headerFile : main.headerFiles) {
	    if (PRINT_PARSED_DATA) {
		main.printConstants(headerFile);
		main.printVariables(headerFile);
		main.printMethods(headerFile);
	    }
	}

	// Global use of parsed constants, variables, and methods

	for (HeaderFile headerFile : main.headerFiles) {
	    main.injectConstants(headerFile);
	}
	for (HeaderFile headerFile : main.headerFiles) {
	    main.injectVariables(headerFile);
	}
	for (HeaderFile headerFile : main.headerFiles) {
	    main.injectMethods(headerFile);
	}

	// Export

	if (DO_EXPORT) {
	    main.exportCodeFiles(AcoTspCToJava.TARGET_DIRECTORY);
	}

	// Options

	if (PARSE_OPTIONS) {
	    OptionsFile opt = new OptionsFile(new File(SOURCE_DIRECTORY + File.separator + OPTIONS_FILE));
	    opt.parse();
	    opt.printOptions();
	}
    }

    private void exportCodeFiles(String directory) throws IOException {
	if (PRINT_EXPORTED_FILES) {
	    System.out.println();
	    System.out.println("Eported files:");
	    System.out.println();
	}
	for (CodeFile file : codeFiles) {
	    String filePath = file.export(new File(directory));
	    if (PRINT_EXPORTED_FILES) {
		System.out.println(filePath);
	    }
	}
	System.out.println();
    }

    private void getCodeFiles(String directory) {
	File[] codeFiles = Files.getFiles(directory, ".c");
	for (File file : codeFiles) {
	    this.codeFiles.add(new CodeFile(file));
	}
    }

    private List<String> getDoNotReplaceVariables() {
	List<String> vars = new LinkedList<String>();
	vars.add("n"); // has been renamed to number_of_cities
	vars.add("iteration");
	return vars;
    }

    private List<String> getDoNotReplaceConstants() {
	List<String> vars = new LinkedList<String>();
	vars.add("HDR_PARSE");
	return vars;
    }

    private void getHeaderFiles(String directory) {
	File[] headerFiles = Files.getFiles(directory, ".h");
	for (File file : headerFiles) {
	    this.headerFiles.add(new HeaderFile(file));
	}

	// No header file for AcoTsp
	HeaderFile acoTspHeaderFile = new HeaderFile();
	acoTspHeaderFile.setMethods(acoTspHeaderFile.getAcoTspFunctions());
    }

    private void injectConstants(HeaderFile headerFile) {
	String id = headerFile.getId();
	List<String> noConst = getDoNotReplaceConstants();
	for (CodeFile codeFile : codeFiles) {
	    Map<String, Tupel> constants = headerFile.getConstants();
	    for (String con : constants.keySet()) {
		if (noConst.contains(con)) {
		    continue;
		} else {
		    codeFile.constantsMap.put(con, id);
		}
	    }
	}
    }

    private void injectVariables(HeaderFile headerFile) {
	String id = headerFile.getId();
	List<String> noVar = getDoNotReplaceVariables();
	for (CodeFile codeFile : codeFiles) {
	    for (String var : headerFile.getVariables()) {
		if (noVar.contains(var)) {
		    continue;
		} else {
		    codeFile.variablesMap.put(var, id);
		}
	    }
	}
    }

    private void injectMethods(HeaderFile headerFile) {
	String id = headerFile.getId();
	for (CodeFile codeFile : codeFiles) {
	    for (String var : headerFile.getMethods()) {
		codeFile.methodsMap.put(var, id);
	    }
	}
    }

    private void parseHeaderFiles() throws IOException {
	for (HeaderFile headerFile : headerFiles) {
	    headerFile.parse();

	    if (headerFile.getId().equals("Tsp")) {
		headerFile.addVariable("instance");
	    }

	    if (headerFile.getId().equals("Ants")) {
		headerFile.addVariable("ant_colony");
	    }
	}
    }

    private void printConstants(HeaderFile headerFile) {
	Map<String, Tupel> constants = headerFile.getConstants();
	if (constants.isEmpty()) {
	    return;
	}
	System.out.println();
	System.out.println("Constants in " + headerFile.getFileName() + ": (" + constants.size() + ")");
	System.out.println();
	for (String constant : constants.keySet()) {
	    Tupel tupel = constants.get(constant);
	    System.out.print(" " + constant);
	    System.out.print("   v: " + tupel.v);
	    System.out.println("   c: " + tupel.c);
	}
	System.out.println();
    }

    private void printMethods(HeaderFile headerFile) {
	List<String> methods = headerFile.getMethods();
	if (methods.isEmpty()) {
	    return;
	}
	System.out.println();
	System.out.println("Methods in " + headerFile.getFileName() + ": (" + methods.size() + ")");
	System.out.println();
	for (String method : methods) {
	    System.out.println(" " + method);
	}
	System.out.println();
    }

    private void printNamesOfCodeFiles() {
	System.out.println();
	System.out.println("Code files:");
	System.out.println();
	for (CodeFile file : codeFiles) {
	    System.out.print(file.getFileName());
	    System.out.println("          " + file.getId());
	}
	System.out.println();
    }

    private void printNamesOfHeaderFiles() {
	System.out.println();
	System.out.println("Header files:");
	System.out.println();
	for (HeaderFile file : headerFiles) {
	    System.out.print(file.getFileName());
	    System.out.println("          " + file.getId());
	}
	System.out.println();
    }

    private void printVariables(HeaderFile headerFile) {
	List<String> variables = headerFile.getVariables();
	if (variables.isEmpty()) {
	    return;
	}
	System.out.println();
	System.out.println("Variables in " + headerFile.getFileName() + ": (" + variables.size() + ")");
	System.out.println();
	for (String var : variables) {
	    System.out.println(" " + var);
	}
	System.out.println();
    }
}
