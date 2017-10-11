package cop5556fa17;



//import java.util.Arrays;
import java.util.*;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
import cop5556fa17.Parser.SyntaxException;
import cop5556fa17.AST.*;

import static cop5556fa17.Scanner.Kind.*;

public class Parser {

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

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * Main method called by compiler to parser input.
	 * Checks for EOF
	 * 
	 * @throws SyntaxException
	 */
	public Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}
	

	/**
	 * Program ::=  IDENTIFIER   ( Declaration SEMI | Statement SEMI )*   
	 * 
	 * Program is start symbol of our grammar.
	 * 
	 * @throws SyntaxException
	 */
	
	Program program() throws SyntaxException {
		Token temp = t;
		match(Kind.IDENTIFIER);
		ArrayList<ASTNode> list = new ArrayList<>();
		while(t.kind==Kind.IDENTIFIER || t.kind==Kind.KW_int || t.kind==Kind.KW_boolean || t.kind==Kind.KW_image 
				|| t.kind==Kind.KW_url || t.kind==Kind.KW_file) {
			if(t.kind==Kind.IDENTIFIER) {
				list.add(statement());
				match(Kind.SEMI);
			}
			else {
				list.add(declaration());
				match(Kind.SEMI);
			}
		}
		return new Program(temp,temp,list);
	}

	/**
	 * Expression ::=  OrExpression  OP_Q  Expression OP_COLON Expression    | OrExpression
	 * 
	 * Our test cases may invoke this routine directly to support incremental development.
	 * 
	 * @throws SyntaxException
	 */
	Expression expression() throws SyntaxException {
		Token temp = t;
		Expression e0 = null;
		Expression e1 = null;
		Expression e2 = null;
		e0 = orExpression();
		if(t.kind==Kind.OP_Q) {
			consume();
			e1 = expression();
			match(Kind.OP_COLON);
			e2 = expression();
			e0 = new Expression_Conditional(temp,e0,e1,e2);
		}
		return e0;
	}
	
	Declaration declaration() throws SyntaxException {
		if(t.kind==Kind.KW_int || t.kind==Kind.KW_boolean) {
			return variableDeclaration();
		}
		else if(t.kind==Kind.KW_image) {
			return imageDeclaration();
		}
		else if(t.kind==Kind.KW_url || t.kind==Kind.KW_file) {
			return sourceSinkDeclaration();
		}
		else
			throw new SyntaxException(t,"From declaration: Wrong keyword "+t.kind);
	}
	
	Declaration_Variable variableDeclaration() throws SyntaxException {
		Token temp = t;
		if(t.kind==Kind.KW_int || t.kind==Kind.KW_boolean) {
			Token type = t;
			consume();
			Token name = t;
			match(Kind.IDENTIFIER);
			if(t.kind==Kind.OP_ASSIGN) {
				consume();
				Expression e = expression();
				return new Declaration_Variable(temp, type, name, e);
			}
			else
				return new Declaration_Variable(temp, type, name, null);
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
	
	Declaration_SourceSink sourceSinkDeclaration() throws SyntaxException {
		Token temp = t;
		sourceSinkType();
		Token name = t;
		match(Kind.IDENTIFIER);
		Token type = t;
		match(Kind.OP_ASSIGN);
		Source source = source();
		return new Declaration_SourceSink(temp, type, name, source);
	}
	
	Source source() throws SyntaxException {
		Token temp = t;
		if(t.kind == Kind.STRING_LITERAL) {
			String st = t.getText();
			consume();
			return new Source_StringLiteral(temp,st);
		}
		else if(t.kind == Kind.IDENTIFIER) {
			Token name = t;
			consume();
			return new Source_Ident(temp,name);
		}
		else if(t.kind==Kind.OP_AT) {
			consume();
			Expression e = expression();
			return new Source_CommandLineParam(temp,e);
		}
		else
			throw new SyntaxException(t, "From source: wrong keyword, "+t.kind);
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
	
	Declaration_Image imageDeclaration() throws SyntaxException {
		Token temp = t;
		match(Kind.KW_image);
		Expression e0 = null,e1 = null;
		Source s = null;
		if(t.kind==Kind.LSQUARE) {
			consume();
			e0 = expression();
			match(Kind.COMMA);
			e1 = expression();
			match(Kind.RSQUARE);
		}
		Token name = t;
		match(Kind.IDENTIFIER);
		if(t.kind==Kind.OP_LARROW) {
			consume();
			s = source();
		}
		return new Declaration_Image(temp,e0,e1,name,s);
	}
	
	Statement statement() throws SyntaxException {
		Token name = t;
		match(Kind.IDENTIFIER);
		if(t.kind==Kind.OP_LARROW)
			return imageInStatement(name);
		else if(t.kind==Kind.OP_RARROW) {
			return imageOutStatement(name);
		}
		else
			return assignmentStatement(name);
	}
		
	Statement_Out imageOutStatement(Token name) throws SyntaxException{
//		match(Kind.IDENTIFIER);
		match(Kind.OP_RARROW);
		Sink s = sink();
		return new Statement_Out(name,name,s);
	}
	
	Sink sink() throws SyntaxException {
		Token temp = t;
		if(t.kind == Kind.IDENTIFIER) {
			Token name = t;
			consume();
			return new Sink_Ident(temp,name);
		}
		else if(t.kind == Kind.KW_SCREEN) {
			consume();
			return new Sink_SCREEN(temp);
		}
		else
			throw new SyntaxException(t, "From sink: wrong keyword, "+t.kind);
	}
	
	Statement_In imageInStatement(Token name) throws SyntaxException{
//		match(Kind.IDENTIFIER);
		match(Kind.OP_LARROW);
		Source s = source();
		return new Statement_In(name, name, s);
	}
	
	Statement_Assign assignmentStatement(Token name) throws SyntaxException {
		LHS l = lhs(name);
		match(Kind.OP_ASSIGN);
		Expression e = expression();
		return new Statement_Assign(name,l,e);
	}
	
	Expression orExpression() throws SyntaxException {
		Token temp = t;
		Expression e0 = null, e1=null;
		e0 = andExpression();
		while(t.kind==Kind.OP_OR) {
			Token op = t;
			consume();
			e1 = andExpression();
			e0 = new Expression_Binary(temp,e0,op,e1);
		}
		return e0;
	}
	
	Expression andExpression() throws SyntaxException {
		Token temp = t;
		Expression e0 = null, e1=null;
		e0 = eqExpression();
		while(t.kind==Kind.OP_AND) {
			Token op = t;
			consume();
			e1 = eqExpression();
			e0 = new Expression_Binary(temp,e0,op,e1);
		}
		return e0;
	}
	
	Expression eqExpression() throws SyntaxException {
		Token temp = t;
		Expression e0 = null,e1=null;
		e0 = relExpression();
		while(t.kind==Kind.OP_EQ || t.kind==Kind.OP_NEQ) {
			Token op = t;
			consume();
			e1 = relExpression();
			e0 = new Expression_Binary(temp, e0, op, e1);
		}
		return e0;
	}
	
	Expression relExpression() throws SyntaxException {
		Token temp = t;
		Expression e0 = null,e1=null;
		e0 = addExpression();
		while(t.kind==Kind.OP_LT || t.kind==Kind.OP_GT || t.kind==Kind.OP_LE || t.kind==Kind.OP_GE) {
			Token op = t;
			consume();
			e1 = addExpression();
			e0 = new Expression_Binary(temp, e0, op, e1);
		}
		return e0;
	}
	
	Expression addExpression() throws SyntaxException {
		Token temp = t;
		Expression e0 = null,e1=null;
		e0 = multExpression();
		while(t.kind==Kind.OP_PLUS || t.kind==Kind.OP_MINUS) {
			Token op = t;
			consume();
			e1 = multExpression();
			e0 = new Expression_Binary(temp, e0, op, e1);
		}
		return e0;
	}
	
	Expression multExpression() throws SyntaxException {
		Token temp = t;
		Expression e0 = null,e1=null;
		e0 = unaryExpression();
		while(t.kind==Kind.OP_TIMES || t.kind==Kind.OP_DIV || t.kind==Kind.OP_MOD) {
			Token op = t;
			consume();
			e1 = unaryExpression();
			e0 = new Expression_Binary(temp, e0, op, e1);
		}
		return e0;
	}
	
	Expression unaryExpression() throws SyntaxException {
		Token temp = t;
		if(t.kind==Kind.OP_PLUS || t.kind==Kind.OP_MINUS) {
			consume();
			Expression e1 = unaryExpression();
			return new Expression_Unary(temp, temp, e1);
		}
		else
			return unaryExpressionNotPlusMinus();
	}
	
	Expression unaryExpressionNotPlusMinus() throws SyntaxException {
		Token temp = t;
		switch(t.kind) {
		case OP_EXCL:
			consume();
			return unaryExpression();
		case IDENTIFIER:
			return identOrPixelSelectorExpression();
		case KW_x:case KW_y:case KW_r:case KW_a:case KW_X:case KW_Y:case KW_Z:case KW_A:case KW_R:case KW_DEF_X:case KW_DEF_Y:
			Kind k = temp.kind;
			consume();
			return new Expression_PredefinedName(temp,k);
		default:
			return primary();
		}
		
	}
	
	Expression primary() throws SyntaxException {
		Token temp = t;
		Expression e0 = null;
		if(t.kind==Kind.INTEGER_LITERAL) {
			consume();
			int i = temp.intVal();
			e0 = new Expression_IntLit(temp,i);
		}
		else if(t.kind==Kind.LPAREN) {
			consume();
			expression();
			match(Kind.RPAREN);
		}
		else if(t.kind==Kind.BOOLEAN_LITERAL) {
			consume();
			String str = temp.getText();
			if(str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
				boolean val = Boolean.valueOf(str);
				e0 = new Expression_BooleanLit(temp, val);
			}
		}
		else
			functionApplication();
		return e0;
	}
	
	Expression identOrPixelSelectorExpression() throws SyntaxException {
		Token temp = t;
		match(Kind.IDENTIFIER);
		if(t.kind==Kind.LSQUARE) {
			consume();
			Index i = selector();
			match(Kind.RSQUARE);
			return new Expression_PixelSelector(temp,temp,i);
		}
		else
			return new Expression_Ident(temp,temp);
	}
	
	LHS lhs(Token name) throws SyntaxException {
//		match(Kind.IDENTIFIER);
		Index i = null;
		if(t.kind==Kind.LSQUARE) {
			consume();
			i = lhsSelector();
			match(Kind.RSQUARE);
		}
		return new LHS(name, name, i);
	}
	
	Expression functionApplication() throws SyntaxException {
		Token temp = t;
		functionName();
		if(t.kind==Kind.LPAREN) {
			Kind k = t.kind;
			consume();
			Expression e = expression();
			match(Kind.RPAREN);
			return new Expression_FunctionAppWithExprArg(temp,k,e);
		}
		else if(t.kind==Kind.LSQUARE) {
			Kind ki = t.kind;
			consume();
			Index i = selector();
			match(Kind.RSQUARE);
			return new Expression_FunctionAppWithIndexArg(temp,ki,i);
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
	
	Index lhsSelector() throws SyntaxException {
		Index i = null;
		match(Kind.LSQUARE);
		{
			if(t.kind==Kind.KW_x)
				i = xySelector();
			else if(t.kind==Kind.KW_r)
				i = raSelector();
			else
				throw new SyntaxException(t,"From lhsSelector: wrong keyword "+t.kind);
		}
		match(Kind.RSQUARE);
		return i;
	}
	
	Index xySelector() throws SyntaxException {
		Token temp = t;
		Token kwx = t;
		match(Kind.KW_x);
		Expression e0 = new Expression_PredefinedName(kwx,Kind.KW_x);
		match(Kind.COMMA);
		Token kwy = t;
		match(Kind.KW_y);
		Expression e1 = new Expression_PredefinedName(kwy,Kind.KW_y);
		return new Index(temp,e0,e1);
	}
	
	Index raSelector() throws SyntaxException {
		Token temp = t;
		Token kwr = t;
		Expression e0 = new Expression_PredefinedName(kwr,Kind.KW_r);
		match(Kind.KW_r);
		match(Kind.COMMA);
		Token kwa = t;
		match(Kind.KW_A);
		Expression e1 = new Expression_PredefinedName(kwa,Kind.KW_A);
		return new Index(temp,e0,e1);
	}
	
	Index selector() throws SyntaxException {
		Token temp = t;
		Expression e0 = expression();
		match(Kind.COMMA);
		Expression e1 = expression();
		return new Index(temp,e0,e1);
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
