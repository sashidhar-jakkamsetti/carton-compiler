package machineCodeGeneration;

import java.util.HashSet;

import dataStructures.*;
import dataStructures.Blocks.IBlock;
import dataStructures.Instructions.*;
import dataStructures.Results.*;
import intermediateCodeRepresentation.ControlFlowGraph;

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

    private void compute(Instruction instruction, HashSet<MachineCode> byteCode) throws Exception
    {
        // MachineCode is in the format: 'op    a  b   c'
        // CFG already has iGraph which is interfernece graph which tells you the register of each instruction result. Used for filling Reg C.
    }

    private void load(IResult result)
    {
        
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