package registerAllocation;

import java.util.*;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;
import dataStructures.Instructions.Instruction.DeleteMode;
import dataStructures.Operator.OperatorCode;
import dataStructures.Results.InstructionResult;
import intermediateCodeRepresentation.*;
import utility.Constants;

public class RegisterAllocator
{
    private HashMap<Integer, LiveRange> iGraph;
    private ArrayList<PhiInstruction> phiWeb;

    private Integer regSize;
    private Integer trueRegSize;
    private Integer clusterCounter;

    private HashMap<Integer, ArrayList<LiveRange>> clusters;
    private HashMap<Integer, Integer> id2cluster;


    public RegisterAllocator(InterferenceGraph iGraph, Integer actualRegsize)
    {
        this.iGraph = iGraph.getIGraph();
        this.phiWeb = iGraph.getPhis();
        regSize = Constants.REGISTER_SIZE;
        trueRegSize = Constants.REGISTER_SIZE;
        if(actualRegsize > 0)
        {
            regSize = actualRegsize;
            trueRegSize = actualRegsize;
        }
        clusterCounter = Constants.CLUSTER_OFFSET;
        clusters = new HashMap<Integer, ArrayList<LiveRange>>();
        id2cluster = new HashMap<Integer, Integer>();
    }

    public void allocate(ControlFlowGraph cfg) throws Exception
    {
        groupClusters();
        color();
        adjustSpillAddress();
        ungroupClusters();
        
        if(checkColoring())
        {
            // setting the color.
            for (IBlock block : cfg.getAllBlocks())
            {
                for (Instruction instruction : block.getInstructions()) 
                {
                    if(instruction.deleteMode == DeleteMode._NotDeleted)
                    {
                        instruction.setColoredInstruction(iGraph);
                        if(instruction.opcode == OperatorCode.move)
                        {
                            if(instruction.coloredI.operandX.equals(instruction.coloredI.operandY))
                            {
                                instruction.deleteMode = DeleteMode.CP;
                            }
                        }
                    }
                }
            }
            // correcting the target block.
            for (IBlock block : cfg.getAllBlocks())
            {
                for (Instruction instruction : block.getInstructions()) 
                {
                    if(instruction.deleteMode == DeleteMode._NotDeleted)
                    {
                        instruction.setColoredInstruction(iGraph);
                    }
                }
            }
            cfg.iGraph = iGraph;
        }
        else
        {
            throw new Exception("Register allocation failed during graph coloring.");
        }
    }

    private void color()
    {
        if(!iGraph.values().stream().anyMatch(live -> live.alive))
        {
            return;
        }

        Integer id = findAppropriateNode();
        if(id == -1)
        {
            id = findLowestCostNode();
        }
        
        LiveRange live = iGraph.get(id);
        live.alive = false;

        color();

        Integer availableColor = getAvailableColor(live, regSize);
        if(availableColor == -1)
        {
            live.color = getAvailableColor(live, ++regSize);
        }
        else
        {
            live.color = availableColor;
        }
        live.alive = true;
    }

    private void adjustSpillAddress()
    {
        for (LiveRange live : iGraph.values()) 
        {
            if(live.alive && live.color > trueRegSize)
            {
                live.color += Constants.SPILL_REGISTER_OFFSET - trueRegSize;
            }
        }
    }

    private Integer findAppropriateNode()
    {
        for (Integer id : iGraph.keySet()) 
        {
            if(iGraph.get(id).alive && iGraph.get(id).neighbors.size() < regSize)
            {
                return id;
            }
        }

        return -1;
    }

    private Integer findLowestCostNode()
    {
        Integer min = Integer.MAX_VALUE;
        Integer rId = -1;
        for (Integer id : iGraph.keySet()) 
        {
            if(iGraph.get(id).alive && iGraph.get(id).cost < min)
            {
                min = iGraph.get(id).cost;
                rId = id;
            }
        }

        return rId;
    }

    private Integer getAvailableColor(LiveRange live, Integer regSize)
    {
        Boolean[] available = new Boolean[regSize+1];
        Arrays.fill(available, true);

        for (Integer neighbor : live.neighbors) 
        {
            if(iGraph.containsKey(neighbor) && iGraph.get(neighbor).alive)
            {
                available[iGraph.get(neighbor).color] = false;
            }
        }

        for (Integer i = 1; i <= regSize; i++) 
        {
            if(available[i])
            {
                return i;
            }
        }

        return -1;
    }

