package dataStructures.Instructions;

import dataStructures.Results.*;

import java.util.*;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Operator.OperatorCode;

public class Instruction 
{
    public Integer id;
    public OperatorCode opcode;
    public IResult operandX;
    public IResult operandY;

    public DeleteMode deleteMode;
    public Instruction akaI; // Holds optimized instruction 
    public Instruction coloredI; // Holds register allocated instruction

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

        coloredI = new Instruction(programCounter);
        coloredI.opcode = opcode;
    }

    public void setAkaInstruction(Instruction akaInstruction) 
    {
        this.akaI = akaInstruction;
    }

    public void setColoredInstruction(Instruction coloredInstruction) 
    {
        this.coloredI = coloredInstruction;
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

    public void setColoredInstruction(HashMap<Integer, LiveRange> iGraph)
    {
        setColoredInstructionOperand(iGraph, true);
        setColoredInstructionOperand(iGraph, false);
    }

    public void setColoredInstructionOperand(HashMap<Integer, LiveRange> iGraph, Boolean bOperandX)
    {
        if(akaI != null)
        {
            if(bOperandX && akaI.operandX != null)
            {
                coloredI.operandX = akaI.operandX;
                if(akaI.operandX instanceof InstructionResult)
                {
                    if(iGraph.containsKey(akaI.operandX.getIid()))
                    {
                        coloredI.operandX = new RegisterResult(iGraph.get(akaI.operandX.getIid()).color);
                    }
                }
            }
            else if(!bOperandX && akaI.operandY != null)
            {
                coloredI.operandY = akaI.operandY;
                if(akaI.operandY instanceof InstructionResult)
                {
                    if(iGraph.containsKey(akaI.operandY.getIid()))
                    {
                        coloredI.operandY = new RegisterResult(iGraph.get(akaI.operandY.getIid()).color);
                    }
                }
                else if(akaI.operandY instanceof BranchResult)
                {
                    // buggy.
                    Boolean isSet = false;
                    IBlock nBlock = ((BranchResult)akaI.operandY).targetBlock;
                    while(!isSet && nBlock != null)
                    {
                        ArrayList<Instruction> targetInstructions = 
                                        (ArrayList<Instruction>)nBlock.getInstructions();
                        for (int i = 0; i < targetInstructions.size(); i++) 
                        {
                            Instruction instruction = targetInstructions.get(i);
                            if(instruction.deleteMode == DeleteMode._NotDeleted)
                            {
                                isSet = true;
                                coloredI.operandY = new InstructionResult(instruction.id);
                                break;
                            }
                        }
                        nBlock = nBlock.getChild();
                    }
                }
            }
        }
    }

    @Override
    public Instruction clone()
    {
        Instruction instr = new Instruction(id);
        instr.opcode = opcode;
        if(operandX != null)
        {
            instr.operandX = operandX.clone();
        }
        if(operandY != null)
        {
            instr.operandY = operandY.clone();
        }
        instr.deleteMode = deleteMode;
        instr.akaI = new Instruction(id);
        if(akaI != null)
        {
            instr.akaI.opcode = akaI.opcode;
            if(akaI.operandX != null)
            {
                instr.akaI.operandX = akaI.operandX.clone();
            }
            if(akaI.operandY != null)
            {
                instr.akaI.operandY = akaI.operandY.clone();
            }
        }
        instr.coloredI = new Instruction(id);
        if(coloredI != null)
        {
            instr.coloredI.opcode = coloredI.opcode;
            if(coloredI.operandX != null)
            {
                instr.coloredI.operandX = coloredI.operandX.clone();
            }
            if(coloredI.operandY != null)
            {
                instr.coloredI.operandY = coloredI.operandY.clone();
            }
        }
        return instr;
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
