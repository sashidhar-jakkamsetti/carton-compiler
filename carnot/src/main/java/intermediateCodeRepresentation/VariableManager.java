package intermediateCodeRepresentation;

import java.util.*;

import dataStructures.ArrayVar;
import exceptions.*;
import utility.Constants;

@SuppressWarnings("serial")
public class VariableManager
{
    private HashSet<Integer> variables;
    private HashMap<Integer, ArrayVar> arrays;
    private HashMap<Integer, Integer> ssaMap;
    private Integer arrayAddress;

    public VariableManager()
    {
        variables = new HashSet<Integer>();
        ssaMap = new HashMap<Integer, Integer>();
        arrays = new HashMap<Integer, ArrayVar>();
        arrayAddress = Constants.ARRAY_ADDRESS_OFFSET;
    }

    public void addVariable(Integer variable) throws IllegalVariableException
    {
        if(variables.contains(variable))
        {
            throw new IllegalVariableException("Variable: " + variable + " already declared!");
        }
        else
        {
            variables.add(variable);
        }
    }

    public HashSet<Integer> getVariables()
    {
        return variables;
    } 

    public boolean isVariable(Integer variable)
    {
        return variables.contains(variable);
    }

    public void addArray(Integer variable, ArrayVar arrayVar) throws IllegalVariableException
    {
        if(arrays.containsKey(variable))
        {
            throw new IllegalVariableException("Array: " + variable + " already declared!");
        }
        else
        {
            arrayVar.address = arrayAddress;
            arrayAddress += arrayVar.arraySize;
            arrays.put(variable, arrayVar);
            variables.add(variable);
        }
    }

    public boolean isArray(Integer variable)
    {
        if(variables.contains(variable))
        {
            return arrays.containsKey(variable);
        }
        return false;
    }

    public ArrayVar getArray(Integer variable)
    {
        return arrays.get(variable);
    }

    public void updateSsaMap(Integer variable, Integer version)
    {
        ssaMap.put(variable, version);
    }

    public void setSsaMap(HashMap<Integer, Integer> restoreSsa)
    {
        ssaMap.clear();
        ssaMap.putAll(restoreSsa);
    }

    public Integer getSsaVersion(Integer variable)
    {
        if(ssaMap.containsKey(variable))
        {
            return ssaMap.get(variable);
        }

        return -1;
    }

    public HashMap<Integer, Integer> getSsaMap()
    {
        return ssaMap;
    }

    public void copySsaTo(HashMap<Integer, Integer> copy)
    {
        copy.putAll(ssaMap);
    }
}