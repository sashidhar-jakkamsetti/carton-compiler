package dataStructures.Blocks;

import dataStructures.*;
import dataStructures.Instructions.Instruction;
import dataStructures.Instructions.PhiInstruction;
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
        phiManager = new PhiManager();
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

    public List<PhiInstruction> getPhis()
    {
        if(phiManager != null && phiManager.phis != null && phiManager.phis.values().size() > 0)
        {
            return new ArrayList<PhiInstruction>(phiManager.phis.values());
        }
        return new ArrayList<PhiInstruction>();
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

    public void updateIncomingVManager(VariableManager globalVManager, VariableManager localVManager)
    {
        globalVManager.setSsaMap(globalSsa);

        if(localVManager != null && localSsa.size() > 0)
        {
            localVManager.setSsaMap(localSsa);
        }
        
        for (Integer key : phiManager.phis.keySet()) 
        {
            if(globalVManager.isVariable(key))
            {
                globalVManager.updateDefUseChain(key, phiManager.phis.get(key).id, phiManager.phis.get(key).id);
            }  
            else if(localVManager != null && localVManager.isVariable(key))
            {
                localVManager.updateDefUseChain(key, phiManager.phis.get(key).id, phiManager.phis.get(key).id);
            }  
        }
    }

    public void createPhis(HashMap<Integer, String> address2identifier)
    {
        freezeSsa(parent.globalSsa, parent.localSsa);
        createPhis(address2identifier, parent.globalSsa, thenBlock.globalSsa, elseBlock.globalSsa, globalSsa);
        createPhis(address2identifier, parent.localSsa, thenBlock.localSsa, elseBlock.localSsa, localSsa);
    }

    private void createPhis(HashMap<Integer, String> address2identifier, HashMap<Integer, Integer> iSsaMap, 
                HashMap<Integer, Integer> tSsaMap, HashMap<Integer, Integer> eSsaMap, HashMap<Integer, Integer> ssaMap)
    {
        for (Integer key : iSsaMap.keySet()) 
        {
            Variable x = new Variable(address2identifier.get(key), key);
            if(iSsaMap.get(key) != tSsaMap.get(key))
            {
                VariableResult x1 = new VariableResult();
                x1.set(new Variable(address2identifier.get(key), key, tSsaMap.get(key)));
                VariableResult x2 = new VariableResult();
                x2.set(new Variable(address2identifier.get(key), key, iSsaMap.get(key)));

                phiManager.addPhi(x, x1, x2);
                ssaMap.put(key, x.version);
            }

            if(elseBlock != null && eSsaMap != null)
            {
                if(iSsaMap.get(key) != eSsaMap.get(key))
                {
                    VariableResult x1 = new VariableResult();
                    x1.set(new Variable(address2identifier.get(key), key, iSsaMap.get(key)));
                    VariableResult x2 = new VariableResult();
                    x2.set(new Variable(address2identifier.get(key), key, eSsaMap.get(key)));

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