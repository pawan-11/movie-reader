package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import util.Quote;
import util.Util;

public class TextScriptParser {

	public List<Quote> script;
	public HashMap<String, ArrayList<Quote>> quotes;
	
	public TextScriptParser() {
		clear();
	}
	
	public void clear() {
		script = new ArrayList<Quote>(1000);
		quotes = new HashMap<String, ArrayList<Quote>>();
	}
	
	public boolean parse(String quote_file) {
		quote_file += ".txt";
		String path = Paths.get(Util.getParentDir(), "movie_quotes_txts", quote_file).toString();
		return parse(new File(path));
	}
	
	public boolean parse(File quote_file) {
		try {
			
			BufferedReader reader = new BufferedReader(new FileReader(quote_file));
			String line;
			String[] quote_parts;
			Quote quote;
			while ((line = reader.readLine()) != null) {
				quote_parts = line.split("~");
				quote = new Quote(Integer.parseInt(quote_parts[0]), quote_parts[1], quote_parts[2]);
				script.add(quote);
				if (!quotes.containsKey(quote_parts[1]))
					quotes.put(quote_parts[1], new ArrayList<Quote>());
				quotes.get(quote_parts[1]).add(quote);
			}
			reader.close();
		}
		catch (Exception e) {
			Util.print("failed to parse text script "+quote_file.getName());
			return false;
		}
		Util.print("parsed text file "+quote_file.getName()+" "+(quotes.size() > 0));
		return quotes.size() > 0;
	}
	
	public static boolean save(List<Quote> quotes, String quote_file) {
		quote_file += ".txt";
		try {
			File quotes_dir = new File(Paths.get(Util.getParentDir(), "movie_quotes").toUri().getPath());
			if (!quotes_dir.exists())
				if (!quotes_dir.mkdir()) return false;
			String path = Paths.get(quotes_dir.getPath(), quote_file).toString();
			FileWriter writer = new FileWriter(path, false);
			for (Quote q: quotes)
				writer.write(q.quote_no+"~"+q.character+"~"+q.quote+"\n");
			writer.close();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
