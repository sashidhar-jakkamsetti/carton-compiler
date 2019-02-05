package dataStructures.Blocks;

import dataStructures.*;
import java.util.*;

public interface IBlock 
{
    public Integer getId();
    public List<Instruction> getInstructions();
    public void addInstruction(int opcode, Result x, Result y);
    public Instruction getInstruction(int programCounter);
    public void setParent(IBlock block);
    public IBlock getParent();
    public void setChild(IBlock block);
    public IBlock getChild(IBlock block);
}