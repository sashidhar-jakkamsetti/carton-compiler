package intermediateCodeRepresentation;

import java.util.*;

import dataStructures.Array;
import exceptions.*;

@SuppressWarnings("serial")
public class VariableManager
{
    private static HashSet<Integer> variables;
    private static HashSet<Integer> globalVariables;
    private static HashMap<Integer, Array> arrays;
    private static HashMap<Integer, Integer> ssaMap;
    private static HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> defUseChain;

    public VariableManager()
    {
        variables = new HashSet<Integer>();
        globalVariables = new HashSet<Integer>();
        ssaMap = new HashMap<Integer, Integer>();
        defUseChain = new HashMap<Integer, HashMap<Integer, ArrayList<Integer>>>();
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

    public boolean isArray(Integer array)
    {
        return arrays.containsKey(array);
    }
    
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