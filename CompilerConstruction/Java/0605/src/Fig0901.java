/*				 Selection set
 * 1) S -> BD		{b,c}
 * 2) B -> bB		{b}
 * 3) B -> c		{c}
 * 4) D -> de		{d}
 */

public class Fig0901 {
	public static void main(String[] args) {
		ArgsTokenMgr tMgr = new ArgsTokenMgr(args);
		Fig0901Parser parser = new Fig0901Parser(tMgr);
		parser.parse();
		
		System.out.println("Accept");
	}
}


class Fig0901Parser {
	private ArgsTokenMgr tMgr;
	private char currentToken;
	public Fig0901Parser(ArgsTokenMgr tMgr) {
		this.tMgr = tMgr;
		advance();
	}
	
	private void advance() {
		currentToken = tMgr.getNextToken();
	}
	
	private void consume(char expected) {
		if (currentToken == expected) {
			advance();
		} else {
			throw new RuntimeException("Expecting \"" + expected + "\"");
		}
	}
	
	public void parse() {
		S();
		if (currentToken != '#') {
			throw new RuntimeException("Excepting end of input");
		}
	}
	
	private void S() {
		B();
		D();
	}
	
	private void B() {
		switch (currentToken) {
		case 'b':
			consume('b');
			B();
			break;
		case 'c':
			consume('c');
			break;
		default:
			break;
		}
	}
	
	private void D() {
		consume('d');
		consume('e');
	}
}
