package dataStructures.Results;

public class InstructionResult implements IResult
{
    //public Instruction instruction;
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
        InstructionResult iResult = new InstructionResult();
        iResult.iid = iid;
        return (IResult)iResult;
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
        String res = "(" + iid.toString() + ")";
        return res;
    }
}