package dataStructures.Results;

import dataStructures.Instructions.Instruction;

public class InstructionResult implements IResult
{
    public Instruction instruction;

    public InstructionResult()
    {
        instruction = null;
    }
    
    @Override
    public void set(Object value) 
    {
        instruction = (Instruction)value;
    }

    @Override
    public IResult clone() 
    {
        return null;
    }

    @Override
    public Integer compareTo(IResult result) 
    {
        return null;
    }

    @Override 
    public String toString()
    {
        return "";
    }
}