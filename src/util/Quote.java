package util;

public class Quote {

	public String quote;
	public String character;
	public int quote_no; //the quote number from script
	
	public Quote(int quote_no, String character, String quote) {
		this.quote_no = quote_no;
		this.character = character;
		this.quote = quote;
	}
	
	public String toString() {
		return quote_no+" "+character+":"+quote;
	}
}
