package machineCodeGeneration;

import dataStructures.Blocks.IBlock;
import dataStructures.Instructions.*;
import dataStructures.Results.*;
import dataStructures.Operator.OperatorCode;
import registerAllocation.*;
import java.util.*;
import machineCodeGeneration.DLX;

public class MachineCodeGenerator 
{
    private RegisterAllocator registerAllocator;
    private static ArrayList<Integer> program;

    public MachineCodeGenerator()
    {
        registerAllocator = RegisterAllocator.getInstance();
    }

    private void load(IResult result)
    {
        
    }

    public void compute(Instruction instruction)
    {
        OperatorCode opcode = instruction.opcode;
        if(opcode == OperatorCode.add)
        {
            if(instruction.operandY instanceof ConstantResult)
            {
                program.add(DLX.assemble(DLX.ADDI, ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandX).register,
                                                   ((ConstantResult)instruction.operandY).constant));
            }
            else
            {
                program.add(DLX.assemble(DLX.ADD , ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandY).register));
            }
        }
        else if(opcode == OperatorCode.sub)
        {
            if(instruction.operandY instanceof ConstantResult)
            {
                program.add(DLX.assemble(DLX.SUBI, ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandX).register,
                                                   ((ConstantResult)instruction.operandY).constant));
            }
            else
            {
                program.add(DLX.assemble(DLX.SUB , ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandY).register));
            }
        }
        else if(opcode == OperatorCode.mul)
        {
            if(instruction.operandY instanceof ConstantResult)
            {
                program.add(DLX.assemble(DLX.MULI, ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandX).register,
                                                   ((ConstantResult)instruction.operandY).constant));
            }
            else
            {
                program.add(DLX.assemble(DLX.MUL , ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandY).register));
            }
        }
        else if(opcode == OperatorCode.div)
        {
            if(instruction.operandY instanceof ConstantResult)
            {
                program.add(DLX.assemble(DLX.DIVI, ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandX).register,
                                                   ((ConstantResult)instruction.operandY).constant));
            }
            else
            {
                program.add(DLX.assemble(DLX.DIV , ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandY).register));
            }
        }
        else if(opcode == OperatorCode.cmp)
        {
            if(instruction.operandY instanceof ConstantResult)
            {
                program.add(DLX.assemble(DLX.CMPI, ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandX).register,
                                                   ((ConstantResult)instruction.operandY).constant));
            }
            else
            {
                program.add(DLX.assemble(DLX.CMP , ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandY).register));
            }
        }
        else if(opcode == OperatorCode.adda)
        {
            program.add(DLX.assemble(DLX.LDW, ((RegisterResult)instruction.operandX).register,
                                              ((RegisterResult)instruction.operandX).register,
                                              ((ConstantResult)instruction.operandY).constant));
        }
        else if(opcode == OperatorCode.load)
        {
            program.add(DLX.assemble(DLX.LDW, ((RegisterResult)instruction.operandX).register,
                                              ((RegisterResult)instruction.operandX).register,
                                              0));
        }
        else if(opcode == OperatorCode.store)
        {
            if(instruction.operandY instanceof ConstantResult)
            {
                program.add(DLX.assemble(DLX.STX, ((RegisterResult)instruction.operandX).register,
                                                  ((RegisterResult)instruction.operandX).register,
                                                  ((ConstantResult)instruction.operandY).constant));
            }
            else
            {
                program.add(DLX.assemble(DLX.STW , ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandX).register,
                                                   ((RegisterResult)instruction.operandY).register));
            }
        }
        else if(opcode == OperatorCode.move)
        {
            if(instruction.id == instruction.operandY.getIid()) // Return
            {

            }
            else // Normal move instruction
            {
                if(instruction.operandY instanceof ConstantResult)
                {
                    program.add(DLX.assemble(DLX.ADDI, ((RegisterResult)instruction.operandX).register,
                                                       ((ConstantResult)instruction.operandY).constant,
                                                       0));
                }
                else
                {
                    program.add(DLX.assemble(DLX.ADD , ((RegisterResult)instruction.operandX).register,
                                                       ((RegisterResult)instruction.operandY).register,
                                                       0));
                }
            }
        }
        else if(opcode == OperatorCode.beq)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            program.add(DLX.assemble(DLX.BEQ, ((RegisterResult)instruction.operandX).register,
                                              c));
        }
        else if(opcode == OperatorCode.bne)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            program.add(DLX.assemble(DLX.BNE, ((RegisterResult)instruction.operandX).register,
                                              c));
        }
        else if(opcode == OperatorCode.blt)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            program.add(DLX.assemble(DLX.BLT, ((RegisterResult)instruction.operandX).register,
                                              c));
        }
        else if(opcode == OperatorCode.bge)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            program.add(DLX.assemble(DLX.BGE, ((RegisterResult)instruction.operandX).register,
                                              c));
        }
        else if(opcode == OperatorCode.ble)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            program.add(DLX.assemble(DLX.BLE, ((RegisterResult)instruction.operandX).register,
                                              c));
        }
        else if(opcode == OperatorCode.bgt)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            program.add(DLX.assemble(DLX.BGT, ((RegisterResult)instruction.operandX).register,
                                              c));
        }
        else if(opcode == OperatorCode.bra)
        {
            // Distinguish function call from the others
            // because we have to do the prologue and epilogue stuff
            // but, how do we do this...? since the inputSym is no longer accessible
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            program.add(DLX.assemble(DLX.BSR, c));
        }
        else if(opcode == OperatorCode.read)
        {
            program.add(DLX.assemble(DLX.RDI, ((RegisterResult)instruction.operandX).register));
        }
        else if(opcode == OperatorCode.write)
        {
            program.add(DLX.assemble(DLX.WRD, ((RegisterResult)instruction.operandY).register));
        }
        else if(opcode == OperatorCode.writeNL)
        {
            program.add(DLX.assemble(DLX.WRL));
        }
    }

    public static ArrayList<Integer> getProgram()
    {
        return program;
    }
}