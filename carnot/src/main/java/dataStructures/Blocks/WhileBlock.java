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
        phiManager = new PhiManager();
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

    public IBlock getLoopBlock()
    {
        return loopBlock;
    }

    public IBlock getFollowBlock()
    {
        return followBlock;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if(phiManager != null && phiManager.phis != null && phiManager.phis.keySet().size() > 0)
        {
            for(PhiInstruction instruction : phiManager.phis.values())
            {
                sb.append(instruction.toString() + "\\l");
            }
        }
        for(Instruction instruction : instructions)
        {
            sb.append(instruction.toString() + "\\l");
        }

        return sb.toString();
    }

    public void fixupBranch(Integer iid, IBlock targetBlock)
    {
        getInstruction(iid).operandY.set(targetBlock);
    }

    public void updateIncomingVManager(VariableManager globalVManager, VariableManager localVManager)
    {
        globalVManager.setSsaMap(globalSsa);
        localVManager.setSsaMap(localSsa);
        
        for (Integer key : phiManager.phis.keySet()) 
        {
            if(globalVManager.isVariable(key))
            {
                globalVManager.updateDefUseChain(key, phiManager.phis.get(key).id, phiManager.phis.get(key).id);
            }  
            else if(localVManager.isVariable(key))
            {
                localVManager.updateDefUseChain(key, phiManager.phis.get(key).id, phiManager.phis.get(key).id);
            }  
        }
    }

    public void createPhis(HashMap<Integer, String> address2identifier)
    {
        createPhis(address2identifier, parent.globalSsa, loopBlock.globalSsa, globalSsa);
        createPhis(address2identifier, parent.localSsa, loopBlock.localSsa, localSsa);
    }

    private void createPhis(HashMap<Integer, String> address2identifier, HashMap<Integer, Integer> pSsaMap, 
                HashMap<Integer, Integer> lSsaMap, HashMap<Integer, Integer> ssaMap)
    {
        for (Integer key : pSsaMap.keySet()) 
        {
            Variable x = new Variable(address2identifier.get(key), key);
            if(pSsaMap.get(key) != lSsaMap.get(key))
            {
                VariableResult x1 = new VariableResult();
                x1.set(new Variable(address2identifier.get(key), key, pSsaMap.get(key)));
                VariableResult x2 = new VariableResult();
                x2.set(new Variable(address2identifier.get(key), key, lSsaMap.get(key)));

                phiManager.addPhi(x, x1, x2);
                ssaMap.put(key, x.version);
            }
        }
    }

    // TODO: update defUseChain also. Along with nested while loops.
    public void updatePhiVarOccurances()
    {

    }
}