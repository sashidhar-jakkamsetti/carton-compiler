package dataStructures.Results;

public interface IResult
{
    public void set(Object value);
    public IResult clone();
    public void setIid(Integer iid);
    public Integer getIid();
    public Integer compareTo(IResult result);
    public IResult toInstruction();
    public String toString();
}