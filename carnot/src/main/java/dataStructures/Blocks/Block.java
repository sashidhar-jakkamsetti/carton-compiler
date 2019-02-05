package dataStructures.Blocks;

import dataStructures.*;
import java.util.*;

public abstract class Block implements IBlock
{
    protected Integer id;
    protected List<Instruction> instructions;
    protected Block parent;
    protected Block child;

    public Integer getId()
    {
        return id;
    }

    public List<Instruction> getInstructions()
    {
        return instructions;
    }

    public void addInstruction(int opcode, Result x, Result y)
    {

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
}