package dataStructures.Blocks;

import dataStructures.*;
import intermediateCodeRepresentation.PhiManager;

import java.util.*;

public class JoinBlock extends Block implements IBlock
{
    private BasicBlock thenBlock;
    private BasicBlock elseBlock;

    private PhiManager phiManager;

    public void setThenBlock(IBlock block)
    {
        thenBlock = (BasicBlock)block;
    }

    public IBlock getThenBlock()
    {
        return thenBlock;
    }

    public void setElseBlock(IBlock block)
    {
        elseBlock = (BasicBlock)block;
    }

    public IBlock getElseBlock(IBlock block)
    {
        return elseBlock;
    }
}