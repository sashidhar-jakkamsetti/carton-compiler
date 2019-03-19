package intermediateCodeRepresentation;

import java.util.*;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;
import dataStructures.Instructions.Instruction.DeleteMode;
import dataStructures.Operator.*;
import dataStructures.Results.*;
import exceptions.IllegalVariableException;
import optimization.Optimizer;
import utility.Constants;

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
        pc = Constants.INSTRUCTION_START_COUNTER;
        optimizer.reset();
    }

    private IntermediateCodeGenerator()
    {
        pc = Constants.INSTRUCTION_START_COUNTER;
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

    public void setOptimizerReturnIds(HashMap<Integer, Integer> returnIds)
    {
        optimizer.setReturnIds(returnIds);
    }

    public void compute(IBlock block, Token opToken, BranchResult y, Boolean optimize)
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
        if(optimize)
        {
            optimizer.optimize(block, instruction);
        }
    }

    public void compute(IBlock block, Token opToken, IResult x, IResult y, Boolean optimize)
    {
        compute(block, Operator.getOpCode(opToken), x, y, optimize);
    }

    public void compute(IBlock block, OperatorCode opCode, IResult x, IResult y, Boolean optimize)
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
        if(optimize)
        {
            optimizer.optimize(block, instruction);
        }
    }

    public void insertInstructionAtLast(IBlock block, OperatorCode opCode, IResult x, IResult y)
    {
        if(block != null && opCode == OperatorCode.move)
        {
            Instruction instruction = new Instruction(pc++, opCode, y, x);
            instruction.setAkaInstruction(y, x);
            if(block.getInstructions().size() > 0)
            {
                if(Operator.branchOpCodes.contains(block.getInstructions().get((block.getInstructions().size() - 1)).opcode))
                {
                    block.addInstruction(instruction, block.getInstructions().size() - 1);
                }
                else
                {
                    block.addInstruction(instruction);
                }
            }
            else
            {
                block.addInstruction(instruction);
            }
        }
    }

    public void loadArrayElement(IBlock block, VariableManager vManager, IResult vResult, Boolean optimize) 
    {
        ArrayVar array = (ArrayVar)((VariableResult)vResult).variable;

        if(array.indexList.size() > 0)
        {
            IResult res = array.indexList.get(0);
            if(res.getIid() > 0)
            {
                res = res.toInstruction();
            }
            compute(block, OperatorCode.mul, res, new ConstantResult(4), optimize);
            compute(block, OperatorCode.add, new ConstantResult(0), vResult, optimize);
            compute(block, OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2), optimize);
            compute(block, OperatorCode.load, null, new InstructionResult(pc - 1), optimize);

            for (Integer index = 1; index < array.indexList.size(); index++)
            {
                IResult res1 = array.indexList.get(index);
                if(res1.getIid() > 0)
                {
                    res1 = res1.toInstruction();
                }
                compute(block, OperatorCode.mul, res1, new ConstantResult(4), optimize);
                compute(block, OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2), optimize);
                compute(block, OperatorCode.load, null, new InstructionResult(pc - 1), optimize);
            }
        }
    }

    public void storeArrayElement(IBlock block, VariableManager vManager, IResult lhsResult, IResult rhsResult, Boolean optimize) 
    {
        ArrayVar array = (ArrayVar)((VariableResult)lhsResult).variable;

        if(array.indexList.size() > 0)
        {
            IResult res = array.indexList.get(0);
            if(res.getIid() > 0)
            {
                res = res.toInstruction();
            }
            compute(block, OperatorCode.mul, res, new ConstantResult(4), optimize);
            compute(block, OperatorCode.add, new ConstantResult(0), lhsResult, optimize);
            compute(block, OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2), optimize);

            for (Integer index = 1; index < array.indexList.size(); index++)
            {
                IResult res1 = array.indexList.get(index);
                if(res1.getIid() > 0)
                {
                    res1 = res1.toInstruction();
                }
                compute(block, OperatorCode.load, null, new InstructionResult(pc - 1), optimize);
                compute(block, OperatorCode.mul, res1, new ConstantResult(4), optimize);
                compute(block, OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2), optimize);
            }

            compute(block, OperatorCode.store, new InstructionResult(pc - 1), rhsResult, optimize);
        }
    }

    public void declareVariable(IBlock block, VariableManager vManager, VariableResult vResult, Boolean put, Boolean optimize) throws IllegalVariableException
    {
        if(vManager.isVariable(vResult.variable.address))
        {
            throw new IllegalVariableException("Duplicate variable!");
        }
        else
        {
            vManager.updateSsaMap(vResult.variable.address, vResult.variable.version);
    
            if(vResult.variable instanceof ArrayVar)
            {
                ArrayVar arrayVar = (ArrayVar)vResult.variable;
                vManager.addArray(arrayVar.address, arrayVar);
            }
            else 
            {
                vManager.addVariable(vResult.variable.address);
                if(vResult.variable.version == Constants.FORMAL_PARAMETER_VERSION)
                {
                    VariableResult newVResult = (VariableResult)vResult.clone();
                    newVResult.variable.version = pc;
                    vManager.updateSsaMap(vResult.variable.address, pc);
                    compute(block, OperatorCode.move, newVResult, vResult, optimize);
                }
                else if(put)
                {
                    compute(block, OperatorCode.move, vResult, new ConstantResult(), optimize);
                }
            }
        }
    }

    public void loadGlobalsInit(IBlock block, Function function, VariableManager vManager, HashMap<Integer, String> ad2id, Boolean optimize)
    {
        for (Integer ad : vManager.getVariables()) 
        {
            if(!vManager.isArray(ad))
            {
                Variable v = new Variable(ad2id.get(ad), ad);
                vManager.updateSsaMap(ad, pc);
                v.version = Constants.GLOBAL_VARIABLE_VERSION;
                VariableResult vResult = new VariableResult();
                vResult.set(v);
                iCodeGenerator.compute(block, OperatorCode.load, null, vResult, optimize);
                function.globalLog.put(ad, pc - 1);
            }
        }
    }

    public void loadGlobalsFuncCall(IBlock block, HashMap<Integer, IResult> tamperedGlobals,
                                        VariableManager vManager, HashMap<Integer, String> ad2id, Boolean optimize)
    {
        for (Integer ad : tamperedGlobals.keySet()) 
        {
            Variable v = new Variable(ad2id.get(ad), ad);
            v.version = Constants.GLOBAL_VARIABLE_VERSION;
            vManager.updateSsaMap(ad, pc);
            VariableResult vResult = new VariableResult();
            vResult.set(v);
            iCodeGenerator.compute(block, OperatorCode.load, null, vResult, optimize);
        }
    }

    public void storeGlobals(IBlock block, HashMap<Integer, IResult> tamperedGlobals, Function function, HashMap<Integer, String> ad2id, Boolean optimize)
    {
        for (Integer ad : tamperedGlobals.keySet())
        {
            if(function != null && function.globalLog.containsKey(ad))
            {
                if(tamperedGlobals.get(ad) instanceof VariableResult)
                {
                    Variable v = ((VariableResult)tamperedGlobals.get(ad)).variable;
                    if(v.version.equals(function.globalLog.get(ad)))
                    {
                        continue;
                    }
                }
            }
            Variable v = new Variable(ad2id.get(ad), ad);
            v.version = Constants.GLOBAL_VARIABLE_VERSION;
            VariableResult vResult = new VariableResult();
            vResult.set(v);
            iCodeGenerator.compute(block, OperatorCode.store, vResult, tamperedGlobals.get(ad), optimize);
        }
    }

    public void cleanGlobals(IBlock block, Function function)
    {
        for (Integer ad : function.globalLog.keySet()) 
        {
            if(!function.tamperedGlobals.containsKey(ad))
            {
                Instruction instruction = block.getInstruction(function.globalLog.get(ad));
                instruction.deleteMode = DeleteMode.CP;
            }
        }
    }
}