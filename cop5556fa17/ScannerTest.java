/**
 * /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Fall 2017.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Fall 2017 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2017
 */

package cop5556fa17;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;

import static cop5556fa17.Scanner.Kind.*;

public class ScannerTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 *Retrieves the next token and checks that it is an EOF token. 
	 *Also checks that this was the last token.
	 *
	 * @param scanner
	 * @return the Token that was retrieved
	 */
	
	Token checkNextIsEOF(Scanner scanner) {
		Scanner.Token token = scanner.nextToken();
		assertEquals(Scanner.Kind.EOF, token.kind);
		assertFalse(scanner.hasTokens());
		return token;
	}


	/**
	 * Retrieves the next token and checks that its kind, position, length, line, and position in line
	 * match the given parameters.
	 * 
	 * @param scanner
	 * @param kind
	 * @param pos
	 * @param length
	 * @param line
	 * @param pos_in_line
	 * @return  the Token that was retrieved
	 */
	Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
		Token t = scanner.nextToken();
		assertEquals(scanner.new Token(kind, pos, length, line, pos_in_line), t);
		return t;
	}

	/**
	 * Retrieves the next token and checks that its kind and length match the given
	 * parameters.  The position, line, and position in line are ignored.
	 * 
	 * @param scanner
	 * @param kind
	 * @param length
	 * @return  the Token that was retrieved
	 */
	Token check(Scanner scanner, Scanner.Kind kind, int length) {
		Token t = scanner.nextToken();
		assertEquals(kind, t.kind);
		assertEquals(length, t.length);
		return t;
	}

	/**
	 * Simple test case with a (legal) empty program
	 *   
	 * @throws LexicalException
	 */
	@Test
	public void testEmpty() throws LexicalException {
		String input = "";  //The input is the empty string.  This is legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
	}
	
	@Test
	public void testSingle() throws LexicalException {
		String input = "@";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_AT, 0, 1, 1, 1);
		checkNextIsEOF(scanner);
	}
	@Test
	public void testDouble() throws LexicalException {
		String input = "==";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_EQ, 0, 2, 1, 1);
		checkNextIsEOF(scanner);
	}
	@Test
	public void testDigits() throws LexicalException {
		String input = "123";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 3, 1, 1);
		checkNextIsEOF(scanner);
	}
	@Test
	public void testIdentifier() throws LexicalException {
		String input = "/*the*/";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, OP_DIV, 0, 1, 1, 1);
		checkNext(scanner, OP_TIMES, 1, 1, 1, 2);
		checkNext(scanner, IDENTIFIER, 2, 3, 1, 3);
		checkNext(scanner, OP_TIMES, 5, 1, 1, 6);
		checkNext(scanner, OP_DIV, 6, 1, 1, 7);
		checkNextIsEOF(scanner);
	}
	@Test
	public void test8() throws LexicalException {
		String input = "abcwdh//kdjhfkjnksgd";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 6, 1, 1);
		checkNextIsEOF(scanner);
	}
	@Test
	public void test9() throws LexicalException {
		String input = "b\bab";
		
		show(input);
		
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			Scanner s = new Scanner(input).scan();
			show(s);
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(1,e.getPos());
			throw e;
		}
			}
	@Test
	public void test10() throws LexicalException {
		String input = "a\tabc";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_a, 0, 1, 1, 1);
		checkNext(scanner, IDENTIFIER, 2, 3, 1, 3);
		checkNextIsEOF(scanner);
	}
	@Test
	public void test11() throws LexicalException {
		String input = "0123";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
		checkNext(scanner, INTEGER_LITERAL, 1, 3, 1, 2);
		checkNextIsEOF(scanner);
	}
	@Test
	public void test12() throws LexicalException {
		String input = "\"(\"\\n\")\"";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(3,e.getPos());
			throw e;
		}
