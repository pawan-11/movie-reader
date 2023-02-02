package util;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import model.JSONParser;

public class Util {

	public static String toCamelCase(String s) {
	//	s = s.toLowerCase();
		String words[] = s.split(" ");
		String new_s = "";
		for (String word: words) {
			if (word.length() == 0) continue;
			new_s += Character.toUpperCase(word.charAt(0));
			new_s += word.substring(1, word.length())+" ";
		}
		return new_s.trim();
	}
	
	public static void print(String s) {
		System.out.println(s);
	}
	
	public static void print(Object o) {
		System.out.println(o);
	}
	
	public static void print(String[] ss) {
		for (String s: ss)
			System.out.print(s+"|");
		print("");
	}
	
	public static void println(Collection<String> lines) {
		for (String line: lines)
			print(line);
		print("");
	}
	
	public static void printQ(Collection<Quote> quotes) {
		for (Quote q: quotes)
			print(q);
		print("");
	}
	
	public static void printQ(Collection<Quote> quotes, int millis) {
		for (Quote q: quotes) {
			print(q);
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
		print("");
	}
	
	public static void mergeQuotes(List<Quote> dest, List<Quote> src) {
		int dest_idx = 0;
		for (Quote q: src) {
			while (dest_idx < dest.size() && dest.get(dest_idx).quote_no < q.quote_no) {
				dest_idx++;
			}
			dest.add(dest_idx, q);
		}
	}
	
	public static String trim_last_dir(String path) {
		String new_path = path;
		int idx = path.length()-2;
		while (idx >= 0 && path.charAt(idx) != '/')
			idx -= 1;		
		new_path = new_path.substring(0, idx);
		return new_path;
	}
	
	public static String getParentDir() {
		String path;
		try {
			path = new File(JSONParser.class.getProtectionDomain().getCodeSource().
						getLocation().toURI()).getPath();
			path = trim_last_dir(path);
			return path;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static String removeExt(String file_name) {
		String[] parts = file_name.split("\\.");
		if (parts.length == 0) return "";
		return parts[0];
	}
	
	static FileChooser filechooser = new FileChooser();
	{
		filechooser.setInitialDirectory(new File(System.getProperty("user.home")));
	}
	
	public static File askFile() {
		try {
			filechooser.setTitle("Open Movie Screenplay Pdf");
			FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Pdf Files", "*.pdf");
			filechooser.getExtensionFilters().add(filter);
			File file = filechooser.showOpenDialog(null);
			
			return file;
		}
		catch (Exception e) {
			Util.print("bad file uploaded");
		}	
		return null;
	}
}

