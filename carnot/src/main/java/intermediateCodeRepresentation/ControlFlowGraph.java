package intermediateCodeRepresentation;

import java.util.*;

import dataStructures.Instruction;
import dataStructures.Variable;
import dataStructures.Blocks.BasicBlock;

public class ControlFlowGraph
{
    public static BasicBlock head;
    public static List<Instruction> instructions;

    private static HashMap<Variable, Integer> versionTable;

    public static HashMap<Variable, List<Instruction>> defUseChain;

}