//		checkNext(scanner, INTEGER_LITERAL, 0, 1, 1, 1);
//		checkNext(scanner, INTEGER_LITERAL, 1, 3, 1, 2);
//		checkNextIsEOF(scanner);
	}
	@Test
	public void test13() throws LexicalException {
		String input = "x=\"\b\";";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, KW_x, 0, 1, 1, 1);
		checkNext(scanner, OP_ASSIGN, 1, 1, 1, 2);
		checkNext(scanner, STRING_LITERAL, 2, 3, 1, 3);
		checkNext(scanner, SEMI, 5, 1, 1, 6);
		checkNextIsEOF(scanner);
	}
	@Test
	public void test14() throws LexicalException {
		String input = "Test\rabc";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, IDENTIFIER, 0, 4, 1, 1);
		checkNext(scanner, IDENTIFIER, 5, 3, 2, 1);
		checkNextIsEOF(scanner);
	}
	@Test
	public void test15() throws LexicalException {
		String input = "\" greet\\ings\"";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {  //
            show(e);
            assertEquals(7,e.getPos());
            throw e;
        }
        
	}
	/**
	 * Test illustrating how to put a new line in the input program and how to
	 * check content of tokens.
	 * 
	 * Because we are using a Java String literal for input, we use \n for the
	 * end of line character. (We should also be able to handle \n, \r, and \r\n
	 * properly.)
	 * 
	 * Note that if we were reading the input from a file, as we will want to do 
	 * later, the end of line character would be inserted by the text editor.
	 * Showing the input will let you check your input is what you think it is.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void testSemi() throws LexicalException {
		String input = ";;\n;;";
		Scanner scanner = new Scanner(input).scan();
		show(input);
		show(scanner);
		checkNext(scanner, SEMI, 0, 1, 1, 1);
		checkNext(scanner, SEMI, 1, 1, 1, 2);
		checkNext(scanner, SEMI, 3, 1, 2, 1);
		checkNext(scanner, SEMI, 4, 1, 2, 2);
		checkNextIsEOF(scanner);
	}
	
	/**
	 * This example shows how to test that your scanner is behaving when the
	 * input is illegal.  In this case, we are giving it a String literal
	 * that is missing the closing ".  
	 * 
	 * Note that the outer pair of quotation marks delineate the String literal
	 * in this test program that provides the input to our Scanner.  The quotation
	 * mark that is actually included in the input must be escaped, \".
	 * 
	 * The example shows catching the exception that is thrown by the scanner,
	 * looking at it, and checking its contents before rethrowing it.  If caught
	 * but not rethrown, then JUnit won't get the exception and the test will fail.  
	 * 
	 * The test will work without putting the try-catch block around 
	 * new Scanner(input).scan(); but then you won't be able to check 
	 * or display the thrown exception.
	 * 
	 * @throws LexicalException
	 */
	@Test
	public void operatortest() throws LexicalException {
	    String input = "-><>,->:\n+***@!=! X x 078868 789 1 2 3 4 78318 138472        46 //dxjvbdjbvcodj\n daxcdx 5875 \"vscdvsc\" dsd$hdg_ atan \n ";
	    Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, OP_RARROW, 0, 2, 1, 1);
        checkNext(scanner, OP_LT,     2, 1, 1, 3);
        checkNext(scanner, OP_GT,     3, 1, 1, 4);
        checkNext(scanner, COMMA,     4, 1, 1, 5);
        checkNext(scanner, OP_RARROW, 5, 2, 1, 6);
        checkNext(scanner, OP_COLON,  7, 1, 1, 8);
        checkNext(scanner, OP_PLUS,   9, 1, 2, 1);
        checkNext(scanner, OP_POWER,  10, 2, 2, 2);
        checkNext(scanner, OP_TIMES,  12, 1, 2, 4);
        checkNext(scanner, OP_AT,     13, 1, 2, 5);
        checkNext(scanner, OP_NEQ,    14, 2, 2, 6);
        checkNext(scanner, OP_EXCL,   16, 1, 2, 8);
        checkNext(scanner, KW_X,      18, 1, 2, 10);
        checkNext(scanner, KW_x,      20, 1, 2, 12);
        checkNext(scanner, INTEGER_LITERAL,   22, 1, 2, 14);
        checkNext(scanner, INTEGER_LITERAL,   23, 5, 2, 15);
        checkNext(scanner, INTEGER_LITERAL,   29, 3, 2, 21);
        checkNext(scanner, INTEGER_LITERAL,   33, 1, 2, 25);
        checkNext(scanner, INTEGER_LITERAL,   35, 1, 2, 27);
        checkNext(scanner, INTEGER_LITERAL,   37, 1, 2, 29);
        checkNext(scanner, INTEGER_LITERAL,   39, 1, 2, 31);
        checkNext(scanner, INTEGER_LITERAL,   41, 5, 2, 33);
        checkNext(scanner, INTEGER_LITERAL,   47, 6, 2, 39);
        checkNext(scanner, INTEGER_LITERAL,   61, 2, 2, 53);
        checkNext(scanner, IDENTIFIER,        81, 6, 3, 2);
        checkNext(scanner, INTEGER_LITERAL,   88, 4, 3, 9);
        checkNext(scanner, STRING_LITERAL,    93,9,3,14);
        checkNext(scanner, IDENTIFIER,        103,8,3,24);
        checkNext(scanner, KW_atan,           112,4,3,33);
        
        
        checkNextIsEOF(scanner);
	    
	}
	@Test
    public void extraCharTest() throws LexicalException {
        String input = "abc def/n345 #abc ";
        show(input);
        thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {  //
            show(e);
            assertEquals(13,e.getPos());
            throw e;
        }
        
    }
	@Test
    public void testing() throws LexicalException {
        String input = "99999999999999999";
        show(input);
        thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
        try {
            new Scanner(input).scan();
        } catch (LexicalException e) {  //
            show(e);
            assertEquals(17,e.getPos());
            throw e;
        }
        
    }
	@Test
    public void digittest() throws LexicalException {
        String input = "123()+4+54321  true file abs polar_a boolean ";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 3, 1, 1);
        checkNext(scanner, LPAREN,     3, 1, 1, 4);
        checkNext(scanner, RPAREN,     4, 1, 1, 5);
        checkNext(scanner, OP_PLUS,     5, 1, 1, 6);
        checkNext(scanner, INTEGER_LITERAL, 6, 1, 1, 7);
        checkNext(scanner, OP_PLUS,  7, 1, 1, 8);
        checkNext(scanner, INTEGER_LITERAL,   8, 5, 1, 9);
        checkNext(scanner, BOOLEAN_LITERAL,   15, 4, 1, 16);
        checkNext(scanner, KW_file,   20, 4, 1, 21);
        checkNext(scanner, KW_abs,   25, 3, 1, 26);
        checkNext(scanner, KW_polar_a,   29, 7, 1, 30);
        checkNext(scanner, KW_boolean,   37, 7, 1, 38);
        
        
    }
	@Test
    public void escapesequences() throws LexicalException {
        String input = "123()+4+54321  true file abs polar_a boolean  image\r\n image \n cart_y \n file \"cdvda\" ";
        Scanner scanner = new Scanner(input).scan();
        show(input);
        show(scanner);
        checkNext(scanner, INTEGER_LITERAL, 0, 3, 1, 1);
        checkNext(scanner, LPAREN,          3, 1, 1, 4);
        checkNext(scanner, RPAREN,          4, 1, 1, 5);
        checkNext(scanner, OP_PLUS,         5, 1, 1, 6);
        checkNext(scanner, INTEGER_LITERAL, 6, 1, 1, 7);
        checkNext(scanner, OP_PLUS,         7, 1, 1, 8);
        checkNext(scanner, INTEGER_LITERAL, 8, 5, 1, 9);
        checkNext(scanner, BOOLEAN_LITERAL, 15, 4, 1, 16);
        checkNext(scanner, KW_file,         20, 4, 1, 21);
        checkNext(scanner, KW_abs,          25, 3, 1, 26);
        checkNext(scanner, KW_polar_a,      29, 7, 1, 30);
        checkNext(scanner, KW_boolean,      37, 7, 1, 38);
        
        checkNext(scanner, KW_image,        46, 5, 1, 47);
        checkNext(scanner, KW_image,        54, 5, 2, 2);
        checkNext(scanner, KW_cart_y,       62, 6, 3, 2);
        checkNext(scanner, KW_file,         71, 4, 4, 2);
        checkNext(scanner, STRING_LITERAL,  76, 7, 4, 7);
        
        
        
    }
	@Test
	public void failUnclosedStringLiteral() throws LexicalException {
		String input = " \" greetings  ";
		show(input);
		thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
		try {
			new Scanner(input).scan();
		} catch (LexicalException e) {  //
			show(e);
			assertEquals(13,e.getPos());
			throw e;
		}
	}


}
