package machineCodeGeneration;

import java.util.List;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.Instruction;
import dataStructures.Instructions.Instruction.DeleteMode;
import dataStructures.Results.*;
import dataStructures.Operator.*;
import intermediateCodeRepresentation.ControlFlowGraph;
import machineCodeGeneration.DLX;
import java.util.*;

public class MachineCodeGenerator 
{
    private ControlFlowGraph cfg;
    private MachineCode[] mCode;
    private Integer mCodeCounter;
    private Stack<IBlock> blockStack;
    private boolean[] alreadyVisitedBlocks;
    private HashMap<Integer, MachineCode> targetNum;
    private HashMap<Integer, Integer> PC2MCPC;
    private Integer MCPC;

    public MachineCodeGenerator(ControlFlowGraph cfg)
    {
        blockStack = new Stack<IBlock>();
        alreadyVisitedBlocks = new boolean[cfg.getAllBlocks().size()];
        this.cfg = cfg;
        mCode = new MachineCode[DLX.MemSize];
        mCodeCounter = 0;
        targetNum = new HashMap<Integer, MachineCode>();
        MCPC = 0;
        PC2MCPC = new HashMap<Integer, Integer>();
    }

    public void generate() throws Exception
    {
        Function main = new Function(cfg.head, cfg.tail);
        main.vManager = cfg.mVariableManager;

        List<MachineCode> byteCode = new ArrayList<MachineCode>();
        generate(main, byteCode);
        byteCode.forEach(c -> mCode[mCodeCounter++] = c);

        for (Function f : cfg.functions) 
        {
            byteCode = new ArrayList<MachineCode>();
            f.vManager.setGlobalVariables(main.vManager.getVariables());
            generate(f, byteCode);
            byteCode.forEach(c -> mCode[mCodeCounter++] = c);
        }
    }

    private void generate(Function function, List<MachineCode> byteCode) throws Exception
    {
        IBlock cBlock = function.head;
        blockStack.push(cBlock);
        alreadyVisitedBlocks[cBlock.getId()] = true;
        while(!blockStack.isEmpty())
        {
            generate(blockStack.pop(), byteCode);
        }
    }

    private void generate(IBlock block, List<MachineCode> byteCode) throws Exception
    {
        // Different types of block might have different functionalities. Handle this case.
        // return the next block for execution. Or use stack and execute by recursivly calling this function if necessary.
        // Need to handle push pop of caller and callee.
        // Global variables are restored in VariableManager. You can check by 'isGlobalVariable(v)'.
        for (Instruction i : block.getInstructions())
        {
            if(i.coloredI != null && i.deleteMode == DeleteMode._NotDeleted)
            {
                compute(i.coloredI, byteCode, MCPC);
                MCPC++;
            }
        }

        if(block instanceof IfBlock)
        {
            IfBlock ifBlock = (IfBlock)block;
            if(ifBlock.getJoinBlock() != null && !alreadyVisitedBlocks[ifBlock.getJoinBlock().getId()])
            {
                blockStack.push(ifBlock.getJoinBlock());
                alreadyVisitedBlocks[ifBlock.getJoinBlock().getId()] = true;
            }
            if(ifBlock.getElseBlock() != null && !alreadyVisitedBlocks[ifBlock.getElseBlock().getId()])
            {
                blockStack.push(ifBlock.getElseBlock());
                alreadyVisitedBlocks[ifBlock.getElseBlock().getId()] = true;
            }
            if(ifBlock.getThenBlock() != null && !alreadyVisitedBlocks[ifBlock.getThenBlock().getId()])
            {
                blockStack.push(ifBlock.getThenBlock());
                alreadyVisitedBlocks[ifBlock.getThenBlock().getId()] = true;
            }
        }
        else if(block instanceof WhileBlock)
        {
            WhileBlock wBlock = (WhileBlock)block;
            if(wBlock.getFollowBlock() != null && !alreadyVisitedBlocks[wBlock.getFollowBlock().getId()])
            {
                blockStack.push(wBlock.getFollowBlock());
                alreadyVisitedBlocks[wBlock.getFollowBlock().getId()] = true;
            }
            if(wBlock.getLoopBlock() != null && !alreadyVisitedBlocks[wBlock.getLoopBlock().getId()])
            {
                blockStack.push(wBlock.getLoopBlock());
                alreadyVisitedBlocks[wBlock.getLoopBlock().getId()] = true;
            }
        }
        else
        {
            if(block.getChild() != null && !alreadyVisitedBlocks[block.getChild().getId()])
            {
                blockStack.push(block.getChild());
                alreadyVisitedBlocks[block.getChild().getId()] = true;
            }
        }
    }

