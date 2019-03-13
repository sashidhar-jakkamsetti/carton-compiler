package registerAllocation;

import java.util.*;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;
import dataStructures.Instructions.Instruction.DeleteMode;
import dataStructures.Operator.OperatorCode;
import dataStructures.Results.*;
import intermediateCodeRepresentation.ControlFlowGraph;

public class InterferenceGraph
{
    private HashMap<Integer, LiveRange> iGraph;
    private ControlFlowGraph cfg;
    private PhiEliminator phiEliminator;

    public InterferenceGraph(ControlFlowGraph cfg)
    {
        this.cfg = cfg;
        phiEliminator = new PhiEliminator();
        iGraph = new HashMap<Integer, LiveRange>();
    }

    public void construct()
    {
        for (Function f : cfg.functions) 
        {
            construct(f, new ArrayList<Integer>());
        }

        construct(new Function(cfg.head, cfg.tail), new ArrayList<Integer>());
    }

    private void construct(Function function, ArrayList<Integer> liveSet)
    {
        IBlock cBlock = function.tail;
        while(cBlock != null)
        {
            cBlock = construct(cBlock, liveSet);
        }
    }

    private IBlock construct(IBlock block, ArrayList<Integer> liveSet)
    {
        if(block instanceof JoinBlock)
        {
            JoinBlock jBlock = (JoinBlock)block;
            construct(jBlock, liveSet);
            phiEliminator.eliminate(jBlock);
            List<PhiInstruction> phis = new ArrayList<PhiInstruction>(jBlock.getPhiMap().values());

            ArrayList<Integer> copyLiveSetThen = new ArrayList<Integer>(liveSet);
            for(Integer i = phis.size() - 1; i >= 0; i--)
            {
                Integer id = phis.get(i).akaI.id;
                copyLiveSetThen.removeIf(ins -> ins.equals(id));
                if(!iGraph.containsKey(id))
                {
                    LiveRange dummy = new LiveRange(id);
                    iGraph.put(id, dummy);
                }
                addToGraph(phis.get(i).akaI.operandX, copyLiveSetThen);
            }

            IBlock tBlock = jBlock.getThenBlock();
            if(tBlock != null)
            {
                do
                {
                    tBlock = construct(tBlock, copyLiveSetThen);
                    if(tBlock == null)
                    {
                        return null;
                    }
                }while(!(tBlock instanceof IfBlock));
            }

            ArrayList<Integer> copyLiveSetElse = new ArrayList<Integer>(liveSet);
            for(Integer i = phis.size() - 1; i >= 0; i--)
            {
                Integer id = phis.get(i).akaI.id;
                copyLiveSetElse.removeIf(ins -> ins.equals(id));
                if(!iGraph.containsKey(id))
                {
                    LiveRange dummy = new LiveRange(id);
                    iGraph.put(id, dummy);
                }
                addToGraph(phis.get(i).akaI.operandY, copyLiveSetElse);
            }

            IBlock eBlock = jBlock.getElseBlock();
            if(eBlock != null)
            {
                do
                {
                    eBlock = construct(eBlock, copyLiveSetElse);
                    if(eBlock == null)
                    {
                        return null;
                    }
                }while(!(eBlock instanceof IfBlock));
            }

            liveSet.clear();
            liveSet.addAll(copyLiveSetElse);
            for (Integer id : copyLiveSetThen) 
            {
                addEdge(id, liveSet, false);
                if(!liveSet.contains(id))
                {
                    liveSet.add(id);
                }
            }
            
            // convergence point.
            if(jBlock.getParent() == tBlock)
            {
                if(jBlock.getParent() != null)
                {
                    return construct(jBlock.getParent(), liveSet);
                }
                return null;
            }
            else
            {
                System.out.println("Missing if convergence point while constructing register interference graph at blockId = " + block.getId());
            }
        }
        else if(block instanceof WhileBlock)
        {
            WhileBlock wBlock = (WhileBlock)block;
            List<PhiInstruction> phis = new ArrayList<PhiInstruction>(wBlock.getPhiMap().values());
            // Not clear why two iterations. But prof told to do so.
            for(Integer k = 0; k < 2; k++)
            {
                construct(wBlock, liveSet);
                phiEliminator.eliminate(wBlock);

                ArrayList<Integer> copyLiveSetLoop = new ArrayList<Integer>(liveSet);
                for(Integer i = phis.size() - 1; i >= 0; i--)
                {
                    Integer id = phis.get(i).akaI.id;
                    copyLiveSetLoop.removeIf(ins -> ins.equals(id));
                    if(!iGraph.containsKey(id))
                    {
                        LiveRange dummy = new LiveRange(id);
                        iGraph.put(id, dummy);
                    }
                    addToGraph(phis.get(i).akaI.operandY, copyLiveSetLoop);
                }
    
                IBlock lBlock = wBlock.getChild();
                if(lBlock != null)
                {
                    do
                    {
                        lBlock = construct(lBlock, copyLiveSetLoop);
                        if(lBlock == null)
                        {
                            return null;
                        }
                    }while(lBlock != wBlock);
                }

                liveSet.clear();
                liveSet.addAll(copyLiveSetLoop);
            }
            
            construct(wBlock, liveSet);
            for(Integer i = phis.size() - 1; i >= 0; i--)
            {
                Integer id = phis.get(i).akaI.id;
                liveSet.removeIf(ins -> ins.equals(id));
                if(!iGraph.containsKey(id))
                {
                    LiveRange dummy = new LiveRange(id);
                    iGraph.put(id, dummy);
                }
                addToGraph(phis.get(i).akaI.operandX, liveSet);
            }

            return wBlock.getParent();
        }
        else
        {
            construct((Block)block, liveSet);
            return block.getParent();
        }

        return null;
    }

