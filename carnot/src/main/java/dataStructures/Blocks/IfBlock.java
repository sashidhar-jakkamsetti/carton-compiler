package dataStructures.Blocks;

import dataStructures.*;

import java.util.*;

public class IfBlock extends Block implements IBlock
{
    private BasicBlock thenBlock;
    private BasicBlock elseBlock;
    private BasicBlock joinBlock = (BasicBlock)child;

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