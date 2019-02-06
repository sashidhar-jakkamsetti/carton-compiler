package dataStructures.Results;

public interface IResult
{
    public void set(Object value);
    public IResult clone();
    public Integer compareTo(IResult result);
    public String toString();
}