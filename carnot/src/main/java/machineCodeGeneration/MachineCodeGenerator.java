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
    private HashMap<Integer, IResult> fixBranch;
    private HashMap<Integer, Integer> id2pc;
    private Integer pc;

    private HashSet<Integer> returnIds;
    private HashSet<Integer> braFuncIds;

    public MachineCodeGenerator(ControlFlowGraph cfg)
    {
        this.cfg = cfg;
        mCode = new MachineCode[DLX.MemSize];
        mCodeCounter = 0;
        fixBranch = new HashMap<Integer, IResult>();
        pc = 0;
        id2pc = new HashMap<Integer, Integer>();

        returnIds = new HashSet<Integer>();
        storeAllReturns();
        braFuncIds = new HashSet<Integer>();
        storeAllFuncJumps();
    }

    public void generate() throws Exception
    {
        Function main = new Function(cfg.head, cfg.tail);
        main.vManager = cfg.mVariableManager;

        ArrayList<MachineCode> byteCode = new ArrayList<MachineCode>();
        generate(main, byteCode);
        byteCode.forEach(c -> mCode[mCodeCounter++] = c);

        for (Function f : cfg.functions) 
        {
            byteCode = new ArrayList<MachineCode>();
            generate(f, byteCode);
            byteCode.forEach(c -> mCode[mCodeCounter++] = c);
        }
        fixBranch();
    }

    private void generate(Function function, ArrayList<MachineCode> byteCode) throws Exception
    {
        IBlock cBlock = function.head;
        while(cBlock != null)
        {
            cBlock = generate(cBlock, byteCode);
        }
        function.lastMCode = pc;
    }

    private IBlock generate(IBlock block, ArrayList<MachineCode> byteCode) throws Exception
    {
        if(block instanceof IfBlock)
        {
            IfBlock iBlock = (IfBlock)block;
            generate(iBlock, byteCode);

            IBlock tBlock = iBlock.getThenBlock();
            if(iBlock.getThenBlock() != null)
            {
                do
                {
                    tBlock = generate(tBlock, byteCode);
                    if(tBlock == null)
                    {
                        return null;
                    }
                }while(!(tBlock instanceof JoinBlock));
            }

            IBlock eBlock = iBlock.getElseBlock();
            if(iBlock.getElseBlock() != null)
            {
                do
                {
                    eBlock = generate(eBlock, byteCode);
                    if(eBlock == null)
                    {
                        return eBlock;
                    }
                }while(!(eBlock instanceof JoinBlock));
            }

            if(tBlock == iBlock.getChild())
            {
                if(iBlock.getChild() != null)
                {
                    return generate(iBlock.getChild(), byteCode);
                }
                return null;
            }
            else
            {
                System.out.println("Missing if convergence point while generating machine code at blockId = " + block.getId());
            }
        }
        else if(block instanceof WhileBlock)
        {
            WhileBlock wBlock = (WhileBlock)block;
            generate(wBlock, byteCode);

            IBlock lBlock = wBlock.getLoopBlock();
            if(lBlock != null)
            {
                do
                {
                    lBlock = generate(lBlock, byteCode);
                    if(lBlock == null)
                    {
                        return null;
                    }
                }while(lBlock != wBlock);
            }

            if(wBlock.getFollowBlock() != null)
            {
                return generate(wBlock.getFollowBlock(), byteCode);
            }
            return null;
        }
        else
        {
            generate(block, byteCode);
            return block.getChild();
        }

        return null;
    }

    private void generate(Block block, ArrayList<MachineCode> byteCode) throws Exception
    {
        for (Instruction i : block.getInstructions())
        {
            if(i.coloredI != null && i.deleteMode == DeleteMode._NotDeleted)
            {
                byteCode.addAll(compute(i.coloredI));
                id2pc.put(i.id, pc);
                pc++;
            }
        }
    }

    private ArrayList<MachineCode> compute(Instruction instruction) throws Exception
    {
        Integer regA = 0;
        ArrayList<MachineCode> bC = new ArrayList<MachineCode>();
        if(cfg.iGraph.containsKey(instruction.id))
        {
            regA = cfg.iGraph.get(instruction.id).color;
        }

        if(instruction.opcode == OperatorCode.add || instruction.opcode == OperatorCode.adda)
        {
            bC.add(computeArthimetic(instruction, DLX.ADD, regA));
        }
        else if(instruction.opcode == OperatorCode.sub)
        {
            bC.add(computeArthimetic(instruction, DLX.SUB, regA));
        }
        else if(instruction.opcode == OperatorCode.mul)
        {
            bC.add(computeArthimetic(instruction, DLX.MUL, regA));
        }
        else if(instruction.opcode == OperatorCode.div)
        {
            bC.add(computeArthimetic(instruction, DLX.DIV, regA));
        }
        else if(instruction.opcode == OperatorCode.cmp)
        {
            bC.add(computeArthimetic(instruction, DLX.CMP, regA));
        }
        else if(instruction.opcode == OperatorCode.load)
        {
            Integer regB = ((RegisterResult)instruction.operandX).register;
            bC.add(new MachineCode(pc, DLX.LDW, regA, regB, 0));
        }
        else if(instruction.opcode == OperatorCode.store)
        {
            Integer opcode = (instruction.operandY instanceof ConstantResult)? DLX.STX : DLX.STW;
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = (instruction.operandY instanceof ConstantResult)? 
                                ((ConstantResult)instruction.operandY).constant : 
                                    ((RegisterResult)instruction.operandY).register;
            bC.add(new MachineCode(pc, opcode, regA, regB, regC));
        }
        else if(instruction.opcode == OperatorCode.move)
        {
            bC.addAll(computeMove(instruction, regA));
        }
        else if(instruction.opcode == OperatorCode.beq)
        {
            bC.add(computeBranch(instruction, DLX.BEQ, regA));
        }
        else if(instruction.opcode == OperatorCode.bne)
        {
            bC.add(computeBranch(instruction, DLX.BNE, regA));
        }
        else if(instruction.opcode == OperatorCode.blt)
        {
            bC.add(computeBranch(instruction, DLX.BLT, regA));
        }
        else if(instruction.opcode == OperatorCode.bge)
        {
            bC.add(computeBranch(instruction, DLX.BGE, regA));
        }
        else if(instruction.opcode == OperatorCode.ble)
        {
            bC.add(computeBranch(instruction, DLX.BLE, regA));
        }
        else if(instruction.opcode == OperatorCode.bgt)
        {
            bC.add(computeBranch(instruction, DLX.BGT, regA));
        }
        else if(instruction.opcode == OperatorCode.bra)
        {
            bC.addAll(computeForward(instruction, regA));
        }
        else if(instruction.opcode == OperatorCode.read)
        {
            bC.add(new MachineCode(pc, DLX.RDI, regA));
        }
        else if(instruction.opcode == OperatorCode.write)
        {
            Integer regB = ((RegisterResult)instruction.operandX).register;
            bC.add(new MachineCode(pc, DLX.WRD, regB));
        }
        else if(instruction.opcode == OperatorCode.writeNL)
        {
            bC.add(new MachineCode(pc, DLX.WRL));
        }
        else if(instruction.opcode == OperatorCode.end)
        {
            bC.add(new MachineCode(pc, DLX.RET, 0));
        }

        return bC;
    }

    private ArrayList<MachineCode> computeMove(Instruction instruction, Integer regA)
    {
        ArrayList<MachineCode> bC = new ArrayList<MachineCode>();
        if(instruction.id == instruction.operandY.getIid())
        {

        }
        else
        {
            if(instruction.operandX instanceof ConstantResult)
            {
                Integer mnemo = DLX.ADDI;
                Integer b = ((ConstantResult)instruction.operandX).constant;
                Integer regC = ((RegisterResult)instruction.operandY).register;
                bC .add(new MachineCode(pc, mnemo, regC, 0, b));
            }
            else
            {
                Integer mnemo = DLX.ADD;
                Integer regB = ((RegisterResult)instruction.operandX).register;
                Integer regC = ((RegisterResult)instruction.operandY).register;
                bC .add(new MachineCode(pc, mnemo, regC, regB, 0));
            }
        }

        return bC;
    }

    private ArrayList<MachineCode> computeForward(Instruction instruction, Integer regA)
    {
        ArrayList<MachineCode> bC = new ArrayList<MachineCode>();
        //bC = new MachineCode(pc, DLX.BSR, pc);
        Integer target = ((InstructionResult)instruction.operandY).iid;
        if(target > instruction.id) // if statement (we have to jump forward)
        {
            fixBranch.put(pc, ((InstructionResult)instruction.operandY));
        }
        else // while statement (we have to jump backward)
        {
            //bC = new MachineCode(pc, DLX.BSR, id2pc.get(target) - pc);
        }

        return bC;
    }

    private MachineCode computeBranch(Instruction instruction, Integer opcode, Integer regA)
    {
        Integer regB = ((RegisterResult)instruction.operandX).register;
        fixBranch.put(pc, instruction.operandY);
        return new MachineCode(pc, opcode, regB, pc);
    }

    private MachineCode computeArthimetic(Instruction instruction, Integer opcode, Integer regA) 
    {
        if(instruction.operandX instanceof ConstantResult && instruction.operandY instanceof ConstantResult)
        {
            Integer res = ((ConstantResult)instruction.operandX).constant 
                            + ((ConstantResult)instruction.operandY).constant;
            if(opcode == 1 || opcode == 5)
            {
                res = ((ConstantResult)instruction.operandX).constant 
                        - ((ConstantResult)instruction.operandY).constant;
            }
            else if(opcode == 2)
            {
                res = ((ConstantResult)instruction.operandX).constant 
                        * ((ConstantResult)instruction.operandY).constant;
            }
            else if(opcode == 3)
            {
                res = ((ConstantResult)instruction.operandX).constant 
                        / ((ConstantResult)instruction.operandY).constant;
            }
            return new MachineCode(pc, DLX.ADDI, regA, 0, res);
        }
        else if(instruction.operandX instanceof ConstantResult)
        {
            Integer regB = ((RegisterResult)instruction.operandY).register;
            Integer c = ((ConstantResult)instruction.operandX).constant;
            return new MachineCode(pc, opcode + 16, regA, regB, c);
        }
        else if(instruction.operandX instanceof ConstantResult)
        {
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer c = ((ConstantResult)instruction.operandY).constant;
            return new MachineCode(pc, opcode + 16, regA, regB, c);
        }
        else
        {
            Integer regB = ((RegisterResult)instruction.operandX).register;
            Integer regC = ((RegisterResult)instruction.operandY).register;
            return new MachineCode(pc, opcode + 16, regA, regB, regC);
        }
    }

    private void fixBranch()
    {
        for (Integer id : fixBranch.keySet()) 
        {
            if(fixBranch.get(id) instanceof InstructionResult)
            {
                mCode[id].c = fixBranch.get(id).getIid();
            }
            else if(fixBranch.get(id) instanceof BranchResult)
            {
                BranchResult bResult = (BranchResult)fixBranch.get(id);
                if(((Block)bResult.targetBlock).belongsTo != null)
                {
                    mCode[id].c = ((Block)bResult.targetBlock).belongsTo.lastMCode;
                }
            }
        }
    }

    private void storeAllReturns()
    {
        for (Function f : cfg.functions)
        {
            if(f.returnInstruction != null)
            {
                returnIds.add(f.returnInstruction.getIid());
            }
        }
    }

    private void storeAllFuncJumps()
    {
        for (Function f : cfg.functions)
        {
            Boolean isSet = false;
            IBlock nBlock = f.head;
            while(!isSet && nBlock != null)
            {
                for (Instruction first : nBlock.getInstructions()) 
                {
                    if(first.deleteMode == DeleteMode._NotDeleted)
                    {
                        isSet = true;
                        braFuncIds.add(first.id);
                        break;
                    }
                }
                nBlock = nBlock.getChild();
            }
        }
    }

    public MachineCode[] getMCode()
    {
        return mCode;
    }

    public Integer getMCodeLength()
    {
        return mCodeCounter;
    }

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