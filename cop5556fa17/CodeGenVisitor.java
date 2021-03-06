package cop5556fa17;

import java.util.ArrayList;
import java.awt.image.BufferedImage;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556fa17.Scanner.Kind;
import cop5556fa17.TypeUtils.Type;
import cop5556fa17.AST.ASTNode;
import cop5556fa17.AST.ASTVisitor;
import cop5556fa17.AST.Declaration;
import cop5556fa17.AST.Declaration_Image;
import cop5556fa17.AST.Declaration_SourceSink;
import cop5556fa17.AST.Declaration_Variable;
import cop5556fa17.AST.Expression;
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
import cop5556fa17.AST.Source;
import cop5556fa17.AST.Source_CommandLineParam;
import cop5556fa17.AST.Source_Ident;
import cop5556fa17.AST.Source_StringLiteral;
import cop5556fa17.AST.Statement_In;
import cop5556fa17.AST.Statement_Out;
import cop5556fa17.AST.Statement_Assign;
import cop5556fa17.ImageFrame;
import cop5556fa17.ImageSupport;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */


	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}
	
	BufferedImage bi = new BufferedImage(1, 1, 1);

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.name;  
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);
		cw.visitSource(sourceFileName, null);
		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();		
		//add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);		
		// if GRADE, generates code to add string to log
//		CodeGenUtils.genLog(GRADE, mv, "entering main");
		//creating static variables for expression_predefined
		FieldVisitor fv = cw.visitField(ACC_STATIC, "x", "I", null, false);
        fv.visitEnd();
        fv = cw.visitField(ACC_STATIC, "y", "I", null, 0);fv.visitEnd();
		 fv = cw.visitField(ACC_STATIC, "r", "I", null, 0);fv.visitEnd();
		 fv = cw.visitField(ACC_STATIC, "a", "I", null, 0);fv.visitEnd();
		 fv = cw.visitField(ACC_STATIC, "X", "I", null, 0);fv.visitEnd();
		 fv = cw.visitField(ACC_STATIC, "Y", "I", null, 0);fv.visitEnd();
		 fv = cw.visitField(ACC_STATIC, "Z", "I", null, 0xFFFFFF);fv.visitEnd();
		 fv = cw.visitField(ACC_STATIC, "R", "I", null, 0);fv.visitEnd();
		 fv = cw.visitField(ACC_STATIC, "A", "I", null, 0);fv.visitEnd();
		 fv = cw.visitField(ACC_STATIC, "DEF_X", "I", null, 256);fv.visitEnd();
		 fv = cw.visitField(ACC_STATIC, "DEF_Y", "I", null, 256);fv.visitEnd();
         
		// visit decs and statements to add field to class
		//  and instructions to main method, respectivley
		ArrayList<ASTNode> decsAndStatements = program.decsAndStatements;
		for (ASTNode node : decsAndStatements) {
			node.visit(this, arg);
		}

		//generates code to add string to log
