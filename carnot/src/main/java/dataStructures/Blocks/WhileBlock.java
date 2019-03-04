package dataStructures.Blocks;

import dataStructures.*;
import dataStructures.Results.*;
import dataStructures.Instructions.*;
import dataStructures.Instructions.Instruction.DeleteMode;
import dataStructures.Operator.OperatorCode;
import intermediateCodeRepresentation.*;

import java.util.*;

public class WhileBlock extends Block implements IBlock
{
    private Block loopBlock;
    private Block followBlock;

    private PhiManager phiManager;

    public WhileBlock(Integer id)
    {
        super(id);
        loopBlock = null;
        followBlock = null;
        phiManager = new PhiManager();
    }

    public void setLoopBlock(IBlock block)
    {
        loopBlock = (Block)block;
    }

    public IBlock getLoopBlock()
    {
        return loopBlock;
    }

    public void setFollowBlock(IBlock fBlock)
    {
        followBlock = (Block)fBlock;
    }

    public IBlock getFollowBlock()
    {
        return followBlock;
    }

    @Override
    public Instruction getInstruction(Integer programCounter)
    {
        Instruction instruction = super.getInstruction(programCounter);
        if(instruction != null)
        {
            return instruction;
        }

        Optional<PhiInstruction> filteredInstruction = phiManager.phis.values().stream().filter(phi -> phi.id == programCounter).findFirst();
        if(filteredInstruction.isPresent())
        {
            return filteredInstruction.get();
        }

        return null;
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
    public String toString(Boolean optimized)
    {
        StringBuilder sb = new StringBuilder();
        String instructionString = "";
        if(phiManager != null && phiManager.phis != null && phiManager.phis.keySet().size() > 0)
        {
            for(PhiInstruction instruction : phiManager.phis.values())
            {
                if(optimized)
                {
                    if(instruction.deleteMode == DeleteMode._NotDeleted)
                    {
                        instructionString = instruction.akaI.toString();
                    }
                }
                else 
                {
                    instructionString = instruction.toString();
                }
    
                if(instructionString != null && instructionString != "")
                {
                    sb.append(instructionString + "\\l");
                    instructionString = "";
                }
            }
        }
        
        sb.append(super.toString(optimized));
        return sb.toString();
    }

    public void fixupBranch(Integer iid, IBlock targetBlock)
    {
        getInstruction(iid).operandY.set(targetBlock);
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

    public void createPhis(IBlock lBlock, HashMap<Integer, String> address2identifier, IntermediateCodeGenerator iCodeGenerator)
    {
        createPhis(address2identifier, iCodeGenerator, parent.globalSsa, lBlock.getGlobalSsa(), globalSsa);
        createPhis(address2identifier, iCodeGenerator, parent.localSsa, lBlock.getLocalSsa(), localSsa);
    }

    private void createPhis(
                HashMap<Integer, String> address2identifier, IntermediateCodeGenerator iCodeGenerator,
                HashMap<Integer, Integer> pSsaMap, 
                HashMap<Integer, Integer> lSsaMap, HashMap<Integer, Integer> ssaMap
                )
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

                phiManager.addPhi(this, x, x1, x2);
                ssaMap.put(key, x.version);
            }
        }
    }

    // TODO: update defUseChain.
    public void updatePhiVarOccurances()
    {
        HashMap<Integer, Boolean> stopTable = new HashMap<Integer, Boolean>();
        for (Integer key : phiManager.phis.keySet()) 
        {
            stopTable.put(key, false);
        }

        updatePhiVarOccurances(this, instructions, stopTable);

        Stack<IBlock> nBlocks = new Stack<IBlock>();
        Stack<IBlock> nfBlocks = new Stack<IBlock>();
        nBlocks.add(loopBlock);
        Boolean alreadyVisitedBlocks[] = new Boolean[1000];
        Arrays.fill(alreadyVisitedBlocks, false);
        alreadyVisitedBlocks[id] = true;
        while((!nBlocks.isEmpty() || !nfBlocks.isEmpty()))
        {
            IBlock cBlock;
            if(!nBlocks.isEmpty())
            {
                cBlock = nBlocks.pop();
            }
            else 
            {
                cBlock = nfBlocks.pop();
            }

            alreadyVisitedBlocks[cBlock.getId()] = true;
            updatePhiVarOccurances(cBlock, stopTable);

            if(cBlock instanceof WhileBlock)
            {
                if(((WhileBlock)cBlock).getLoopBlock() != null)
                {
                    if(!alreadyVisitedBlocks[((WhileBlock)cBlock).getLoopBlock().getId()])
                    {
                        nBlocks.push(((WhileBlock)cBlock).getLoopBlock());
                    }
                }

                if(((WhileBlock)cBlock).getFollowBlock() != null)
                {
                    if(!alreadyVisitedBlocks[((WhileBlock)cBlock).getFollowBlock().getId()])
                    {
                        nfBlocks.push(((WhileBlock)cBlock).getFollowBlock());
                    }
                }
            }
            else if(cBlock instanceof IfBlock)
            {
                if(((IfBlock)cBlock).getThenBlock() != null)
                {
                    if(!alreadyVisitedBlocks[((IfBlock)cBlock).getThenBlock().getId()])
                    {
                        nBlocks.push(((IfBlock)cBlock).getThenBlock());
                    }
                }

                if(((IfBlock)cBlock).getElseBlock() != null)
                {
                    if(!alreadyVisitedBlocks[((IfBlock)cBlock).getElseBlock().getId()])
                    {
                        nBlocks.push(((IfBlock)cBlock).getElseBlock());
                    }
                }

                if(((IfBlock)cBlock).getJoinBlock() != null)
                {
                    if(!alreadyVisitedBlocks[((IfBlock)cBlock).getJoinBlock().getId()])
                    {
                        nfBlocks.push(((IfBlock)cBlock).getJoinBlock());
                    }
                }
            }
            else 
            {
                if(cBlock.getChild() != null)
                {
                    if(!(cBlock.getChild() instanceof JoinBlock) && !alreadyVisitedBlocks[cBlock.getChild().getId()])
                    {
                        nBlocks.push(cBlock.getChild());
                    }
                }
            }
        }
    }

    public void updatePhiVarOccurances(IBlock b, List<Instruction> instructions, HashMap<Integer, Boolean> stopTable)
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
                    IntermediateCodeGenerator.optimizer.optimize(b, instruction);
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
                        IntermediateCodeGenerator.optimizer.optimize(b, instruction);
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
            List<PhiInstruction> bPhis;
            if(b instanceof WhileBlock)
            {
                bPhis = ((WhileBlock)b).getPhis();
            }
            else
            {
                bPhis = ((JoinBlock)b).getPhis();
            }

            for (PhiInstruction i : bPhis)
            {
                if(phiManager.phis.containsKey(i.variable.address) 
                        && stopTable.containsKey(i.variable.address))
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

        updatePhiVarOccurances(b, b.getInstructions(), stopTable);
    }
}