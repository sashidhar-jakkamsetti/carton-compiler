package dataStructures.Blocks;

import dataStructures.*;
import dataStructures.Results.*;
import dataStructures.Instructions.*;
import intermediateCodeRepresentation.*;

import java.util.*;

public class WhileBlock extends Block implements IBlock
{
    private Block loopBlock;
    private Block followBlock;

    private PhiManager phiManager;

    public WhileBlock(Integer id, Function function)
    {
        super(id, function);
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

        Optional<PhiInstruction> filteredInstruction = phiManager.phis.values().stream().filter(phi -> phi.id.equals(programCounter)).findFirst();
        if(filteredInstruction.isPresent())
        {
            return filteredInstruction.get();
        }

        return null;
    }

    public HashMap<Integer, PhiInstruction> getPhiMap()
    {
        HashMap<Integer, PhiInstruction> maturePhiMap = new HashMap<Integer, PhiInstruction>();
        for (PhiInstruction phi : phiManager.phis.values()) 
        {
            maturePhiMap.put(phi.id, phi);
        }
        return maturePhiMap;
    }

    public ArrayList<PhiInstruction> getPhis()
    {
        if(phiManager != null && phiManager.phis != null && phiManager.phis.values().size() > 0)
        {
            return new ArrayList<PhiInstruction>(phiManager.phis.values());
        }
        return new ArrayList<PhiInstruction>();
    }

    @Override
    public String toString(Boolean optimized, Boolean dce, Boolean colored)
    {
        StringBuilder sb = new StringBuilder();
        if(colored)
        {
            sb.append(super.toString(optimized, dce, colored));
        }
        else
        {
            ArrayList<Instruction> phiInstructions = new ArrayList(phiManager.phis.values());
            sb.append(super.toStringUtil(phiInstructions, optimized, dce, colored));
            sb.append(super.toString(optimized, dce, colored));
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

    public void createPhis(IBlock lBlock, HashMap<Integer, String> address2identifier, 
                        IntermediateCodeGenerator iCodeGenerator, Boolean optimize)
    {
        createPhis(address2identifier, iCodeGenerator, parent.globalSsa, lBlock.getGlobalSsa(), globalSsa, optimize);
        createPhis(address2identifier, iCodeGenerator, parent.localSsa, lBlock.getLocalSsa(), localSsa, optimize);
    }

    private void createPhis(
                HashMap<Integer, String> address2identifier, IntermediateCodeGenerator iCodeGenerator,
                HashMap<Integer, Integer> pSsaMap, 
                HashMap<Integer, Integer> lSsaMap, HashMap<Integer, Integer> ssaMap,
                Boolean optimize)
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

                phiManager.addPhi(this, x, x1, x2, optimize);
                ssaMap.put(key, x.version);
            }
        }
    }

    // TODO: update defUseChain.
    public void updatePhiVarOccurances(Boolean optimize)
    {
        updatePhiVarOccurances(this, instructions, optimize);

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
            updatePhiVarOccurances(cBlock, optimize);

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

    public void updatePhiVarOccurances(IBlock b, List<Instruction> instructions, Boolean optimize)
    {
        for (Instruction i : instructions) 
        {
            if(i.operandX instanceof VariableResult)
            {
                Variable varToUpdate = ((VariableResult)i.operandX).variable;

                if(phiManager.phis.containsKey(varToUpdate.address))
                {
                    PhiInstruction phiInstr = phiManager.phis.get(varToUpdate.address);
                    if(varToUpdate.version == ((VariableResult)phiInstr.operandX).variable.version)
                    {
                        varToUpdate.version = phiInstr.variable.version;
                    }
                }
            }

            if(i.operandY instanceof VariableResult)
            {
                Variable varToUpdate = ((VariableResult)i.operandY).variable;

                if(phiManager.phis.containsKey(varToUpdate.address))
                {
                    PhiInstruction phiInstr = phiManager.phis.get(varToUpdate.address);
                    if(varToUpdate.version == ((VariableResult)phiInstr.operandX).variable.version)
                    {
                        varToUpdate.version = phiInstr.variable.version;
                    }
                }
            }

            if(optimize)
            {
                IntermediateCodeGenerator.optimizer.optimize(b, i);
            }
        }
    }

    public void updatePhiVarOccurances(IBlock b, Boolean optimize)
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
                if(phiManager.phis.containsKey(i.variable.address))
                {
                    PhiInstruction phiInstr = phiManager.phis.get(i.variable.address);
                    if(phiInstr.operandX instanceof VariableResult)
                    {
                        if(i.operandX instanceof VariableResult)
                        {
                            Variable varToUpdate = ((VariableResult)i.operandX).variable;
                            if(varToUpdate.version == ((VariableResult)phiInstr.operandX).variable.version)
                            {
                                varToUpdate.version = phiInstr.variable.version;
                            }
                        }
            
                        if(i.operandY instanceof VariableResult)
                        {
                            Variable varToUpdate = ((VariableResult)i.operandY).variable;
                            if(varToUpdate.version == ((VariableResult)phiInstr.operandX).variable.version)
                            {
                                varToUpdate.version = phiInstr.variable.version;
                            }
                        }
                    }
                }

                if(optimize)
                {
                    IntermediateCodeGenerator.optimizer.optimize(b, i);
                }
            }
        }

        updatePhiVarOccurances(b, b.getInstructions(), optimize);
    }

    public void optimizeWhilePhis(Boolean optimize)
    {
        optimizeWhilePhis(this, optimize);

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
            optimizeWhilePhis(cBlock, optimize);

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

    private void optimizeWhilePhis(IBlock b, Boolean optimize)
    {
        if(optimize)
        {
            List<PhiInstruction> bPhis;
            if(b instanceof WhileBlock)
            {
                bPhis = ((WhileBlock)b).getPhis();
                for (PhiInstruction i : bPhis)
                {
                    IntermediateCodeGenerator.optimizer.optimize(b, i);
                }
            }
        }
    }
}