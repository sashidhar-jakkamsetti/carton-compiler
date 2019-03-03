package dataStructures.Blocks;

import dataStructures.DomTreeNode;
import dataStructures.Instructions.*;

import java.util.*;

public interface IBlock 
{
    public Integer getId();
    public List<Instruction> getInstructions();
    public void addInstruction(Instruction instruction);
    public void addInstruction(ArrayList<Instruction> instruction);
    public Instruction getInstruction(Integer programCounter);
    public void setParent(IBlock block);
    public IBlock getParent();
    public void setChild(IBlock block);
    public IBlock getChild();
    public String toString();
    public void freezeSsa(HashMap<Integer, Integer> globalSsa, HashMap<Integer, Integer> localSsa);
    public HashMap<Integer, Integer> getGlobalSsa();
    public HashMap<Integer, Integer> getLocalSsa();

    public Instruction searchCommonSubexpression(Instruction instruction);
    public void addSubexpression(Instruction instruction);
    public DomTreeNode getDomTreeNode();
}