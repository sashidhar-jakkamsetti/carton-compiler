package intermediateCodeRepresentation;

import java.util.ArrayList;

import dataStructures.Operator;
import dataStructures.Token;
import dataStructures.Variable;
import dataStructures.Instructions.*;
import dataStructures.Operator.*;
import dataStructures.Results.*;

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
            else if(opCode == OperatorCode.read || opCode == OperatorCode.writeNL)
            {
                instruction = new Instruction(pc++, opCode, null, null);
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
}