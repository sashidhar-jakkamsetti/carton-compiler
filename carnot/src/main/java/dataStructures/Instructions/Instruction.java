package dataStructures.Instructions;

import dataStructures.Results.*;
import dataStructures.Variable;
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

        akaI = new Instruction(programCounter);
        akaI.opcode = opcode;
    }

    public void setAkaInstruction(Instruction akaInstruction) 
    {
        this.akaI = akaInstruction;
    }

    public void setAkaInstruction(IResult x, IResult y)
    {
        setAkaInstructionOperand(x, true);
        setAkaInstructionOperand(y, false);
    }

    public void setAkaInstructionOperand(IResult r, Boolean bOperandX)
    {
        if(r != null)
        {
            // Arrays and formal parameters are not condensed to intruction results. What about global variables?
            if(r instanceof VariableResult && !((VariableResult)r).isArray && ((VariableResult)r).variable.version != -1)
            {
                Variable v = ((VariableResult)r).variable;
                if(bOperandX)
                {
                    akaI.operandX = new InstructionResult(v.version);
                }
                else
                {
                    akaI.operandY = new InstructionResult(v.version);
                }
            }
            else 
            {
                if(bOperandX)
                {
                    akaI.operandX = r;
                }
                else
                {
                    akaI.operandY = r;
                }
            }
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
                Boolean res = operandX.equals(instruction.operandX) && operandY.equals(instruction.operandY);
                if((opcode == OperatorCode.mul || opcode == OperatorCode.add) && !res)
                {
                    res = operandX.equals(instruction.operandY) && operandY.equals(instruction.operandX);
                }
                return res;
            }
        }

        return false;
    }
}
