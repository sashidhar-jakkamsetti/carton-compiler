package dataStructures;

import java.util.ArrayList;
import java.util.HashMap;

import dataStructures.Instructions.Instruction;
import dataStructures.Operator.OperatorCode;

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
        if(instruction.opcode == OperatorCode.store)
        {
            if(!instructions.containsKey(OperatorCode.load))
            {
                instructions.put(OperatorCode.load, new ArrayList<Instruction>());
            }
            instructions.get(OperatorCode.load).add(instruction.clone());
        }
        if(!instructions.containsKey(instruction.opcode))
        {
            instructions.put(instruction.opcode, new ArrayList<Instruction>());
        }
        instructions.get(instruction.opcode).add(instruction.clone());
    }

    public void delete(Instruction instruction)
    {
        if(instructions.containsKey(instruction.opcode))
        {
            ArrayList<Instruction> is = instructions.get(instruction.opcode);
            is.removeIf(i -> i.id == instruction.id);
        }
    }

    public Instruction find(Instruction instruction)
    {
        if(instruction.opcode != OperatorCode.store && instructions.containsKey(instruction.opcode))
        {
            for(Integer idx = instructions.get(instruction.opcode).size() - 1; idx >= 0; idx--)
            {
                // TODO: Kill not working
                if(instruction.opcode == OperatorCode.load 
                        && instructions.get(instruction.opcode).get(idx).opcode == OperatorCode.store)
                {
                    return null;
                }
                
                if(instructions.get(instruction.opcode).get(idx).equals(instruction))
                {
                    return instructions.get(instruction.opcode).get(idx);
                }
            }
        }
        
        return null;
    }
}