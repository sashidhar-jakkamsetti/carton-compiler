package machineCodeGeneration;

import java.util.*;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.Instruction;
import dataStructures.Instructions.Instruction.DeleteMode;
import dataStructures.Results.*;
import dataStructures.Operator.*;
import intermediateCodeRepresentation.ControlFlowGraph;
import machineCodeGeneration.DLX;
import utility.Constants;

public class MachineCodeGenerator 
{
    private ControlFlowGraph cfg;
    private MachineCode[] mCode;
    private Integer mCodeCounter;
    private HashMap<Integer, IResult> fixBranch;
    private HashMap<Integer, Integer> id2pc;
    private Integer pc;
    private Integer regSize;

    private HashMap<Integer, Integer> returnIds;
    private HashMap<Integer, Integer> funcFirst;
    private HashMap<Integer, Integer> params2Func;

    private Boolean pushedLocals;

    public MachineCodeGenerator(ControlFlowGraph cfg, Integer regSize)
    {
        this.cfg = cfg;
        mCode = new MachineCode[DLX.MemSize];
        mCodeCounter = 0;
        fixBranch = new HashMap<Integer, IResult>();
        pc = 0;
        this.regSize = regSize;
        id2pc = new HashMap<Integer, Integer>();

        returnIds = cfg.getAllReturns();
        funcFirst = cfg.getAllFuncFirsts();
        params2Func = cfg.getParams2Func();

        pushedLocals = false;
    }

    public void generate() throws Exception
    {
        // Set stack pointer
        mCode[mCodeCounter++] = new MachineCode(pc++, DLX.ADDI, Constants.R_STACK_POINTER, Constants.R0, 0);
        // Set global variable pointer
        mCode[mCodeCounter++] = new MachineCode(pc++, DLX.ADDI, Constants.R_GLOBAL_POINTER, Constants.R0, Constants.GLOBAL_VARIABLE_ADDRESS_OFFSET);

        Function main = new Function(cfg.head, cfg.tail);
        main.name = "main";
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

        // Set frame pointer
        byteCode.add(new MachineCode(pc++, DLX.ADDI, Constants.R_FRAME_POINTER, Constants.R_STACK_POINTER, 0));

        while(cBlock != null)
        {
            cBlock = generate(cBlock, byteCode);
        }
        function.lastMCode = pc;

        if(function.name != "main" && function.returnInstruction == null)
        {
            epilog(byteCode);

            // Cleanup formals
            popFormals(byteCode, function);

            // Return to the caller
            byteCode.add(new MachineCode(pc++, DLX.RET, null, null, Constants.R_RETURN_ADDRESS));
        }
    }

