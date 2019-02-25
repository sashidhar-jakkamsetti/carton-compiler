package intermediateCodeRepresentation;

import java.util.*;

import dataStructures.ArrayVar;
import exceptions.*;

@SuppressWarnings("serial")
public class VariableManager
{
    private HashSet<Integer> variables;
    private HashSet<Integer> globalVariables;
    private HashMap<Integer, ArrayVar> arrays;
    private HashMap<Integer, Integer> ssaMap;
    private HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> defUseChain;
    private Integer arrayAddress;

    public VariableManager()
    {
        variables = new HashSet<Integer>();
        globalVariables = new HashSet<Integer>();
        ssaMap = new HashMap<Integer, Integer>();
        defUseChain = new HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();
        arrays = new HashMap<Integer, ArrayVar>();
        arrayAddress = 1000; // Random
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

    public void setGlobalVariables(HashSet<Integer> gVariables)
    {
        globalVariables = gVariables;
    }

    public HashSet<Integer> getGlobalVariables()
    {
        return globalVariables;
    }

    public HashSet<Integer> getVariables()
    {
        return variables;
    } 

    public boolean isGlobalVariable(Integer variable)
    {
        return globalVariables.contains(variable);
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

    // TODO: SSA for arrays
    public void updateSsaMap(Integer variable, Integer version)
    {
        ssaMap.put(variable, version);
    }

    public void setSsaMap(HashMap<Integer, Integer> restoreSsa)
    {
        ssaMap = restoreSsa;
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

    public void updateDefUseChain(Integer variable, Integer defInstruction, Integer useInstruction)
    {
        if(defUseChain.containsKey(variable))
        {
            if(defUseChain.get(variable).containsKey(defInstruction))
            {
                defUseChain.get(variable).get(defInstruction).add(useInstruction);
            }
            else 
            {
                defUseChain.get(variable).put(
                    defInstruction, new ArrayList<Integer>() 
                        {{
                            add(useInstruction);
                        }}
                    );
            }
        }
        else
        {
            defUseChain.put(
                variable, new HashMap<Integer, ArrayList<Integer>>() 
                    {{
                        put(
                            defInstruction, new ArrayList<Integer>() 
                            {{
                                add(useInstruction);
                            }}
                        );
                    }}
                );
        }
    }
}