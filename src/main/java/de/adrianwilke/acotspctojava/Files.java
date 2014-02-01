package de.adrianwilke.acotspctojava;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

public abstract class Files {

    static List<String> getUTF8TextFileContent(String textFileName) throws IOException {
	List<String> lines = new LinkedList<String>();
	Reader r = new InputStreamReader(new FileInputStream(textFileName), "UTF8");
	BufferedReader in = new BufferedReader(r);
	String line;
	while ((line = in.readLine()) != null)
	    lines.add(line);
	in.close();
	return lines;
    }

    static File[] getFiles(String directory, final String fileExtension) {
	return new File(directory).listFiles(new FilenameFilter() {
	    public boolean accept(File dir, String name) {
		if (name.endsWith(fileExtension))
		    return true;
		else
		    return false;
	    }
	});
    }

    static void writeUTF8TextFileContent(String textFileName, List<String> lines) throws IOException {
	String lineFeed = System.getProperty("line.separator");
	Writer w = new OutputStreamWriter(new FileOutputStream(textFileName), "UTF8");
	BufferedWriter out = new BufferedWriter(w);
	for (String line : lines) {
	    out.write(line);
	    out.write(lineFeed);
	}
	out.close();
    }
}
