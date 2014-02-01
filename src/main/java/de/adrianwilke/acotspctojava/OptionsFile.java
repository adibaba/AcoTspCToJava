package de.adrianwilke.acotspctojava;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OptionsFile {

    class Option {
	String shortOpt;
	String longOpt;
	String descr;
    }

    Map<String, Option> options = new LinkedHashMap<String, Option>();

    private final String LS = System.getProperty("line.separator");

    private File sourceFile;

    public OptionsFile(File sourceFile) {
	this.sourceFile = sourceFile;
    }

    public void parse() throws IOException {
	List<String> lines = Files.getUTF8TextFileContent(sourceFile.getAbsolutePath());

	boolean nextIsDefine = false;
	boolean printDefine = false;

	boolean parseCmdLine = false;

	for (String line : lines) {

	    if (!nextIsDefine && line.startsWith("#define")) {
		nextIsDefine = true;
		continue;
	    } else if (nextIsDefine) {
		nextIsDefine = false;

		StringBuilder sb = new StringBuilder();
		String[] strings = line.split("\\s{1,}");

		String shortOpt = removeNoCharsNumMinus(strings[2]);
		if (shortOpt.isEmpty() || !shortOpt.contains("-")) {
		    sb.append("ERROR " + "No short opt" + LS);
		}
		shortOpt = removeNoCharsNum(shortOpt);

		String longOpt = removeNoCharsNumMinus(strings[3]);
		String descr;
		if (longOpt.isEmpty() || !longOpt.contains("-")) {
		    longOpt = null;
		    descr = line.substring(line.indexOf(shortOpt) + shortOpt.length());
		    descr = descr.substring(0, descr.lastIndexOf("\\n"));
		} else {
		    longOpt = removeNoCharsNum(longOpt);
		    descr = line.substring(line.indexOf(longOpt) + longOpt.length());
		    descr = descr.substring(0, descr.lastIndexOf("\\n"));
		}

		if (printDefine) {
		    sb.append(line + LS);
		    sb.append(Arrays.toString(strings) + LS);
		    sb.append(shortOpt + LS);
		    sb.append(longOpt + LS);
		    sb.append(descr + LS);
		    sb.append(LS);
		    System.out.println(sb.toString());
		}

		Option option = new Option();
		option.shortOpt = shortOpt;
		option.longOpt = longOpt;
		option.descr = descr;
		options.put(shortOpt, option);

		continue;
	    }

	    if (line.startsWith("int parse_commandline")) {
		System.out.println(options.size() + " options in define");
		parseCmdLine = true;
	    }
	    if (parseCmdLine) {
		if (line.trim().startsWith("if ( options.opt_")) {
		    // System.out.println(line);
		} else

		if (line.trim().startsWith("fputs")) {
		    // System.out.println(line.substring(line.indexOf("\"")+1,line.lastIndexOf("\"")));
		}

		if (line.trim().startsWith("printf")) {
		    // System.out.println(line);
		}

		if (line.trim().startsWith("fprintf")) {
		    // System.out.println(line.substring(line.indexOf("\"")+1));
		    // System.out.println(line.substring(line.indexOf("\"")+1,line.lastIndexOf("\"")));
		}
	    }
	}
    }

    public void printOptions() {
	StringBuilder sb = new StringBuilder();
	for (String key : options.keySet()) {
	    Option option = options.get(key);
	    sb.append("options.addOption(\"" + option.shortOpt + "\", ");
	    if (option.longOpt != null)
		sb.append("\"" + option.longOpt + "\", ");
	    if (key.equals("d") || key.equals("u") || key.equals("v") || key.equals("w") || key.equals("x")
		    || key.equals("y") || key.equals("z") || key.equals("quiet") || key.equals("h"))
		sb.append("false, ");
	    else
		sb.append("true, ");
	    sb.append("\"" + option.descr + "\");");
	    sb.append(LS);
	}
	System.out.println(sb.toString());
    }

    private String removeNoCharsNumMinus(String string) {
	return string.replaceAll("[^a-zA-Z0-9\\-]", "");
    }

    private String removeNoCharsNum(String string) {
	return string.replaceAll("[^a-zA-Z0-9]", "");
    }
}
