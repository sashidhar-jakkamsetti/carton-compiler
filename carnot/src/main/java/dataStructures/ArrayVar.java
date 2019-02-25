package dataStructures;

import java.util.ArrayList;

import dataStructures.Results.IResult;

public class ArrayVar extends Variable
{
    public ArrayList<Integer> dimentionList;
    public ArrayList<IResult> indexList;
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

    public ArrayVar(String name, Integer address, Integer version)
    {
        this.name = name;
        this.address = address;
        this.version = version;
    }

    public Variable getBaseAddress()
    {
        return new Variable(name, address, version);
    }

    public Variable getElementAddress(ArrayList<Integer> indexList)
    {
        Integer eAddress = address;
        StringBuilder sb = new StringBuilder();
        for (Integer index : indexList) 
        {
            eAddress += index * 4;
            sb.append("[" + index.toString() +"]");
        }

        return new Variable(String.format("%s_%s", name, sb.toString()), eAddress, version);
    }

    @Override
    public String toString()
    {
        return String.format("%s_%s", name, version);
    }
}