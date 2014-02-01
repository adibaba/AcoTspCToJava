package de.adrianwilke.acotspctojava;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class PrintParser {

    public static void main(String[] args) throws IOException {

	String file = "";

	Reader reader = new InputStreamReader(new FileInputStream(file), "UTF8");
	BufferedReader bufferedReader = new BufferedReader(reader);
	String line = bufferedReader.readLine();
	StringBuilder sb = new StringBuilder();
	while (line != null) {
	    sb.append(line + " ");
	    line = bufferedReader.readLine();
	}
	bufferedReader.close();

	parsePrintString(sb.toString());
    }

    static void parsePrintString(String s) {

	s = s.trim();

	System.out.println(s);
	System.out.println();

	if (s.startsWith("System.out.println")) {
	    s = s.substring(s.indexOf("(\"") + 2, s.lastIndexOf(");"));

	    if (s.startsWith("\\n")) {
		s = s.substring(2);
	    }

	    String[] sarr = s.split(",");
	    for (int i = 0; i < sarr.length; i++) {
		System.out.println("[" + i + "]" + sarr[i]);
	    }
	    System.out.println();

	    for (int i = 1; i < sarr.length; i++) {
		sarr[0] = sarr[0].replaceFirst("\\%ld", "\"+" + sarr[i] + "+\"");
	    }

	    if (sarr[0].endsWith("+\"\\n\"")) {
		sarr[0] = sarr[0].substring(0, sarr[0].length() - 5);
	    }

	    System.out.println("\"" + sarr[0].trim());
	}
    }
}
