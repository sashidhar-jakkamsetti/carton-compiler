package dataStructures.Results;

public class InstructionResult implements IResult
{
    public Integer iid;

    public InstructionResult()
    {
        iid = -1;
    }

    public InstructionResult(Integer iid)
    {
        this.iid = iid;
    }
    
    @Override
    public void set(Object value) 
    {
        iid = (Integer)value;
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
        InstructionResult iResult = new InstructionResult(iid);
        return (IResult)iResult;
    }

    @Override
    public Boolean equals(IResult result) 
    {
        if(result instanceof InstructionResult)
        {
            InstructionResult iResult = (InstructionResult)result;
            return iid == iResult.iid;
        }
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
        String res = "(" + iid.toString() + ")";
        return res;
    }
}