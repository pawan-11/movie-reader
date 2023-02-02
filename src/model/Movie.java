package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Quote;
import util.Util;

public class Movie {
	
	private String name;
	private List<Quote> script;
	private Map<String, ArrayList<Quote>> quotes;

	
	public Movie(String name, List<Quote> script, Map<String, ArrayList<Quote>> quotes) {
		this.name = name;
		this.script = script;
		this.quotes = quotes;
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, ArrayList<Quote>> getQuotes() {
		return quotes;
	}
	
	public List<Quote> getScript() {
		return script;
	}
	
	public List<Quote> getQuotes(String character) {
		if (!quotes.containsKey(character)) {
			StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
			StackTraceElement e = stacktrace[2];//maybe this number needs to be corrected
			String methodName = e.getMethodName();
			//Util.print("hey you "+methodName+", character:|"+character +"| is not in "+name+"'s script");
			//Util.print(this.keySet());
			return new ArrayList<Quote>(0);		
		}
		return quotes.get(character);
	}
	
	public List<String> getPopularCharacters(int max) {
		Set<String> characters = quotes.keySet();
		List<String> pop_chrcters = new ArrayList<String>(){
			private static final long serialVersionUID = 1L;
			public boolean add(String chrcter) {
				if (size() < max)
					super.add(chrcter);
				else
					for (int i = 0; i < max; i++) {
						if (quotes.get(get(i)).size() < quotes.get(chrcter).size()) {
							remove(i);
							add(chrcter);
							return true;
						}
					}
				return false;
			}
		};
		for (String chrcter: characters) {
			pop_chrcters.add(chrcter);
		}
		return pop_chrcters;
	}
	
	public String toString() {
		return name;
	}
}
