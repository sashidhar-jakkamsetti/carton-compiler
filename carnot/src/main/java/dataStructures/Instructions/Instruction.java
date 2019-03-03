package dataStructures.Instructions;

import dataStructures.Results.*;
import dataStructures.Operator.OperatorCode;

public class Instruction 
{
    public Integer id;
    public OperatorCode opcode;
    public IResult operandX;
    public IResult operandY;

    public DeleteMode deleteMode;
    public Instruction akaI;

    public enum DeleteMode
    {
        CP, // Copy propagation
        CSE, // Common subexpression elimination
        DCE, // Dead code elimination
        NUMBER, // Result of instruction is some number, so replace it with the number directly. 
                // E.g: 3: move #10 (3) --> use #10 instead of (3)
        _NotDeleted // NOT deleted
    }

    public Instruction(Integer programCounter)
    {
        id = programCounter;
    }

    public Instruction(Integer programCounter, OperatorCode opcode, IResult x, IResult y)
    {
        id = programCounter;
        this.opcode = opcode;
        operandX = x;
        operandY = y;
        deleteMode = DeleteMode._NotDeleted;

        akaI.id = id;
        akaI.opcode = opcode;
    }

    public void setAkaInstruction(Instruction akaInstruction) 
    {
        this.akaI = akaInstruction;
    }

    public void setAkaInstruction(IResult x, IResult y)
    {
        setAkaInstructionOperand(x);
        setAkaInstructionOperand(y);
    }

    public void setAkaInstructionOperand(IResult x)
    {
        if(x != null)
        {
            if(x instanceof VariableResult && !((VariableResult)x).isArray)
            {
                akaI.operandX = new InstructionResult(((VariableResult)x).variable.version);
            }
            else 
            {
                akaI.operandX = x;
            }
        }
    }

    @Override
    public String toString()
    {
        if(deleteMode == DeleteMode._NotDeleted)
        {
            return toStringUtil();
        }
        else
        {
            return null;
        }
    }

    private String toStringUtil()
    {
        String ret = "";
        if(operandX != null && operandY != null)
        {
            ret = String.format("%s : %s %s %s", id, opcode.toString(), operandX.toString(), operandY.toString());
        }
        else if(operandX != null && operandY == null)
        {
            ret = String.format("%s : %s %s", id, opcode.toString(), operandX.toString());
        }
        else if(operandX == null && operandY != null)
        {
            ret = String.format("%s : %s %s", id, opcode.toString(), operandY.toString());
        }
        else 
        {
            ret = String.format("%s : %s", id, opcode.toString());
        }
        return ret;
    }

    public Boolean equals(Instruction instruction)
    {
        if(opcode == instruction.opcode)
        {
            if(operandX == null && operandY == null)
            {
                return instruction.operandX == null && instruction.operandY == null;
            }
            else if(operandX == null)
            {
                return instruction.operandX == null && operandY.equals(instruction.operandY);
            }
            else if(operandY == null)
            {
                return instruction.operandY == null && operandX.equals(instruction.operandX);
            }
            else
            {
                return operandX.equals(instruction.operandX) && operandY.equals(instruction.operandY);
            }
        }

        return false;
    }
}