    private void construct(Block block, ArrayList<Integer> liveSet)
    {
        for (Integer i = block.getInstructions().size() - 1; i >= 0; i--)
        {
            construct(block.getInstructions().get(i), liveSet);
        }
    }

    private void construct(Instruction instruction, ArrayList<Integer> liveSet)
    {
        if(instruction.deleteMode == DeleteMode._NotDeleted)
        {
            liveSet.removeIf( i -> i.equals(instruction.akaI.id));

            // Moves of formal parameters or phi resolution or return instruction.
            if(instruction.opcode == OperatorCode.move)
            {
                addToGraph(instruction.akaI.operandX, liveSet);

                // valid for phi instruction or return instruction or use of formal parameters.
                if(instruction.akaI.operandY != null && instruction.akaI.operandY instanceof InstructionResult)
                {
                    liveSet.removeIf( i -> i.equals(instruction.akaI.operandY.getIid()));
                }
            }
            else
            {
                addToGraph(instruction.akaI.operandX, liveSet);
                addToGraph(instruction.akaI.operandY, liveSet);
            }
        }
    }

    private void addToGraph(IResult result, ArrayList<Integer> liveSet)
    {
        if(result != null && result instanceof InstructionResult)
        {
            Integer id = result.getIid();
            addEdge(id, liveSet, true);
            addToLive(result, liveSet);
        }
    }

    private void addToLive(IResult result, ArrayList<Integer> liveSet)
    {
        if(!liveSet.contains(result.getIid()))
        {
            liveSet.add(result.getIid());
        }
    }

    private void addEdge(Integer id, ArrayList<Integer> liveSet, Boolean incrementCost)
    {
        LiveRange cLive;
        if(iGraph.containsKey(id))
        {
            cLive = iGraph.get(id);
        }
        else
        {
            cLive = new LiveRange(id);
            iGraph.put(id, cLive);
        }

        if(incrementCost)
        {
            cLive.cost += 1;
        }

        for (Integer live : liveSet)
        {
            if(live != id)
            {
                cLive.addNeighbor(live);
                if(iGraph.containsKey(live))
                {
                    iGraph.get(live).addNeighbor(id);
                }
            }
        }
    }

    public ArrayList<PhiInstruction> getPhis()
    {
        return phiEliminator.getPhis();
    }

    public HashMap<Integer, LiveRange> getIGraph()
    {
        return iGraph;
    }
}