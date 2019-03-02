package dataStructures.Results;

public class ConstantResult implements IResult
{
    public Integer constant;
    public Integer iid;

    public ConstantResult() 
    {
        constant = 0;
        iid = -1;
    }

    public ConstantResult(Integer constant) 
    {
        this.constant = constant;
        iid = -1;
    }

    @Override
    public void set(Object value) 
    {
        if(value instanceof String)
        {
            constant = Integer.parseInt((String)value);
        }
        else if(value instanceof Integer)
        {
            constant = (Integer)value;
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
        ConstantResult cResult = new ConstantResult();
        cResult.iid = iid;
        cResult.constant = constant;
        return (IResult)cResult;
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
        return constant.toString();
    }
}