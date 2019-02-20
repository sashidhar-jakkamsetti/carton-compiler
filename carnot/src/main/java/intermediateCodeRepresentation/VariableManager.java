package intermediateCodeRepresentation;

import java.util.*;

import dataStructures.Array;
import exceptions.*;

public class VariableManager
{
    private static HashSet<Integer> variables;
    private static HashSet<Integer> globalVariables;
    private static HashMap<Integer, Array> arrays;
    private static HashMap<Integer, ArrayList<Integer>> ssaMap;
    private static HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> defUseChain;

    public VariableManager()
    {
        variables = new HashSet<Integer>();
        globalVariables = new HashSet<Integer>();
        ssaMap = new HashMap<Integer, ArrayList<Integer>>();
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

    @SuppressWarnings("serial")
    public void updateSsaMap(Integer variable, Integer version)
    {
        if(ssaMap.containsKey(variable))
        {
            ssaMap.get(variable).add(version);
        }
        else
        {
            ssaMap.put(variable, new ArrayList<Integer>() 
                {{
                    add(version);
                }}
            );
        }
    }

    public Integer getSsaVersion(Integer variable)
    {
        if(ssaMap.containsKey(variable))
        {
            Integer lastIndex = ssaMap.get(variable).size() - 1;
            return ssaMap.get(variable).get(lastIndex);
        }

        return -1;
    }

    @SuppressWarnings("serial")
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