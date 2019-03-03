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

    public void reset()
    {
        cpMap.clear();
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
        instruction.setAkaInstruction(instruction.operandX, instruction.operandY);

        // Copy Propagation
        if(instruction.opcode == OperatorCode.move)
        {
            if(instruction.operandY instanceof InstructionResult)
            {
                if(instruction.akaI.operandX instanceof InstructionResult 
                        && cpMap.containsKey(instruction.akaI.operandX.getIid()))
                {
                    instruction.akaI.operandX = cpMap.get(instruction.akaI.operandX.getIid());
                }
            }

            if(instruction.operandY instanceof VariableResult)
            {
                // Don't disturb formal parameters
                if(instruction.akaI.operandY instanceof VariableResult 
                            && ((VariableResult)instruction.akaI.operandY).variable.version == -1)
                {
                    return;
                }

                if(instruction.operandX instanceof ConstantResult)
                {
                    instruction.deleteMode = DeleteMode.NUMBER;
                    cpMap.put(instruction.id, instruction.akaI.operandX);
                }
                else
                {
                    instruction.deleteMode = DeleteMode.CP;
                    if(instruction.akaI.operandX instanceof InstructionResult 
                                && cpMap.containsKey(instruction.akaI.operandX.getIid()))
                    {
                        instruction.akaI.operandX = cpMap.get(instruction.akaI.operandX.getIid());
                    }
                    cpMap.put(instruction.akaI.operandY.getIid(), instruction.akaI.operandX);
                }
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
                    cpMap.put(instruction.id, new InstructionResult(cSubexpression.id));
                    instruction.deleteMode = DeleteMode.CSE;
                }
                else
                {
                    if(instruction.opcode == OperatorCode.phi)
                    {
                        if(instruction.akaI.operandX instanceof InstructionResult 
                            && instruction.akaI.operandY instanceof InstructionResult
                                && instruction.akaI.operandX.getIid() == instruction.akaI.operandY.getIid())
                        {
                            instruction.deleteMode = DeleteMode.CP;
                            cpMap.put(instruction.id, instruction.operandX);
                        }
                    }
                    block.addSubexpression(instruction.akaI);
                }
            }
            else if(instruction.opcode == OperatorCode.write || instruction.opcode == OperatorCode.bne ||
                        instruction.opcode == OperatorCode.beq || instruction.opcode == OperatorCode.ble ||
                            instruction.opcode == OperatorCode.blt || instruction.opcode == OperatorCode.bge ||
                                instruction.opcode == OperatorCode.bgt)
            {
                if(instruction.akaI.operandX != null 
                        && instruction.akaI.operandX instanceof InstructionResult 
                                && cpMap.containsKey(instruction.akaI.operandX.getIid()))
                {
                    instruction.akaI.operandX = cpMap.get(instruction.akaI.operandX.getIid());
                }
            }
        }
    }
}