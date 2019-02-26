package dataStructures.Blocks;

import dataStructures.Instructions.*;

import java.util.*;

public class Block implements IBlock
{
    protected Integer id;
    protected List<Instruction> instructions;
    protected Block parent;
    protected Block child;
    protected HashMap<Integer, Integer> globalSsa;
    protected HashMap<Integer, Integer> localSsa;

    public Block(Integer id)
    {
        this.id = id;
        instructions = new ArrayList<Instruction>();
        parent = null;
        child = null;
        globalSsa = new HashMap<Integer, Integer>();
        localSsa = new HashMap<Integer, Integer>();
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

    public IBlock getChild()
    {
        return child;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for(Instruction instruction : instructions)
        {
            sb.append(instruction.toString() + "\\l");
        }

        return sb.toString();
    }

    public void freezeSsa(HashMap<Integer, Integer> globalSsa, HashMap<Integer, Integer> localSsa)
    {
        this.globalSsa.putAll(globalSsa);

        if(localSsa != null)
        {
            this.localSsa.putAll(localSsa);
        }
    }
    
    public HashMap<Integer, Integer> getGlobalSsa()
    {
        return globalSsa;
    }

    public HashMap<Integer, Integer> getLocalSsa()
    {
        return localSsa;
    }
}