package intermediateCodeRepresentation;

import dataStructures.Function;
import dataStructures.Token;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;
import dataStructures.Operator.OperatorCode;
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

    public Instruction Compute(Token opToken, IResult x, IResult y)
    {
        OperatorCode opcode = OperatorCode.read;
        //TODO: get appropriate opcode

        Instruction instruction = new Instruction(pc++, opcode, x, y);

        return instruction;
    }

    public Instruction Compute(OperatorCode opCode, IResult x, IResult y)
    {
        OperatorCode opcode = OperatorCode.read;
        //TODO: get appropriate opcode

        Instruction instruction = new Instruction(pc++, opcode, x, y);

        return instruction;
    }

    public void declareVariable(IBlock block, VariableManager vManager, VariableResult vResult) throws IllegalVariableException
    {

    }
}