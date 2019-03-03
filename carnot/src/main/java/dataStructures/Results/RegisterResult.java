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

    public RegisterResult(Integer regNo)
    {
        register = regNo;
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
    public Boolean equals(IResult result) 
    {
        RegisterResult rResult = (RegisterResult)result;
        return register == rResult.register;
    }

    @Override
    public IResult toInstruction()
    {
        return new InstructionResult(iid);
    }

    @Override 
    public String toString()
    {
        return "R" + register.toString();
    }
}