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
    public Instruction akaInstruction;

    public enum DeleteMode
    {
        CP, // Copy propagation
        CSE, // Common subexpression elimination
        DCE, // Dead code elimination
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

        akaInstruction.id = id;
        akaInstruction.opcode = opcode;
    }

    public void setAkaInstruction(Instruction akaInstruction) 
    {
        this.akaInstruction = akaInstruction;
    }

    public void setAkaInstruction(IResult x, IResult y)
    {
        setAkaInstructionOperand(x);
        setAkaInstructionOperand(y);
    }

    public void setAkaInstructionOperand(IResult x)
    {
        if(x instanceof VariableResult)
        {
            akaInstruction.operandX = new InstructionResult(((VariableResult)x).variable.version);
        }
        else 
        {
            akaInstruction.operandX = x;
        }
    }

    @Override
    public String toString()
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
            if(operandX.equals(instruction.operandX) && operandY.equals(instruction.operandY))
            {
                return true;
            }
        }

        return false;
    }
}
