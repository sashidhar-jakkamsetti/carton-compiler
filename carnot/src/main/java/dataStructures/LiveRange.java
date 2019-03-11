package dataStructures;

import java.util.ArrayList;
import java.util.HashSet;

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
        if(!neighbors.contains(id))
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

    public String toString()
    {
        return id.toString() + "__R" + color.toString(); 
    }
}