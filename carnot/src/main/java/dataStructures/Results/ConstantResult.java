package dataStructures.Results;

public class ConstantResult implements IResult
{
    public Integer constant;

    public ConstantResult() 
    {
        constant = 0;
    }

    @Override
    public void set(Object value) 
    {
        constant = (Integer)value;
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