//		CodeGenUtils.genLog(GRADE, mv, "leaving main");
		
		//adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);
		
		//adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		
		//handles parameters and local variables of main. Right now, only args
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		//Sets max stack size and number of local vars.
		//Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the constructor,
		//asm will calculate this itself and the parameters are ignored.
		//If you have trouble with failures in this routine, it may be useful
		//to temporarily set the parameter in the ClassWriter constructor to 0.
		//The generated classfile will not be correct, but you will at least be
		//able to see what is in it.
		mv.visitMaxs(0, 0);
		
		//terminate construction of main method
		mv.visitEnd();
		
		//terminate class construction
		cw.visitEnd();

		//generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitDeclaration_Variable(Declaration_Variable declaration_Variable, Object arg) throws Exception {
		FieldVisitor fv =null;
		if(declaration_Variable.ntype == Type.BOOLEAN) {
			fv = cw.visitField(ACC_STATIC, declaration_Variable.name, "Z", null, false);
		}
		else if(TypeUtils.getType(declaration_Variable.firstToken) == Type.INTEGER) {
			fv = cw.visitField(ACC_STATIC, declaration_Variable.name, "I", null, 0);
		}
		fv.visitEnd();
		if(declaration_Variable.e!=null) {
			declaration_Variable.e.visit(this, arg);
			if(declaration_Variable.ntype == Type.BOOLEAN)
				mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, "Z");
			else if(declaration_Variable.ntype == Type.INTEGER)
				mv.visitFieldInsn(PUTSTATIC, className, declaration_Variable.name, "I");
		}
		return null;
	}

	@Override
	public Object visitExpression_Binary(Expression_Binary expression_Binary, Object arg) throws Exception {
			Expression e0 = expression_Binary.e0;
			Expression e1 = expression_Binary.e1;
		e0.visit(this, arg);
		e1.visit(this, arg);
//		if(e0.ntype==Type.INTEGER && e1.ntype==Type.INTEGER) {
		Label endlabel = new Label();
		Label truelabel = new Label();
			switch(expression_Binary.op) {
			case OP_MINUS:
				mv.visitInsn(ISUB);
				break;
			case OP_PLUS:
				mv.visitInsn(IADD);
				break;
			case OP_DIV:
				mv.visitInsn(IDIV);
				break;
			case OP_MOD:
				mv.visitInsn(IREM);
				break;
			case OP_TIMES:
				mv.visitInsn(IMUL);
				break;
//			case OP_POWER:
//				mv.visitInsn(arg0);
//				break;
			case OP_AND:
				mv.visitInsn(IAND);
				break;
			case OP_OR:
				mv.visitInsn(IOR);
				break;
			case OP_EQ:
				mv.visitJumpInsn(IF_ICMPEQ, truelabel);
				//false loop
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endlabel);
				//true loop
				mv.visitLabel(truelabel);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, endlabel);
				break;
			case OP_NEQ:
				mv.visitJumpInsn(IF_ICMPNE, truelabel);
				//false loop
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endlabel);
				//true loop
				mv.visitLabel(truelabel);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, endlabel);
				break;
			case OP_GE:
				mv.visitJumpInsn(IF_ICMPGE, truelabel);
				//false loop
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endlabel);
				//true loop
				mv.visitLabel(truelabel);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, endlabel);
				break;
			case OP_GT:
				mv.visitJumpInsn(IF_ICMPGT, truelabel);
				//false loop
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endlabel);
				//true loop
				mv.visitLabel(truelabel);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, endlabel);
				break;
			case OP_LT:
				mv.visitJumpInsn(IF_ICMPLT, truelabel);
				//false loop
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endlabel);
				//true loop
				mv.visitLabel(truelabel);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, endlabel);
				break;
			case OP_LE:
				mv.visitJumpInsn(IF_ICMPLE, truelabel);
				//false loop
				mv.visitInsn(ICONST_0);
				mv.visitJumpInsn(GOTO, endlabel);
				//true loop
				mv.visitLabel(truelabel);
				mv.visitInsn(ICONST_1);
				mv.visitJumpInsn(GOTO, endlabel);
				break;
			default:
				break;
			}
