package dataStructures.Blocks;

import dataStructures.*;
import intermediateCodeRepresentation.PhiManager;

import java.util.*;

public class WhileBlock extends Block implements IBlock
{
    private BasicBlock doBlock;
    private BasicBlock loopBlock;
    private BasicBlock followBlock = (BasicBlock)child;

    private PhiManager phiManager;

    public void setDoBlock(IBlock block)
    {
        doBlock = (BasicBlock)block;
    }

    public IBlock getDoBlock()
    {
        return doBlock;
    }

    public void setLoopBlock(IBlock block)
    {
        loopBlock = (BasicBlock)block;
    }

    public IBlock getLoopBlock(IBlock block)
    {
        return loopBlock;
    }
}