package dataStructures;

import java.util.*;

public class LiveRange
{
    public Integer id;
    public Integer color;
    public Integer cost;
    public Boolean alive;
    public HashSet<Integer> neighbors;

    public LiveRange(Integer id)
    {
        this(id, -1, 0, true);
    }

    public LiveRange(Integer id, Integer color, Integer cost, Boolean alive)
    {
        this.id = id;
        this.color = color;
        this.cost = cost;
        this.alive = alive;
        neighbors = new HashSet<Integer>();
    }

    public void addNeighbor(Integer id)
    {
        if(!neighbors.contains(id) && this.id != id)
        {
            neighbors.add(id);
        }
    }

    public void addNeighbors(HashSet<Integer> idList)
    {
        for (Integer id : idList) 
        {
            addNeighbor(id);
        }
    }

    public LiveRange clone()
    {
        LiveRange clone = new LiveRange(id, color, cost, alive);
        clone.addNeighbors(neighbors);
        return clone;
    }

    public String toString()
    {
        return id.toString() + "__R" + color.toString(); 
    }
}