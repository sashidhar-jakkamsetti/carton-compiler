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
    public Integer lastMCode;
    public VariableManager vManager;
    public HashMap<Integer, IResult> tamperedGlobals;
    public HashMap<Integer, Integer> globalLog;
    public InstructionResult returnInstruction;
    private ArrayList<IResult> parameters;

    public Function(Block head, Block tail)
    {
        this.head = head;
        this.tail = tail;
        lastMCode = -1;
        address = -1;
    }

    public Function(String name, Integer address)
    {
        this.name = name;
        this.address = address;
        head = null;
        tail = null;
        lastMCode = -1;
        vManager = new VariableManager();
        tamperedGlobals = new HashMap<Integer, IResult>();
        globalLog = new HashMap<Integer, Integer>();
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

    public ArrayList<IResult> getParameters()
    {
        return parameters;
    }
}