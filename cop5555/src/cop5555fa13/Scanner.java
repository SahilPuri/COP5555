package cop5555fa13;

import static cop5555fa13.TokenStream.Kind.*;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import javax.print.attribute.standard.Chromaticity;

import cop5555fa13.TokenStream.Kind;
import cop5555fa13.TokenStream.LexicalException;
import cop5555fa13.TokenStream.Token;

public class Scanner {

   //ADD METHODS AND FIELDS
	TokenStream stream;
	int characterPtr;
	//List of keywords
	static String[] keywords ={"image", "int", "boolean","pixel","pixels","blue","red","green","Z","shape","width","height","location","x_loc",
			"y_loc","SCREEN_SIZE","visible","x","y","pause","while","if","else"};
	//List of corresponding classes
	static Kind[] keywords_class={image,_int,_boolean,pixel,pixels,blue,red,green,Z,shape, width, height,location, x_loc, 
		y_loc, SCREEN_SIZE, visible,x, y,pause, _while,_if, _else};
	static char[] seperators = {'.',';',',','(',')','[',']','{','}',':','?'}; 
	static Kind[] seperators_class = {DOT, SEMI, COMMA, LPAREN, RPAREN, LSQUARE, RSQUARE, LBRACE, RBRACE, COLON, QUESTION};
	static String[] operators = { "=","|","&","==","!=","<",">","<=",">=","+","-","*","/","%","!","<<",">>"};
	static Kind[] operators_class={ASSIGN, OR, AND, EQ, NEQ, LT, GT, LEQ, GEQ, PLUS, MINUS, TIMES, DIV, MOD, NOT, LSHIFT, RSHIFT};
	
	static HashMap<String,TokenStream.Kind> reservedWords= new HashMap<String, TokenStream.Kind>();
	static HashMap<Character,TokenStream.Kind> seperatorMaps= new HashMap<Character, TokenStream.Kind>();
	static HashMap<String,TokenStream.Kind> operatorMaps= new HashMap<String, TokenStream.Kind>();
	
	//Initialize the Hashmaps
	static{
		for(int i=0;i<keywords.length;i++)
			reservedWords.put(keywords[i], keywords_class[i]);
		reservedWords.put("true",BOOLEAN_LIT);
		reservedWords.put("false",BOOLEAN_LIT);
		for(int i=0;i<seperators.length;i++)
			seperatorMaps.put(seperators[i], seperators_class[i]);
		
		for(int i=0;i<operators.length;i++)
			operatorMaps.put(operators[i], operators_class[i]);
	}
	
    
	public Scanner(TokenStream stream) {
		//IMPLEMENT THE CONSTRUCTOR
		this.stream=stream;
		this.characterPtr=0;
	}


	public void scan() throws LexicalException {
		//THIS IS PROBABLY COMPLETE
		Token t;
		do {
			t = next();
			if (t.kind.equals(COMMENT)) {
				stream.comments.add((Token) t);
			} else{
				System.out.println(t);
				stream.tokens.add(t);
			}
		} while (!t.kind.equals(EOF));
	}

