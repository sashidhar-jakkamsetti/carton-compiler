package dataStructures.Blocks;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Results.*;
import dataStructures.Instructions.*;
import dataStructures.Operator.OperatorCode;
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

    public ArrayList<PhiInstruction> getPhis()
    {
        return (ArrayList<PhiInstruction>)phiManager.phis.values();
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

    // TODO: update defUseChain also.
    public void updatePhiVarOccurances()
    {
        HashMap<Integer, Boolean> stopTable = new HashMap<Integer, Boolean>();
        for (Integer key : phiManager.phis.keySet()) 
        {
            stopTable.put(key, false);
        }

        updatePhiVarOccurances(instructions, stopTable);

        Stack<IBlock> nBlocks = new Stack<IBlock>();
        nBlocks.add(loopBlock);
        Boolean alreadyVisitedBlocks[] = new Boolean[1000];
        alreadyVisitedBlocks[id] = true;
        while(!nBlocks.isEmpty() && stopTable.values().stream().anyMatch(t -> t == false))
        {
            IBlock cBlock = nBlocks.pop();
            alreadyVisitedBlocks[cBlock.getId()] = true;
            updatePhiVarOccurances(cBlock, stopTable);

            if(cBlock instanceof WhileBlock)
            {
                if(!alreadyVisitedBlocks[((WhileBlock)cBlock).getLoopBlock().getId()])
                {
                    nBlocks.push(((WhileBlock)cBlock).getLoopBlock());
                }
            }
            else if(cBlock instanceof IfBlock)
            {
                if(!alreadyVisitedBlocks[((IfBlock)cBlock).getThenBlock().getId()])
                {
                    nBlocks.push(((IfBlock)cBlock).getThenBlock());
                }
                if(!alreadyVisitedBlocks[((IfBlock)cBlock).getElseBlock().getId()])
                {
                    nBlocks.push(((IfBlock)cBlock).getElseBlock());
                }
            }
            else 
            {
                if(!alreadyVisitedBlocks[cBlock.getChild().getId()])
                {
                    nBlocks.push(cBlock.getChild());
                }
            }
        }
    }

    public void updatePhiVarOccurances(List<Instruction> instructions, HashMap<Integer, Boolean> stopTable)
    {
        for (Instruction instruction : instructions) 
        {
            if(instruction.operandX instanceof VariableResult)
            {
                Variable varToUpdate = ((VariableResult)instruction.operandX).variable;

                if(stopTable.containsKey(varToUpdate.address) && !stopTable.get(varToUpdate.address))
                {
                    PhiInstruction phiInstr = phiManager.phis.get(varToUpdate.address);
                    varToUpdate.version = phiInstr.variable.version;
                }
            }

            if(instruction.opcode != OperatorCode.move)
            {
                if(instruction.operandY instanceof VariableResult)
                {
                    Variable varToUpdate = ((VariableResult)instruction.operandY).variable;

                    if(stopTable.containsKey(varToUpdate.address) && !stopTable.get(varToUpdate.address))
                    {
                        PhiInstruction phiInstr = phiManager.phis.get(varToUpdate.address);
                        varToUpdate.version = phiInstr.variable.version;
                    }
                }
            }
            else
            {
                if(instruction.operandY instanceof VariableResult)
                {
                    Variable varToUpdate = ((VariableResult)instruction.operandY).variable;

                    if(stopTable.containsKey(varToUpdate.address) && !stopTable.get(varToUpdate.address))
                    {
                        stopTable.put(varToUpdate.address, true);
                    }
                }
            }
        }
    }

    public void updatePhiVarOccurances(IBlock b, HashMap<Integer, Boolean> stopTable)
    {
        if(b instanceof WhileBlock || b instanceof JoinBlock)
        {
            ArrayList<PhiInstruction> bPhis = (b instanceof WhileBlock) ? ((WhileBlock)b).getPhis() : ((JoinBlock)b).getPhis();
            for (PhiInstruction i : bPhis)
            {
                if(phiManager.phis.containsKey(i.variable.address) 
                        && stopTable.containsKey(i.variable.address) 
                                && !stopTable.get(i.variable.address))
                {
                    PhiInstruction phiI = phiManager.phis.get(i.variable.address);
                    if(i.operandX instanceof VariableResult && phiI.operandX instanceof VariableResult)
                    {
                        if(((VariableResult)i.operandX).variable.version == ((VariableResult)phiI.operandX).variable.version)
                        {
                            ((VariableResult)i.operandX).variable.version = phiI.variable.version;
                            stopTable.put(phiI.variable.address, true);
                        }
                        else if(((VariableResult)i.operandY).variable.version == ((VariableResult)phiI.operandX).variable.version)
                        {
                            ((VariableResult)i.operandY).variable.version = phiI.variable.version;
                            stopTable.put(phiI.variable.address, true);
                        }
                    }
                }
            }
        }

        updatePhiVarOccurances(b.getInstructions(), stopTable);
    }
}