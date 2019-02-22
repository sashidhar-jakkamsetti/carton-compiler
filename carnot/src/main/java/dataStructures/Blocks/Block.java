package dataStructures.Blocks;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Results.*;
import dataStructures.Instructions.*;
import intermediateCodeRepresentation.*;

import java.util.*;

public class Block implements IBlock
{
    protected Integer id;
    protected List<Instruction> instructions;
    protected Block parent;
    protected Block child;

    public Block(Integer id)
    {
        this.id = id;
        instructions = new ArrayList<Instruction>();
        parent = null;
        child = null;
    }

    public Integer getId()
    {
        return id;
    }

    public List<Instruction> getInstructions()
    {
        return instructions;
    }

    public void addInstruction(Instruction instruction)
    {
        instructions.add(instruction);
    }

    public void addInstruction(ArrayList<Instruction> instruction)
    {
        instructions.addAll(instruction);
    }

    public Instruction getInstruction(int programCounter)
    {
        return (Instruction)instructions.stream().filter(instruction -> instruction.id == programCounter).toArray()[0];
    }

    public void setParent(IBlock block)
    {
        parent = (Block)block;
    }

    public IBlock getParent()
    {
        return parent;
    }

    public void setChild(IBlock block)
    {
        child = (Block)block;
    }

    public IBlock getChild(IBlock block)
    {
        return child;
    }

    public String toString()
    {
        return "";
    }
}