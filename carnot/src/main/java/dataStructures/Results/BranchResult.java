package dataStructures.Results;

import dataStructures.Token;
import dataStructures.Blocks.IBlock;
import dataStructures.Token.TokenType;

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
        return null;
    }

    @Override
    public Integer compareTo(IResult result) 
    {
        return null;
    }

    @Override
    public IResult toInstruction()
    {
        return new InstructionResult(iid);
    }

    @Override 
    public String toString()
    {
        return "";
    }

}