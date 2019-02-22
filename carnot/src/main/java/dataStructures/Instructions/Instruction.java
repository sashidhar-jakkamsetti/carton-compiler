package dataStructures.Instructions;

import dataStructures.Results.*;
import dataStructures.Operator.OperatorCode;

public class Instruction 
{
    public Integer id;
    public OperatorCode opcode;
    public IResult operandX;
    public IResult operandY;
    public boolean isXIntermediateResult;
    public boolean isYIntermediateResult; 

    public Instruction(Integer programCounter, OperatorCode opcode, IResult x, IResult y)
    {
        id = programCounter;
        this.opcode = opcode;
        operandX = x;
        operandY = y;
        isXIntermediateResult = false;
        isYIntermediateResult = false;
    }

    public Instruction(Integer programCounter, OperatorCode opcode, IResult x, IResult y, boolean xInterResult, boolean yInterResult)
    {
        this(programCounter, opcode, x, y);
        isXIntermediateResult = xInterResult;
        isYIntermediateResult = yInterResult;
    }

    @Override
    public String toString()
    {
        String ret = "";
        if(operandX != null && operandY != null)
        {
            ret = String.format("{0} : {1} {2} {3}", id, opcode.toString(), operandX.toString(), operandY.toString());
        }
        else if(operandY == null)
        {
            ret = String.format("{0} : {1} {2}", id, opcode.toString(), operandX.toString());
        }
        return ret;
    }
}
