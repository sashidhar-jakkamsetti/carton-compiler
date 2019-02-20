package dataStructures.Results;

public class RegisterResult implements IResult
{
    public Integer register;
    public Integer iid;

    public RegisterResult()
    {
        register = -1;
        iid = -1;
    }

    @Override
    public void set(Object value) 
    {
        register = (Integer)value;
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
        return "";
    }
}