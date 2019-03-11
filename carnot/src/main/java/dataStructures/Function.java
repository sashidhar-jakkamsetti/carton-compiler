package dataStructures;

import java.util.*;

import dataStructures.Blocks.*;
import dataStructures.Results.*;
import intermediateCodeRepresentation.VariableManager;

public class Function 
{
    public String name;
    public Integer address;

    public Block head;
    public Block tail;
    public VariableManager vManager;
    public InstructionResult returnInstruction;
    private ArrayList<IResult> parameters;

    public Function(Block head, Block tail)
    {
        this.head = head;
        this.tail = tail;
    }

    public Function(String name, Integer address)
    {
        this.name = name;
        this.address = address;
        head = null;
        vManager = new VariableManager();
        returnInstruction = null;
        parameters = new ArrayList<IResult>();
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