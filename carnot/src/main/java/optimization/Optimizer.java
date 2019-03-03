package optimization;

import java.util.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;
import dataStructures.Instructions.Instruction.DeleteMode;
import dataStructures.Operator.OperatorCode;
import dataStructures.Results.*;

public class Optimizer
{
    private HashMap<Integer, IResult> cpMap;
    private static Optimizer optimizer;
    
    private Optimizer()
    {
        cpMap = new HashMap<Integer, IResult>();
    }

    public static Optimizer getInstance()
    {
        if(optimizer == null)
        {
            optimizer = new Optimizer();
        }

        return optimizer;
    }

    public void optimize(IBlock block, Instruction instruction)
    {
        // Copy Propagation
        if(instruction.opcode == OperatorCode.move)
        {
            if(instruction.operandX instanceof ConstantResult && instruction.operandY instanceof VariableResult)
            {
                instruction.deleteMode = DeleteMode.NUMBER;
                instruction.setAkaInstructionOperand(instruction.operandX);
                cpMap.put(instruction.id, instruction.akaI.operandX);
            }

            if(instruction.operandY instanceof VariableResult)
            {
                instruction.deleteMode = DeleteMode.CP;
                instruction.setAkaInstruction(instruction.operandX, instruction.operandY);
                if(instruction.akaI.operandX instanceof InstructionResult 
                            && cpMap.containsKey(instruction.akaI.operandX.getIid()))
                {
                    instruction.akaI.operandX = cpMap.get(instruction.akaI.operandX.getIid());
                }
                cpMap.put(instruction.akaI.operandY.getIid(), instruction.akaI.operandX);
            }
        }
        else 
        {
            // Common Subexpression Elimination
            if(instruction.opcode == OperatorCode.neg || instruction.opcode == OperatorCode.add ||
                instruction.opcode == OperatorCode.mul || instruction.opcode == OperatorCode.sub ||
                    instruction.opcode == OperatorCode.div || instruction.opcode == OperatorCode.adda ||
                        instruction.opcode == OperatorCode.cmp || instruction.opcode == OperatorCode.store ||
                            instruction.opcode == OperatorCode.load || instruction.opcode == OperatorCode.phi)
            {
                instruction.setAkaInstruction(instruction.operandX, instruction.operandY);
                if(instruction.akaI.operandX != null 
                        && instruction.akaI.operandX instanceof InstructionResult 
                                && cpMap.containsKey(instruction.akaI.operandX.getIid()))
                {
                    instruction.akaI.operandX = cpMap.get(instruction.akaI.operandX.getIid());
                }

                if(instruction.akaI.operandY != null 
                        && instruction.akaI.operandY instanceof InstructionResult 
                                && cpMap.containsKey(instruction.akaI.operandY.getIid()))
                {
                    instruction.akaI.operandY = cpMap.get(instruction.akaI.operandY.getIid());
                }

                Instruction cSubexpression = block.searchCommonSubexpression(instruction.akaI);
                if(cSubexpression != null)
                {
                    instruction.setAkaInstruction(cSubexpression);
                    instruction.deleteMode = DeleteMode.CSE;
                }
                else
                {
                    block.addSubexpression(instruction.akaI);
                }
            }
        }
    }
}