    private void groupClusters()
    {
        for (PhiInstruction phi : phiWeb) 
        {
            if(id2cluster.containsKey(phi.id))
            {
                if(phi.akaI.operandX != null && phi.akaI.operandX instanceof InstructionResult)
                {
                    if(!isInterferingWithCluster(phi.akaI.operandX.getIid(), id2cluster.get(phi.id)))
                    {
                        addToCluster(phi.akaI.operandX.getIid(), id2cluster.get(phi.id));
                    }
                }
    
                if(phi.akaI.operandY != null && phi.akaI.operandY instanceof InstructionResult)
                {
                    if(!isInterferingWithCluster(phi.akaI.operandY.getIid(), id2cluster.get(phi.id)))
                    {
                        addToCluster(phi.akaI.operandY.getIid(), id2cluster.get(phi.id));
                    }
                }
            }
            else if(phi.akaI.operandX != null && phi.akaI.operandX instanceof InstructionResult 
                        && id2cluster.containsKey(phi.akaI.operandX.getIid()))
            {
                if(!isInterferingWithCluster(phi.id, id2cluster.get(phi.akaI.operandX.getIid())))
                {
                    addToCluster(phi.id, id2cluster.get(phi.akaI.operandX.getIid()));
                    if(phi.akaI.operandY != null && phi.akaI.operandY instanceof InstructionResult)
                    {
                        if(!isInterferingWithCluster(phi.akaI.operandY.getIid(), id2cluster.get(phi.akaI.operandX.getIid())))
                        {
                            addToCluster(phi.akaI.operandY.getIid(), id2cluster.get(phi.akaI.operandX.getIid()));
                        }
                    }
                }
                else
                {
                    clusterCounter += 1;
                    addToCluster(phi.id, clusterCounter);

                    if(phi.akaI.operandY != null && phi.akaI.operandY instanceof InstructionResult)
                    {
                        if(!isInterferingWithCluster(phi.akaI.operandY.getIid(), clusterCounter))
                        {
                            addToCluster(phi.akaI.operandY.getIid(), clusterCounter);
                        }
                        else if(!isInterferingWithCluster(phi.akaI.operandY.getIid(), id2cluster.get(phi.akaI.operandX.getIid())))
                        {
                            addToCluster(phi.akaI.operandY.getIid(), id2cluster.get(phi.akaI.operandX.getIid()));
                        }
                    }
                }
            }
            else if(phi.akaI.operandY != null && phi.akaI.operandY instanceof InstructionResult 
                        && id2cluster.containsKey(phi.akaI.operandY.getIid()))
            {
                if(!isInterferingWithCluster(phi.id, id2cluster.get(phi.akaI.operandY.getIid())))
                {
                    addToCluster(phi.id, id2cluster.get(phi.akaI.operandY.getIid()));
                    if(phi.akaI.operandX != null && phi.akaI.operandX instanceof InstructionResult)
                    {
                        if(!isInterferingWithCluster(phi.akaI.operandX.getIid(), id2cluster.get(phi.akaI.operandY.getIid())))
                        {
                            addToCluster(phi.akaI.operandX.getIid(), id2cluster.get(phi.akaI.operandY.getIid()));
                        }
                    }
                }
                else
                {
                    clusterCounter += 1;
                    addToCluster(phi.id, clusterCounter);

                    if(phi.akaI.operandX != null && phi.akaI.operandX instanceof InstructionResult)
                    {
                        if(!isInterferingWithCluster(phi.akaI.operandX.getIid(), clusterCounter))
                        {
                            addToCluster(phi.akaI.operandX.getIid(), clusterCounter);
                        }
                        else if(!isInterferingWithCluster(phi.akaI.operandX.getIid(), id2cluster.get(phi.akaI.operandY.getIid())))
                        {
                            addToCluster(phi.akaI.operandX.getIid(), id2cluster.get(phi.akaI.operandY.getIid()));
                        }
                    }
                }
            }
            else
            {
                clusterCounter += 1;
                addToCluster(phi.id, clusterCounter);

                if(phi.akaI.operandX != null && phi.akaI.operandX instanceof InstructionResult)
                {
                    if(!isInterferingWithCluster(phi.akaI.operandX.getIid(), clusterCounter))
                    {
                        addToCluster(phi.akaI.operandX.getIid(), clusterCounter);
                    }
                }
    
                if(phi.akaI.operandY != null && phi.akaI.operandY instanceof InstructionResult)
                {
                    if(!isInterferingWithCluster(phi.akaI.operandY.getIid(), clusterCounter))
                    {
                        addToCluster(phi.akaI.operandY.getIid(), clusterCounter);
                    }
                }
            }
        }

        for (Integer clusterNo : clusters.keySet()) 
        {
            LiveRange unified = new LiveRange(clusterNo);
            for (LiveRange individual : clusters.get(clusterNo)) 
            {
                unified.cost += individual.cost;
                unified.addNeighbors(individual.neighbors);
            }
            unified.cost /= clusters.get(clusterNo).size();
            iGraph.put(clusterNo, unified);
        }

        for (Integer clusterNo : clusters.keySet()) 
        {
            for (LiveRange individual : clusters.get(clusterNo)) 
            {
                replace(individual.id, clusterNo);
            }
        }
    }

