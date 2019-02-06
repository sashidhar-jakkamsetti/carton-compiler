package dataStructures.Results;

import dataStructures.Blocks.IBlock;

public class BranchResult implements IResult
{
    public Integer condition;
    public Integer fixuplocation;
    public IBlock targetBlock;

    public BranchResult() 
    {
        condition = 0;
        fixuplocation = -1;
        targetBlock = null;
    }

    public BranchResult(Integer condition, Integer fixuplocation, IBlock targetBlock)
    {
        this.condition = condition;
        this.fixuplocation = fixuplocation;
        this.targetBlock = targetBlock;
    }

    @Override
    public void set(Object value) 
    {
        this.fixuplocation = (Integer)value;
    }

    public void set(IBlock block)
    {
        this.targetBlock = block;
    }

    public void set(Integer fixuploc, IBlock block)
    {
        this.fixuplocation = fixuploc;
        this.targetBlock = block;
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