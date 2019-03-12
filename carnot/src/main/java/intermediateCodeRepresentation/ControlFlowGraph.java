package intermediateCodeRepresentation;

import java.util.*;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;

public class ControlFlowGraph
{
    public Block head;
    public Block tail;
    private List<Block> blocks;
    public VariableManager mVariableManager;
    public Boolean done;

    public List<Function> functions;
    public HashMap<Integer, LiveRange> iGraph;

    private Integer bc;

    public ControlFlowGraph()
    {
        bc = 0;
        head = new Block(bc++);
        blocks = new ArrayList<Block>();
        functions = new ArrayList<Function>();
        iGraph = new HashMap<Integer, LiveRange>();
        mVariableManager = new VariableManager();
        done = false;
        blocks.add(head);
    }

    public Block initializeBlock()
    {
        Block block = new Block(bc++);
        blocks.add(block);

        return block;
    }

    public IfBlock initializeIfBlock()
    {
        IfBlock block = new IfBlock(bc++);
        blocks.add(block);

        return block;
    }

    public JoinBlock initializeJoinBlock()
    {
        JoinBlock block = new JoinBlock(bc++);
        blocks.add(block);

        return block;
    }

    public WhileBlock initializeWhileBlock()
    {
        WhileBlock block = new WhileBlock(bc++);
        blocks.add(block);

        return block;
    }

    public boolean isExists(Function function)
    {
        return functions.stream().anyMatch(f -> f.address == function.address);
    }

    public void addFunction(Function function)
    {
        functions.add(function);
    }

    public Function getFunction(Function function)
    {
        if(isExists(function))
        {
            return (Function)functions.stream().filter(f -> f.address == function.address).toArray()[0];
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
}