    private void ungroupClusters()
    {
        for (Integer clusterNo : clusters.keySet()) 
        {
            if(iGraph.containsKey(clusterNo))
            {
                Integer clusterColor = iGraph.get(clusterNo).color;
                remove(clusterNo);
                for (LiveRange individual : clusters.get(clusterNo)) 
                {
                    individual.color = clusterColor;
                    addLiveRange(individual);
                }
            }   
        }
    }

    private Boolean isInterferingWithCluster(Integer id, Integer clusterNo)
    {
        if(clusters.containsKey(clusterNo))
        {
            for (LiveRange elementLiveRange : clusters.get(clusterNo)) 
            {
                if(elementLiveRange.neighbors.contains(id))
                {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private void addToCluster(Integer id, Integer clusterNo)
    {
        if(iGraph.containsKey(id) && !id2cluster.containsKey(id))
        {
            if(!clusters.containsKey(clusterNo))
            {
                clusters.put(clusterNo, new ArrayList<LiveRange>());
            }

            LiveRange live = iGraph.get(id).clone();
            live.alive = false;
            clusters.get(clusterNo).add(live);
            id2cluster.put(id, clusterNo);
        }
    }

    private void addLiveRange(LiveRange live)
    {
        iGraph.put(live.id, live);
        for (Integer neighbor : live.neighbors) 
        {
            if(iGraph.containsKey(neighbor))
            {
                iGraph.get(neighbor).addNeighbor(live.id);
            }
        }
    }

    private void remove(Integer id)
    {
        iGraph.remove(id);
        for (LiveRange live : iGraph.values()) 
        {
            live.neighbors.remove(id);
        }
    }

    private void replace(Integer id, Integer clusterNo)
    {
        iGraph.remove(id);
        for (LiveRange live : iGraph.values()) 
        {
            if(live.neighbors.contains(id))
            {
                live.neighbors.remove(id);
                live.addNeighbor(clusterNo);
            }
        }
    }

    private Boolean checkColoring()
    {
        Integer coloredCount = 0;
        Integer wrongCount = 0;
        Integer usedRegs = 0;
        for (Integer id : iGraph.keySet()) 
        {
            if(iGraph.get(id).color > 0)
            {
                if(iGraph.get(id).color > usedRegs)
                {
                    usedRegs = iGraph.get(id).color;
                }
                coloredCount++;
                for (Integer neighbor : iGraph.get(id).neighbors)
                {
                    if(!id.equals(neighbor) && iGraph.containsKey(id) && iGraph.containsKey(neighbor) 
                            && iGraph.get(id).color == iGraph.get(neighbor).color)
                    {
                        wrongCount++;
                        System.out.println(String.format("Coloring gone wrong for node: %s", id.toString()));
                    }
                }
            }
        }

        if(usedRegs > trueRegSize)
        {
            Integer spilledCount = usedRegs - Constants.SPILL_REGISTER_OFFSET;
            System.out.println("Used registers: " + trueRegSize.toString() + "/" 
                                    + trueRegSize.toString() + ", spilled " + spilledCount.toString());
        }
        else
        {
            System.out.println("Used registers: " + usedRegs.toString());
        }

        if(wrongCount > 0)
        {
            System.out.println(wrongCount.toString() + " nodes have wrong colors.");
            return false;
        }

        if(coloredCount == iGraph.keySet().size())
        {
            return true;
        }
        else
        {
            System.out.println("All nodes are NOT colored.");
            return false; 
        }
    }
}