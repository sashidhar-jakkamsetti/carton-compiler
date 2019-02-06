package dataStructures.Blocks;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Results.*;
import dataStructures.Instructions.*;
import intermediateCodeRepresentation.*;

import java.util.*;

public class WhileBlock extends Block implements IBlock
{
    private Block doBlock;
    private Block loopBlock;
    private Block followBlock = (Block)child;

    private PhiManager phiManager;

    public WhileBlock(Integer id)
    {
        super(id);
        doBlock = null;
        loopBlock = null;
    }

    public void setDoBlock(IBlock block)
    {
        doBlock = (Block)block;
    }

    public IBlock getDoBlock()
    {
        return doBlock;
    }

    public void setLoopBlock(IBlock block)
    {
        loopBlock = (Block)block;
    }

    public IBlock getLoopBlock(IBlock block)
    {
        return loopBlock;
    }
}