//		}
		mv.visitLabel(endlabel);
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Binary.ntype);
		return null;
	}

	@Override
	public Object visitExpression_Unary(Expression_Unary expression_Unary, Object arg) throws Exception {
		expression_Unary.e.visit(this, arg);
		if(expression_Unary.op==Kind.OP_MINUS) {
			mv.visitInsn(INEG);
		}
		else if(expression_Unary.op==Kind.OP_EXCL) {
			if(expression_Unary.e.ntype==Type.INTEGER) {
				Integer int_one = new Integer(0x7FFFFFFF);
				mv.visitLdcInsn(int_one);
				mv.visitInsn(IXOR);
			}
			else if(expression_Unary.e.ntype==Type.BOOLEAN){
				mv.visitInsn(ICONST_1);
				mv.visitInsn(IXOR);
			}
		}
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Unary.ntype);
		return null;
	}

	// generate code to leave the two values on the stack
	@Override
	public Object visitIndex(Index index, Object arg) throws Exception {
		index.e0.visit(this, null);
		index.e1.visit(this, null);
		if(index.isCartesian()) {
			
		}
		else {
			mv.visitInsn(DUP2);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			mv.visitInsn(DUP2_X2);
			mv.visitInsn(POP);
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
		}
		return null;
	}

	@Override
	public Object visitExpression_PixelSelector(Expression_PixelSelector expression_PixelSelector, Object arg)
			throws Exception {
		
		mv.visitFieldInsn(GETSTATIC, className, expression_PixelSelector.name, ImageSupport.ImageDesc);
		expression_PixelSelector.index.visit(this, null);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getPixel", ImageSupport.getPixelSig, false);
		return null;
	}

	@Override
	public Object visitExpression_Conditional(Expression_Conditional expression_Conditional, Object arg)
			throws Exception {
		
		Label falseLabel = new Label();
		Label endLabel = new Label();
		expression_Conditional.condition.visit(this, arg);
		mv.visitJumpInsn(IFEQ, falseLabel);
		expression_Conditional.trueExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, endLabel);
		
		mv.visitLabel(falseLabel);
		expression_Conditional.falseExpression.visit(this, arg);
		mv.visitLabel(endLabel);
		
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Conditional.trueExpression.ntype);
		return null;
	}


	@Override
	public Object visitDeclaration_Image(Declaration_Image declaration_Image, Object arg) throws Exception {
		FieldVisitor fv = cw.visitField(ACC_STATIC, declaration_Image.name, ImageSupport.ImageDesc, null, null);
		fv.visitEnd();
		if(declaration_Image.source == null && declaration_Image.xSize == null){
             mv.visitFieldInsn(GETSTATIC, className, "DEF_X", "I");
             mv.visitFieldInsn(GETSTATIC, className, "DEF_Y", "I");
             mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);
         } 
         else if(declaration_Image.source != null && declaration_Image.xSize == null){
             declaration_Image.source.visit(this, null);
             mv.visitInsn(ACONST_NULL);
             mv.visitInsn(ACONST_NULL);
             mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);
         }
         else if(declaration_Image.source == null && declaration_Image.xSize != null){
             declaration_Image.xSize.visit(this, null);
             declaration_Image.ySize.visit(this, null);
             mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage", ImageSupport.makeImageSig, false);
//             mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)"+ImageSupport.IntegerDesc, false);
//             mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "makeImage","(II)"+ ImageSupport.ImageDesc, false);
         }
         else if(declaration_Image.source != null && declaration_Image.xSize != null){
             declaration_Image.source.visit(this, null);
             declaration_Image.xSize.visit(this, null);
             mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)"+ImageSupport.IntegerDesc, false);
             declaration_Image.ySize.visit(this, null);
             mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)"+ImageSupport.IntegerDesc, false);
             mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);         
         } 
		mv.visitFieldInsn(PUTSTATIC, className, declaration_Image.name, ImageSupport.ImageDesc);
		return null;
	}
	
  
	@Override
	public Object visitSource_StringLiteral(Source_StringLiteral source_StringLiteral, Object arg) throws Exception {
		mv.visitLdcInsn(source_StringLiteral.fileOrUrl);
		return null;
	}

	

	@Override
	public Object visitSource_CommandLineParam(Source_CommandLineParam source_CommandLineParam, Object arg)
			throws Exception {
			mv.visitVarInsn(ALOAD, 0);
			source_CommandLineParam.paramNum.visit(this, arg);
			mv.visitInsn(AALOAD);
			return null;
	}

	@Override
	public Object visitSource_Ident(Source_Ident source_Ident, Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, source_Ident.name, ImageSupport.StringDesc);
		return null;
	}


	@Override
	public Object visitDeclaration_SourceSink(Declaration_SourceSink declaration_SourceSink, Object arg)
			throws Exception {
		FieldVisitor fv = cw.visitField(ACC_STATIC, declaration_SourceSink.name, ImageSupport.StringDesc, null, null);
		fv.visitEnd();
		if(declaration_SourceSink.source!=null) {
			declaration_SourceSink.source.visit(this, arg);
			mv.visitFieldInsn(PUTSTATIC, className, declaration_SourceSink.name, ImageSupport.StringDesc);
		}
		return null;
	}
	


	@Override
	public Object visitExpression_IntLit(Expression_IntLit expression_IntLit, Object arg) throws Exception {
		
		mv.visitLdcInsn(expression_IntLit.value);
//		CodeGenUtils.genLogTOS(GRADE, mv, Type.INTEGER);
		return null;
	}
	

	@Override
	public Object visitExpression_FunctionAppWithExprArg(
			Expression_FunctionAppWithExprArg expression_FunctionAppWithExprArg, Object arg) throws Exception {
		
		expression_FunctionAppWithExprArg.arg.visit(this, arg);
		if(expression_FunctionAppWithExprArg.function==Kind.KW_abs) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "abs", RuntimeFunctions.absSig, false);
		}
		else if(expression_FunctionAppWithExprArg.function==Kind.KW_log) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "log", RuntimeFunctions.logSig, false);
		}
		return null;
	}

	@Override
	public Object visitExpression_FunctionAppWithIndexArg(
			Expression_FunctionAppWithIndexArg expression_FunctionAppWithIndexArg, Object arg) throws Exception {
		
		expression_FunctionAppWithIndexArg.arg.e0.visit(this, null);
		expression_FunctionAppWithIndexArg.arg.e1.visit(this, null);
		switch (expression_FunctionAppWithIndexArg.function) {
		case KW_cart_x:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_x", RuntimeFunctions.cart_xSig, false);
			break;
		case KW_cart_y:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "cart_y", RuntimeFunctions.cart_ySig, false);
			break;
		case KW_polar_r:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
			break;
		case KW_polar_a:
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitExpression_PredefinedName(Expression_PredefinedName expression_PredefinedName, Object arg)
			throws Exception {
		String str = "";
//		Integer i = new Integer(256);
		switch (expression_PredefinedName.kind) {
		case KW_x:  str = "x"; break;
		case KW_y: str = "y"; break;
		case KW_r: str = "r"; break;
		case KW_a: str = "a"; break;
		case KW_X: str = "X"; break;
		case KW_Y: str = "Y"; break;
		case KW_Z: str = "Z"; break;
		case KW_R: str = "R"; break;
		case KW_A: str = "A"; break;
		case KW_DEF_X: str = "DEF_X"; break;
		case KW_DEF_Y: str = "DEF_Y"; break;
		default: break;
		}
		mv.visitFieldInsn(GETSTATIC, className, str, "I");
		return null;
	}

	/** For Integers and booleans, the only "sink"is the screen, so generate code to print to console.
	 * For Images, load the Image onto the stack and visit the Sink which will generate the code to handle the image.
	 */
	@Override
	public Object visitStatement_Out(Statement_Out statement_Out, Object arg) throws Exception {
		
		//statement_Out.sink.visit(this, arg);
		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		if(statement_Out.getDec().ntype == Type.INTEGER) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "I");
		}
		else if(statement_Out.getDec().ntype == Type.BOOLEAN) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, "Z");
		}
		else if(statement_Out.getDec().ntype == Type.IMAGE) {
			mv.visitFieldInsn(GETSTATIC, className, statement_Out.name, ImageSupport.ImageDesc);
		}
		CodeGenUtils.genLogTOS(GRADE, mv, statement_Out.getDec().ntype);
		if(statement_Out.getDec().ntype == Type.INTEGER) {
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
		}
		else if(statement_Out.getDec().ntype == Type.BOOLEAN) {
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
		}
		else if(statement_Out.getDec().ntype == Type.IMAGE) {
			statement_Out.sink.visit(this, arg);
		}
		return null;
	}

	/**
	 * Visit source to load rhs, which will be a String, onto the stack
	 * 
	 *  In HW5, you only need to handle INTEGER and BOOLEAN
	 *  Use java.lang.Integer.parseInt or java.lang.Boolean.parseBoolean 
	 *  to convert String to actual type. 
	 *  
	 *  TODO HW6 remaining types
	 */
	@Override
	public Object visitStatement_In(Statement_In statement_In, Object arg) throws Exception {
//		statement_In.source.visit(this, arg);
		if(statement_In.getDec().ntype==Type.INTEGER) {
			statement_In.source.visit(this, null);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I",false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "I");
		}
		else if(statement_In.getDec().ntype==Type.BOOLEAN) {
			statement_In.source.visit(this, null);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTSTATIC, className, statement_In.name, "Z");
		}
		else {
			Declaration_Image di = (Declaration_Image)statement_In.getDec();
			if(di.xSize == null){
 	            statement_In.source.visit(this, null);
	            mv.visitInsn(ACONST_NULL);
	            mv.visitInsn(ACONST_NULL);
	            mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage",ImageSupport.readImageSig, false);
//	            CodeGenUtils.genLogTOS(GRADE, mv, statement_In.getDec().ntype);
 		   
 		    }
 		   else if( di.xSize != null){
                statement_In.source.visit(this, null);
				di.xSize.visit(this, null);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)"+ImageSupport.IntegerDesc, false);
				di.ySize.visit(this, null);
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf","(I)"+ImageSupport.IntegerDesc, false);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "readImage", ImageSupport.readImageSig, false);         
//				CodeGenUtils.genLogTOS(GRADE, mv, statement_In.getDec().ntype);
 		   
 	   }  
 		   mv.visitFieldInsn(PUTSTATIC, className, di.name, ImageSupport.ImageDesc);
		}
		return null;
	}


	/**
	 * In HW5, only handle INTEGER and BOOLEAN types.
	 */
	@Override
	public Object visitLHS(LHS lhs, Object arg) throws Exception {
//		lhs.index.visit(this, arg);
		if(lhs.ntype==Type.INTEGER) {
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "I");
		}
		else if(lhs.ntype==Type.BOOLEAN) {
			mv.visitFieldInsn(PUTSTATIC, className, lhs.name, "Z");
		}
		else if(lhs.ntype==Type.IMAGE) {
//			lhs.index.visit(this, arg);
			mv.visitFieldInsn(GETSTATIC, className, lhs.name, ImageSupport.ImageDesc);
			mv.visitFieldInsn(GETSTATIC, className, "x", "I");
			mv.visitFieldInsn(GETSTATIC, className, "y", "I");
			mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "setPixel", ImageSupport.setPixelSig, false);
		}
		return null;
	}
	

	@Override
	public Object visitSink_SCREEN(Sink_SCREEN sink_SCREEN, Object arg) throws Exception {
		mv.visitMethodInsn(INVOKESTATIC, ImageFrame.className, "makeFrame", "("+ImageSupport.ImageDesc+")"+ImageSupport.JFrameDesc, false);
		mv.visitInsn(POP);
		return null;
	}

	@Override
	public Object visitSink_Ident(Sink_Ident sink_Ident, Object arg) throws Exception {
		mv.visitFieldInsn(GETSTATIC, className, sink_Ident.name, ImageSupport.StringDesc);
		mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "write", ImageSupport.writeSig, false);
		return null;
	}

	@Override
	public Object visitExpression_BooleanLit(Expression_BooleanLit expression_BooleanLit, Object arg) throws Exception {
		mv.visitLdcInsn(expression_BooleanLit.value);
//		CodeGenUtils.genLogTOS(GRADE, mv, Type.BOOLEAN);
		return null;
	}

	@Override
	public Object visitExpression_Ident(Expression_Ident expression_Ident,
			Object arg) throws Exception {
		if(expression_Ident.ntype==Type.BOOLEAN) {
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "Z");
		}
		else if(expression_Ident.ntype==Type.INTEGER) {
			mv.visitFieldInsn(GETSTATIC, className, expression_Ident.name, "I");
		}
