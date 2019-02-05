package dataStructures;

import java.util.HashMap;

public class Token 
{
    public String value;
    public TokenType type;
    public Integer id;
    public Integer ssaId;

    public enum TokenType 
    {
        errorToken, // ("", 0)

        timesToken, // ("*", 1)
        divToken, // ("/", 2)

        plusToken, // ("+", 11)
        minusToken, // ("-", 12)

        eqlToken, // ("==", 20)
        neqToken, // ("!=", 21)
        lssToken, // ("<", 22)
        geqToken, // (">=", 23)
        leqToken, // ("<=", 24)
        gtrToken, // (">", 25)

        periodToken, // (".", 30)
        commaToken, // (",", 31),
        openbracketToken, // ("[", 32)
        closebracketToken, // ("]", 34)
        closeparenToken, // (")", 35)

        becomesToken, // ("<-", 40)
        thenToken, // ("then", 41)
        doToken, // ("do", 42)

        openparenToken, // ("(", 50)

        number, // ("", 60)
        ident, // ("", 61)

        semiToken, // (";", 70)

        endToken, // ("}", 80)
        odToken, // ("od", 81)
        fiToken, // ("fi", 82)

        elseToken, // ("else", 90)

        letToken, // ("let", 100)
        callToken, // ("call", 101)
        ifToken, // ("if", 102)
        whileToken, // ("while", 103)
        returnToken, // ("return", 104)

        varToken, // ("var", 110)
        arrToken, // ("array", 111)
        funcToken, // ("function", 112)
        procToken, // ("procedure", 113)

        beginToken, // ("{", 150)
        mainToken, // ("main", 200)
        eofToken, // ("\0", 255)
    }
    
    @SuppressWarnings("serial")
    private static final HashMap<String, Tuple<TokenType, Integer>> tokenValueMap = new HashMap<String, Tuple<TokenType, Integer>>() 
    {
        {
            put("", new Tuple<TokenType, Integer>(TokenType.errorToken, 0));

            put("*", new Tuple<TokenType, Integer>(TokenType.timesToken, 1));
            put("/", new Tuple<TokenType, Integer>(TokenType.divToken, 2));

            put("+", new Tuple<TokenType, Integer>(TokenType.plusToken,11));
            put("-", new Tuple<TokenType, Integer>(TokenType.minusToken, 12));

            put("==", new Tuple<TokenType, Integer>(TokenType.eqlToken, 20));
            put("!=", new Tuple<TokenType, Integer>(TokenType.neqToken, 21));
            put("<", new Tuple<TokenType, Integer>(TokenType.lssToken, 22));
            put(">=", new Tuple<TokenType, Integer>(TokenType.geqToken, 23));
            put("<=", new Tuple<TokenType, Integer>(TokenType.leqToken, 24));
            put(">", new Tuple<TokenType, Integer>(TokenType.gtrToken, 25));

            put(".", new Tuple<TokenType, Integer>(TokenType.periodToken, 30));
            put(",", new Tuple<TokenType, Integer>(TokenType.commaToken, 31));
            put("[", new Tuple<TokenType, Integer>(TokenType.openbracketToken, 32));
            put("]", new Tuple<TokenType, Integer>(TokenType.closebracketToken,34));
            put(")", new Tuple<TokenType, Integer>(TokenType.closeparenToken, 35));
            put("<-", new Tuple<TokenType, Integer>(TokenType.becomesToken, 40));
            put("then", new Tuple<TokenType, Integer>(TokenType.thenToken, 41));
            put("do", new Tuple<TokenType, Integer>(TokenType.doToken, 42));

            put("(", new Tuple<TokenType, Integer>(TokenType.openparenToken, 50));

            //put("number", new Tuple<TokenType, Integer>(TokenType.number, 60));
            //put("identifier", new Tuple<TokenType, Integer>(TokenType.ident, 61));

            put(";", new Tuple<TokenType, Integer>(TokenType.semiToken, 70));

            put("}", new Tuple<TokenType, Integer>(TokenType.endToken, 80));
            put("od", new Tuple<TokenType, Integer>(TokenType.odToken, 81));
            put("fi", new Tuple<TokenType, Integer>(TokenType.fiToken, 82));

            put("else", new Tuple<TokenType, Integer>(TokenType.elseToken,90));

            put("let", new Tuple<TokenType, Integer>(TokenType.letToken, 100));
            put("call", new Tuple<TokenType, Integer>(TokenType.callToken, 101));
            put("if", new Tuple<TokenType, Integer>(TokenType.ifToken, 102));
            put("while", new Tuple<TokenType, Integer>(TokenType.whileToken, 103));
            put("return", new Tuple<TokenType, Integer>(TokenType.returnToken, 104));

            put("var", new Tuple<TokenType, Integer>(TokenType.varToken,110));
            put("array", new Tuple<TokenType, Integer>(TokenType.arrToken, 111));
            put("function", new Tuple<TokenType, Integer>(TokenType.funcToken, 112));
            put("procedure", new Tuple<TokenType, Integer>(TokenType.procToken, 113));

            put("{", new Tuple<TokenType, Integer>(TokenType.beginToken, 150));
            put("main", new Tuple<TokenType, Integer>(TokenType.mainToken, 200));
            put(Character.toString((char)255), new Tuple<TokenType, Integer>(TokenType.eofToken, 255));
        }
    };

    private Token(String value, TokenType type, int id, int ssaid)
    {
        this.value = value;
        this.type = type;
        this.id = id;
        this.ssaId = ssaid;
    }

    public static Token getToken(String tokenCharacters) 
    {        
        if(tokenValueMap.containsKey(tokenCharacters))
        {
            return new Token(tokenCharacters, tokenValueMap.get(tokenCharacters).x, tokenValueMap.get(tokenCharacters).y, 0);
        }
        else
        {
            if(tokenCharacters.matches("[0-9]+"))
            {
                return new Token(tokenCharacters, TokenType.number, 60, 1);
            }
            else if(tokenCharacters.matches("([a-zA-Z])([a-zA-Z0-9])*")) 
            {
                return new Token(tokenCharacters, TokenType.ident, 61, 1);
            }
            else 
            {
                return new Token(tokenCharacters, TokenType.errorToken, 0, 0);
            }
        }
    }

    public Token Clone() 
    {
        return new Token(value, type, id, ssaId);
    }

    @Override
    public String toString()
    {
        return String.format("{0}_{1}", value, ssaId);
    }

    public boolean isSameType(Token token) 
    {
        return type == token.type;
    }

    public boolean isSameType(TokenType type)
    {
        return this.type == type;
    }

    public boolean compare(Token token)
    {
        return (value == token.value && type == token.type);
    }

    public boolean deepCompare(Token token)
    {
        return (value == token.value && type == token.type && ssaId == token.ssaId);
    }

    public static boolean containsKnownKey(String key)
    {
        return tokenValueMap.containsKey(key);
    }
}