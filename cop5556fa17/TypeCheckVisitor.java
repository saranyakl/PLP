package cop5556fa17;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.Scanner.Token;
//import jdk.internal.dynalink.support.TypeUtilities;

import java.net.URL;
import java.util.HashMap;
//import android.webkit.URLUtil;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression_Binary;
import cop5556fa17.AST.Expression_BooleanLit;
import cop5556fa17.AST.Expression_Conditional;
import cop5556fa17.AST.Expression_FunctionAppWithExprArg;
import cop5556fa17.AST.Expression_FunctionAppWithIndexArg;
import cop5556fa17.AST.Expression_Ident;
import cop5556fa17.AST.Expression_IntLit;
import cop5556fa17.AST.Expression_PixelSelector;
import cop5556fa17.AST.Expression_PredefinedName;
import cop5556fa17.AST.Expression_Unary;
import cop5556fa17.AST.Index;
import cop5556fa17.AST.LHS;
import cop5556fa17.AST.Program;
import cop5556fa17.AST.Sink_Ident;
import cop5556fa17.AST.Sink_SCREEN;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.TypeUtils.Type;

public class TypeCheckVisitor implements ASTVisitor {
	

		@SuppressWarnings("serial")
		public static class SemanticException extends Exception {
			Token t;

			public SemanticException(Token t, String message) {
				super("line " + t.line + " pos " + t.pos_in_line + ": "+  message);
				this.t = t;
			}

		}		
		HashMap<String,Declaration> symbolTable = new HashMap<String,Declaration>();

	
	/**
	 * The program name is only used for naming the class.  It does not rule out
	 * variables with the same name.  It is returned for convenience.
	 * 
	 * @throws Exception 
	 */
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		for (ASTNode node: program.decsAndStatements) {
			node.visit(this, arg);
		}
		return program.name;
	}

	@Override
	public Object visitDeclaration_Variable(
			Declaration_Variable declaration_Variable, Object arg)
			throws Exception {
		if(symbolTable.containsKey(declaration_Variable.name))
			throw new SemanticException(declaration_Variable.firstToken, "From Declaration_Variable, name already exists");
		declaration_Variable.ntype = TypeUtils.getType(declaration_Variable.firstToken);
		if(declaration_Variable.e!=null) {
			Type et = (Type) declaration_Variable.e.visit(this, null);
			if(declaration_Variable.ntype != et) {
				throw new SemanticException(declaration_Variable.firstToken, "From declaration_Variable, type not matching");
			}
		}
		symbolTable.put(declaration_Variable.name, declaration_Variable);
		return declaration_Variable.ntype;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary,
			Object arg) throws Exception {	
//		if(expression_Binary.e0!=null && expression_Binary.e1!=null) {
			Type e0t = (Type) expression_Binary.e0.visit(this, null);
			Type e1t = (Type) expression_Binary.e1.visit(this, null);
			switch(expression_Binary.op) {
			case OP_EQ:case OP_NEQ:
				expression_Binary.ntype = Type.BOOLEAN;
				break;
			case OP_GE:case OP_GT:case OP_LE:case OP_LT:
				if(e0t==Type.INTEGER)
					expression_Binary.ntype = Type.BOOLEAN;
				break;
			case OP_AND:case OP_OR:
				if(e0t==Type.INTEGER || e0t==Type.BOOLEAN) {
					expression_Binary.ntype = e0t;
				}
				break;
			case OP_DIV:case OP_MINUS:case OP_MOD:case OP_PLUS:case OP_POWER:case OP_TIMES:
				if(e0t==Type.INTEGER) {
					expression_Binary.ntype = Type.INTEGER;
				}
				break;
			default:
				break;
//				expression_Binary.ntype = Type.NONE;
		}
			if(e0t==e1t) {
				
			}
			else {
				throw new SemanticException(expression_Binary.firstToken,"expression_Binary, require failed");
			}
//		}
		if(expression_Binary.ntype == Type.NONE)
			throw new SemanticException(expression_Binary.firstToken,"expression_Binary, require statement failed");
		return expression_Binary.ntype;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary,
			Object arg) throws Exception {
//		if(expression_Unary.e!=null) {
			Type et = (Type) expression_Unary.e.visit(this, null);
			switch(expression_Unary.op) {
			case OP_EXCL:
				if(et==Type.BOOLEAN || et==Type.INTEGER) {
					expression_Unary.ntype = et;
				}
				break;
			case OP_MINUS: case OP_PLUS:
				if(et==Type.INTEGER) {
					expression_Unary.ntype = Type.INTEGER;
				}
				break;
			default:
				break;
			}
//		}
		if(expression_Unary.ntype==Type.NONE)
			throw new SemanticException(expression_Unary.firstToken, "From Expression_Unary, require failed");
		return expression_Unary.ntype;
	}

	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
