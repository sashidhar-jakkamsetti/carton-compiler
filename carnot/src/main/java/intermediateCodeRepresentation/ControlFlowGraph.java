package intermediateCodeRepresentation;

import java.util.*;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;
import dataStructures.Instructions.Instruction.DeleteMode;
import dataStructures.Results.IResult;
import dataStructures.Results.VariableResult;
import utility.Constants;

public class ControlFlowGraph
{
    public Block head;
    public Block tail;
    private List<Block> blocks;
    public VariableManager mVariableManager;
    public Boolean done;

    public List<Function> functions;
    public HashMap<Integer, LiveRange> iGraph;
    private HashMap<Integer, Integer> returnIds;

    private Integer bc;

    public ControlFlowGraph()
    {
        bc = Constants.BLOCK_START_COUNTER;
        head = new Block(bc++, null);
        blocks = new ArrayList<Block>();
        functions = new ArrayList<Function>();
        iGraph = new HashMap<Integer, LiveRange>();
        mVariableManager = new VariableManager();
        returnIds = new HashMap<Integer, Integer>();
        done = false;
        blocks.add(head);
    }

    public Block initializeBlock(Function function)
    {
        Block block = new Block(bc++, function);
        blocks.add(block);

        return block;
    }

    public IfBlock initializeIfBlock(Function function)
    {
        IfBlock block = new IfBlock(bc++, function);
        blocks.add(block);

        return block;
    }

    public JoinBlock initializeJoinBlock(Function function)
    {
        JoinBlock block = new JoinBlock(bc++, function);
        blocks.add(block);

        return block;
    }

    public WhileBlock initializeWhileBlock(Function function)
    {
        WhileBlock block = new WhileBlock(bc++, function);
        blocks.add(block);

        return block;
    }

    public boolean isExists(Function function)
    {
        return functions.stream().anyMatch(f -> f.address.equals(function.address));
    }

    public boolean isExists(Integer function)
    {
        return functions.stream().anyMatch(f -> f.address.equals(function));
    }

    public void addFunction(Function function)
    {
        functions.add(function);
    }

    public Function getFunction(Function function)
    {
        if(isExists(function))
        {
            return (Function)functions.stream().filter(f -> f.address.equals(function.address)).toArray()[0];
        }

        return null;
    }

    public Function getFunction(Integer function)
    {
        if(isExists(function))
        {
            return (Function)functions.stream().filter(f -> f.address.equals(function)).toArray()[0];
        }

        return null;
    }

    public List<Block> getAllBlocks()
    {
        return blocks;
    }

    public Instruction getInstruction(Integer id)
    {
        Instruction rInstruction = null;
        for (IBlock block : blocks) 
        {
            rInstruction = block.getInstruction(id);
            if(rInstruction != null)
            {
                return rInstruction;
            }
        }
        
        return rInstruction;
    }

    public HashMap<Integer, Integer> getAllReturns()
    {
        for (Function f : functions)
        {
            if(f.returnInstruction != null && !returnIds.containsKey(f.returnInstruction.getIid()))
            {
                returnIds.put(f.returnInstruction.getIid(), f.address);
            }
        }
        return returnIds;
    }

    public HashMap<Integer, Integer> getAllFuncFirsts()
    {
        HashMap<Integer, Integer> funcFirst = new HashMap<Integer, Integer>();
        for (Function f : functions) 
        {
            if(f.address > -1)
            {
                Boolean isSet = false;
                IBlock nBlock = f.head;
                while(!isSet && nBlock != null)
                {
                    for (Instruction first : nBlock.getInstructions()) 
                    {
                        if(first.deleteMode == DeleteMode._NotDeleted)
                        {
                            isSet = true;
                            funcFirst.put(first.id, f.address);
                            break;
                        }
                    }
                    nBlock = nBlock.getChild();
                }
            }   
        }
        return funcFirst;
    }

    public HashMap<Integer, Integer> getParams2Func()
    {
        HashMap<Integer, Integer> params2Func = new HashMap<Integer, Integer>();
        for (Function f : functions) 
        {
            for(IResult result : f.getParameters())
            {
                if(result instanceof VariableResult)
                {
                    Variable v = ((VariableResult)result).variable;
                    params2Func.put(v.address, f.address);
                }
            }
        }
        return params2Func;
    }
}