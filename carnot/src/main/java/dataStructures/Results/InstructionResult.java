package dataStructures.Results;

public class InstructionResult implements IResult
{
    //public Instruction instruction;
    public Integer iid;

    public InstructionResult()
    {
        iid = -1;
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
        String res = "(" + iid.toString() + ")";
        return res;
    }
}