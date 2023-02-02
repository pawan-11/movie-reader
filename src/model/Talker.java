package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import util.Quote;
import util.Util;

public class Talker {

	protected List<Quote> quotes;
	protected static Map<String, List<String>> word_map;
	protected String name, slogan;
	private int quote_no; //read index
	
	public Talker(String name, String slogan, List<Quote> quotes) {
		this.name = name;
		this.quotes = quotes;
		this.slogan = slogan;
		
		resetRead();
	}
	
	public String readNext() {
		if (quotes.size() == 0)
			return name+": I have no quotes";
		
		++quote_no;
		
		if (quote_no >= quotes.size()) {
			quote_no = -1; //back to start
			return "THE START";
		}
		
		Quote q = quotes.get(quote_no);
		return Util.toCamelCase(q.character)+": "+q.quote;
	}
	

	public String readPrev() {
		if (quotes.size() == 0)
			return name+" not uploaded or cannot be parsed";
		
		--quote_no;
		if (quote_no < 0) {
			quote_no = quotes.size();
			return "THE END";
		}
		Quote q = quotes.get(quote_no);
		return Util.toCamelCase(q.character)+": "+q.quote;
	}
	
	public void resetRead() {
		quote_no = -1;
	}
	
	public String getResponse(String msg) {
		String[] words = msg.split(" ");
		Set<String> keys = word_map.keySet();
		
		for (String word: words)
			word = word.toLowerCase();
		
		List<String> responses = null;
		
		outer:
			for (String word: words) {
				for (String key: keys) {
					if (word.equals(key)) {
						responses = word_map.get(key);
						break outer;
					}
				}
			}
		
		if (responses == null) { //if not a question
			int square = (int)(Math.random()*6);
			if (square == 0 || quotes.size() == 0) //1/6 chance of random response
				responses = word_map.get("random");
			else { 
				List<Quote> matching_quotes = new ArrayList<Quote>();
				for (String word: words) {
					matching_quotes.addAll(searchQuotesFor(word, quotes));
				}
				if (matching_quotes.size() > 0) {
					int r = (int)(Math.random()*matching_quotes.size());
					Quote q = matching_quotes.get(r);
					return Util.toCamelCase(q.character)+": "+q.quote;
				}
				int r = (int)(Math.random()*quotes.size());
				Quote q = quotes.get(r);
				return Util.toCamelCase(q.character)+": "+q.quote;
			}
		}
		if (responses.size() > 0) {		
			int r = (int)(Math.random()*responses.size());
			return Util.toCamelCase(this.getName()+": "+responses.get(r));
		}
		return Util.toCamelCase(this.getName())+": I don't have any random response";
	}
	
	protected List<Quote> searchQuotesFor(String keyword, List<Quote> quotes) {
		keyword = keyword.toLowerCase();
		
		ArrayList<Quote> keyword_quotes = new ArrayList<Quote>();
		for (Quote q: quotes)
			if (q.quote.toLowerCase().contains(keyword))
				keyword_quotes.add(q);
		
		return keyword_quotes;
	}
	
	public String getName() {
		return name;
	}

	public String getSlogan() {
		return slogan;
	}
	
	public List<Quote> getQuotes() {
		return quotes;
	}
	
	public static Map<String, List<String>> getWordMap() {
		return word_map;
	}
	
	public static void setWordMap(Map<String, List<String>> word_map) {
		Talker.word_map = word_map;
	}
	
	public static Map<String, List<String>> makeResponses() {
		
		Map<String, List<String>> word_map = new HashMap<String, List<String>>();
		word_map.put("what", new ArrayList<String>());
		word_map.put("how", new ArrayList<String>());
		word_map.put("when", new ArrayList<String>());
		word_map.put("who", new ArrayList<String>());
		word_map.put("which", new ArrayList<String>());
		word_map.put("why", new ArrayList<String>());
		word_map.put("where", new ArrayList<String>());
		word_map.put("is", new ArrayList<String>());
		word_map.put("are", new ArrayList<String>());
		word_map.put("will", new ArrayList<String>());
		word_map.put("would", new ArrayList<String>());
		word_map.put("do", new ArrayList<String>());
		word_map.put("does", new ArrayList<String>());
		word_map.put("random", new ArrayList<String>());
		//whose
		
		word_map.get("what").addAll(Arrays.asList("your mother", "your father", "your sister", "your boyfriend", 
				"motherfather", "an idiota"));
		word_map.get("how").addAll(Arrays.asList("i don't know", "don't ask me how", "i like you", "ask your mother", 
				"hmmm.. by sucking", "shoot")); //different types, how are, how will, how can <- nested map needed 
		word_map.get("when").addAll(Arrays.asList("2021", "2000", "69th full moon after today",
				"tomorrow", "yesterday", "when you were born", "this week", "this month", "this year",
				"i was there.. 3000 years ago"));
		word_map.get("who").addAll(Arrays.asList("sauron", "your mom", "you", "me", "your nan", "bonus mom", "jasmine"
				, "clarissa", "velma", "ravnit", "sharleen", "talia", "harjot", "gurpinder", "jen", "lexie", "megan"
				, "bonus", "parmg", "pp", "pawanpreet", "pawan", "eminem", "drake", "j cole", "caroline", "sukhjot", 
				"george", "bilbo", "galadriel did it", "lebrooon james", "the weeknd", "mikka", "mubdi"));
		word_map.get("which").addAll(word_map.get("who"));
		word_map.get("why").addAll(Arrays.asList("just because..", "because your mom", "because you deserve it",
				"same reason my beard is white", "because you suck", "you are sexy", "you got no friends"));
		word_map.get("where").addAll(Arrays.asList("india", "canada", "your basement", "at her house", "in her street",
				"at the park", "in your room", "on your street", "on mars"));
		word_map.get("is").addAll(Arrays.asList("yep!", "no", "maybe", "of course!", "are you an idiot", 
				"sure", "good question", "maybe", "of course"));
		word_map.get("are").addAll(word_map.get("is"));
		List<String> will = word_map.get("is");
		will.addAll(Arrays.asList("naa"));
		word_map.get("will").addAll(will);
		word_map.get("do").addAll(Arrays.asList("sure", "good question", "maybe", "of course", "YES!!", "a lot",
				"no", "a little", "more than anyone", "ask again", "she likes you", "like every other"));
		word_map.get("does").addAll(word_map.get("do"));
		word_map.get("random").addAll(Arrays.asList("its a nice day", "peepa pig", "santu claus", "You shall not ask!",
				"boring..", "whats new", "you can ask me anything", "oh really"));
		return word_map;
	}
}
