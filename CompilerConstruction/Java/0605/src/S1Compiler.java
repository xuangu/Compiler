import java.io.*;
import java.util.*;

public class S1Compiler {
	public static void main(String[] args) throws IOException {
		System.out.println("S1 compiler written by ...");
		
		if (args.length != 1) {
			System.err.println("Wrong number and line args");
			System.exit(1);
		}
		
		boolean isInDebugMode = false;
		
		String inFileName = args[0] + ".s";
		String outFileName = args[0] + ".a";
		
		Scanner inFile = new Scanner(new File(inFileName));
		PrintWriter outFile = new PrintWriter(outFileName);
		
		outFile.println("; from S1 compiler written by ...");
		
		S1SymTab symTab = new S1SymTab();
		S1TokenMgr tMgr = new S1TokenMgr(inFile, outFile, isInDebugMode);
		S1CodeGeneration codeGen = new S1CodeGeneration(outFile, symTab);
		S1Parser parser = new S1Parser(symTab, tMgr, codeGen);
		
		try {
			parser.parse();
		} catch (RuntimeException e) {
			System.err.println(e.getMessage());
			outFile.println(e.getMessage());
			outFile.close();
			System.exit(1);
		}
		
		outFile.close();
	}
} 

class Token {
	public int type;
	public int beginLine, beginColumn, endLine, endColumn;
	public String image;
	public Token nextToken;
}

interface S1Constants {
	public int EOF = 0;
	public int PRINTLN = 1;
	public int UNSIGNED = 2;
	public int ID = 3;
	public int ASSIGN = 4;
	public int SEMICOLON = 5;
	public int LEFTPAREN = 6;
	public int RIGHTPAREN = 7;
	public int PLUS = 8;
	public int MINUS = 9;
	public int TIMES = 10;
	public int DIVIDE = 11;
	public int ERROR = 12;
	
	String[] tokenImage = {
							"<EOF>",
							"PRINTLN",
							"<UNSIGNED>",
							"ID",
							"ASSIGN",
							"SEMICOLON",
							"LEFTPAREN",
							"RIGHTPAREN",
							"\"+\"",
							"\"-\"",
							"\"*\"",
							"DIVIDE",
							"ERROR"
							};
}

class S1SymTab {
	private ArrayList<String> symbolTable;
	
	public S1SymTab() {
		this.symbolTable = new ArrayList<String>();
	}
	
	public void enter(String symbol) {
		if (symbolTable.indexOf(symbol) < 0) {
			symbolTable.add(symbol);	
		}
	}
	
	public String getSymbol(int index) {
		return symbolTable.get(index);
	}
	
	public int getSize() {
		return symbolTable.size();
	}
}

class S1TokenMgr implements S1Constants {
	private Scanner inFile;
	private PrintWriter outFile;
	private boolean isInDebugMode;
	private char currentChar;
	private int currentColumnNumber;
	private int currentLineNumber;
	private String inputLine;
	private Token token;
	private StringBuffer buffer;
	
	public S1TokenMgr(Scanner inFile, PrintWriter outFile, boolean isInDebugMode) {
		this.inFile = inFile;
		this.outFile = outFile;
		this.isInDebugMode = isInDebugMode;
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
				// 添加源代码作为注释
				outFile.println(";" + inputLine);
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
		} else if (Character.isLetter(currentChar)) {
			buffer.setLength(0);
			do {
				buffer.append(currentChar);
				token.endColumn = currentColumnNumber;
				token.endLine = currentLineNumber;
				getNextChar();
			} while (Character.isLetterOrDigit(currentChar));
			
			token.image = buffer.toString();
			
			if (token.image.equals("println")) {
				token.type = PRINTLN;
			} else {
				token.type = ID;
			}
		} else {
			switch (currentChar) {
			case '=': 
				token.type = ASSIGN;
				break;
			case ';': 
				token.type = SEMICOLON;
				break;
			case '(': 
				token.type = LEFTPAREN;
				break;
			case ')':
				token.type = RIGHTPAREN;
				break;
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
				token.type = ERROR;
				break;
			}
			token.image = Character.toString(currentChar);
			token.endColumn = currentColumnNumber;
			token.endLine = currentLineNumber;
			
			getNextChar();
		}
		if (isInDebugMode) {
			System.out.printf("type=%s, beginLine=%3d, beginColumn=%3d, endLine=%3d, endColumn=%3d, image:%s\n",
					tokenImage[token.type], token.beginLine, token.beginColumn, token.endLine, token.endColumn, token.image);	
		}
		
		return token;
	}
}

class S1Parser implements S1Constants {
	private S1TokenMgr tMgr;
	private S1SymTab symbolTable;
	private S1CodeGeneration codeGen;
	private Token currentToken;
	private Token previousToken;
	
