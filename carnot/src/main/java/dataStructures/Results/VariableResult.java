package dataStructures.Results;

import dataStructures.Variable;

public class VariableResult implements IResult
{
    public Variable variable;

    public VariableResult() 
    {
        variable = null;
    }

    @Override
    public void set(Object value) 
    {
        variable = (Variable)value;
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