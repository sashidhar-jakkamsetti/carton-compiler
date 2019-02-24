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
            arraySize += dim;
        }
    }

    public ArrayVar(String name, Integer address, Integer version, ArrayList<Integer> dimentionList)
    {
        this.name = name;
        this.address = address;
        this.version = version;
        this.dimentionList = dimentionList;
        for(Integer dim : dimentionList)
        {
            arraySize += dim;
        }
    }

    @Override
    public String toString()
    {
        return String.format("{0}_{1}", name, version);
    }
}