	public S1Parser(S1SymTab symTab, S1TokenMgr tMgr, S1CodeGeneration codeGen) {
		this.tMgr = tMgr;
		this.symbolTable = symTab;
		this.codeGen = codeGen;
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
	
	private RuntimeException genEx(String errorMessage) {
		return new RuntimeException("Encountered \"" +
									currentToken.image +
									"\" on line " + currentToken.beginLine +
									"\" on column " + currentToken.beginColumn +
									System.getProperty("line.separator") +
									errorMessage);
	}
	
	public void parse() {
		program();
	}
	
	private void program() {
		statementList();
		
		codeGen.endCode();
		
		if (currentToken.type != EOF) {
			genEx("parse run, expecting <EOF>");
		}
	}
	
	private void statementList() {
		switch (currentToken.type) {
		case ID:
		case PRINTLN:
			statement();
			statementList();
			break;
		case EOF:
			;
			break;
		default:
			throw genEx("parse run, expecting statement or <EOF>");
		}
	}
	
	private void statement() {
		switch (currentToken.type) {
		case ID:
			assignStatement();
			break;
		case PRINTLN:
			printlnStatement();
			break;
		default:
			throw genEx("Expecting statement");
		}
	}
	
	private void assignStatement() {
		Token token = currentToken;
		consume(ID);
		symbolTable.enter(token.image);
		codeGen.emitInstruction("pc", token.image);
		consume(ASSIGN);
		expr();
		codeGen.emitInstruction("stav");
		consume(SEMICOLON);
	}
	
	private void printlnStatement() {
		consume(PRINTLN);
		consume(LEFTPAREN);
		expr();
		codeGen.emitInstruction("dout");
		codeGen.emitInstruction("pc", "'\\n'");
		codeGen.emitInstruction("aout");
		consume(RIGHTPAREN);
		consume(SEMICOLON);
	}
	
	private void expr() {
		term();
		termList();
	}
	
	private void term() {
		factor();
		factorList();
	}
	
	private void termList() {
		switch (currentToken.type) {
		case PLUS:
			consume(PLUS);
			term();
			codeGen.emitInstruction("add");
			termList();
			break;
		case RIGHTPAREN:
		case SEMICOLON:
			break;
		default:
			genEx("Expecting \"+\", \")\", or \";\"");
		}
	}
	
	private void factor() {
		Token token;
		switch (currentToken.type) {
		case UNSIGNED:
			token = currentToken;
			consume(UNSIGNED);
			codeGen.emitInstruction("pwc", token.image);
			break;
			
		case ID:
			token = currentToken;
			consume(ID);
			symbolTable.enter(token.image);
			codeGen.emitInstruction("p", token.image);
			break;
			
		case PLUS:
			consume(PLUS);
			token = currentToken;
			consume(UNSIGNED);
			codeGen.emitInstruction("pwc", token.image);
			break;
			
		case MINUS:
			consume(MINUS);
			token = currentToken;
			consume(UNSIGNED);
			codeGen.emitInstruction("pwc", "-" + token.image);
			break;
			
		case LEFTPAREN:
			consume(LEFTPAREN);
			expr();
			consume(RIGHTPAREN);
			break;
		
		case TIMES:
			
		
		case DIVIDE:
			
		
		default:
			throw genEx("Expecting operator or " + tokenImage[UNSIGNED]);
		}
	}
	
	private void factorList() {
		switch (currentToken.type) {
		case TIMES:
			consume(TIMES);
			factor();
			codeGen.emitInstruction("mult");
			factorList();
			break;
		case RIGHTPAREN:
		case SEMICOLON:
		case PLUS:
			break;
		default:
			throw genEx("Expecting op, \")\", or \";\"");
		}
	}
	
	
}

class S1CodeGeneration {
	private PrintWriter outFile;
	private S1SymTab symbolTable;
	
	public S1CodeGeneration(PrintWriter outFile, S1SymTab symbolTable) {
		this.outFile = outFile;
		this.symbolTable = symbolTable;
	}
	
	public void endCode() {
		outFile.println();
		emitInstruction("halt");
		
		int size = symbolTable.getSize();
		
		for (int i = 0; i < size; i++) {
			emitdw(symbolTable.getSymbol(i), "0");
		}
	}
	
	public void emitInstruction(String operation) {
		outFile.printf("		%-4s%n", operation);
	}
	
	public void emitInstruction(String operation, String operand) {
		outFile.printf("		%-4s		%s%n", operation, operand);
	}
	
	private void emitdw(String lable, String value) {
		outFile.printf("		%-9s dw		%s%n", lable + ":", value );
	}
}





























