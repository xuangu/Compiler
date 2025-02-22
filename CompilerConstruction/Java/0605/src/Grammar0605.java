import java.util.Stack;

/* Grammar0605
 * 1) S -> bScA
 * 2) S -> cbd
 * 3) A -> bcA
 * 4) A -> d
 */
public class Grammar0605 {
	public static void main(String[] args) {
		ArgsTokenMgr tm = new ArgsTokenMgr(args);
		
		Grammar0605Parser parser = new Grammar0605Parser(tm);
		
		parser.parse();
	}
}


class ArgsTokenMgr {
	private int index;
	String input;
	
	ArgsTokenMgr(String[] args) {
		if (args.length > 0) {
			input = args[0];
		} else {
			input = "";
		}
		
		index = 0;
		System.out.println("input = " + input);
	}
	
	public char getNextToken() {
		if (index < input.length()) {
			return input.charAt(index++);
		} else {
			return '#';
		}
	}
}


class Grammar0605Parser {
	private ArgsTokenMgr tMgr;
	private Stack<Character> stack;
	private char currentToken;
	
	public Grammar0605Parser(ArgsTokenMgr tMgr) {
		this.tMgr = tMgr;
		advance();
		stack = new Stack<Character>();
		stack.push('$');
		stack.push('S');
	}
	
	private void advance() {
		currentToken = tMgr.getNextToken();
	}
	
	public void parse() {
		boolean done = false;
		
		while (!done) {
			switch (stack.peek()) {
			case 'S':
				if (currentToken == 'b') {
					stack.pop();
					stack.push('A');
					stack.push('c');
					stack.push('S');
					advance();
				} else if (currentToken == 'c') {
					stack.pop();
					stack.push('d');
					stack.push('b');
					advance();
				} else {
					done = true;
				}
				
				break;
			
			case 'A':
				if (currentToken == 'b') {
					stack.pop();
					stack.push('A');
					stack.push('c');
					advance();
				} else {
					done = true;
				}
				
				break;
				
			case 'b':
			case 'c':
			case 'd':
				if (stack.peek().charValue() == currentToken) {
					stack.pop();
					advance();
				} else {
					done = true;
				}
				
				break;
			
			case '$':
				done = true;
				break;
				
			default:
				break;
			}
		}
		
		if (currentToken == '#' && stack.peek() == '$') {
			System.out.println("accept");
		} else {
			System.out.println("reject");
		}
	}
}


