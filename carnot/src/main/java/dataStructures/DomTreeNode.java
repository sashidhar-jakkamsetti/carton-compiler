package dataStructures;

import java.util.ArrayList;
import java.util.HashMap;

import dataStructures.Instructions.Instruction;

public class DomTreeNode
{
    public Integer id;
    private HashMap<Operator.OperatorCode, ArrayList<Instruction>> instructions;
    private DomTreeNode parent;

    public DomTreeNode(Integer id)
    {
        this.id = id;
        instructions = new HashMap<Operator.OperatorCode, ArrayList<Instruction>>();
        parent = null;
    }

    public DomTreeNode getParent()
    {
        return parent;
    }

    public void setParent(DomTreeNode node)
    {
        parent = node;
    }

    public void add(Instruction instruction)
    {
        if(!instructions.containsKey(instruction.opcode))
        {
            instructions.put(instruction.opcode, new ArrayList<Instruction>());
        }

        instructions.get(instruction.opcode).add(instruction);
    }

    public Instruction find(Instruction instruction)
    {
        if(instructions.containsKey(instruction.opcode))
        {
            for(Integer idx = instructions.get(instruction.opcode).size(); idx >= 0; idx--)
            {
                if(instructions.get(instruction.opcode).get(idx).equals(instruction))
                {
                    return instructions.get(instruction.opcode).get(idx);
                }
            }
        }
        
        return null;
    }
}