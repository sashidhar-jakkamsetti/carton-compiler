package dataStructures.Results;

import dataStructures.ArrayVar;
import dataStructures.Variable;

public class VariableResult implements IResult
{
    public Variable variable;
    public boolean isArray;
    public Integer iid;

    public VariableResult() 
    {
        variable = null;
        isArray = false;
        iid = -1;
    }

    @Override
    public void set(Object value) 
    {
        variable = (Variable)value;
        if(value instanceof ArrayVar)
        {
            isArray = true;
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
    public Boolean equals(IResult result) 
    {
        return false;
    }

    @Override
    public IResult toInstruction()
    {
        return new InstructionResult(iid);
    }

    @Override 
    public String toString()
    {
        return variable.toString();
    }
}