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
            else if(opCode == OperatorCode.phi)
            {
                VariableResult varResult = (VariableResult)x;
                Variable varX = varResult.variable;
                Variable variable = new Variable(varX.name, varX.address, pc++);
                instruction = new PhiInstruction(pc++, variable, x, y);
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
        if(opCode == OperatorCode.move)
        {
            return new Instruction(pc++, opCode, x, y);
        }

        return null;
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
            block.addInstruction(Compute(OperatorCode.move, new ConstantResult(), vResult)); // fishy
        }
    }
}