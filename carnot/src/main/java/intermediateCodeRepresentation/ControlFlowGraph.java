package intermediateCodeRepresentation;

import java.util.*;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;

public class ControlFlowGraph
{
    public static Block head;
    public IBlock current;
    public static List<Instruction> instructions;
    public static List<Block> blocks;

    private static Integer bc;
    private static ControlFlowGraph cfg;

    private ControlFlowGraph()
    {
        bc = 0;
        head = new Block(bc++);
        current = head;
        blocks = new ArrayList<Block>();
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


}