    private void load(IResult result)
    {
        
    }

    private void compute(Instruction instruction, List<MachineCode> byteCode, Integer MCPC) throws Exception
    {
        // After getting the instruction, I have to check the instruction id against the interference graph
        // and see which register it has been allocated to.
        // Also, I have to check whether the register allocated is a spilled register
        // (if the register no. is >100 then that is spilled)
        OperatorCode opcode = instruction.opcode;

        PC2MCPC.put(instruction.id, MCPC);

        // This is needed to update the branch target instruction id to the actual ones used in Machine Code.
        if(targetNum.containsKey(instruction.id))
        {
            if(targetNum.get(instruction.id).op == DLX.BSR)
            {
                Integer oldMCPc = targetNum.get(instruction.id).a;
                targetNum.get(instruction.id).a = MCPC - oldMCPc;
            }
            else
            {
                Integer oldMCPc = targetNum.get(instruction.id).b;
                targetNum.get(instruction.id).b = MCPC - oldMCPc;
            }
        }

        if(opcode == OperatorCode.add)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.ADDI
                            : DLX.ADD;
            Integer regA = cfg.iGraph.get(instruction.id).color;
            if(instruction.operandX instanceof ConstantResult)
            {
                Integer res = ((ConstantResult)instruction.operandX).constant + ((ConstantResult)instruction.operandY).constant;
                MachineCode bC = new MachineCode(DLX.ADDI, regA, 0, res);
                byteCode.add(bC);
            }
            else
            {
                Integer regB = ((RegisterResult)instruction.operandX).register;
                Integer regC = (instruction.operandY instanceof ConstantResult)
                               ? ((ConstantResult)instruction.operandY).constant
                               : ((RegisterResult)instruction.operandY).register;
                MachineCode bC = new MachineCode(mnemo, regA, regB, regC);
                byteCode.add(bC);
            }
        }
        else if(opcode == OperatorCode.sub)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.SUBI
                            : DLX.SUB;
            Integer regA = cfg.iGraph.get(instruction.id).color;
            if(instruction.operandX instanceof ConstantResult)
            {
                Integer res = ((ConstantResult)instruction.operandX).constant - ((ConstantResult)instruction.operandY).constant;
                MachineCode bC = new MachineCode(DLX.ADDI, regA, 0, res);
                byteCode.add(bC);
            }
            else
            {
                Integer regB = ((RegisterResult)instruction.operandX).register;
                Integer regC = (instruction.operandY instanceof ConstantResult)
                               ? ((ConstantResult)instruction.operandY).constant
                               : ((RegisterResult)instruction.operandY).register;
                MachineCode bC = new MachineCode(mnemo, regA, regB, regC);
                byteCode.add(bC);
            }
        }
        else if(opcode == OperatorCode.mul)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.MULI
                            : DLX.MUL;
            Integer regA = cfg.iGraph.get(instruction.id).color;
            if(instruction.operandX instanceof ConstantResult)
            {
                Integer res = ((ConstantResult)instruction.operandX).constant * ((ConstantResult)instruction.operandY).constant;
                MachineCode bC = new MachineCode(DLX.ADDI, regA, 0, res);
                byteCode.add(bC);
            }
            else
            {
                Integer regB = ((RegisterResult)instruction.operandX).register;
                Integer regC = (instruction.operandY instanceof ConstantResult)
                               ? ((ConstantResult)instruction.operandY).constant
                               : ((RegisterResult)instruction.operandY).register;
                MachineCode bC = new MachineCode(mnemo, regA, regB, regC);
                byteCode.add(bC);
            }
        }
        else if(opcode == OperatorCode.div)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.DIVI
                            : DLX.DIV;
            Integer regA = cfg.iGraph.get(instruction.id).color;
            if(instruction.operandX instanceof ConstantResult)
            {
                Integer res = ((ConstantResult)instruction.operandX).constant / ((ConstantResult)instruction.operandY).constant;
                MachineCode bC = new MachineCode(DLX.ADDI, regA, 0, res);
                byteCode.add(bC);
            }
            else
            {
                Integer regB = ((RegisterResult)instruction.operandX).register;
                Integer regC = (instruction.operandY instanceof ConstantResult)
                               ? ((ConstantResult)instruction.operandY).constant
                               : ((RegisterResult)instruction.operandY).register;
                MachineCode bC = new MachineCode(mnemo, regA, regB, regC);
                byteCode.add(bC);
            }
        }
        else if(opcode == OperatorCode.cmp)
        {
            Integer mnemo = (instruction.operandY instanceof ConstantResult)
                            ? DLX.CMPI
                            : DLX.CMP;
            Integer regA = cfg.iGraph.get(instruction.id).color;
            if(instruction.operandX instanceof ConstantResult && instruction.operandY instanceof ConstantResult)
            {
                Integer a = ((ConstantResult)instruction.operandX).constant - ((ConstantResult)instruction.operandY).constant;
                if(a < 0)
                {
                    MachineCode bC = new MachineCode(DLX.ADDI, regA, 0, -1);
                    byteCode.add(bC);
                }
                else if(a > 0)
                {
                    MachineCode bC = new MachineCode(DLX.ADDI, regA, 0, 1);
                    byteCode.add(bC);
                }
            }
            else
            {
                Integer regB = (instruction.operandX instanceof ConstantResult)
                               ? ((ConstantResult)instruction.operandX).constant
                               : ((RegisterResult)instruction.operandX).register;
                Integer regC = (instruction.operandY instanceof ConstantResult)
                               ? ((ConstantResult)instruction.operandY).constant
                               : ((RegisterResult)instruction.operandY).register;
                MachineCode bC = new MachineCode(mnemo, regA, regB, regC);
                byteCode.add(bC);
            }
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
                if(instruction.operandX instanceof ConstantResult)
                {
                    Integer mnemo = DLX.ADDI;
                    Integer regB = ((ConstantResult)instruction.operandX).constant;
                    Integer regC = ((RegisterResult)instruction.operandY).register;
                    MachineCode bC = new MachineCode(mnemo, regC, 0, regB);
                    byteCode.add(bC);
                }
                else
                {
                    Integer mnemo = DLX.ADD;
                    Integer regB = ((RegisterResult)instruction.operandX).register;
                    Integer regC = ((RegisterResult)instruction.operandY).register;
                    MachineCode bC = new MachineCode(mnemo, regC, regB, 0);
                    byteCode.add(bC);
                }
            }
        }
        else if(opcode == OperatorCode.beq)
        {
            Integer c = MCPC;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.BEQ, regB, c);
            byteCode.add(bC);
            // Modify c afterwards.
            Integer target = ((InstructionResult)instruction.operandY).iid;
            targetNum.put(target, bC);
        }
        else if(opcode == OperatorCode.bne)
        {
            Integer c = MCPC;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.BNE, regB, c);
            byteCode.add(bC);
            // Modify c afterwards.
            Integer target = ((InstructionResult)instruction.operandY).iid;
            targetNum.put(target, bC);
        }
        else if(opcode == OperatorCode.blt)
        {
            Integer c = MCPC;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.BLT, regB, c);
            byteCode.add(bC);
            // Modify c afterwards.
            Integer target = ((InstructionResult)instruction.operandY).iid;
            targetNum.put(target, bC);
        }
        else if(opcode == OperatorCode.bge)
        {
            Integer c = MCPC;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.BGE, regB, c);
            byteCode.add(bC);
            // Modify c afterwards.
            Integer target = ((InstructionResult)instruction.operandY).iid;
            targetNum.put(target, bC);
        }
        else if(opcode == OperatorCode.ble)
        {
            Integer c = MCPC;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.BLE, regB, c);
            byteCode.add(bC);
            // Modify c afterwards.
            Integer target = ((InstructionResult)instruction.operandY).iid;
            targetNum.put(target, bC);
        }
        else if(opcode == OperatorCode.bgt)
        {
            Integer c = MCPC;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.BGT, regB, c);
            // Modify c afterwards.
            Integer target = ((InstructionResult)instruction.operandY).iid;
            targetNum.put(target, bC);
        }
        else if(opcode == OperatorCode.bra)
        {
            // Distinguish function call from the others
            // because we have to do the prologue and epilogue stuff
            // but, how do we do this...? since the inputSym is no longer accessible
            Integer c = MCPC;
            MachineCode bC = new MachineCode(DLX.BSR, c);
            // Modify c afterwards.
            Integer target = ((InstructionResult)instruction.operandY).iid;
            if(target > instruction.id) // if statement (we have to jump forward)
            {
                targetNum.put(target, bC);
            }
            else // while statement (we have to jump backward)
            {
                bC = new MachineCode(DLX.BSR, PC2MCPC.get(target) - MCPC);
            }
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.read)
        {
            Integer regA = cfg.iGraph.get(instruction.id).color;
            MachineCode bC = new MachineCode(DLX.RDI, regA);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.write)
        {
            Integer regB = ((RegisterResult)instruction.operandX).register;
            MachineCode bC = new MachineCode(DLX.WRD, regB);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.writeNL)
        {
            MachineCode bC = new MachineCode(DLX.WRL);
            byteCode.add(bC);
        }
        else if(opcode == OperatorCode.end)
        {
            MachineCode bC = new MachineCode(DLX.RET, 0);
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