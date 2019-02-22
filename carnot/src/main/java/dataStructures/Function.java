package dataStructures;

import java.util.*;

import dataStructures.Blocks.*;
import dataStructures.Instructions.Instruction;
import dataStructures.Results.*;
import intermediateCodeRepresentation.VariableManager;

public class Function 
{
    public String name;
    public Integer address;

    public Block head;
    public VariableManager vManager;
    public InstructionResult returnInstruction;
    private ArrayList<IResult> parameters;

    public Function(String name, Integer address)
    {
        this.name = name;
        this.address = address;
        head = null;
        vManager = new VariableManager();
        parameters = new ArrayList<IResult>();
    }

    public void setGlobalVariables(HashSet<Integer> gVariables)
    {
        vManager.setGlobalVariables(gVariables);
    }

    public void addParameter(IResult result)
    {
        parameters.add(result);
    }

    public IResult getParameter(Integer index)
    {
        return parameters.get(index);
    }
}