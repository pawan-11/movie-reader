package model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import util.Quote;
import util.Util;

public class PdfScriptParser {

	//storage for parsed info
	public HashMap<String, ArrayList<Quote>> quotes;
	public ArrayList<Quote> script; //chronological order quotes

	//tools for parsing
	private String quote = "", character = "", quote_stack = "", bracket_stack = "";
	private double page_width;
	private int quote_no;
	private HashMap<Integer, Integer> unindented_starts; //position of first character in a quote, and number of quotes it has happened
	private int unindented_start_pos = 0;

	public PdfScriptParser() {
		clear();
	}

	public void clear() {
		quotes = new HashMap<String, ArrayList<Quote>>();
		script = new ArrayList<Quote>();
		unindented_starts = new HashMap<Integer, Integer>(){
			private static final long serialVersionUID = 1L;		
			public boolean containsKey(Object key) {
				return super.containsKey(roundPos((Integer)key));
			}		
			public Integer get(Object key) {
				return super.get(roundPos((Integer)key));
			}	
			public Integer put(Integer key, Integer val) {
				return super.put(roundPos(key), val);
			}
		};
		unindented_start_pos = 0;
		unindented_starts.put(unindented_start_pos, 0);
		character = quote = quote_stack = "";
	}

	int prev_end_pos = 0;
	int prev_start_pos = 0;
	public void parseLine(String line, List<TextPosition> positions) {
		String new_line = removeBrackets(line, positions);
		int start_pos = 0, end_pos = (int)page_width;
		if (new_line.length() > 0) {
			start_pos = roundPos(positions.get(0).getX());
			end_pos = roundPos(positions.get(positions.size()-1).getX());
		}
		//Util.print(start_pos+", "+prev_end_pos+"||"+line+"||");
		
	//	if (new_line.toLowerCase().contains("ELDRED".toLowerCase()) ||
	//			(new_line.toLowerCase().contains("WORPLE".toLowerCase())))
	//		Util.print(new_line+"||"+start_pos+", "+prev_start_pos);
	
		//unindented or blank line, ending a quote 
		if (line.length() == 0 || (new_line.length() > 0 && 
				!(quote_stack.length() == 1 && (quote.length() == 0 || prev_start_pos == start_pos)) &&
				!(start_pos-prev_end_pos < 30 && new_line.matches("[a-z]+")) &&
				isUspReliable(start_pos)))
		{	
			if (new_line.length() > 0)
				reportUsp(start_pos);
			if (quote_stack.length() == 1  && quote.length() > 0) {
				addQuote(character, quote);				
			}
		}
		if (isCharacter(new_line) && start_pos > page_width*0.36 && start_pos < page_width/2+50) {
			//same character name, split by newline (ex. eldred worple in hbp)
			if (start_pos-prev_end_pos <= 40 && quote_stack.length() == 1 && quote.length() == 0) {
				if (quotes.containsKey(character) && quotes.get(character).size() == 0) {	
					quotes.remove(character);
				//	Util.print("removed "+character+ ", new name: "+character+" "+new_line.toLowerCase());
				}
				
				character += " "+new_line.toLowerCase();
				addCharacter(character);
			}
			else {
				if (quote_stack.length() == 1) {//if a quote was already open by prev character
					addQuote(character, quote);
				}
				if (quotes.containsKey(character) && quotes.get(character).size() == 0) {	
					quotes.remove(character);
				//	Util.print("removed "+character+ " expected quote but new character came "+new_line);
				}

				character = new_line.toLowerCase();
				quote_stack = "\"";
				addCharacter(character);

				//if this quote should be added to previous quote (in case new page)
				if (line.contains("cont'd") && quote.length() == 0) { 
					Quote old_quote = script.get(script.size()-1);
					quote_no = old_quote.quote_no;
					quote = old_quote.quote;

					script.remove(script.size()-1);
					quotes.get(character).remove(quotes.get(character).size()-1);
				}

			}
		}
		else {
			if (quote_stack.length() == 1 && new_line.length() > 0) {
				quote += new_line+ " "; 
			//	prev_start_pos = start_pos;
			//	prev_end_pos = (int)positions.get(positions.size()-1).getX();
			}
			else if (new_line.length() > 0)
				reportUsp(start_pos);
		}
		
		if (new_line.length() > 0) {
			prev_start_pos = start_pos;
			prev_end_pos = end_pos;
		}
	}

	private void reportUsp(int start_pos) {
		if (unindented_starts.containsKey(start_pos))
			unindented_starts.put(start_pos, unindented_starts.get(start_pos)+1);
		else
			unindented_starts.put(start_pos, 1);

		if (unindented_starts.get(start_pos) > unindented_starts.get(unindented_start_pos)) {
			unindented_start_pos = start_pos;
			//Util.print("most unindented pos "+unindented_start_pos+" #of lines:"+unindented_starts.get(start_pos)+" e.g."+new_line);
		}

	}
	public boolean parsePDF(InputStream file) {
		try {
			return parsePDF(Loader.loadPDF(file));
		} catch (IOException e) {
			Util.print("could not parse inputstream");
			return false;
		}
	}

