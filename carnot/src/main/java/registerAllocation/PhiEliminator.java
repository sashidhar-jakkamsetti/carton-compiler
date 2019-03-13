package registerAllocation;

import java.util.*;

import dataStructures.Blocks.*;
import dataStructures.Instructions.*;
import dataStructures.Operator.OperatorCode;
import dataStructures.Results.VariableResult;
import intermediateCodeRepresentation.*;

public class PhiEliminator
{
    private HashMap<Integer, PhiInstruction> phiMap;
    private ArrayList<PhiInstruction> phis;
    private IntermediateCodeGenerator iCodeGenerator;

    public PhiEliminator()
    {
        iCodeGenerator = IntermediateCodeGenerator.getInstance();
        phiMap = new HashMap<Integer, PhiInstruction>();
        phis = new ArrayList<PhiInstruction>();
    }

    public void eliminate(IBlock block)
    {
        if(block instanceof JoinBlock)
        {
            JoinBlock jBlock = (JoinBlock)block;
            HashMap<Integer, PhiInstruction> cPhiMap = jBlock.getPhiMap();
            if(!isExists(cPhiMap))
            {
                phis.addAll(jBlock.getPhis());
                phiMap.putAll(cPhiMap);
                for (PhiInstruction i : cPhiMap.values()) 
                {
                    VariableResult vResult = new VariableResult();
                    vResult.set(i.variable);

                    if(jBlock.getThenBlock() != null)
                    {
                        iCodeGenerator.insertInstructionAtLast(jBlock.getThenBlock(), OperatorCode.move, vResult, i.akaI.operandX);
                    }
                    else
                    {
                        iCodeGenerator.insertInstructionAtLast(jBlock.getParent(), OperatorCode.move, vResult, i.akaI.operandX);
                    }

                    if(jBlock.getElseBlock() != null)
                    {
                        iCodeGenerator.insertInstructionAtLast(jBlock.getElseBlock(), OperatorCode.move, vResult, i.akaI.operandY);
                    }
                    else
                    {
                        iCodeGenerator.insertInstructionAtLast(jBlock.getParent(), OperatorCode.move, vResult, i.akaI.operandY);
                    }
                }
            }
        }
        else if(block instanceof WhileBlock)
        {
            WhileBlock wBlock = (WhileBlock)block;
            HashMap<Integer, PhiInstruction> cPhiMap = wBlock.getPhiMap();
            if(!isExists(cPhiMap))
            {
                phis.addAll(wBlock.getPhis());
                phiMap.putAll(cPhiMap);
                for (PhiInstruction i : cPhiMap.values()) 
                {
                    VariableResult vResult = new VariableResult();
                    vResult.set(i.variable);
                    iCodeGenerator.insertInstructionAtLast(wBlock.getParent(), OperatorCode.move, vResult, i.akaI.operandX);
                    iCodeGenerator.insertInstructionAtLast(wBlock.getChild(), OperatorCode.move, vResult, i.akaI.operandY);
                }
            }
        }
    }

    private Boolean isExists(HashMap<Integer, PhiInstruction> cPhiMap)
    {
        return phiMap.keySet().containsAll(cPhiMap.keySet());
    }

    public ArrayList<PhiInstruction> getPhis()
    {
        return phis;
    }
}