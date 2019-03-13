package machineCodeGeneration;

import dataStructures.LiveRange;
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
    private HashMap<Integer, LiveRange> iGraph;
    private static ArrayList<Integer> program;

    public MachineCodeGenerator(InterferenceGraph iGraph)
    {
        this.iGraph = iGraph.getIGraph();
    }

    private void load(IResult result)
    {
        
    }

    public void compute(Instruction instruction)
    {
        // After getting the instruction, I have to check the instruction id against the interference graph
        // and see which register it has been allocated to.
        // Also, I have to check whether the register allocated is a spilled register
        // (if the register no. is >100 then that is spilled)
        OperatorCode opcode = instruction.opcode;
        // I have to check whether the values actually exist or not.
        if(opcode == OperatorCode.add)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.ADDI
                            : DLX.ADD;
            Integer regA = iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            program.add(DLX.assemble(mnemo, regA, regB, regC));
        }
        else if(opcode == OperatorCode.sub)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.SUBI
                            : DLX.SUB;
            Integer regA = iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            program.add(DLX.assemble(mnemo, regA, regB, regC));
        }
        else if(opcode == OperatorCode.mul)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.MULI
                            : DLX.MUL;
            Integer regA = iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            program.add(DLX.assemble(mnemo, regA, regB, regC));
        }
        else if(opcode == OperatorCode.div)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.DIVI
                            : DLX.DIV;
            Integer regA = iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            program.add(DLX.assemble(mnemo, regA, regB, regC));
        }
        else if(opcode == OperatorCode.cmp)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.CMPI
                            : DLX.CMP;
            Integer regA = iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            program.add(DLX.assemble(mnemo, regA, regB, regC));
        }
        else if(opcode == OperatorCode.adda)
        {
            Integer mnemo = DLX.LDW;
            Integer regA = iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            program.add(DLX.assemble(mnemo, regA, regB, regC));
        }
        else if(opcode == OperatorCode.load)
        {
            Integer mnemo = DLX.LDW;
            Integer regA = iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            program.add(DLX.assemble(mnemo, regA, regB));
        }
        else if(opcode == OperatorCode.store)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult) ? DLX.STX : DLX.STW;
            Integer regA = iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            program.add(DLX.assemble(mnemo, regA, regB, regC));
        }
        else if(opcode == OperatorCode.move)
        {
            if(instruction.id == instruction.operandY.getIid()) // Return
            {

            }
            else // Normal move instruction
            {
                Integer mnemo = (instruction.operandY instanceof ConstantResult)
                                ? DLX.ADDI
                                : DLX.ADD;
                Integer regB = ((RegisterResult)instruction.operandX).register;
                Integer regC = (instruction.operandY instanceof ConstantResult)
                               ? ((ConstantResult)instruction.operandY).constant
                               : ((RegisterResult)instruction.operandY).register;
                program.add(DLX.assemble(mnemo, regB, regC, 0));
            }
        }
        else if(opcode == OperatorCode.beq)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            program.add(DLX.assemble(DLX.BEQ, regB, c));
        }
        else if(opcode == OperatorCode.bne)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            program.add(DLX.assemble(DLX.BNE, regB, c));
        }
        else if(opcode == OperatorCode.blt)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            program.add(DLX.assemble(DLX.BLT, regB, c));
        }
        else if(opcode == OperatorCode.bge)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            program.add(DLX.assemble(DLX.BGE, regB, c));
        }
        else if(opcode == OperatorCode.ble)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            program.add(DLX.assemble(DLX.BLE, regB, c));
        }
        else if(opcode == OperatorCode.bgt)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            program.add(DLX.assemble(DLX.BGT, regB, c));
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
            Integer regB = ((RegisterResult)instruction.operandX).register;
            program.add(DLX.assemble(DLX.RDI, regB));
        }
        else if(opcode == OperatorCode.write)
        {
            Integer regA = ((RegisterResult)instruction.operandY).register;
            program.add(DLX.assemble(DLX.WRD, regA));
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