	public boolean parsePDF(File file) {
		try {
			return parsePDF(Loader.loadPDF(file));
		} catch (IOException e) {
			Util.print("could not parse file "+file.getName());
			return false;
		}
	}

	public boolean parsePDF(PDDocument doc) {
		try {			
			PDFTextStripper stripper = new PDFTextStripper() {		
				@Override
				protected void writeString(String line, List<TextPosition> positions) throws IOException {
					parseLine(line, positions);
					//	super.writeString(text, positions);
				}
			};			
			stripper.setSortByPosition(true);
			stripper.setStartPage(1);	
		//	stripper.setEndPage(5);
			//stripper.setSpacingTolerance(stripper.getAverageCharTolerance()+3);
			stripper.setEndPage(doc.getNumberOfPages());

			page_width = doc.getPage(0).getBBox().getWidth();
			Util.print("page width of "+doc+" is "+page_width);
			stripper.getText(doc);

			//Util.print(unindented_starts.toString());

			if (quotes.containsKey(character) && quotes.get(character).size() == 0) {//remove the last character if it doesnt have any quote
				quotes.remove(character);
			//	Util.print("removed at end "+character);
			}
			return true;
		}
		catch (IOException e) {
		}
		catch (NumberFormatException e) {		
		}
		Util.print("error parsing pddocument with pages "+doc.getNumberOfPages());
		return false;
	}

	//helpers
	public void addCharacter(String chrcter) {
		chrcter = chrcter.toLowerCase();
		if (!quotes.containsKey(chrcter)) { //new character 
			quotes.put(chrcter, new ArrayList<Quote>());	
		}
	}
	public void addQuote(String chrcter, String quot) {
		quot = quot.trim();
		chrcter = chrcter.toLowerCase();
		if (quot.length() == 0 || !quotes.containsKey(chrcter)) return;
		Quote q = new Quote(quote_no, chrcter, quot);
		
		quotes.get(chrcter).add(q);				
		script.add(q);
		quote_no++;

		character = quote_stack = quote = "";	
	}

	public boolean isCharacter(String chrcter) {
		//	if (chrcter.toLowerCase().contains("mrs"))
		//		Util.print("is character "+chrcter);
		String[] words = chrcter.split(" ");
		if (words.length == 0) return false;
		if (words[0].length() == 1)
			if (!Character.isUpperCase(words[0].charAt(0))) return false;
		
		for (String word: words) {
			if (word.equals("and") || word.equals("or"))
				return false;
		}
		for (int i = 0; i < chrcter.length(); i++) {
			char c = chrcter.charAt(i);
			if (c == ' ' || (i < chrcter.length()-1 && "-\\./".contains(c+"")))
				continue;
			else if (!(Character.isLetter(c) && Character.isUpperCase(c))) {
				//a lowercase letter
				if (!(i < chrcter.length()-1 && Character.isUpperCase(chrcter.charAt(i+1)))) //names like McGONAGALL 
					return false;
			}
		}
		return chrcter.length() > 1; //spaces < 3
	}

	public String removeBrackets(String line, List<TextPosition> positions) {
		//ArrayList<String> words = new ArrayList<>(Arrays.asList(line.trim().split(" ")));		
		String new_line = "";
		boolean remove_space = true;
		int i = 0;

		while (i < line.length()) {			
			if (line.charAt(i) == '(')
				bracket_stack += "(";

			remove_space &= line.charAt(i) == ' ';

			if (bracket_stack.length() != 0 || remove_space || line.charAt(i) == '*') { //word is inside open bracket	
				positions.remove(new_line.length()); //remove at this index	
				if (line.charAt(i) == ')')
					bracket_stack = bracket_stack.substring(0, bracket_stack.length()-1); //remove last open bracket
			}	
			else {
				new_line += line.charAt(i);
			}
			remove_space = line.charAt(i) == ' ';
			i++;
		}

		int untrimmed_len = new_line.length();
		new_line = new_line.trim();
		for (int j = 0; j < untrimmed_len-new_line.length(); j++)
			positions.remove(positions.size()-1);

		assert(new_line.length() == positions.size());
		return new_line;
	}

	private boolean isUspReliable(int pos) {
		boolean isMostUspReliable = unindented_starts.get(unindented_start_pos) > 1;
		boolean isPosUsp = unindented_starts.containsKey(pos) && unindented_starts.get(pos) > 1;
		return (isMostUspReliable && pos <= unindented_start_pos) || isPosUsp || pos > page_width/2;
	}

	private int roundPos(double pos) {
		return (int)(20*(Math.round(pos/20)));
	}
}

