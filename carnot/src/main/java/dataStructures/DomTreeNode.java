package dataStructures;

import java.util.ArrayList;
import java.util.HashMap;

import dataStructures.Instructions.Instruction;
import dataStructures.Operator.OperatorCode;
import dataStructures.Results.VariableResult;

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

    public void addKill(ArrayList<Instruction> kill)
    {
        if(!instructions.containsKey(OperatorCode.load))
        {
            instructions.put(OperatorCode.load, new ArrayList<Instruction>());
        }
        for (Instruction k : kill) 
        {
            instructions.get(OperatorCode.load).add(0, k.clone());
        }
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
        else
        {
            if(!instructions.containsKey(instruction.opcode))
            {
                instructions.put(instruction.opcode, new ArrayList<Instruction>());
            }
            instructions.get(instruction.opcode).add(instruction.clone());
        }
    }

    public void delete(Instruction instruction)
    {
        if(instructions.containsKey(instruction.opcode))
        {
            ArrayList<Instruction> is = instructions.get(instruction.opcode);
            is.removeIf(i -> i.id.equals(instruction.id));
        }
    }

    public Instruction find(Instruction instruction)
    {
        if(instruction.opcode == OperatorCode.store)
        {
            return new Instruction(-2);
        }

        if(instruction.opcode == OperatorCode.load)
        {
            if(instructions.containsKey(OperatorCode.load))
            {
                for(Integer idx = instructions.get(instruction.opcode).size() - 1; idx >= 0; idx--)
                {
                    if(instructions.get(OperatorCode.load).get(idx).opcode == OperatorCode.store)
                    {
                        Instruction investigator = instructions.get(OperatorCode.load).get(idx);
                        if(instruction.operandX instanceof VariableResult && investigator.operandX instanceof VariableResult)
                        {
                            VariableResult xR = (VariableResult)instruction.operandX;
                            VariableResult yR = (VariableResult)investigator.operandX;
    
                            if(xR.isArray && yR.isArray && xR.variable.address == yR.variable.address)
                            {
                                return new Instruction(-2);
                            }
                        }
                    }
                    else
                    {
                        if(instructions.get(OperatorCode.load).get(idx).operandY.equals(instruction.operandY))
                        {
                            return instructions.get(instruction.opcode).get(idx).clone();
                        }
                    }
                }
            }
            
            return new Instruction(-1);
        }

        if(instructions.containsKey(instruction.opcode))
        {
            for(Integer idx = instructions.get(instruction.opcode).size() - 1; idx >= 0; idx--)
            {
                if(instructions.get(instruction.opcode).get(idx).equals(instruction))
                {
                    return instructions.get(instruction.opcode).get(idx).clone();
                }
            }
        }
        
        return new Instruction(-1);
    }
}