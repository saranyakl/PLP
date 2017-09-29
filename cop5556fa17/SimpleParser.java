package cop5556fa17;



import java.util.Arrays;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.SimpleParser.SyntaxException;

import static cop5556fa17.Scanner.Kind.*;

public class SimpleParser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}


	Scanner scanner;
	Token t;

	SimpleParser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public void parse() throws SyntaxException {
		program();
		matchEOF();
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	void program() throws SyntaxException {
		match(Kind.IDENTIFIER);
		while(t.kind==Kind.IDENTIFIER || t.kind==Kind.KW_int || t.kind==Kind.KW_boolean || t.kind==Kind.KW_image 
				|| t.kind==Kind.KW_url || t.kind==Kind.KW_file) {
			if(t.kind==Kind.IDENTIFIER) {
				statement();
				match(Kind.SEMI);
			}
			else {
				declaration();
				match(Kind.SEMI);
			}
		}
	}

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	void expression() throws SyntaxException {
		orExpression();
		if(t.kind==Kind.OP_Q) {
			consume();
			expression();
			match(Kind.OP_COLON);
			expression();
		}
	}
	
	void declaration() throws SyntaxException {
		if(t.kind==Kind.KW_int || t.kind==Kind.KW_boolean) {
			variableDeclaration();
		}
		else if(t.kind==Kind.KW_image) {
			imageDeclaration();
		}
		else if(t.kind==Kind.KW_url || t.kind==Kind.KW_file) {
			sourceSinkDeclaration();
		}
		else
			throw new SyntaxException(t,"From declaration: Wrong keyword "+t.kind);
	}
	
	void variableDeclaration() throws SyntaxException {
		if(t.kind==Kind.KW_int || t.kind==Kind.KW_boolean) {
			consume();
			match(Kind.IDENTIFIER);
			if(t.kind==Kind.OP_ASSIGN) {
				consume();
				expression();
			}
		}
		else
			throw new SyntaxException(t,"From variableDeclaration: wrond keyword "+t.kind);
	}
	
	void varType() throws SyntaxException {
		if(t.kind == Kind.KW_int)
			consume();
		else if(t.kind == KW_boolean)
			consume();
		else
			throw new SyntaxException(t, "From vartype: wrong keyword, "+t.kind);
		return;
	}
	
	void sourceSinkDeclaration() throws SyntaxException {
		sourceSinkType();
		match(Kind.IDENTIFIER);
		match(Kind.OP_ASSIGN);
		source();
	}
	
	void source() throws SyntaxException {
		if(t.kind == Kind.STRING_LITERAL)
			consume();
		else if(t.kind == Kind.IDENTIFIER)
			consume();
		else if(t.kind==Kind.OP_AT) {
			consume();
			expression();
		}
		else
			throw new SyntaxException(t, "From source: wrong keyword, "+t.kind);
		return;
	}
	
	void sourceSinkType() throws SyntaxException {
		if(t.kind == Kind.KW_url)
			consume();
		else if(t.kind == Kind.KW_file)
			consume();
		else
			throw new SyntaxException(t, "From sourcesinktype: wrong keyword, "+t.kind);
		return;
	}
	
	void imageDeclaration() throws SyntaxException {
		match(Kind.KW_image);
		if(t.kind==Kind.LSQUARE) {
			consume();
			expression();
			match(Kind.COMMA);
			expression();
			match(Kind.RSQUARE);
		}
		match(Kind.IDENTIFIER);
		if(t.kind==Kind.OP_LARROW) {
			consume();
			source();
		}
	}
	
	void statement() throws SyntaxException {
		match(Kind.IDENTIFIER);
		if(t.kind==Kind.OP_LARROW)
			imageInStatement();
		else if(t.kind==Kind.OP_RARROW)
			imageOutStatement();
		else
			assignmentStatement();
	}
		
	void imageOutStatement() throws SyntaxException{
//		match(Kind.IDENTIFIER);
		match(Kind.OP_RARROW);
		sink();
	}
	
	void sink() throws SyntaxException {
		if(t.kind == Kind.IDENTIFIER) {
			//TODO: Check if ident is a file
			consume();
		}
		else if(t.kind == Kind.KW_SCREEN)
			consume();
		else
			throw new SyntaxException(t, "From sink: wrong keyword, "+t.kind);
		return;
	}
	
	void imageInStatement() throws SyntaxException{
//		match(Kind.IDENTIFIER);
		match(Kind.OP_LARROW);
		source();
	}
	
	void assignmentStatement() throws SyntaxException {
		lhs();
		match(Kind.OP_ASSIGN);
		expression();
	}
	
	void orExpression() throws SyntaxException {
		andExpression();
		while(t.kind==Kind.OP_OR) {
			consume();
			andExpression();
		}
	}
	
	void andExpression() throws SyntaxException {
		eqExpression();
		while(t.kind==Kind.OP_AND) {
			consume();
			eqExpression();
		}
	}
	
	void eqExpression() throws SyntaxException {
		relExpression();
		while(t.kind==Kind.OP_EQ || t.kind==Kind.OP_NEQ) {
			consume();
			relExpression();
		}
	}
	
	void relExpression() throws SyntaxException {
		addExpression();
		while(t.kind==Kind.OP_LT || t.kind==Kind.OP_GT || t.kind==Kind.OP_LE || t.kind==Kind.OP_GE) {
			consume();
			addExpression();
		}
	}
	
	void addExpression() throws SyntaxException {
		multExpression();
		while(t.kind==Kind.OP_PLUS || t.kind==Kind.OP_MINUS) {
			consume();
			multExpression();
		}
	}
	
	void multExpression() throws SyntaxException {
		unaryExpression();
		while(t.kind==Kind.OP_TIMES || t.kind==Kind.OP_DIV || t.kind==Kind.OP_MOD) {
			consume();
			unaryExpression();
		}
	}
	
	void unaryExpression() throws SyntaxException {
		if(t.kind==Kind.OP_PLUS || t.kind==Kind.OP_MINUS) {
			consume();
			unaryExpression();
		}
		else
			unaryExpressionNotPlusMinus();
	}
	
	void unaryExpressionNotPlusMinus() throws SyntaxException {
		switch(t.kind) {
		case OP_EXCL:
			consume();
			unaryExpression();
			break;
		case IDENTIFIER:
			identOrPixelSelectorExpression();
			break;
		case KW_x:case KW_y:case KW_r:case KW_a:case KW_X:case KW_Y:case KW_Z:case KW_A:case KW_R:case KW_DEF_X:case KW_DEF_Y:
			consume();
			break;
		default:
			primary();
		}
	}
	
	void primary() throws SyntaxException {
		if(t.kind==Kind.INTEGER_LITERAL)
			consume();
		else if(t.kind==Kind.LPAREN) {
			consume();
			expression();
			match(Kind.RPAREN);
		}
		else if(t.kind==Kind.BOOLEAN_LITERAL)
			consume();
		else
			functionApplication();
	}
	
	void identOrPixelSelectorExpression() throws SyntaxException {
		match(Kind.IDENTIFIER);
		if(t.kind==Kind.LSQUARE) {
			consume();
			selector();
			match(Kind.RSQUARE);
		}
	}
	
	void lhs() throws SyntaxException {
//		match(Kind.IDENTIFIER);
		if(t.kind==Kind.LSQUARE) {
			consume();
			lhsSelector();
			match(Kind.RSQUARE);
		}
	}
	
	void functionApplication() throws SyntaxException {
		functionName();
		if(t.kind==Kind.LPAREN) {
			consume();
			expression();
			match(Kind.RPAREN);
		}
		else if(t.kind==Kind.LSQUARE) {
			consume();
			selector();
			match(Kind.RSQUARE);
		}
		else
			throw new SyntaxException(t,"From functionApplication: wrong keyword "+t.kind);
	}
	
	void functionName() throws SyntaxException {
		switch(t.kind) {
		case KW_sin:
			consume();
			break;
		case KW_cos:
			consume();
			break;
		case KW_atan:
			consume();
			break;
		case KW_abs:
			consume();
			break;
		case KW_cart_x:
			consume();
			break;
		case KW_cart_y:
			consume();
			break;
		case KW_polar_a:
			consume();
			break;
		case KW_polar_r:
			consume();
			break;
		default:
			throw new SyntaxException(t,"From functionName: wrong keyword, "+t.kind);
		}
	}
	
	void lhsSelector() throws SyntaxException {
		match(Kind.LSQUARE);
		{
			if(t.kind==Kind.KW_x)
				xySelector();
			else if(t.kind==Kind.KW_r)
				raSelector();
			else
				throw new SyntaxException(t,"From lhsSelector: wrong keyword "+t.kind);
		}
		match(Kind.RSQUARE);
	}
	
	void xySelector() throws SyntaxException {
		match(Kind.KW_x);
		match(Kind.COMMA);
		match(Kind.KW_y);
	}
	
	void raSelector() throws SyntaxException {
		match(Kind.KW_r);
		match(Kind.COMMA);
		match(Kind.KW_A);
	}
	
	void selector() throws SyntaxException {
		expression();
		match(Kind.COMMA);
		expression();
	}
	
	//match function
	private void match(Kind kind) throws SyntaxException {
		if(t.kind == kind){
			consume();
		}
		else
			throw new SyntaxException(t, "Match: Expected "+kind+" but saw "+t.kind );
	}
	
	//consume function
	private Token consume() throws SyntaxException{
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}
	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind == EOF) {
			return t;
		}
		String message =  "Expected EOL at " + t.line + ":" + t.pos_in_line;
		throw new SyntaxException(t, message);
	}
}
