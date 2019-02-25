package dataStructures.Results;

import dataStructures.Variable;

public class VariableResult implements IResult
{
    public Variable variable;
    public Integer iid;

    public VariableResult() 
    {
        variable = null;
        iid = -1;
    }

    @Override
    public void set(Object value) 
    {
        variable = (Variable)value;
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
        InstructionResult result = new InstructionResult();
        result.set(iid);

        return result;
    }

    @Override 
    public String toString()
    {
        return variable.toString();
    }
}