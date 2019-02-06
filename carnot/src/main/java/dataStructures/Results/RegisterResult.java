package dataStructures.Results;

public class RegisterResult implements IResult
{
    public Integer register;

    public RegisterResult()
    {
        register = -1;
    }

    @Override
    public void set(Object value) 
    {
        register = (Integer)value;
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