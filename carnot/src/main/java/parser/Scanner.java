package parser;

import dataStructures.Token;
import dataStructures.Token.TokenType;
import exceptions.InvalidTokenException;

public class Scanner {
    private char inputSym;
    private FileReader reader;
    private static Scanner scanner;

    private Token prevToken;
    private String stringUnderConstruction;

    public enum ScannerState
    {
        stop, // to stop the machine
        singleChar, // when token is a single character
        relOp, // when token is a comparision operator like <, <=, !=, ==,....
        digit, // when token is a digit which becomes a number
        letter // when token is a letter which becomes either a keyword or identifier
    }

    public static Scanner getInstance(String fileName) 
    {
        if (scanner == null) 
        {
            scanner = new Scanner(fileName);

            if (scanner.reader == null) 
            {
                scanner = null;
            }
        }

        return scanner;
    }

    private Scanner(String fileName) 
    {
        reader = FileReader.getInstance(fileName);
        next();
    }

    private void next() 
    {
        if (reader != null) 
        {
            inputSym = reader.getSym();
        }
    }

    private void nextLine()
    {
        if(reader != null)
        {
            reader.nextLine();
        }
    }

    public Token getSym()
    {
        if(prevToken != null && prevToken.isSameType(TokenType.eofToken)) 
        {
            return prevToken;
        }

        runStateMachine();
        Token token = Token.getToken(stringUnderConstruction);
        if(token.isSameType(TokenType.errorToken))
        {
            error(new InvalidTokenException(String.format("Unknown token: {0}", stringUnderConstruction))); 
        }

        prevToken = token;
        return token;
    }

    private void runStateMachine()
    {
        ScannerState state = ScannerState.singleChar;
        stringUnderConstruction = "";

        while(state != ScannerState.stop)
        {            
            switch(state)
            {
                case singleChar:
                    if(inputSym == '#')
                    {
                        nextLine();
                        next();
                    }
                    else if(inputSym == ' ' || inputSym == '\t' 
                                || inputSym == '\n' || inputSym == '\r')
                    {
                        next();
                    }
                    else if(Token.containsKnownKey(Character.toString(inputSym)))
                    {
                        eat();
    
                        if(inputSym == (char)FileReader.EOF)
                        {
                            state = ScannerState.stop;
                        }
                        else if(inputSym == '>' || inputSym == '<'
                                    || inputSym == '=' || inputSym == '!')
                        {
                            state = ScannerState.relOp;
                            next();
                        }
                        else if(Character.isDigit(inputSym))
                        {
                            state = ScannerState.digit;
                            next();
                        }
                        else if(Character.isLetter(inputSym))
                        {
                            state = ScannerState.letter;
                            next();
                        }
                    }
                    break;
                
                case relOp:
                    state = ScannerState.stop;

                    if(inputSym == '=' || inputSym == '-') 
                    {
                        eat();
                        next();
                    }
                    break;

                case digit:
                    if(Character.isDigit(inputSym))
                    {
                        eat();
                        next();
                    }
                    else
                    {
                        state = ScannerState.stop;
                    }
                    break;

                case letter:
                    if(Character.isLetterOrDigit(inputSym))
                    {
                        eat();
                        next();
                    }
                    else 
                    {
                        state = ScannerState.stop;
                    }
                    break;

                default:
                    state = ScannerState.stop;
            }
        }
    }

    private void eat()
    {
        stringUnderConstruction += Character.toString(inputSym);
    }

    public void error(Exception exception)
    {
        reader.error(exception);
    }
}
