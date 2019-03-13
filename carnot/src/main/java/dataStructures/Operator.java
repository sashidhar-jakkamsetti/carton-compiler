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

    public static final HashSet<OperatorCode> arthimeticOpCodes = new HashSet<OperatorCode>()
    {
        {
            add(OperatorCode.add);
            add(OperatorCode.sub);
            add(OperatorCode.mul);
            add(OperatorCode.div);
            add(OperatorCode.cmp);
        }
    };

    public static final HashMap<TokenType, OperatorCode> branchingOperator = new HashMap<TokenType, OperatorCode>()
    {
        {
            put(TokenType.eqlToken, OperatorCode.bne);
            put(TokenType.neqToken, OperatorCode.beq);
            put(TokenType.lssToken, OperatorCode.bge);
            put(TokenType.leqToken, OperatorCode.bgt);
            put(TokenType.gtrToken, OperatorCode.ble);
            put(TokenType.geqToken, OperatorCode.blt);

            put(TokenType.thenToken, OperatorCode.bra);
            put(TokenType.doToken, OperatorCode.bra);
            put(TokenType.callToken, OperatorCode.bra);
        }
    };

    public static final HashSet<OperatorCode> branchOpCodes = new HashSet<OperatorCode>()
    {
        {
            add(OperatorCode.bne);
            add(OperatorCode.beq);
            add(OperatorCode.bge);
            add(OperatorCode.bgt);
            add(OperatorCode.ble);
            add(OperatorCode.blt);
            add(OperatorCode.bra);
        }
    };

    public static final HashMap<TokenType, OperatorCode> assignmentOperator = new HashMap<TokenType, OperatorCode>()
    {
        {
            put(TokenType.letToken, OperatorCode.move);
            put(TokenType.becomesToken, OperatorCode.move);
            put(TokenType.returnToken, OperatorCode.move);
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

    public static OperatorCode getOpCode(Token opToken)
    {
        if(arithmeticOperator.containsKey(opToken.type))
        {
            return arithmeticOperator.get(opToken.type);
        }
        else if(relationOperator.containsKey(opToken.type))
        {
            return relationOperator.get(opToken.type);
        }
        else if(assignmentOperator.containsKey(opToken.type))
        {
            return assignmentOperator.get(opToken.type);
        }
        else if(standardIoOperator.containsKey(opToken.value))
        {
            return standardIoOperator.get(opToken.value);
        }
        else if(opToken.isSameType(TokenType.periodToken))
        {
            return OperatorCode.end;
        }

        return null;
    };
}