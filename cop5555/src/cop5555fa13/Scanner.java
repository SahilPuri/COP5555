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
		Token token;
		
		while(characterPtr < stream.inputChars.length && Character.isWhitespace(stream.inputChars[characterPtr]))
			characterPtr++;
		
		if(characterPtr >= stream.inputChars.length){
			token = stream.new Token(EOF,characterPtr,characterPtr);
			return token;
		}
		
		begin=characterPtr;
		
		do{
			
			char nextChar;
			
			if( characterPtr == stream.inputChars.length )
				nextChar=' ';
			else
				nextChar=stream.inputChars[characterPtr];
	
			switch(state){
				case 0://Start State
					if(Character.isAlphabetic(nextChar) || nextChar == '_' || nextChar== '$')
						state=1;
					else if(nextChar == '0'){
						end=++characterPtr;
						token = stream.new Token(INT_LIT,begin,end);
						return token;
					}else if(Character.isDigit(nextChar))
						state=2;
					break;
				case 1://Normal Literal or String Literal
					
					if(!Character.isAlphabetic(nextChar) && !Character.isDigit(nextChar) && nextChar != '_' && nextChar!= '$'){
						end=characterPtr;
						token = stream.new Token(IDENT,begin,end);
						return token; 
					}
					break;
				case 2://Integer Literal
					if(!Character.isDigit(nextChar)){
						end=characterPtr;
						token = stream.new Token(INT_LIT,begin,end);
						return token; 
					}
					break;
			}
			characterPtr++;
			
		}while(characterPtr <= stream.inputChars.length);
		
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
		String input = "042abc01 320";
		String expected = "0,42,abc01,320,";  //comma separated (and terminated)
		                                           //text of tokens in input
		compareText(input,expected);
	}
}


