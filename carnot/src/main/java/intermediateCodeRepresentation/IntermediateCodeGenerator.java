package intermediateCodeRepresentation;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;
import dataStructures.Operator.*;
import dataStructures.Results.*;
import exceptions.IllegalVariableException;
import optimization.Optimizer;

public class IntermediateCodeGenerator
{
    private static Integer pc;
    public static Optimizer optimizer;
    private static IntermediateCodeGenerator iCodeGenerator;

    public static IntermediateCodeGenerator getInstance()
    {
        if(iCodeGenerator == null)
        {
            iCodeGenerator = new IntermediateCodeGenerator();
        }

        return iCodeGenerator;
    }

    public void reset()
    {
        pc = 0;
        optimizer.reset();
    }

    private IntermediateCodeGenerator()
    {
        pc = 0;
        optimizer = Optimizer.getInstance();
    }

    public Integer getPC()
    {
        return pc;
    }

    public void incrementPC()
    {
        pc++;
    }

    public void compute(IBlock block, Token opToken, BranchResult y)
    {
        OperatorCode opCode  = Operator.branchingOperator.get(opToken.type);
        Instruction instruction;
        if(opCode == OperatorCode.bra)
        {
            instruction = new Instruction(pc++, opCode, null, y);
        }
        else
        {
            instruction = new Instruction(pc++, opCode, y.toInstruction(), y);
        }

        block.addInstruction(instruction);
        optimizer.optimize(block, instruction);
    }

    public void compute(IBlock block, Token opToken, IResult x, IResult y)
    {
        compute(block, Operator.getOpCode(opToken), x, y);
    }

    public void compute(IBlock block, OperatorCode opCode, IResult x, IResult y)
    {
        if(opCode == null)
        {
            return;
        }

        Instruction instruction;
        if(opCode == OperatorCode.move || opCode == OperatorCode.store)
        {
            instruction = new Instruction(pc++, opCode, y, x);
        }
        else if(opCode == OperatorCode.load)
        {
            instruction = new Instruction(pc++, opCode, null, y);
        }
        else
        {
            instruction = new Instruction(pc++, opCode, x, y);
        }

        block.addInstruction(instruction);
        optimizer.optimize(block, instruction);
    }

    public void loadArrayElement(IBlock block, VariableManager vManager, IResult vResult) 
    {
        ArrayVar array = (ArrayVar)((VariableResult)vResult).variable;

        if(array.indexList.size() > 0)
        {
            IResult res = array.indexList.get(0);
            if(res.getIid() > 0)
            {
                res = res.toInstruction();
            }
            compute(block, OperatorCode.mul, res, new ConstantResult(4));
            compute(block, OperatorCode.add, new ConstantResult(0), vResult);  // Frame Pointer is R28
            compute(block, OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2));
            compute(block, OperatorCode.load, null, new InstructionResult(pc - 1));

            for (Integer index = 1; index < array.indexList.size(); index++)
            {
                IResult res1 = array.indexList.get(index);
                if(res1.getIid() > 0)
                {
                    res1 = res1.toInstruction();
                }
                compute(block, OperatorCode.mul, res1, new ConstantResult(4));
                compute(block, OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2));
                compute(block, OperatorCode.load, null, new InstructionResult(pc - 1));
            }
        }
    }

    public void storeArrayElement(IBlock block, VariableManager vManager, IResult lhsResult, IResult rhsResult) 
    {
        ArrayVar array = (ArrayVar)((VariableResult)lhsResult).variable;

        if(array.indexList.size() > 0)
        {
            IResult res = array.indexList.get(0);
            if(res.getIid() > 0)
            {
                res = res.toInstruction();
            }
            compute(block, OperatorCode.mul, res, new ConstantResult(4));
            compute(block, OperatorCode.add, new ConstantResult(0), lhsResult);  // Frame Pointer is R28
            compute(block, OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2));

            for (Integer index = 1; index < array.indexList.size(); index++)
            {
                IResult res1 = array.indexList.get(index);
                if(res1.getIid() > 0)
                {
                    res1 = res1.toInstruction();
                }
                compute(block, OperatorCode.load, null, new InstructionResult(pc - 1));
                compute(block, OperatorCode.mul, res1, new ConstantResult(4));
                compute(block, OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2));
            }

            compute(block, OperatorCode.store, new InstructionResult(pc - 1), rhsResult);
        }
    }

    public void declareVariable(IBlock block, VariableManager vManager, VariableResult vResult, Boolean put) throws IllegalVariableException
    {
        if(vManager.isVariable(vResult.variable.address))
        {
            throw new IllegalVariableException("Duplicate variable!");
        }
        else
        {
            vManager.updateSsaMap(vResult.variable.address, vResult.variable.version);
            vManager.updateDefUseChain(vResult.variable.address, vResult.variable.version, vResult.variable.version);
    
            if(vResult.variable instanceof ArrayVar)
            {
                ArrayVar arrayVar = (ArrayVar)vResult.variable;
                vManager.addArray(arrayVar.address, arrayVar);
            }
            else 
            {
                vManager.addVariable(vResult.variable.address);
                if(put)
                {
                    compute(block, OperatorCode.move, vResult, new ConstantResult());
                }
            }
        }
    }
}