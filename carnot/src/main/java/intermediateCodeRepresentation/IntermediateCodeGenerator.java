package intermediateCodeRepresentation;

import java.util.ArrayList;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;
import dataStructures.Operator.*;
import dataStructures.Results.*;
import exceptions.IllegalVariableException;

public class IntermediateCodeGenerator
{
    private Integer pc;

    public IntermediateCodeGenerator()
    {
        pc = 0;
    }

    public Integer getPC()
    {
        return pc;
    }

    public Instruction compute(Token opToken, BranchResult y)
    {
        OperatorCode opCode  = Operator.branchingOperator.get(opToken.type);
        if(opCode == OperatorCode.bra)
        {
            return new Instruction(pc++, opCode, null, y);
        }
        else
        {
            return new Instruction(pc++, opCode, y.toInstruction(), y);
        }
    }

    public Instruction compute(Token opToken, IResult x, IResult y)
    {
        OperatorCode opCode = Operator.getOpCode(opToken);
        
        return compute(opCode, x, y);
    }

    public Instruction compute(OperatorCode opCode, IResult x, IResult y)
    {
        if(opCode == OperatorCode.move || opCode == OperatorCode.store)
        {
            return new Instruction(pc++, opCode, y, x);
        }
        else if(opCode == OperatorCode.load)
        {
            return new Instruction(pc++, opCode, null, y);
        }
        else if(opCode == OperatorCode.phi)
        {
            return new PhiInstruction(pc++);
        }
        else
        {
            return new Instruction(pc++, opCode, x, y);
        }
    }

    public ArrayList<Instruction> loadArrayElement(VariableManager vManager, IResult vResult) 
    {
        ArrayList<Instruction> instructions = new  ArrayList<Instruction>();
        ArrayVar array = (ArrayVar)((VariableResult)vResult).variable;

        if(array.indexList.size() > 0)
        {
            IResult res = array.indexList.get(0);
            if(res.getIid() > 0)
            {
                res = res.toInstruction();
            }
            instructions.add(compute(OperatorCode.mul, res, new ConstantResult(4)));
            instructions.add(compute(OperatorCode.add, new RegisterResult(28), new ConstantResult(array.getBaseAddress().address)));  // Frame Pointer is R28
            instructions.add(compute(OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2)));
            instructions.add(compute(OperatorCode.load, null, new InstructionResult(pc - 1)));

            for (Integer index = 1; index < array.indexList.size(); index++)
            {
                IResult res1 = array.indexList.get(index);
                if(res1.getIid() > 0)
                {
                    res1 = res1.toInstruction();
                }
                instructions.add(compute(OperatorCode.mul, res1, new ConstantResult(4)));
                instructions.add(compute(OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2)));
                instructions.add(compute(OperatorCode.load, null, new InstructionResult(pc - 1)));
            }
        }
        
        return instructions;
    }

    public ArrayList<Instruction> storeArrayElement(VariableManager vManager, IResult lhsResult, IResult rhsResult) 
    {
        ArrayList<Instruction> instructions = new  ArrayList<Instruction>();
        ArrayVar array = (ArrayVar)((VariableResult)lhsResult).variable;

        if(array.indexList.size() > 0)
        {
            IResult res = array.indexList.get(0);
            if(res.getIid() > 0)
            {
                res = res.toInstruction();
            }
            instructions.add(compute(OperatorCode.mul, res, new ConstantResult(4)));
            instructions.add(compute(OperatorCode.add, new RegisterResult(28), new ConstantResult(array.getBaseAddress().address)));  // Frame Pointer is R28
            instructions.add(compute(OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2)));
            instructions.add(compute(OperatorCode.load, null, new InstructionResult(pc - 1)));

            for (Integer index = 1; index < array.indexList.size(); index++)
            {
                IResult res1 = array.indexList.get(index);
                if(res1.getIid() > 0)
                {
                    res1 = res1.toInstruction();
                }
                instructions.add(compute(OperatorCode.mul, res1, new ConstantResult(4)));
                instructions.add(compute(OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2)));
                instructions.add(compute(OperatorCode.load, null, new InstructionResult(pc - 1)));
            }

            instructions.add(compute(OperatorCode.store, new InstructionResult(pc - 1), rhsResult));
        }

        return instructions;
    }

    public void declareVariable(IBlock block, VariableManager vManager, VariableResult vResult) throws IllegalVariableException
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
            block.addInstruction(compute(OperatorCode.move, vResult, new ConstantResult())); // fishy: what about formal parameters?
        }
    }
}