	private Token next() throws LexicalException{
        //COMPLETE THIS METHOD.  THIS IS THE FUN PART!
		int state=0;
		int begin=0;
		int end=0;
		char nextChar=' ';
		Token token;
		while(characterPtr < stream.inputChars.length && Character.isWhitespace(stream.inputChars[characterPtr]))
			characterPtr++;
		
		if(characterPtr >= stream.inputChars.length){
			token = stream.new Token(EOF,characterPtr,characterPtr);
			return token;
		}
		
		begin=characterPtr;
		
		do{
			if( characterPtr == stream.inputChars.length )
				nextChar=' ';
			else
				nextChar=stream.inputChars[characterPtr];
			
			switch(state){
				
				case 0://Start State
					if(Character.isAlphabetic(nextChar) || nextChar == '_' || nextChar== '$')//Indicates the token will be an Identifier or reserved word
						state=1;
					else if(nextChar == '0'){//Initiates with Zero, token found
						end=++characterPtr;
						token = stream.new Token(INT_LIT,begin,end);
						return token;
					}else if(Character.isDigit(nextChar))// Initiates the integer
						state=2;
					else if(nextChar == '"')//Double quotes indicate strings
						state=3;
					else if(seperatorMaps.containsKey(nextChar)){
						characterPtr++;
						return stream.new Token(seperatorMaps.get(nextChar),begin,characterPtr);
					}else if(operatorMaps.containsKey(String.valueOf(nextChar))){
						if(nextChar == '/' && characterPtr+1 < stream.inputChars.length)//State for operator '/', and comments
							state=5;
						else if(characterPtr+1 < stream.inputChars.length && (nextChar=='='||nextChar=='>'||nextChar=='<'||nextChar=='!'))//State for operator with possible size 2
							state=4;
						else{//State for operator with possible size 1
							characterPtr++;
							return stream.new Token(operatorMaps.get(String.valueOf(nextChar)),begin,characterPtr);
						}
					}else
						throw stream.new LexicalException(characterPtr, "illegal character " + nextChar);
						
					break;
				case 1://Normal Literal or String Literal
					
					if(!Character.isAlphabetic(nextChar) && !Character.isDigit(nextChar) && nextChar != '_' && nextChar!= '$'){
						//The token is completed if it does not follow the following grammar [a-zA-Z0-9_$]
						end=characterPtr;
						token = stream.new Token(IDENT,begin,end);
						String token_value=token.getText();
						if(reservedWords.containsKey(token_value))//Finding if identifiers is a reserved words
							token=stream.new Token(reservedWords.get(token_value),begin,end);
						return token; 
					}
					break;
				case 2://Integer Literal
					if(!Character.isDigit(nextChar)){//Only Integers
						end=characterPtr;
						token = stream.new Token(INT_LIT,begin,end);
						return token; 
					}
					break;
				case 3://String Literal
					begin=characterPtr;
					while(characterPtr < stream.inputChars.length && stream.inputChars[characterPtr] != '"')
						characterPtr++;
					
					if(characterPtr == stream.inputChars.length){
						throw stream.new LexicalException(characterPtr, "illegal defination of String: Missing Quotes");
					}
					end=characterPtr++;//move to next token
					token = stream.new Token(STRING_LIT,begin,end);
					return token; 
				case 4:
					char[] operatorStr={stream.inputChars[characterPtr-1],stream.inputChars[characterPtr]};//Create string of size 2
					if(operatorMaps.containsKey(String.valueOf(operatorStr))){//Operator of size 2 exists 
						characterPtr++;
						return stream.new Token(operatorMaps.get(String.valueOf(operatorStr)),begin,characterPtr);
					}else//Operator of size 1 exists
						return stream.new Token(operatorMaps.get(String.valueOf(stream.inputChars[characterPtr-1])),begin,characterPtr);
				case 5:
					if(nextChar=='/'){//Following lines are comments
						characterPtr++;
						begin=characterPtr;
						nextChar=stream.inputChars[characterPtr];
						while(nextChar!='\n' && nextChar!='\r' && characterPtr<stream.inputChars.length)
							nextChar=stream.inputChars[++characterPtr];
						
						return stream.new Token(COMMENT,begin,characterPtr);
					}else//division operator
						return stream.new Token(operatorMaps.get("/"),begin,characterPtr);
			}
			characterPtr++;//Move to Next character
			
		}while(characterPtr <= stream.inputChars.length);//First overflow of the array indicates EOF
		
		return null;
	}
	
	static void compareText (String input, String expected) throws LexicalException {
		TokenStream stream = new TokenStream(input);
		Scanner s = new Scanner(stream);
		try{
		s.scan();
		}
		catch(LexicalException e){
			System.out.println(e.toString());
			throw e;
		}
		String output = stream.tokenTextListToString();
		System.out.println(output);
		assertEquals(expected,output);	
	}
	
	public static void main(String[] args) throws LexicalException {
		String input = "abc//=|&==!=<><=>=+-/*%!<<\n>>";
		String expected = "abc,>>,";  //comma separated (and terminated)
		                                           //text of tokens in input
		compareText(input,expected);
	}
}