    private IBlock generate(IBlock block, ArrayList<MachineCode> byteCode) throws Exception
    {
        if(block instanceof IfBlock)
        {
            IfBlock iBlock = (IfBlock)block;
            generate((Block)iBlock, byteCode);

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

            if(tBlock == iBlock.getJoinBlock())
            {
                if(iBlock.getJoinBlock() != null)
                {
                    return generate(iBlock.getJoinBlock(), byteCode);
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
            generate((Block)wBlock, byteCode);

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
            generate((Block)block, byteCode);
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
                id2pc.put(i.id, pc);
                byteCode.addAll(compute(i.coloredI));
            }
        }
    }

    // Handle: spill regs
    private ArrayList<MachineCode> compute(Instruction instruction) throws Exception
    {
        int regA = 0;
        ArrayList<MachineCode> bC = new ArrayList<MachineCode>();
        if(cfg.iGraph.containsKey(instruction.id))
        {
            regA = checkSpill(cfg.iGraph.get(instruction.id).color, 0, true, bC);
            storeProxyRegister(regA, 0, bC);
        }

        if(instruction.opcode == OperatorCode.add || instruction.opcode == OperatorCode.adda)
        {
            bC.addAll(computeArthimetic(instruction, DLX.ADD, regA));
        }
        else if(instruction.opcode == OperatorCode.sub)
        {
            bC.addAll(computeArthimetic(instruction, DLX.SUB, regA));
        }
        else if(instruction.opcode == OperatorCode.mul)
        {
            bC.addAll(computeArthimetic(instruction, DLX.MUL, regA));
        }
        else if(instruction.opcode == OperatorCode.div)
        {
            bC.addAll(computeArthimetic(instruction, DLX.DIV, regA));
        }
        else if(instruction.opcode == OperatorCode.cmp)
        {
            bC.addAll(computeArthimetic(instruction, DLX.CMP, regA));
        }
        else if(instruction.opcode == OperatorCode.load)
        {
            bC.addAll(computeLoad(instruction, regA));
        }
        else if(instruction.opcode == OperatorCode.store)
        {
            bC.addAll(computeStore(instruction));
        }
        else if(instruction.opcode == OperatorCode.move)
        {
            bC.addAll(computeMove(instruction));
        }
        else if(instruction.opcode == OperatorCode.beq)
        {
            bC.addAll(computeBranch(instruction, DLX.BEQ));
        }
        else if(instruction.opcode == OperatorCode.bne)
        {
            bC.addAll(computeBranch(instruction, DLX.BNE));
        }
        else if(instruction.opcode == OperatorCode.blt)
        {
            bC.addAll(computeBranch(instruction, DLX.BLT));
        }
        else if(instruction.opcode == OperatorCode.bge)
        {
            bC.addAll(computeBranch(instruction, DLX.BGE));
        }
        else if(instruction.opcode == OperatorCode.ble)
        {
            bC.addAll(computeBranch(instruction, DLX.BLE));
        }
        else if(instruction.opcode == OperatorCode.bgt)
        {
            bC.addAll(computeBranch(instruction, DLX.BGT));
        }
        else if(instruction.opcode == OperatorCode.bra)
        {
            bC.addAll(computeForward(instruction));
        }
        else if(instruction.opcode == OperatorCode.read)
        {
            bC.add(new MachineCode(pc++, DLX.RDI, regA));
        }
        else if(instruction.opcode == OperatorCode.write)
        {
            if(instruction.operandX instanceof RegisterResult)
            {
                int regB = checkSpill(((RegisterResult)instruction.operandX).register, 0, false, bC);
                bC.add(new MachineCode(pc++, DLX.WRD, null, regB, null));
            }
            else if(instruction.operandX instanceof ConstantResult)
            {
                bC.add(new MachineCode(pc++, DLX.WRD, null, ((ConstantResult)instruction.operandX).constant, null));
            }
        }
        else if(instruction.opcode == OperatorCode.writeNL)
        {
            bC.add(new MachineCode(pc++, DLX.WRL));
        }
        else if(instruction.opcode == OperatorCode.end)
        {
            bC.add(new MachineCode(pc++, DLX.RET, null, null, 0));
        }

        return bC;
    }

    private ArrayList<MachineCode> computeLoad(Instruction instruction, int regA) throws Exception
    {
        ArrayList<MachineCode> bC = new ArrayList<MachineCode>();

        if(instruction.operandY instanceof VariableResult && ((VariableResult)instruction.operandY).variable.version == Constants.GLOBAL_VARIABLE_VERSION)
        {
            Variable v = ((VariableResult)instruction.operandY).variable;
            bC.add(new MachineCode(pc++, DLX.LDW, regA, Constants.R_GLOBAL_POINTER, -1 * v.address));
        }
        else if(instruction.operandY instanceof RegisterResult)
        {
            int regB = checkSpill(((RegisterResult)instruction.operandY).register, 1, false, bC);
            bC.add(new MachineCode(pc++, DLX.LDW, regA, regB, 0));
        }
        
        return bC;
    }

    private ArrayList<MachineCode> computeStore(Instruction instruction) throws Exception
    {
        ArrayList<MachineCode> bC = new ArrayList<MachineCode>();

        if(instruction.operandY instanceof VariableResult && ((VariableResult)instruction.operandY).variable.version == Constants.GLOBAL_VARIABLE_VERSION)
        {
            Variable v = ((VariableResult)instruction.operandY).variable;
            if(instruction.operandX instanceof ConstantResult)
            {
                bC.add(new MachineCode(pc++, DLX.ADDI, Constants.R_TEMP, Constants.R0, ((ConstantResult)instruction.operandX).constant));
                bC.add(new MachineCode(pc++, DLX.STW, Constants.R_TEMP, Constants.R_GLOBAL_POINTER, -1 * v.address));
            }
            else
            {
                Integer regA = checkSpill(((RegisterResult)instruction.operandX).register, 2, false, bC);
                bC.add(new MachineCode(pc++, DLX.STW, regA, Constants.R_GLOBAL_POINTER, -1 * v.address));
            }
        }
        else if(instruction.operandY instanceof RegisterResult)
        {
            int regB = checkSpill(((RegisterResult)instruction.operandY).register, 1, false, bC);
            if(instruction.operandX instanceof ConstantResult)
            {
                bC.add(new MachineCode(pc++, DLX.ADDI, Constants.R_TEMP, Constants.R0, ((ConstantResult)instruction.operandX).constant));
                bC.add(new MachineCode(pc++, DLX.STW, Constants.R_TEMP, regB, 0));
            }
            else
            {
                Integer regA = checkSpill(((RegisterResult)instruction.operandX).register, 2, false, bC);
                bC.add(new MachineCode(pc++, DLX.STW, regA, regB, 0));
            }
        }
        
        return bC;
    }

    private ArrayList<MachineCode> computeMove(Instruction instruction) throws Exception
    {
        ArrayList<MachineCode> bC = new ArrayList<MachineCode>();

        // Register moves
        if(instruction.operandY instanceof RegisterResult && instruction.operandX instanceof RegisterResult)
        {
            int regB = checkSpill(((RegisterResult)instruction.operandY).register, 0, true, bC);
            storeProxyRegister(regB, 0, bC);
            int regC = checkSpill(((RegisterResult)instruction.operandX).register, 1, false, bC);
            bC.add(new MachineCode(pc++, DLX.ADD, regB, Constants.R0, regC));
        }
        else if(instruction.operandY instanceof RegisterResult && instruction.operandX instanceof ConstantResult)
        {
            int regB = checkSpill(((RegisterResult)instruction.operandY).register, 0, true, bC);
            storeProxyRegister(regB, 0, bC);
            Integer c = ((ConstantResult)instruction.operandX).constant;
            bC.add(new MachineCode(pc++, DLX.ADDI, regB, Constants.R0, c));
        }

        // Return instruction
        if(instruction.operandY instanceof InstructionResult && returnIds.containsKey(instruction.operandY.getIid()))
        {
            Function function = cfg.getFunction(returnIds.get(instruction.operandY.getIid()));

            // Epilog
            epilog(bC);

            // Cleanup formals
            popFormals(bC, function);

            // Place return result
            if(instruction.operandY.getIid() > 0)
            {
                if(instruction.operandX instanceof RegisterResult)
                {
                    int returnReg = checkSpill(((RegisterResult)instruction.operandX).register, 0, false, bC);
                    bC.add(new MachineCode(pc++, DLX.PSH, returnReg, Constants.R_STACK_POINTER, Constants.BYTE_SIZE));
                }
                else if(instruction.operandX instanceof ConstantResult)
                {
                    bC.add(new MachineCode(pc++, DLX.ADDI, Constants.R_TEMP, Constants.R0, ((ConstantResult)instruction.operandX).constant));
                    bC.add(new MachineCode(pc++, DLX.PSH, Constants.R_TEMP, Constants.R_STACK_POINTER, Constants.BYTE_SIZE));
                }
            }

            // Return to the caller
            bC.add(new MachineCode(pc++, DLX.RET, null, null, Constants.R_RETURN_ADDRESS));
        }

        // Formal params
        if(instruction.operandY instanceof VariableResult 
                && ((VariableResult)instruction.operandY).variable.version == Constants.FORMAL_PARAMETER_VERSION)
        {
            // Store locals
            if(!pushedLocals)
            {
                pushLocals(bC);
                pushedLocals = true;
            }

            // Push formal
            if(instruction.operandX instanceof ConstantResult)
            {
                Integer a = ((ConstantResult)instruction.operandX).constant;
                bC.add(new MachineCode(pc++, DLX.ADDI, Constants.R_TEMP, Constants.R0, a));
                bC.add(new MachineCode(pc++, DLX.PSH, Constants.R_TEMP, Constants.R_STACK_POINTER, Constants.BYTE_SIZE));
            }
            else if(instruction.operandX instanceof RegisterResult)
            {
                int regA = checkSpill(((RegisterResult)instruction.operandX).register, 0, false, bC);
                bC.add(new MachineCode(pc++, DLX.PSH, regA, Constants.R_STACK_POINTER, Constants.BYTE_SIZE));
            }
        }
        else if(instruction.operandX instanceof VariableResult 
                    && ((VariableResult)instruction.operandX).variable.version == Constants.FORMAL_PARAMETER_VERSION)
        {
            // Load formal
            Variable v = ((VariableResult)instruction.operandX).variable;
            if(params2Func.containsKey(v.address))
            {
                Function function = cfg.getFunction(params2Func.get(v.address));
                for(Integer pos = 0; pos < function.getParameters().size(); pos++)
                {
                    if(function.getParameter(pos) instanceof VariableResult)
                    {
                        Variable v1 = ((VariableResult)function.getParameter(pos)).variable;
                        if(v1.address == v.address)
                        {
                            Integer paramLoc = 2 * Constants.BYTE_SIZE + function.getParameters().size() - 1 - pos;
                            if(instruction.operandY instanceof RegisterResult)
                            {
                                int regA = checkSpill(((RegisterResult)instruction.operandY).register, 0, true, bC);
                                bC.add(new MachineCode(pc++, DLX.LDW, regA, Constants.R_STACK_POINTER, paramLoc));
                                storeProxyRegister(regA, 0, bC);
                            }
                        }
                    }
                }
            }
        }

        return bC;
    }

    private ArrayList<MachineCode> computeForward(Instruction instruction) throws Exception
    {
        ArrayList<MachineCode> bC = new ArrayList<MachineCode>();

        // Function call
        if(instruction.operandY instanceof InstructionResult && funcFirst.containsKey(instruction.operandY.getIid()))
        {
            Function function = cfg.getFunction(funcFirst.get(instruction.operandY.getIid()));

            // Store locals
            if(function.getParameters().size() == 0)
            {
                pushLocals(bC);
            }
            else
            {
                pushedLocals = false;
            }

            // Prolog
            prolog(bC);
            fixBranch.put(pc, instruction.operandY);

            // Jump to the callee
            bC.add(new MachineCode(pc++, DLX.JSR, null, null, pc));

            // Load locals
            popLocals(bC);

            // Pickup return result
            if(function.returnInstruction != null && function.returnInstruction.getIid() > 0)
            {
                if(cfg.iGraph.containsKey(function.returnInstruction.getIid()))
                {
                    int returnReg = checkSpill(cfg.iGraph.get(function.returnInstruction.getIid()).color, 0, true, bC);
                    if(returnReg > 0)
                    {
                        bC.add(new MachineCode(pc++, DLX.POP, returnReg, Constants.R_STACK_POINTER, -1 * Constants.BYTE_SIZE));
                        storeProxyRegister(returnReg, 0, bC);
                    }
                }
            }
        }
        else
        {
            fixBranch.put(pc, instruction.operandY);
            bC.add(new MachineCode(pc++, DLX.JSR, null, null, pc));
        }

        return bC;
    }

    private ArrayList<MachineCode> computeBranch(Instruction instruction, Integer opcode) throws Exception
    {
        ArrayList<MachineCode> bC = new ArrayList<MachineCode>();
        int regA = checkSpill(((RegisterResult)instruction.operandX).register, 0, false, bC);
        fixBranch.put(pc, instruction.operandY);
        bC.add(new MachineCode(pc++, opcode, regA, null, pc));
        return bC;
    }

    private ArrayList<MachineCode> computeArthimetic(Instruction instruction, Integer opcode, int regA) throws Exception
    {
        ArrayList<MachineCode> bC = new ArrayList<MachineCode>();
        IResult opYSub = instruction.operandY;
        if(opcode == DLX.ADD && opYSub instanceof VariableResult && ((VariableResult)opYSub).isArray)
        {
            Integer arrayAddress = ((VariableResult)opYSub).variable.address;
            if(((VariableResult)opYSub).variable.address <= Constants.ARRAY_ADDRESS_OFFSET)
            {
                arrayAddress += Constants.ARRAY_ADDRESS_OFFSET;
            }
            opYSub = new ConstantResult(arrayAddress);
        }

        if(instruction.operandX instanceof ConstantResult && opYSub instanceof ConstantResult)
        {
            Integer res = ((ConstantResult)instruction.operandX).constant 
                            + ((ConstantResult)opYSub).constant;
            if(opcode == 1 || opcode == 5)
            {
                res = ((ConstantResult)instruction.operandX).constant 
                        - ((ConstantResult)opYSub).constant;
            }
            else if(opcode == 2)
            {
                res = ((ConstantResult)instruction.operandX).constant 
                        * ((ConstantResult)opYSub).constant;
            }
            else if(opcode == 3)
            {
                res = ((ConstantResult)instruction.operandX).constant 
                        / ((ConstantResult)opYSub).constant;
            }
            bC.add(new MachineCode(pc++, DLX.ADDI, regA, 0, res));
        }
        else if(instruction.operandX instanceof ConstantResult)
        {
            int regB = checkSpill(((RegisterResult)opYSub).register, 1, false, bC);
            Integer c = ((ConstantResult)instruction.operandX).constant;
            bC.add(new MachineCode(pc++, opcode + 16, regA, regB, c));
        }
        else if(opYSub instanceof ConstantResult)
        {
            int regB = checkSpill(((RegisterResult)instruction.operandX).register, 1, false, bC);
            Integer c = ((ConstantResult)opYSub).constant;
            bC.add(new MachineCode(pc++, opcode + 16, regA, regB, c));
        }
        else
        {
            int regB = checkSpill(((RegisterResult)instruction.operandX).register, 1, false, bC);
            int regC = checkSpill(((RegisterResult)opYSub).register, 2, false, bC);
            bC.add(new MachineCode(pc++, opcode, regA, regB, regC));
        }

        return bC;
    }

    private void prolog(ArrayList<MachineCode> byteCode)
    {
        byteCode.add(new MachineCode(pc++, DLX.ADDI, Constants.R_RETURN_ADDRESS, Constants.R0, (pc + 5)));
        byteCode.add(new MachineCode(pc++, DLX.PSH, Constants.R_RETURN_ADDRESS, Constants.R_STACK_POINTER, Constants.BYTE_SIZE));
        byteCode.add(new MachineCode(pc++, DLX.PSH, Constants.R_FRAME_POINTER, Constants.R_STACK_POINTER, Constants.BYTE_SIZE));
        byteCode.add(new MachineCode(pc++, DLX.ADD, Constants.R_FRAME_POINTER, Constants.R0, Constants.R_STACK_POINTER));
    }

    private void epilog(ArrayList<MachineCode> byteCode)
    {
        byteCode.add(new MachineCode(pc++, DLX.ADD, Constants.R_STACK_POINTER, Constants.R0, Constants.R_FRAME_POINTER));
        byteCode.add(new MachineCode(pc++, DLX.POP, Constants.R_FRAME_POINTER, Constants.R_STACK_POINTER, -1 * Constants.BYTE_SIZE));
        byteCode.add(new MachineCode(pc++, DLX.POP, Constants.R_RETURN_ADDRESS, Constants.R_STACK_POINTER, -1 * Constants.BYTE_SIZE));
    }

    private void pushLocals(ArrayList<MachineCode> byteCode)
    {
        for (Integer reg = 1; reg <= regSize; reg++)
        {
            byteCode.add(new MachineCode(pc++, DLX.PSH, reg, Constants.R_STACK_POINTER, Constants.BYTE_SIZE));
        }
    }

    private void popLocals(ArrayList<MachineCode> byteCode)
    {
        for (Integer reg = regSize; reg >= 1; reg--)
        {
            byteCode.add(new MachineCode(pc++, DLX.POP, reg, Constants.R_STACK_POINTER, -1 * Constants.BYTE_SIZE));
        }
    }

    private void popFormals(ArrayList<MachineCode> byteCode, Function function)
    {
        if(function != null)
        {
            for (Integer param = function.getParameters().size(); param >= 0; param--)
            {
                byteCode.add(new MachineCode(pc++, DLX.POP, Constants.R_TEMP, Constants.R_STACK_POINTER, -1 * Constants.BYTE_SIZE));
            }
        }
    }

    private int checkSpill(int reg, Integer proxyNum, Boolean def, ArrayList<MachineCode> bC)
    {
        if(def)
        {
            if(reg > Constants.SPILL_REGISTER_OFFSET)
            {
                return getProxyRegister(proxyNum);
            }
        }
        else
        {
            if(reg > Constants.SPILL_REGISTER_OFFSET)
            {
                bC.add(new MachineCode(pc++, DLX.LDW, getProxyRegister(proxyNum), Constants.R0, reg));
                return getProxyRegister(proxyNum);
            }
        }

        return reg;
    }

    private int getProxyRegister(int proxyNum)
    {
        if(proxyNum < 3)
        {
            return Constants.R_PROXY_OFFSET - proxyNum;
        }
        return Constants.R_PROXY_OFFSET;
    }

    private void storeProxyRegister(int reg, Integer proxyNum, ArrayList<MachineCode> bC)
    {
        if(reg > Constants.SPILL_REGISTER_OFFSET)
        {
            bC.add(new MachineCode(pc++, DLX.STW, getProxyRegister(proxyNum), Constants.R0, reg));
        }
    }

    private void fixBranch()
    {
        for (Integer id : fixBranch.keySet()) 
        {
            if(fixBranch.get(id) instanceof InstructionResult)
            {
                if(id2pc.containsKey(fixBranch.get(id).getIid()))
                {
                    if(mCode[id].op == DLX.JSR && returnIds.containsKey(fixBranch.get(id).getIid()))
                    {
                        mCode[id].c = id2pc.get(fixBranch.get(id).getIid());
                    }
                    else
                    {
                        mCode[id].c = id2pc.get(fixBranch.get(id).getIid()) + 1;
                    }
                }
            }
            else if(fixBranch.get(id) instanceof BranchResult)
            {
                BranchResult bResult = (BranchResult)fixBranch.get(id);
                if(((Block)bResult.targetBlock).belongsTo != null)
                {
                    if(((Block)bResult.targetBlock).belongsTo.lastMCode != -1)
                    {
                        mCode[id].c = ((Block)bResult.targetBlock).belongsTo.lastMCode;
                    }
                    else 
                    {
                        mCode[id].c = id + 1;
                    }
                }
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