package dataStructures.Blocks;

import dataStructures.Instructions.*;

import java.util.*;

public interface IBlock 
{
    public Integer getId();
    public List<Instruction> getInstructions();
    public void addInstruction(Instruction instruction);
    public Instruction getInstruction(int programCounter);
    public void setParent(IBlock block);
    public IBlock getParent();
    public void setChild(IBlock block);
    public IBlock getChild(IBlock block);
    public String toString();
    public void setSsaMap(HashMap<Integer, Integer> ssaMap);
    public HashMap<Integer, Integer> getSsaMap();
}