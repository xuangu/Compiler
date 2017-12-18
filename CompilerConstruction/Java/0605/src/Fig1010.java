import java.io.*;
import java.util.Scanner;

public class Fig1010 {
	public static void main(String[] args) throws IOException {
		Scanner inFile = new Scanner(new File(args[0]));
		Fig1010TokenMgr tMgr = new Fig1010TokenMgr(inFile);
		Fig1010Parser parser = new Fig1010Parser(tMgr);
		parser.parse();
		
		System.out.println("parse successful");
	}
}

interface Fig1010Constants {
	public int EOF = 0;
	public int UNSIGNED = 1;
	public int PLUS = 2;
	public int MINUS = 3;
	public int TIMES = 4;
	public int DIVIDE = 5;
	public int ERROR = 6;
	
	String[] tokenImage = {
							"<EOF>",
							"<UNSIGNED>",
							"\"+\"",
							"\"-\"",
							"\"*\"",
							"\"/\"",
							"ERROR"
							};
}

class Fig1010TokenMgr implements Fig1010Constants {
	private Scanner inFile;
	private char currentChar;
	private int currentColumnNumber;
	private int currentLineNumber;
	private String inputLine;
	private Token token;
	private StringBuffer buffer;
	
	public Fig1010TokenMgr(Scanner inFile) {
		this.inFile = inFile;
		currentChar = '\n';
		currentColumnNumber = 0;
		currentLineNumber = 0;
		buffer = new StringBuffer();
	}
	
	private void getNextChar() {
		if (currentChar == EOF) {
			return;
		}
		if (currentChar == '\n') {
			if (inFile.hasNextLine()) {
				inputLine = inFile.nextLine();
				inputLine += "\n";
				currentColumnNumber = 0;
				currentLineNumber++;
			} else {
				currentChar = EOF;
				
				return ;
			}
		}
		
		currentChar = inputLine.charAt(currentColumnNumber++);
	}
	
	public Token getNextToken() {
		while (Character.isWhitespace(currentChar)) {
			getNextChar();
		}
		
		token = new Token();
		token.nextToken = null;
		
		token.beginColumn = currentColumnNumber;
		token.beginLine = currentLineNumber;
		
		if (currentChar == EOF) {
			token.type = EOF;
			token.image = tokenImage[token.type];
			token.endColumn = currentColumnNumber;
			token.endLine = currentLineNumber;
		} else if (Character.isDigit(currentChar)) {
			buffer.setLength(0);
			do {
				buffer.append(currentChar);
				token.endColumn = currentColumnNumber;
				token.endLine = currentLineNumber;
				getNextChar();
			} while (Character.isDigit(currentChar));
			
			token.type = UNSIGNED;
			token.image = buffer.toString();
		} else {
			switch (currentChar) {
			case '+':
				token.type = PLUS;
				break;
			case '-':
				token.type = MINUS;
				break;
			case '*':
				token.type = TIMES;
				break;
			case '/':
				token.type = DIVIDE;
				break;
			default:
				break;
			}
			token.image = tokenImage[token.type];
			token.endColumn = currentColumnNumber;
			token.endLine = currentLineNumber;
			
			getNextChar();
		}
		
		System.out.printf("type=%s, beginLine=%3d, beginColumn=%3d, endLine=%3d, endColumn=%3d, image:%s\n",
				tokenImage[token.type], token.beginLine, token.beginColumn, token.endLine, token.endColumn, token.image);
		
		return token;
	}
}

//class Token {
//	public int type;
//	public int beginLine, beginColumn, endLine, endColumn;
//	public String image;
//	public Token nextToken;
//}

class Fig1010Parser implements Fig1010Constants {
	private Fig1010TokenMgr tMgr;
	private Token currentToken;
	private Token previousToken;
	
	public Fig1010Parser(Fig1010TokenMgr tMgr) {
		this.tMgr = tMgr;
		currentToken = this.tMgr.getNextToken();
		previousToken = null;
	}
	
	private void advance() {
		previousToken = currentToken;
		if (currentToken.nextToken != null) {
			currentToken = currentToken.nextToken;
		} else {
			// 类比链表在链表尾追加一个节点
			currentToken.nextToken = tMgr.getNextToken();
			currentToken = currentToken.nextToken;
		}
	}
	
	private void consume(int expected) {
		if (currentToken.type == expected) {
			advance();
		} else {
			throw genEx("Expecting " + tokenImage[expected]);
		}
	}
	
	private RuntimeException genEx(String errorMessage) {
		return new RuntimeException("Encountered \"" +
									currentToken.image +
									"\" on line " + currentToken.beginLine +
									"\" on column " + currentToken.beginColumn +
									System.getProperty("line.separator") +
									errorMessage);
	}
	
	public void parse() {
		S();
		if (currentToken.type != EOF) {
			throw genEx("Expecting <EOF>");
		}
	}
	
	private Token getToken(int i) {
		if (i <= 0) {
			return previousToken;
		}
		
		Token token = currentToken;
		
		for (int j = 1; j < i; j++) {
			if (token.nextToken != null) {
				token = token.nextToken;
			} else {
				token.nextToken = tMgr.getNextToken();
				token = token.nextToken;
			}
		}
		
		return token;
	}
	
	private void S() {
		int p;
		
		p = expr();
		
		System.out.println(p);
	}
	
	private int expr() {
		int p, q;
		Token token;
		switch (currentToken.type) {
		case PLUS:
			consume(PLUS);
			p = expr();
			q = expr();
			return p + q;
		
		case MINUS:
			consume(MINUS);
			p = expr();
			q = expr();
			return p - q;
		
		case TIMES:
			consume(TIMES);
			p = expr();
			q = expr();
			return p * q;
		
		case DIVIDE:
			consume(DIVIDE);
			p = expr();
			q = expr();
			return p / q;

		case UNSIGNED:
			token = currentToken;
			consume(UNSIGNED);
			p = Integer.parseInt(token.image);
			return p;
		
		default:
			throw genEx("Expecting operator or " + tokenImage[UNSIGNED]);
		}
	}
}

