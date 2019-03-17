package dataStructures.Blocks;

import dataStructures.Results.*;
import dataStructures.Function;
import dataStructures.Instructions.*;

public class IfBlock extends Block implements IBlock
{
    private Block thenBlock;
    private Block elseBlock;
    private JoinBlock joinBlock;

    public IfBlock(Integer id, Function function)
    {
        super(id, function);
        thenBlock = null;
        elseBlock = null;
        joinBlock = null;
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

    public IBlock getElseBlock()
    {
        return elseBlock;
    }

    public void setJoinBlock(IBlock block)
    {
        joinBlock = (JoinBlock)block;
    }

    public IBlock getJoinBlock()
    {
        return joinBlock;
    }
    
    public void fixupBranch(Integer iid, IBlock targetBlock)
    {
        Instruction instruction = getInstruction(iid);
        if(instruction.operandY instanceof BranchResult)
        {
            instruction.operandY.set(targetBlock);
        }
    }
}