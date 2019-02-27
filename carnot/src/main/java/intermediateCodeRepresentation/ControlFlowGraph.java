package intermediateCodeRepresentation;

import java.util.*;

import dataStructures.Function;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;

public class ControlFlowGraph
{
    public Block head;
    public List<Instruction> instructions;
    public List<Block> blocks;
    public Boolean done;

    public List<Function> functions;

    private Integer bc;

    public ControlFlowGraph()
    {
        bc = 0;
        head = new Block(bc++);
        blocks = new ArrayList<Block>();
        functions = new ArrayList<Function>();
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
}