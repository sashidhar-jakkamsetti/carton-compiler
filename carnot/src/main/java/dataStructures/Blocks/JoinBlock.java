package dataStructures.Blocks;

import dataStructures.*;
import dataStructures.Results.*;
import intermediateCodeRepresentation.*;

import java.util.*;

public class JoinBlock extends Block implements IBlock
{
    private Block thenBlock;
    private Block elseBlock;

    private PhiManager phiManager;

    public JoinBlock(Integer id)
    {
        super(id);
        thenBlock = null;
        elseBlock = null;
        phiManager = new PhiManager(this);
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

    public void createPhis(HashMap<Integer, String> address2identifier)
    {
        setSsaMap(parent.getSsaMap());
        for (Integer key : parent.ssaMap.keySet()) 
        {
            Variable x = new Variable(address2identifier.get(key), key);
            if(parent.ssaMap.get(key) != thenBlock.ssaMap.get(key))
            {
                VariableResult x1 = new VariableResult();
                x1.set(new Variable(address2identifier.get(key), key, thenBlock.ssaMap.get(key)));
                VariableResult x2 = new VariableResult();
                x2.set(new Variable(address2identifier.get(key), key, parent.ssaMap.get(key)));

                phiManager.addPhi(x, x1, x2);
                ssaMap.put(key, x.version);
            }

            if(elseBlock != null && elseBlock.ssaMap != null)
            {
                if(parent.ssaMap.get(key) != elseBlock.ssaMap.get(key))
                {
                    VariableResult x1 = new VariableResult();
                    x1.set(new Variable(address2identifier.get(key), key, parent.ssaMap.get(key)));
                    VariableResult x2 = new VariableResult();
                    x2.set(new Variable(address2identifier.get(key), key, elseBlock.ssaMap.get(key)));
                    
                    if(phiManager.isExists(x))
                    {
                        phiManager.updatePhi(x, null, x2);
                    }
                    else
                    {
                        phiManager.addPhi(x, x1, x2);
                        ssaMap.put(key, x.version);
                    }
                }
            }
        }
    }
}