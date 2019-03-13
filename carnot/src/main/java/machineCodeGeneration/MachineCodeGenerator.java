package machineCodeGeneration;

import java.util.HashSet;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.Instruction;
import dataStructures.Results.*;
import dataStructures.Operator.*;
import dataStructures.MachineCode.*;
import intermediateCodeRepresentation.ControlFlowGraph;
import registerAllocation.*;
import java.util.*;
import machineCodeGeneration.DLX;

public class MachineCodeGenerator 
{
    private ControlFlowGraph cfg;
    private MachineCode[] mCode;
    private Integer mCodeCounter;

    public MachineCodeGenerator(ControlFlowGraph cfg)
    {
        this.cfg = cfg;
        mCode = new MachineCode[DLX.MemSize];
        mCodeCounter = 0;
    }

    public void generate() throws Exception
    {
        Function main = new Function(cfg.head, cfg.tail);
        main.vManager = cfg.mVariableManager;

        HashSet<MachineCode> byteCode = new HashSet<MachineCode>();
        generate(main, byteCode);
        byteCode.forEach(c -> mCode[mCodeCounter++] = c);

        for (Function f : cfg.functions) 
        {
            byteCode = new HashSet<MachineCode>();
            f.vManager.setGlobalVariables(main.vManager.getVariables());
            generate(f, byteCode);
            byteCode.forEach(c -> mCode[mCodeCounter++] = c);
        }
    }

    private void generate(Function function, HashSet<MachineCode> byteCode) throws Exception
    {
        IBlock cBlock = function.head;
        while(cBlock != null)
        {
            cBlock = generate(cBlock, byteCode);
        }
    }

    private IBlock generate(IBlock block, HashSet<MachineCode> byteCode) throws Exception
    {
        // Different types of block might have different functionalities. Handle this case.
        // return the next block for execution. Or use stack and execute by recursivly calling this function if necessary.
        // Need to handle push pop of caller and callee.
        // Global variables are restored in VariableManager. You can check by 'isGlobalVariable(v)'.
        for (Instruction i : block.getInstructions())
        {
            compute(i, byteCode);
        }
        return block.getChild();
    }

    private void load(IResult result)
    {
        
    }

    private void compute(Instruction instruction, HashSet<MachineCode> byteCode) throws Exception
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
            Integer regA = cfg.iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            MachineCode bC = new MachineCode(mnemo, regA, regB, regC);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.sub)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.SUBI
                            : DLX.SUB;
            Integer regA = cfg.iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            MachineCode bC = new MachineCode(mnemo, regA, regB, regC);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.mul)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.MULI
                            : DLX.MUL;
            Integer regA = cfg.iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            MachineCode bC = new MachineCode(mnemo, regA, regB, regC);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.div)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.DIVI
                            : DLX.DIV;
            Integer regA = cfg.iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            MachineCode bC = new MachineCode(mnemo, regA, regB, regC);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.cmp)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.CMPI
                            : DLX.CMP;
            Integer regA = cfg.iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            MachineCode bC = new MachineCode(mnemo, regA, regB, regC);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.adda)
        {
            Integer mnemo = DLX.LDW;
            Integer regA = cfg.iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            MachineCode bC = new MachineCode(mnemo, regA, regB, regC);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.load)
        {
            Integer mnemo = DLX.LDW;
            Integer regA = cfg.iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(mnemo, regA, regB);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.store)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult) ? DLX.STX : DLX.STW;
            Integer regA = cfg.iGraph.get(instruction.id).color;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)
                           ? ((ConstantResult)instruction.operandY).constant
                           : ((RegisterResult)instruction.operandY).register;
            MachineCode bC = new MachineCode(mnemo, regA, regB, regC);
            byteCode.add(bC);
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
                MachineCode bC = new MachineCode(mnemo, regB, regC, 0);
                byteCode.add(bC);
            }
        }
        else if(opcode == OperatorCode.beq)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.BEQ, regB, c);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.bne)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.BNE, regB, c);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.blt)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.BLT, regB, c);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.bge)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.BGE, regB, c);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.ble)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.BLE, regB, c);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.bgt)
        {
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.BGT, regB, c);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.bra)
        {
            // Distinguish function call from the others
            // because we have to do the prologue and epilogue stuff
            // but, how do we do this...? since the inputSym is no longer accessible
            IBlock targetBlock = ((BranchResult)instruction.operandY).targetBlock;
            Integer c = targetBlock.getInstructions().get(0).id;
            MachineCode bC = new MachineCode(DLX.BSR, c);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.read)
        {
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.RDI, regB);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.write)
        {
            Integer regA = ((RegisterResult)instruction.operandY).register;
            MachineCode bC = new MachineCode(DLX.WRD, regA);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.writeNL)
        {
            MachineCode bC = new MachineCode(DLX.WRL);
            byteCode.add(bC);
        }
    }

    // Used for printing the machine code.
    public MachineCode[] getMCode()
    {
        return mCode;
    }

    public Integer getMCodeLength()
    {
        return mCodeCounter;
    }

    // Used for DLX load and execute.
    public int[] getCode()
    {
        int[] code = new int[mCodeCounter];
        for(int i = 0; i < mCodeCounter; i++)
        {
            code[i] = mCode[i].toInteger();
        }
        return code;
    }
}