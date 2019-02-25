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
    private static Integer pc;
    private static IntermediateCodeGenerator iCodeGenerator;

    private IntermediateCodeGenerator()
    {
        pc = 0;
    }

    public static IntermediateCodeGenerator getInstance()
    {
        if(iCodeGenerator == null)
        {
            iCodeGenerator = new IntermediateCodeGenerator();
        }

        return iCodeGenerator;
    }

    public Integer getPC()
    {
        return pc;
    }

    public ArrayList<Instruction> Compute(Token opToken, IResult x, IResult y)
    {
        ArrayList<Instruction> instructions = new  ArrayList<Instruction>();
        ArrayList<OperatorCode> opCodes = new  ArrayList<OperatorCode>();
        opCodes = Operator.getOpCode(opToken);

        for(OperatorCode opCode : opCodes)
        {
            Instruction instruction = null;
            if(opCode == OperatorCode.load || opCode == OperatorCode.bra)
            {
                instruction = new Instruction(pc++, opCode, y, null);
            }
            else if(opCode == OperatorCode.store || opCode == OperatorCode.move)
            {
                instruction = new Instruction(pc++, opCode, y, x);
            }
            else
            {
                instruction = new Instruction(pc++, opCode, x, y);
            }
            instructions.add(instruction);
        }

        return instructions;
    }

    public Instruction Compute(OperatorCode opCode, IResult x, IResult y)
    {
        if(opCode == OperatorCode.move || opCode == OperatorCode.store)
        {
            return new Instruction(pc++, opCode, y, x);
        }
        else if(opCode == OperatorCode.phi)
        {
            return new PhiInstruction(pc++);
        }
        else if(opCode == OperatorCode.mul || opCode == OperatorCode.add || opCode == OperatorCode.adda)
        {
            return new Instruction(pc++, opCode, x, y);
        }
        else if(opCode == OperatorCode.load)
        {
            return new Instruction(pc++, opCode, null, y);
        }

        return null;
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
            instructions.add(Compute(OperatorCode.mul, res, new ConstantResult(4)));

            for (Integer index = 1; index < array.indexList.size(); index++)
            {
                IResult res1 = array.indexList.get(0);
                if(res1.getIid() > 0)
                {
                    res1 = res1.toInstruction();
                }
                instructions.add(Compute(OperatorCode.mul, res1, new ConstantResult(4)));
                instructions.add(Compute(OperatorCode.add, new InstructionResult(pc - 1), new InstructionResult(pc - 2)));
            }
            instructions.add(Compute(OperatorCode.add, new ConstantResult(), new ConstantResult(array.getBaseAddress().address)));

            instructions.add(Compute(OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2)));
            instructions.add(Compute(OperatorCode.load, null, new InstructionResult(pc - 1)));

            vResult = vResult.toInstruction();
            vResult.set(pc - 1);
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
            instructions.add(Compute(OperatorCode.mul, res, new ConstantResult(4)));

            for (Integer index = 1; index < array.indexList.size(); index++)
            {
                IResult res1 = array.indexList.get(0);
                if(res1.getIid() > 0)
                {
                    res1 = res1.toInstruction();
                }
                instructions.add(Compute(OperatorCode.mul, res1, new ConstantResult(4)));
                instructions.add(Compute(OperatorCode.add, new InstructionResult(pc - 1), new InstructionResult(pc - 2)));
            }
            instructions.add(Compute(OperatorCode.add, new RegisterResult(0) , new ConstantResult(array.getBaseAddress().address)));

            instructions.add(Compute(OperatorCode.adda, new InstructionResult(pc - 1), new InstructionResult(pc - 2)));
            instructions.add(Compute(OperatorCode.store, rhsResult, new InstructionResult(pc - 1)));

            lhsResult = lhsResult.toInstruction();
            lhsResult.set(pc - 1);
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
            block.addInstruction(Compute(OperatorCode.move, new ConstantResult(), vResult)); // fishy: what about formal parameters?
        }
    }
}