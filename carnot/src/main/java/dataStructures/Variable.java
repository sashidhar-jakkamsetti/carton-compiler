package dataStructures;

public class Variable
{
    public String name;
    public Integer version;
    public Integer address;

    public Variable() {}

    public Variable(String name, Integer address)
    {
        this.name = name;
        this.address = address;
        this.version = -1;
    }

    public Variable(String name, Integer address, Integer version)
    {
        this.name = name;
        this.address = address;
        this.version = version;
    }

    @Override
    public Variable clone()
    {
        Variable v = new Variable();
        v.name = name;
        v.version = version;
        v.address = address;
        return v;
    }

    @Override
    public String toString()
    {
        return String.format("%s_%s", name, version);
    }
}