//		CodeGenUtils.genLogTOS(GRADE, mv, expression_Ident.getType());
		return null;
	}

	@Override
	public Object visitStatement_Assign(Statement_Assign statement_Assign, Object arg) throws Exception {
		if(statement_Assign.lhs.ntype == Type.INTEGER || statement_Assign.lhs.ntype == Type.BOOLEAN) {
			statement_Assign.e.visit(this, arg);
			statement_Assign.lhs.visit(this, arg);
		}
		else if(statement_Assign.lhs.ntype == Type.IMAGE) {
//			if(statement_Assign.lhs.isCartesian()) {
				mv.visitFieldInsn(GETSTATIC, className, statement_Assign.lhs.name, ImageSupport.ImageDesc);
				mv.visitInsn(DUP);
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getX", ImageSupport.getXSig, false);
//				mv.visitVarInsn(ISTORE, 3);
				mv.visitFieldInsn(PUTSTATIC, className, "X", "I");
				mv.visitMethodInsn(INVOKESTATIC, ImageSupport.className, "getY", ImageSupport.getYSig, false);
//				mv.visitVarInsn(ISTORE, 4);
				mv.visitFieldInsn(PUTSTATIC, className, "Y", "I");
				mv.visitFieldInsn(GETSTATIC, className, "X", "I");
				mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
				mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", RuntimeFunctions.polar_rSig, false);
				mv.visitFieldInsn(PUTSTATIC, className, "R", "I");
//			}
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
			mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", RuntimeFunctions.polar_aSig, false);
			mv.visitFieldInsn(PUTSTATIC, className, "A", "I");
			
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
			             
			             
			Label l1start = new Label();
             Label l1end = new Label();
           
             Label l2start = new Label();
             Label l2end = new Label();
             
             mv.visitLabel(l1start);
             
             mv.visitFieldInsn(GETSTATIC, className, "x", "I");
             
             mv.visitFieldInsn(GETSTATIC, className, "X", "I");
             
             
             mv.visitJumpInsn(IF_ICMPGE, l1end);
             
             
            mv.visitInsn(ICONST_0);
             mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
             
             
             mv.visitLabel(l2start);
             
             
             mv.visitFieldInsn(GETSTATIC, className, "y", "I");
             
             mv.visitFieldInsn(GETSTATIC, className, "Y", "I");
             
             
             
             mv.visitJumpInsn(IF_ICMPGE, l2end);
             
             
             mv.visitFieldInsn(GETSTATIC, className, "x", "I");
             
             mv.visitFieldInsn(GETSTATIC, className, "y", "I");
             
             mv.visitInsn(DUP2);
             
             mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_r", 
                     RuntimeFunctions.polar_rSig, false);
             mv.visitFieldInsn(PUTSTATIC, className, "r", "I");
             
             
             mv.visitMethodInsn(INVOKESTATIC, RuntimeFunctions.className, "polar_a", 
                     RuntimeFunctions.polar_aSig, false);
             mv.visitFieldInsn(PUTSTATIC, className, "a", "I");
             
             
             
             statement_Assign.e.visit(this, arg);
             
             
             statement_Assign.lhs.visit(this, arg);
             
             
             
             mv.visitInsn(ICONST_1);
             mv.visitFieldInsn(GETSTATIC, className, "y", "I");
             mv.visitInsn(IADD);
             mv.visitFieldInsn(PUTSTATIC, className, "y", "I");
             
             
             mv.visitJumpInsn(GOTO, l2start);
             
             
             mv.visitLabel(l2end);
             
             
             mv.visitInsn(ICONST_1);
             mv.visitFieldInsn(GETSTATIC, className, "x", "I");
             mv.visitInsn(IADD);
             mv.visitFieldInsn(PUTSTATIC, className, "x", "I");
             
             
             mv.visitJumpInsn(GOTO, l1start);
             
             
             mv.visitLabel(l1end);
		}
		
		return null;
	}

}
