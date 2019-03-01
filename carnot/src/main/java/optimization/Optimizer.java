package optimization;

import java.util.*;
import dataStructures.*;
import dataStructures.Operator.OperatorCode;

public class Optimizer
{
    private HashMap<OperatorCode, DominatorTree> dTree;
    private CopyPropagation cpMap;
}