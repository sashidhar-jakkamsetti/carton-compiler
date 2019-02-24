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
        phiManager = new PhiManager(this);
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

    public void createPhis(HashMap<Integer, String> address2identifier)
    {
        setSsaMap(parent.getSsaMap());
        for (Integer key : parent.ssaMap.keySet()) 
        {
            Variable x = new Variable(address2identifier.get(key), key);
            if(parent.ssaMap.get(key) != loopBlock.ssaMap.get(key))
            {
                VariableResult x1 = new VariableResult();
                x1.set(new Variable(address2identifier.get(key), key, parent.ssaMap.get(key)));
                VariableResult x2 = new VariableResult();
                x2.set(new Variable(address2identifier.get(key), key, loopBlock.ssaMap.get(key)));

                phiManager.addPhi(x, x1, x2);
                ssaMap.put(key, x.version);
            }
        }
    }

    public void updatePhiVarOccurances()
    {
        
    }
}