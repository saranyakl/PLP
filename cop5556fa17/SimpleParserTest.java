package cop5556fa17;

import static org.junit.Assert.*;
import java.util.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.Token;
import cop5556fa17.SimpleParser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class SimpleParserTest {

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
	 * Simple test case with an empty program.  This test 
	 * expects an SyntaxException because all legal programs must
	 * have at least an identifier
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  This is not legal
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);  //Create a parser
		thrown.expect(SyntaxException.class);
		try {
		parser.parse();  //Parse the program
		}
		catch (SyntaxException e) {
			show(e);
			throw e;
		}
	}

	
	/** Another example.  This is a legal program and should pass when 
	 * your parser is implemented.
	 * 
	 * @throws LexicalException
	 * @throws SyntaxException
	 */

	@Test
	public void testDec1() throws LexicalException, SyntaxException {
		String input = "prog int k;";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);  //
		parser.parse();
	}
	
	@Test
	public void testraSelector() throws LexicalException, SyntaxException {
		String input = "r,A";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);  //
		parser.raSelector();
	}
	
	@Test
	public void testvarType() throws LexicalException, SyntaxException {
		String input = "prog int i=1;";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);  //
		parser.parse();
	}

	@Test
	public void testrdisc() throws LexicalException, SyntaxException {
		String input = "++++res";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);  
		parser.expression();
	}
	
	@Test
	public void testlhs() throws LexicalException, SyntaxException {
		String input = "prog var[[x,y]]=5;";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);  //
		parser.parse();
	}
	@Test
	public void testbool() throws LexicalException, SyntaxException {
		String input = "prog boolean val = false;";
		show(input);
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);  //
		parser.parse();
	}
/**
	 * This example invokes the method for expression directly. 
	 * Effectively, we are viewing Expression as the start
	 * symbol of a sub-language.
	 *  
	 * Although a compiler will always call the parse() method,
	 * invoking others is useful to support incremental development.  
	 * We will only invoke expression directly, but 
	 * following this example with others is recommended.  
	 * 
	 * @throws SyntaxException
	 * @throws LexicalException
	 */
	@Test
	public void expression1() throws SyntaxException, LexicalException {
		String input = "2";
		show(input);
		Scanner scanner = new Scanner(input).scan();  
		show(scanner);   
		SimpleParser parser = new SimpleParser(scanner);  
		parser.expression();  //Call expression directly.  
	}

	//positive test cases
		@Test
	    public void statement() throws LexicalException, SyntaxException {
	        String input = "prog sai [[x,y]] = (67/x*y);"
	                + "sgf[[r,A]] = 534657+x ;"
	                + "nath -> screen ;"
	                + "gsf ->vfgh ;"
	                + "reddy <- @x+y ;"
	                + "dtgbg = u[534/x,67+y];";
	        show(input);
	        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
	        //show(scanner);   //Display the Scanner
	        SimpleParser parser = new SimpleParser(scanner);  //
	        parser.parse();
	    }
		@Test
	    public void selector() throws LexicalException, SyntaxException {
	        String input = "prog int i;intj=1;i=1==2;";
	        show(input);
	        Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
	        //show(scanner);   //Display the Scanner
	        SimpleParser parser = new SimpleParser(scanner);
	        parser.program();
//	        thrown.expect(SyntaxException.class);
//			try {
//			parser.selector();  //Parse the program
//			}
//			catch (SyntaxException e) {
//				show(e);
//				throw e;
//			}
	    }
		
		

	
	}

