package dataStructures.Blocks;

import dataStructures.*;
import dataStructures.Instructions.*;
import dataStructures.Instructions.Instruction.DeleteMode;

import java.util.*;

public class Block implements IBlock
{
    protected Integer id;
    protected List<Instruction> instructions;
    protected Block parent;
    protected Block child;
    protected HashMap<Integer, Integer> globalSsa;
    protected HashMap<Integer, Integer> localSsa;
    protected DomTreeNode dTreeNode;

    public Block(Integer id)
    {
        this.id = id;
        instructions = new ArrayList<Instruction>();
        parent = null;
        child = null;
        dTreeNode = new DomTreeNode(id);
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
        this.instructions.add(instruction);
    }

    public void addInstruction(ArrayList<Instruction> instructions)
    {
        this.instructions.addAll(instructions);
    }

    public void addInstruction(Instruction instruction, Integer idx)
    {
        if(idx < instructions.size())
        {
            this.instructions.add(idx, instruction);
        }
    }

    public void setParent(IBlock block)
    {
        parent = (Block)block;
        dTreeNode.setParent(block.getDomTreeNode());
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

    public DomTreeNode getDomTreeNode()
    {
        return dTreeNode;
    }

    @Override
    public Instruction getInstruction(Integer programCounter)
    {
        Optional<Instruction> filteredInstruction = instructions.stream().filter(instruction -> instruction.id == programCounter).findFirst();
        if(filteredInstruction.isPresent())
        {
            return filteredInstruction.get();
        }

        return null;
    }

    public String toString(Boolean optimized, Boolean dce, Boolean colored, Boolean mCode)
    {
        return toStringUtil(this.instructions, optimized, dce, colored, mCode);
    }

    protected String toStringUtil(List<Instruction> instructions, Boolean optimized, Boolean dce, Boolean colored, Boolean mCode)
    {
        StringBuilder sb = new StringBuilder();
        String instructionString = "";
        for(Instruction instruction : instructions)
        {
            if(mCode)
            {

            }
            else if(colored)
            {
                if(instruction.deleteMode == DeleteMode._NotDeleted)
                {
                    instructionString = instruction.coloredI.toString();
                }
            }
            else if(optimized)
            {
                if(dce)
                {
                    if(instruction.deleteMode == DeleteMode._NotDeleted)
                    {
                        instructionString = instruction.akaI.toString();
                    }
                }
                else
                {
                    if(instruction.deleteMode == DeleteMode._NotDeleted || instruction.deleteMode == DeleteMode.DCE)
                    {
                        instructionString = instruction.akaI.toString();
                    }
                }
            }
            else 
            {
                instructionString = instruction.toString();
            }

            if(instructionString != null && instructionString != "")
            {
                sb.append(instructionString + "\\l");
                instructionString = "";
            }
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

    public Instruction searchCommonSubexpression(Instruction instruction)
    {
        Instruction cSubexpression = dTreeNode.find(instruction);

        if(cSubexpression.id != -2)
        {
            if(cSubexpression.id == -1)
            {
                if(parent != null)
                {
                    cSubexpression = parent.searchCommonSubexpression(instruction);
                }
            }
            else
            {
                return cSubexpression;
            }
        }

        return null;
    }

    public void addSubexpression(Instruction instruction)
    {
        dTreeNode.add(instruction);
    }
}