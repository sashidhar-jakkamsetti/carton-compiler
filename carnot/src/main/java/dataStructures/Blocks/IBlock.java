package dataStructures.Blocks;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Results.*;
import dataStructures.Instructions.*;
import intermediateCodeRepresentation.*;

import java.util.*;

public interface IBlock 
{
    public Integer getId();
    public List<Instruction> getInstructions();
    public void addInstruction(Instruction instruction);
    public void addInstruction(ArrayList<Instruction> instruction);
    public Instruction getInstruction(int programCounter);
    public void setParent(IBlock block);
    public IBlock getParent();
    public void setChild(IBlock block);
    public IBlock getChild(IBlock block);
    public String toString();
}