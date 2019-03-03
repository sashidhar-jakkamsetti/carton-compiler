package dataStructures.Results;

import dataStructures.Token;
import dataStructures.Blocks.IBlock;

public class BranchResult implements IResult
{
    public Token condition;
    public Integer fixuplocation;
    public IBlock targetBlock;
    public Integer iid;

    public BranchResult() 
    {
        condition = null;
        fixuplocation = -1;
        targetBlock = null;
        iid = -1;
    }

    public BranchResult(Token condition, Integer fixuplocation, IBlock targetBlock)
    {
        super();
        this.condition = condition;
        this.fixuplocation = fixuplocation;
        this.targetBlock = targetBlock;
    }

    @Override
    public void set(Object value) 
    {
        if(value instanceof Integer)
        {
            this.fixuplocation = (Integer)value;
        }
        else if(value instanceof IBlock)
        {
            this.targetBlock = (IBlock)value;
        }
        
    }

    @Override
    public void setIid(Integer iid) 
    {
        this.iid = iid;
    }

    @Override
    public Integer getIid()
    {
        return iid;
    }

    @Override
    public IResult clone() 
    {
        BranchResult bResult = new BranchResult();
        bResult.iid = iid;
        // TODO: I should clone targetBlock properly...
        if(targetBlock != null)
        {
            bResult.targetBlock = targetBlock;
        }
        else
        {
            bResult.targetBlock = null;
        }
        bResult.condition = condition;
        bResult.fixuplocation = fixuplocation;
        return (IResult)bResult;
    }

    @Override
    public Boolean equals(IResult result) 
    {
        BranchResult bResult = (BranchResult)result;

        return condition.type == bResult.condition.type && targetBlock.getId() == bResult.targetBlock.getId();
    }

    @Override
    public IResult toInstruction()
    {
        return new InstructionResult(iid);
    }

    @Override 
    public String toString()
    {
        String res = "[" + targetBlock.getId().toString() + "]";
        return res;
    }

}