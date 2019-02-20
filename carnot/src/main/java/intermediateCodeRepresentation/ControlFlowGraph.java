package intermediateCodeRepresentation;

import java.util.*;

import dataStructures.Function;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;

public class ControlFlowGraph
{
    public static Block head;
    public IBlock current;
    public static List<Instruction> instructions;
    public static List<Block> blocks;

    public Function cfunction;
    public static List<Function> functions;

    private static Integer bc;
    private static ControlFlowGraph cfg;

    private ControlFlowGraph()
    {
        bc = 0;
        head = new Block(bc++);
        current = head;
        blocks = new ArrayList<Block>();
        cfunction = null;
        functions = new ArrayList<Function>();
        blocks.add(head);
    }

    public static ControlFlowGraph getInstance()
    {
        if(cfg == null)
        {
            cfg = new ControlFlowGraph();
        }

        return cfg;
    }

    public IBlock initializeBlock()
    {
        Block block = new Block(bc++);
        blocks.add(block);
        current = block;

        return block;
    }

    public IBlock initializeIfBlock()
    {
        IfBlock block = new IfBlock(bc++);
        blocks.add(block);
        current = block;

        return block;
    }

    public IBlock initializeJoinBlock()
    {
        JoinBlock block = new JoinBlock(bc++);
        blocks.add(block);
        current = block;

        return block;
    }

    public IBlock initializeWhileBlock()
    {
        WhileBlock block = new WhileBlock(bc++);
        blocks.add(block);
        current = block;

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
}