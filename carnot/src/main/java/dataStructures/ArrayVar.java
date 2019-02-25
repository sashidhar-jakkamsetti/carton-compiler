package dataStructures;

import java.util.ArrayList;

public class ArrayVar extends Variable
{
    // public String name;
    // public Integer version;
    // public Integer address;
    public ArrayList<Integer> dimentionList;
    public Integer arraySize;

    public ArrayVar() {}

    public ArrayVar(String name, Integer address, ArrayList<Integer> dimentionList)
    {
        this.name = name;
        this.address = address;
        this.version = -1;
        this.dimentionList = dimentionList;
        for(Integer dim : dimentionList)
        {
            arraySize *= dim * 4;
        }
    }

    public ArrayVar(String name, Integer address, Integer version, ArrayList<Integer> dimentionList)
    {
        this(name, address, dimentionList);
        this.version = version;
    }

    @Override
    public String toString()
    {
        return String.format("%s_%s", name, version);
    }
}