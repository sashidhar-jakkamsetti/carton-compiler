package dataStructures.Blocks;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Results.*;
import dataStructures.Instructions.*;
import intermediateCodeRepresentation.*;

import java.util.*;

public class IfBlock extends Block implements IBlock
{
    private Block thenBlock;
    private Block elseBlock;
    private Block joinBlock = (Block)child;

    public IfBlock(Integer id)
    {
        super(id);
        thenBlock = null;
        elseBlock = null;
    }

    public void setThenBlock(IBlock block)
    {
        thenBlock = (Block)block;
    }

    public IBlock getThenBlock()
    {
        return thenBlock;
    }

    public void setElseBlock(IBlock block)
    {
        elseBlock = (Block)block;
    }

    public IBlock getElseBlock(IBlock block)
    {
        return elseBlock;
    }
    
    public void fixupBranch(Integer iid, IBlock targetBlock)
    {
        getInstruction(iid).operandY.set(targetBlock);
    }
}