//		if(index.e0!=null && index.e1!=null) {
			Type e0t = (Type) index.e0.visit(this, null);
			Type e1t = (Type) index.e1.visit(this, null);
			if(e0t==Type.INTEGER && e1t==Type.INTEGER) {
				index.setCartesian(!(index.e0.firstToken.kind == Kind.KW_r && index.e1.firstToken.kind == Kind.KW_A));
			}
			else
				throw new SemanticException(index.firstToken,"From Index, require failed");
			
//		}
//		return index.ntype;
		return index.isCartesian();
	}

	@Override
	public Object visitExpression_PixelSelector(
			Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		if(expression_PixelSelector.index!=null) {
			expression_PixelSelector.index.visit(this, null);
		}
		if(!symbolTable.containsKey(expression_PixelSelector.name)) {
			throw new SemanticException(expression_PixelSelector.firstToken,"From expression_PixelSelector, name doesnot exist");
		}
		Type nametype = symbolTable.get(expression_PixelSelector.name).ntype;
		if(nametype==Type.IMAGE)
			expression_PixelSelector.ntype = Type.INTEGER;
		else if(expression_PixelSelector.index==null) {
			expression_PixelSelector.ntype = nametype;
		}
		
		if(expression_PixelSelector.ntype==Type.NONE)
			throw new SemanticException(expression_PixelSelector.firstToken, "From expression_PixelSelector, require failed");
		return expression_PixelSelector.ntype;
	}

	@Override
	public Object visitExpression_Conditional(
			Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
//		if(expression_Conditional.condition!=null && expression_Conditional.falseExpression!=null 
//				&& expression_Conditional.trueExpression!=null) {
			Type ec = (Type) expression_Conditional.condition.visit(this, null);
			Type et = (Type) expression_Conditional.trueExpression.visit(this, null);
			Type ef = (Type) expression_Conditional.falseExpression.visit(this, null);
			expression_Conditional.ntype = et;
			if(ec==Type.BOOLEAN && et==ef) {
				
			}
			else
				throw new SemanticException(expression_Conditional.firstToken,"From expression_Conditional, require failed");
//		}
		return expression_Conditional.ntype;
	}

	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image,
			Object arg) throws Exception {
		if(symbolTable.containsKey(declaration_Image.name))
			throw new SemanticException(declaration_Image.firstToken, "From declaration_Image, name already exists");
		
		if(declaration_Image.xSize!=null) {
			Type xt = (Type) declaration_Image.xSize.visit(this, null);
			Type yt = Type.NONE;
			if(declaration_Image.ySize==null) {
				throw new SemanticException(declaration_Image.firstToken, "From declaration_Image, require failed");
			}
			else {
				yt = (Type) declaration_Image.ySize.visit(this, null);
			}
			if(xt!=Type.INTEGER && yt!=Type.INTEGER) {
				throw new SemanticException(declaration_Image.firstToken, "From declaration_Image, require failed");
			}
		}
		if(declaration_Image.source!=null) {
			declaration_Image.source.visit(this, null);
		}
		declaration_Image.ntype = Type.IMAGE;
		symbolTable.put(declaration_Image.name, declaration_Image);
		return declaration_Image.ntype;
	}

	@Override
	public Object visitSource_StringLiteral(
			Source_StringLiteral source_StringLiteral, Object arg)
			throws Exception {
		if(isValidURL(source_StringLiteral.fileOrUrl)) {
			source_StringLiteral.ntype = Type.URL;
		}
		else
			source_StringLiteral.ntype = Type.FILE;
		//TODO: should we check if its a file
		return source_StringLiteral.ntype;
	}
	//helper method
	public static boolean isValidURL(String urlString)
	{
	    try
	    {
	        URL url = new URL(urlString);
	        url.toURI();
	        return true;
	    } catch (Exception exception)
	    {
	        return false;
	    }
	}

	@Override
	public Object visitSource_CommandLineParam(
			Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
//		if(source_CommandLineParam.paramNum!=null) {
			Type et = (Type) source_CommandLineParam.paramNum.visit(this, null);
			source_CommandLineParam.ntype=null;
			if(et==Type.INTEGER) {
				
			}
			else
				throw new SemanticException(source_CommandLineParam.firstToken,"From source_CommandLineParam, require failed");
//		}
		return source_CommandLineParam.ntype;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg)
			throws Exception {
		if(symbolTable.containsKey(source_Ident.name)) {
			if(symbolTable.get(source_Ident.name)!=null)
				source_Ident.ntype = symbolTable.get(source_Ident.name).ntype;
			else
				throw new SemanticException(source_Ident.firstToken, "From source_Ident, no declaration found");
		}
		//Symbol table error
		else {
			throw new SemanticException(source_Ident.firstToken, "From source_Ident, name doesnot exists");
		}
		if(!(source_Ident.ntype == Type.FILE || source_Ident.ntype == Type.URL))
			throw new SemanticException(source_Ident.firstToken, "From source_Ident, require failed");
		return source_Ident.ntype;
	}

	@Override
	public Object visitDeclaration_SourceSink(
			Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		if(symbolTable.containsKey(declaration_SourceSink.name))
			throw new SemanticException(declaration_SourceSink.firstToken, "From declaration_Image, name already exists");
		
//		if(declaration_SourceSink.source!=null) {
			Type st = (Type) declaration_SourceSink.source.visit(this, null);
			symbolTable.put(declaration_SourceSink.name, declaration_SourceSink);
			declaration_SourceSink.ntype = TypeUtils.getType(declaration_SourceSink.firstToken);
			if(st==declaration_SourceSink.ntype || st==null) {}
			else
				throw new SemanticException(declaration_SourceSink.firstToken, "From declaration_SourceSink, require failed");
//		}
		return declaration_SourceSink.ntype;
	}

	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit,
			Object arg) throws Exception {
		expression_IntLit.ntype = Type.INTEGER;
		return expression_IntLit.ntype;
	}

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg,
			Object arg) throws Exception {
//		if(expression_FunctionAppWithExprArg.arg!=null) {
			Type et = (Type) expression_FunctionAppWithExprArg.arg.visit(this, null);
			
			if(et==Type.INTEGER)
				;
			else
				throw new SemanticException(expression_FunctionAppWithExprArg.firstToken,"From expression_FunctionAppWithExprArg, require failed");
//		}
		expression_FunctionAppWithExprArg.ntype = Type.INTEGER;
		return expression_FunctionAppWithExprArg.ntype;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg,
			Object arg) throws Exception {
		if(expression_FunctionAppWithIndexArg.arg!=null)
			expression_FunctionAppWithIndexArg.arg.visit(this, null);
		expression_FunctionAppWithIndexArg.ntype = Type.INTEGER;
		return expression_FunctionAppWithIndexArg.ntype;
	}

	@Override
	public Object visitExpression_PredefinedName(
			Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		expression_PredefinedName.ntype = Type.INTEGER;
		return expression_PredefinedName.ntype;
	}

	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg)
			throws Exception {
		if(!symbolTable.containsKey(statement_Out.name))
			throw new SemanticException(statement_Out.firstToken, "From statement_out, name not found");
//		if(statement_Out.sink!=null) {
			Type sinkt = (Type) statement_Out.sink.visit(this, null);
			Type namet = (Type) symbolTable.get(statement_Out.name).ntype;
			statement_Out.setDec(symbolTable.get(statement_Out.name));
			if(symbolTable.get(statement_Out.name)!=null) {
				if(!(((namet==Type.INTEGER || namet == Type.BOOLEAN) && sinkt==Type.SCREEN) 
						|| (namet==Type.IMAGE && (sinkt==Type.FILE || sinkt==Type.SCREEN)))) {
					throw new SemanticException(statement_Out.firstToken,"From statement_Out, require statement failed");
				}
			}
			else
				throw new SemanticException(statement_Out.firstToken, "From statement_out, name not found");
			
//		}
		return statement_Out.ntype;
	}

	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg)
			throws Exception {
//		if(statement_In.source!=null) {
		
			Type st = (Type) statement_In.source.visit(this, null);
			statement_In.setDec(symbolTable.get(statement_In.name));
//			if(symbolTable.get(statement_In.name)!=null && symbolTable.get(statement_In.name).ntype==st) {
//				
//			}
//			else
//				throw new SemanticException(statement_In.firstToken,"from statement_In, Require statement failed");
//		}
		return statement_In.ntype;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign,
			Object arg) throws Exception {
//		if(statement_Assign.lhs!=null && statement_Assign.e!=null) {
			Type et = (Type) statement_Assign.e.visit(this, null);
			Type it = (Type) statement_Assign.lhs.visit(this, null);
			if(et!=it) {
				if(it == Type.IMAGE && et == Type.INTEGER) {}
				else
					throw new SemanticException(statement_Assign.firstToken,"From statement_Assign, type mismatch");
			}
//		}
		statement_Assign.setCartesian(statement_Assign.lhs.isCartesian());
		return statement_Assign.ntype;
	}

	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
		if(lhs.index!=null) {
			boolean b = (boolean) lhs.index.visit(this, null);
			lhs.setCartesian(b);
		}
		if(symbolTable.containsKey(lhs.name)) {
		}
		//Symbol table error
		else
			throw new SemanticException(lhs.firstToken,"From Lhs, No name found");
		lhs.setDec(symbolTable.get(lhs.name));
		lhs.ntype = lhs.getDec().ntype;
		return lhs.ntype;
	}

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg)
			throws Exception {
		sink_SCREEN.ntype = Type.SCREEN;
		return sink_SCREEN.ntype;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg)
			throws Exception {
		if(symbolTable.containsKey(sink_Ident.name)) {
			sink_Ident.ntype = symbolTable.get(sink_Ident.name).ntype;
		}
		//Symbol table error
		else
			throw new SemanticException(sink_Ident.firstToken,"From Sink_Ident, No name found");
		if(sink_Ident.ntype!=Type.FILE)
			throw new SemanticException(sink_Ident.firstToken,"From Sink_Ident, require failed");
		return sink_Ident.ntype;
	}

	@Override
	public Object visitExpression_BooleanLit(
			Expression_BooleanLit expression_BooleanLit, Object arg)
			throws Exception {
		expression_BooleanLit.ntype = Type.BOOLEAN;
		return expression_BooleanLit.ntype;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		if(symbolTable.containsKey(expression_Ident.name)) {
			expression_Ident.ntype = symbolTable.get(expression_Ident.name).ntype;
		}
		//Symbol table error
		else {
			throw new SemanticException(expression_Ident.firstToken,"From Expression_Ident, No name found");
		}
		return expression_Ident.ntype;
	}

}
