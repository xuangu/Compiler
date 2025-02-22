
public class Fig1004 {
	public static void main(String[] args) {
		ArgsTokenMgr tMgr = new ArgsTokenMgr(args);
		Fig1004Parser parser = new Fig1004Parser(tMgr);
		parser.parse();
	}
}

class Fig1004Parser {
	private ArgsTokenMgr tMgr;
	private char currentToken;
	
	Fig1004Parser(ArgsTokenMgr tMgr) {
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
			throw new RuntimeException("Expecting end of input");
		}
	}
	
	private void S() {
		expr();
		System.out.println();
	}
	
	private void expr() {
		switch (currentToken) {
		case '+':
			consume('+');
			expr();
			expr();
			System.out.print('+');
			break;
		
		case '-':
			consume('-');
			expr();
			expr();
			System.out.print('-');
			break;
			
		case '*':
			consume('*');
			expr();
			expr();
			System.out.print('*');
			break;
			
		case '/':
			consume('/');
			expr();
			expr();
			System.out.print('/');
			break;
			
		case 'b':
			consume('b');
			System.out.print('b');
			break;
			
		case 'c':
			consume('c');
			System.out.print('c');
			break;
			
		case 'd':
			consume('d');
			System.out.print('d');
			break;
			
		default:
			throw new RuntimeException("Expecting right characters" + currentToken);
		}
	}
}
