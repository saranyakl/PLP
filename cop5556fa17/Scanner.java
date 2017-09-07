/* *
  * Scanner for the class project in COP5556 Programming Language Principles 
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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Scanner {
	
	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {
		
		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}
		
		public int getPos() { return pos; }

	}

	public static enum Kind {
		IDENTIFIER, INTEGER_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL, 
		KW_x/* x */, KW_X/* X */, KW_y/* y */, KW_Y/* Y */, KW_r/* r */, KW_R/* R */, KW_a/* a */, 
		KW_A/* A */, KW_Z/* Z */, KW_DEF_X/* DEF_X */, KW_DEF_Y/* DEF_Y */, KW_SCREEN/* SCREEN */, 
		KW_cart_x/* cart_x */, KW_cart_y/* cart_y */, KW_polar_a/* polar_a */, KW_polar_r/* polar_r */, 
		KW_abs/* abs */, KW_sin/* sin */, KW_cos/* cos */, KW_atan/* atan */, KW_log/* log */, 
		KW_image/* image */,  KW_int/* int */, 
		KW_boolean/* boolean */, KW_url/* url */, KW_file/* file */, OP_ASSIGN/* = */, OP_GT/* > */, OP_LT/* < */, 
		OP_EXCL/* ! */, OP_Q/* ? */, OP_COLON/* : */, OP_EQ/* == */, OP_NEQ/* != */, OP_GE/* >= */, OP_LE/* <= */, 
		OP_AND/* & */, OP_OR/* | */, OP_PLUS/* + */, OP_MINUS/* - */, OP_TIMES/* * */, OP_DIV/* / */, OP_MOD/* % */, 
		OP_POWER/* ** */, OP_AT/* @ */, OP_RARROW/* -> */, OP_LARROW/* <- */, LPAREN/* ( */, RPAREN/* ) */, 
		LSQUARE/* [ */, RSQUARE/* ] */, SEMI/* ; */, COMMA/* , */, EOF;
	}
	public static enum State{
		START, ERROR, DIGIT/*1..9*/, IDENT/*A..Z, a...z, $, _*/, GOTGT /* > */, GOTEQUAL/* = */, GOTLT/* < */, 
		GOTMINUS /* - */, GOTSTAR /* * */, GOTEXCL /* ! */, GOTSLASH /*/ */, GOTBACKSLASH /* \ */
	}
	/** Class to represent Tokens. 
	 * 
	 * This is defined as a (non-static) inner class
	 * which means that each Token instance is associated with a specific 
	 * Scanner instance.  We use this when some token methods access the
	 * chars array in the associated Scanner.
	 * 
	 * 
	 * @author Beverly Sanders
	 *
	 */
	
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int l, int p) {
			super();
			this.line = l;
			this.posInLine = p;
		}
		
		@Override
		public String toString() {
			return "LinePos [line=" + line + ",posInLine=" + posInLine + "]";
		}
	}
	
	public class Token {
		public final Kind kind;
		public final int pos;
		public final int length;
		public final int line;
		public final int pos_in_line;

		public Token(Kind kind, int pos, int length, int line, int pos_in_line) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.line = line;
			this.pos_in_line = pos_in_line;
		}

		public String getText() {
			if (kind == Kind.STRING_LITERAL) {
				return chars2String(chars, pos, length);
			}
			else return String.copyValueOf(chars, pos, length);
		}

		/**
		 * To get the text of a StringLiteral, we need to remove the
		 * enclosing " characters and convert escaped characters to
		 * the represented character.  For example the two characters \ t
		 * in the char array should be converted to a single tab character in
		 * the returned String
		 * 
		 * @param chars
		 * @param pos
		 * @param length
		 * @return
		 */
		private String chars2String(char[] chars, int pos, int length) {
			StringBuilder sb = new StringBuilder();
			for (int i = pos + 1; i < pos + length - 1; ++i) {// omit initial and final "
				char ch = chars[i];
				if (ch == '\\') { // handle escape
					i++;
					ch = chars[i];
					switch (ch) {
					case 'b':
						sb.append('\b');
						break;
					case 't':
						sb.append('\t');
						break;
					case 'f':
						sb.append('\f');
						break;
					case 'r':
						sb.append('\r'); //for completeness, line termination chars not allowed in String literals
						break;
					case 'n':
						sb.append('\n'); //for completeness, line termination chars not allowed in String literals
						break;
					case '\"':
						sb.append('\"');
						break;
					case '\'':
						sb.append('\'');
						break;
					case '\\':
						sb.append('\\');
						break;
					default:
						assert false;
						break;
					}
				} else {
					sb.append(ch);
				}
			}
			return sb.toString();
		}

		/**
		 * precondition:  This Token is an INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		public String toString() {
			return "[" + kind + "," + String.copyValueOf(chars, pos, length)  + "," + pos + "," + length + "," + line + ","
					+ pos_in_line + "]";
		}

		/** 
		 * Since we overrode equals, we need to override hashCode.
		 * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html#equals-java.lang.Object-
		 * 
		 * Both the equals and hashCode method were generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + line;
			result = prime * result + pos;
			result = prime * result + pos_in_line;
			return result;
		}

		/**
		 * Override equals method to return true if other object
		 * is the same class and all fields are equal.
		 * 
		 * Overriding this creates an obligation to override hashCode.
		 * 
		 * Both hashCode and equals were generated by eclipse.
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (line != other.line)
				return false;
			if (pos != other.pos)
				return false;
			if (pos_in_line != other.pos_in_line)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is 
		 * associated with.
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}
		
//		LinePos getLinePos() {
//			return getLinePosObj(pos);
//		}

	}

	/** 
	 * Extra character added to the end of the input characters to simplify the
	 * Scanner.  
	 */
	static final char EOFchar = 0;
	
	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;
	
	/**
	 * An array of characters representing the input.  These are the characters
	 * from the input string plus and additional EOFchar at the end.
	 */
	final char[] chars;  



	
	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFchar;
		tokens = new ArrayList<Token>();
	}


	/**
	 * Method to scan the input and create a list of Tokens.
	 * 
	 * If an error is encountered during scanning, throw a LexicalException.
	 * 
	 * @return
	 * @throws LexicalException
	 */
	public Scanner scan() throws LexicalException {
		/* TODO  Replace this with a correct and complete implementation!!! */
		int pos = 0;
		int line = 1;
		int posInLine = 1;
		//State state = State.START;
		int startPos = 0;
		//System.out.println(chars.length);
		while(pos<chars.length) {
			char ch = chars[pos];
			if(ch == EOFchar) break;
			startPos = pos;
			switch(ch) {
			//single character token
			case ';':
				{tokens.add(new Token(Kind.SEMI,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case '0':
				{tokens.add(new Token(Kind.INTEGER_LITERAL,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case ',':
				{tokens.add(new Token(Kind.COMMA,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case '&':
				{tokens.add(new Token(Kind.OP_AND,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case '?':
				{tokens.add(new Token(Kind.OP_Q,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case ':':
				{tokens.add(new Token(Kind.OP_COLON,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case '@':
				{tokens.add(new Token(Kind.OP_AT,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case '%':
				{tokens.add(new Token(Kind.OP_MOD,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case '+':
				{tokens.add(new Token(Kind.OP_PLUS,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case '|':
				{tokens.add(new Token(Kind.OP_OR,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case ']':
				{tokens.add(new Token(Kind.RSQUARE,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case '[':
				{tokens.add(new Token(Kind.LSQUARE,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case ')':
				{tokens.add(new Token(Kind.RPAREN,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
			case '(':
				{tokens.add(new Token(Kind.LPAREN,pos, 1, line, posInLine));
				pos++;
				posInLine++;}
				break;
				
			//DOUBLE OR MORE CHARACTER TOKENS
			case '=':
				{ if(chars[pos+1] == '=') {
					tokens.add(new Token(Kind.OP_EQ,startPos,2,line, posInLine));
					pos=pos+2;
					posInLine=posInLine+2;
				}
				else {
					tokens.add(new Token(Kind.OP_ASSIGN,startPos,1,line,posInLine));
					pos++;
					posInLine++;
				}
				}
				break;
			case '>':
				{ if(chars[pos+1] == '=') {
					tokens.add(new Token(Kind.OP_GE,startPos,2,line, posInLine));
					pos=pos+2;
					posInLine=posInLine+2;
				}
				else {
					tokens.add(new Token(Kind.OP_GT,startPos,1,line,posInLine));
					pos++;
					posInLine++;
				}}
				break;
			case '<':
				{ if(chars[pos+1] == '=') {
					tokens.add(new Token(Kind.OP_LE,startPos,2,line, posInLine));
					pos=pos+2;
					posInLine=posInLine+2;
				}
				else if(chars[pos+1] == '-') {
					tokens.add(new Token(Kind.OP_LARROW,startPos,2,line, posInLine));
					pos=pos+2;
					posInLine=posInLine+2;
				}
				else {
					tokens.add(new Token(Kind.OP_LT,startPos,1,line,posInLine));
					pos++;
					posInLine++;
				}}
				break;
			case '-':
				{ if(chars[pos+1] == '>') {
					tokens.add(new Token(Kind.OP_RARROW,startPos,2,line, posInLine));
					pos=pos+2;
					posInLine=posInLine+2;
				}
				else {
					tokens.add(new Token(Kind.OP_MINUS,startPos,1,line,posInLine));
					pos++;
					posInLine++;
				}}
				break;
			case '*':
				{ if(chars[pos+1] == '*') {
					tokens.add(new Token(Kind.OP_POWER,startPos,2,line, posInLine));
					pos=pos+2;
					posInLine=posInLine+2;
				}
				else {
					tokens.add(new Token(Kind.OP_TIMES,startPos,1,line,posInLine));
					pos++;
					posInLine++;
				}}
				break;
			case '!':
				{ if(chars[pos+1] == '=') {
					tokens.add(new Token(Kind.OP_NEQ,startPos,2,line, posInLine));
					pos=pos+2;
					posInLine=posInLine+2;
				}
				else {
					tokens.add(new Token(Kind.OP_EXCL,startPos,1,line,posInLine));
					pos++;
					posInLine++;
				}}
				break;
			case '/':
				{ 
				pos++;}
				break;
			case '\\':
				{ pos++; }
				break;
			default:
				if(Character.isDigit(ch)) {
					int start = pos;
					pos++;
					while(Character.isDigit(chars[pos])) pos++;
					try {
						int i = Integer.parseInt(new String(chars,start,pos-start));
					}
					catch(Exception e){
						throw new LexicalException("Number exceeding 32 bits",pos);
					}
					tokens.add(new Token(Kind.INTEGER_LITERAL,start,pos-start,line,posInLine));
					posInLine = posInLine+pos-start;
				}
				else if(Character.isLetter(ch) || ch == '$' || ch=='_') {
					int start = pos;
					pos++;
					ch = chars[pos];
					while(Character.isLetterOrDigit(chars[pos]) || chars[pos]=='$' || chars[pos]=='_') pos++;
					String str = new String(chars,start,pos-start);
					switch(str) {
					case "x": tokens.add(new Token(Kind.KW_x,start,pos-start,line,posInLine)); break;
					case "X": tokens.add(new Token(Kind.KW_X,start,pos-start,line,posInLine)); break;
					case "y": tokens.add(new Token(Kind.KW_y,start,pos-start,line,posInLine)); break;
					case "Y": tokens.add(new Token(Kind.KW_Y,start,pos-start,line,posInLine)); break;
					case "r": tokens.add(new Token(Kind.KW_r,start,pos-start,line,posInLine)); break;
					case "R": tokens.add(new Token(Kind.KW_R,start,pos-start,line,posInLine)); break;
					case "a": tokens.add(new Token(Kind.KW_a,start,pos-start,line,posInLine)); break;
					case "A": tokens.add(new Token(Kind.KW_A,start,pos-start,line,posInLine)); break;
					case "Z": tokens.add(new Token(Kind.KW_Z,start,pos-start,line,posInLine)); break;
					case "DEF_X": tokens.add(new Token(Kind.KW_DEF_X,start,pos-start,line,posInLine)); break;
					case "DEF_Y": tokens.add(new Token(Kind.KW_DEF_Y,start,pos-start,line,posInLine)); break;
					case "SCREEN": tokens.add(new Token(Kind.KW_SCREEN,start,pos-start,line,posInLine)); break;
					case "polar_r": tokens.add(new Token(Kind.KW_polar_r,start,pos-start,line,posInLine)); break;
					case "polar_a": tokens.add(new Token(Kind.KW_polar_a,start,pos-start,line,posInLine)); break;
					case "cart_y": tokens.add(new Token(Kind.KW_cart_y,start,pos-start,line,posInLine)); break;
					case "cart_x": tokens.add(new Token(Kind.KW_cart_x,start,pos-start,line,posInLine)); break;
					case "abs": tokens.add(new Token(Kind.KW_abs,start,pos-start,line,posInLine)); break;
					case "sin": tokens.add(new Token(Kind.KW_sin,start,pos-start,line,posInLine)); break;
					case "cos": tokens.add(new Token(Kind.KW_cos,start,pos-start,line,posInLine)); break;
					case "log": tokens.add(new Token(Kind.KW_log,start,pos-start,line,posInLine)); break;
					case "atan": tokens.add(new Token(Kind.KW_atan,start,pos-start,line,posInLine)); break;
					case "int": tokens.add(new Token(Kind.KW_int,start,pos-start,line,posInLine)); break;
					case "image": tokens.add(new Token(Kind.KW_image,start,pos-start,line,posInLine)); break;
					case "true": tokens.add(new Token(Kind.KW_boolean,start,pos-start,line,posInLine)); break;
					case "false": tokens.add(new Token(Kind.KW_boolean,start,pos-start,line,posInLine)); break;
					case "url": tokens.add(new Token(Kind.KW_url,start,pos-start,line,posInLine)); break;
					case "file": tokens.add(new Token(Kind.KW_file,start,pos-start,line,posInLine)); break;
					default: tokens.add(new Token(Kind.IDENTIFIER,start,pos-start,line,posInLine)); break;
					}
					posInLine = posInLine+pos-start;
				}
				else {
//					pos++;
					throw new LexicalException("Not a valid token",pos);
				}
					
			break;	
			
			
			
			
			
//			switch(state) {
//			case START:
//			{
//				ch = chars[pos];
//				startPos = pos;
//				switch(ch) {
//				//single character token
//				case ';':
//					{tokens.add(new Token(Kind.SEMI,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case '0':
//					{tokens.add(new Token(Kind.INTEGER_LITERAL,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case ',':
//					{tokens.add(new Token(Kind.COMMA,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case '&':
//					{tokens.add(new Token(Kind.OP_AND,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case '?':
//					{tokens.add(new Token(Kind.OP_Q,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case ':':
//					{tokens.add(new Token(Kind.OP_COLON,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case '@':
//					{tokens.add(new Token(Kind.OP_AT,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case '%':
//					{tokens.add(new Token(Kind.OP_MOD,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case '+':
//					{tokens.add(new Token(Kind.OP_PLUS,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case '|':
//					{tokens.add(new Token(Kind.OP_OR,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case ']':
//					{tokens.add(new Token(Kind.RSQUARE,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case '[':
//					{tokens.add(new Token(Kind.LSQUARE,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case ')':
//					{tokens.add(new Token(Kind.RPAREN,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//				case '(':
//					{tokens.add(new Token(Kind.LPAREN,pos, 1, line, posInLine));
//					pos++;
//					posInLine++;}
//					break;
//					
//				//DOUBLE OR MORE CHARACTER TOKENS
//				case '=':
//					{ state = State.GOTEQUAL;
//					pos++;}
//					break;
//				case '>':
//					{ state = State.GOTGT;
//					pos++;}
//					break;
//				case '<':
//					{ state = State.GOTLT;
//					pos++;}
//					break;
//				case '-':
//					{ state = State.GOTMINUS;
//					pos++;}
//					break;
//				case '*':
//					{ state = State.GOTSTAR;
//					pos++;}
//					break;
//				case '!':
//					{ state = State.GOTEXCL;
//					pos++;}
//					break;
//				case '/':
//					{ state = State.GOTSLASH;
//					pos++;}
//					break;
//				case '\\':
//					{ state = State.GOTBACKSLASH;
//					pos++;}
//					break;
//				default:
//					System.out.println(ch +" "+ Character.isDigit(ch));
//					if(Character.isDigit(ch)) {
//						System.out.println(ch +" IN IF"+ Character.isDigit(ch));
//						state = State.DIGIT;
//						pos++;
//						break;
//					}
//					else if(Character.isJavaIdentifierStart(ch)) {
//						state = State.IDENT;
//						pos++;
//					}
//					else {
//						System.out.println("Error in not identifying");
//						throw new LexicalException("Unidentified character encountered"
//								+ " at line number " + line, posInLine);
//					}
//					break;
//				}
//				
//			}break;
//			case DIGIT:
//			if(Character.isDigit(ch)) {
//				pos++;
//				
//			}
//			else {
//				try {
//					System.out.println(pos +" ch: "+ch +  "test in else");
//					//int x = Integer.parseInt(chars.substring(startPos,pos));
//				}
//				catch(Exception e){
//					String message = "Unidentified character encountered at line number ";
//					throw new LexicalException(message, pos);
//				}
//				tokens.add(new Token(Kind.INTEGER_LITERAL,startPos,pos-startPos,line,posInLine));
//				state = State.START;
//			}
//			break;
//			case IDENT:
//				if(Character.isJavaIdentifierPart(ch))
//					pos++;
//				else {
//					//TODO handle reserved words
//					tokens.add(new Token(Kind.IDENTIFIER,startPos, pos-startPos, line, posInLine));
//					state=State.START;
//				}
//			break;
//			case GOTEQUAL:
//			if(ch == '=') {
//				tokens.add(new Token(Kind.OP_EQ,startPos,2,line, posInLine));
//				pos++;
//			}
//			else {
//				tokens.add(new Token(Kind.OP_ASSIGN,startPos,1,line,posInLine));
//			}
//			state = State.START;
//			break;
//			case GOTGT:
//				if(ch == '=') {
//					tokens.add(new Token(Kind.OP_GE,startPos,2,line, posInLine));
//					pos++;
//				}
//				else {
//					tokens.add(new Token(Kind.OP_GT,startPos,1,line,posInLine));
//				}
//				state = State.START;
//				break;
//			case GOTLT:
//				if(ch == '=') {
//					tokens.add(new Token(Kind.OP_LE,startPos,2,line, posInLine));
//					pos++;
//				}
//				else if(ch == '-') {
//					tokens.add(new Token(Kind.OP_LARROW,startPos,2,line, posInLine));
//					pos++;
//				}
//				else {
//					tokens.add(new Token(Kind.OP_LT,startPos,1,line,posInLine));
//				}
//				state = State.START;
//				break;
//			case GOTMINUS:
//				if(ch == '>') {
//					tokens.add(new Token(Kind.OP_RARROW,startPos,2,line, posInLine));
//					pos++;
//				}
//				else {
//					tokens.add(new Token(Kind.OP_MINUS,startPos,1,line,posInLine));
//				}
//				state = State.START;
//				break;
//			case GOTSTAR:
//				if(ch == '*') {
//					tokens.add(new Token(Kind.OP_POWER,startPos,2,line, posInLine));
//					pos++;
//				}
//				else {
//					tokens.add(new Token(Kind.OP_TIMES,startPos,1,line,posInLine));
//				}
//				state = State.START;
//				break;
//			case GOTEXCL:
//				if(ch == '=') {
//					tokens.add(new Token(Kind.OP_NEQ,startPos,2,line, posInLine));
//					pos++;
//				}
//				else {
//					tokens.add(new Token(Kind.OP_EXCL,startPos,1,line,posInLine));
//				}
//				state = State.START;
//				break;
////			case IDENT:
////			{
////				
////			}break;
////			case GOTZERO:
////			{
////				
////			}break;
//			default:
//				System.out.println("Error");
//				pos++;
//			}
		}
		}
		tokens.add(new Token(Kind.EOF, pos, 0, line, posInLine));
		return this;

	}


	/**
	 * Returns true if the internal iterator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that
	 * the next call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}
	
	/**
	 * Returns the next Token, but does not update the internal iterator.
	 * This means that the next call to nextToken or peek will return the
	 * same Token as returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition:  hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}
	
	
	/**
	 * Resets the internal iterator so that the next call to peek or nextToken
	 * will return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens 
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		return sb.toString();
	}

}
