package dataStructures;

import java.util.*;
import dataStructures.Token.TokenType;

@SuppressWarnings("serial")
public class Operator 
{
    public enum OperatorCode 
    {
        neg, add, sub, mul, div, cmp,
    
        adda, load, store, move, phi,
    
        end, bra, bne, beq, ble, blt, bge, bgt,
    
        read, write, writeNL
    }

    public static final HashMap<TokenType, OperatorCode> arithmeticOperator = new HashMap<TokenType, OperatorCode>()
    {
        {
            put(TokenType.plusToken, OperatorCode.add);
            put(TokenType.minusToken, OperatorCode.sub);
            put(TokenType.timesToken, OperatorCode.mul);
            put(TokenType.divToken, OperatorCode.div);
        }
    };

    public static final HashMap<TokenType, OperatorCode> relationOperator = new HashMap<TokenType, OperatorCode>()
    {
        {
            put(TokenType.eqlToken, OperatorCode.cmp);
            put(TokenType.neqToken, OperatorCode.cmp);
            put(TokenType.lssToken, OperatorCode.cmp);
            put(TokenType.geqToken, OperatorCode.cmp);
            put(TokenType.leqToken, OperatorCode.cmp);
            put(TokenType.gtrToken, OperatorCode.cmp);
        }
    };

    public static final HashMap<TokenType, OperatorCode> branchingOperator = new HashMap<TokenType, OperatorCode>()
    {
        {
            put(TokenType.eqlToken, OperatorCode.bne);
            put(TokenType.neqToken, OperatorCode.beq);
            put(TokenType.lssToken, OperatorCode.blt);
            put(TokenType.leqToken, OperatorCode.ble);
            put(TokenType.gtrToken, OperatorCode.bgt);
            put(TokenType.geqToken, OperatorCode.bge);
        }
    };
    
    public static final HashMap<TokenType, OperatorCode> assignmentOperator = new HashMap<TokenType, OperatorCode>()
    {
        {
            put(TokenType.letToken, OperatorCode.move);
            put(TokenType.becomesToken, OperatorCode.move);
        }
    };

    public static final HashMap<String, OperatorCode> standardIoOperator = new HashMap<String, OperatorCode>()
    {
        {
            put("InputNum", OperatorCode.read);
            put("OutputNum", OperatorCode.write);
            put("OutputNewLine", OperatorCode.writeNL);
        }
    };

    public static ArrayList<OperatorCode> getOpCode(Token opToken)
    {
        ArrayList<OperatorCode> opCodes = new ArrayList<OperatorCode>();
        if(arithmeticOperator.containsKey(opToken.type))
        {
            opCodes.add(arithmeticOperator.get(opToken.type));
        }
        else if(relationOperator.containsKey(opToken.type))
        {
            opCodes.add(relationOperator.get(opToken.type));
            opCodes.add(branchingOperator.get(opToken.type));
        }
        else if(assignmentOperator.containsKey(opToken.type))
        {
            opCodes.add(assignmentOperator.get(opToken.type));
        }
        else
        {
            throw new IllegalArgumentException("Operator Token not found.");
        }
        return opCodes;
    };

    public static ArrayList<OperatorCode> getOpCode(String operator)
    {
        ArrayList<OperatorCode> opCodes = new ArrayList<OperatorCode>();
        if(standardIoOperator.containsKey(operator)){
            opCodes.add(standardIoOperator.get(operator));
        }
        else
        {
            throw new IllegalArgumentException("Operator Token not found.");
        }
        return opCodes;
    };
}