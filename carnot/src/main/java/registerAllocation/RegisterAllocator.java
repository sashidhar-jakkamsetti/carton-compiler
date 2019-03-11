package registerAllocation;

import java.util.*;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;
import dataStructures.Instructions.Instruction.DeleteMode;
import dataStructures.Results.InstructionResult;
import intermediateCodeRepresentation.*;

public class RegisterAllocator
{
    private HashMap<Integer, LiveRange> iGraph;
    private HashMap<Integer, PhiInstruction> phiWeb;

    private Integer regSize = 8;
    private Integer trueRegSize = 8;
    private Integer spillAddress;
    private Integer clusterCounter;

    private HashMap<Integer, ArrayList<LiveRange>> clusters;
    private HashMap<Integer, ArrayList<Integer>> id2cluster;


    public RegisterAllocator(InterferenceGraph iGraph, Integer actualRegsize)
    {
        this.iGraph = iGraph.getIGraph();
        this.phiWeb = iGraph.getPhiMap();
        if(actualRegsize > 0)
        {
            this.trueRegSize = actualRegsize;
            this.regSize = actualRegsize;
        }
        spillAddress = 100;
        clusterCounter = 1000;
        clusters = new HashMap<Integer, ArrayList<LiveRange>>();
    }

    public Boolean allocate(ControlFlowGraph cfg)
    {
        groupClusters();
        color();
        ungroupClusters();
        if(checkColoring())
        {
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
        }
        return false;
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
        live.alive = true;

        Integer availableColor = getAvailableColor(live, regSize);
        if(availableColor == -1)
        {
            live.color = getAvailableColor(live, regSize++);
        }
        else
        {
            live.color = availableColor;
        }
    }

    private Integer findAppropriateNode()
    {
        for (Integer id : iGraph.keySet()) 
        {
            if(iGraph.get(id).neighbors.size() < regSize)
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
            if(iGraph.get(id).cost < min)
            {
                min = iGraph.get(id).cost;
                rId = id;
            }
        }

        return rId;
    }

    private Integer getAvailableColor(LiveRange live, Integer regSize)
    {
        Boolean[] available = new Boolean[regSize];
        Arrays.fill(available, true);

        for (Integer neighbor : live.neighbors) 
        {
            if(iGraph.get(neighbor).alive)
            {
                available[neighbor] = false;
            }
        }

        for (Integer i = 1; i <= regSize; i++) 
        {
            if(available[i])
            {
                if(i > trueRegSize)
                {
                    return spillAddress + i - trueRegSize;
                }
                return i;
            }
        }

        return -1;
    }

    private void groupClusters()
    {
        for (Integer phiId : phiWeb.keySet()) 
        {
            clusterCounter += 1;
            PhiInstruction phi = phiWeb.get(phiId);
            addToCluster(phiId, clusterCounter);
            replace(phiId, clusterCounter);

            if(phi.akaI.operandX != null && phi.akaI.operandX instanceof InstructionResult)
            {
                if(!isInterferingWithCluster(phi.akaI.operandX.getIid(), clusterCounter))
                {
                    addToCluster(phi.akaI.operandX.getIid(), clusterCounter);
                    replace(phi.akaI.operandX.getIid(), clusterCounter);
                }
            }

            if(phi.akaI.operandY != null && phi.akaI.operandY instanceof InstructionResult)
            {
                if(!isInterferingWithCluster(phi.akaI.operandY.getIid(), clusterCounter))
                {
                    addToCluster(phi.akaI.operandY.getIid(), clusterCounter);
                    replace(phi.akaI.operandY.getIid(), clusterCounter);
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
    }

    private void ungroupClusters()
    {
        for (Integer clusterNo : clusters.keySet()) 
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

    private Boolean isInterferingWithCluster(Integer id, Integer clusterNo)
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

    private void addToCluster(Integer id, Integer clusterNo)
    {
        if(iGraph.containsKey(id))
        {
            if(!clusters.containsKey(clusterNo))
            {
                clusters.put(clusterNo, new ArrayList<LiveRange>());
            }

            if(!id2cluster.containsKey(id))
            {
                id2cluster.put(id, new ArrayList<Integer>());
            }

            clusters.get(clusterNo).add(iGraph.get(id));
            id2cluster.get(id).add(clusterNo);
        }
    }

    private void addLiveRange(LiveRange live)
    {
        iGraph.put(live.id, live);
        for (Integer neighbor : live.neighbors) 
        {
            iGraph.get(neighbor).addNeighbor(live.id);
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
            live.neighbors.remove(id);
            live.addNeighbor(clusterNo);
        }
    }

    private Boolean checkColoring()
    {
        for (Integer id : iGraph.keySet()) 
        {
            if(iGraph.get(id).color > 0)
            {
                for (Integer neighbor : iGraph.get(id).neighbors)
                {
                    if(iGraph.get(id).color == iGraph.get(neighbor).color)
                    {
                        System.out.println(String.format("Coloring gone wrong for node: %s", id.toString()));
                        return false;
                    }
                }
            }
        }

        return true;
    }
}