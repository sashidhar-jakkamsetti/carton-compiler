package machineCodeGeneration;

import dataStructures.Instructions.*;
import dataStructures.Results.*;
import dataStructures.Operator.OperatorCode;
import registerAllocation.*;
import java.util.*;
import machineCodeGeneration.DLX;

public class MachineCodeGenerator 
{
    private RegisterAllocator registerAllocator;
    private static ArrayList<Integer> program;

    public MachineCodeGenerator()
    {
        registerAllocator = RegisterAllocator.getInstance();
    }

    private void load(IResult result)
    {
        
    }

    public void compute(Instruction instruction)
    {
        OperatorCode opcode = instruction.opcode;
        if(opcode == OperatorCode.add)
        {
            if(instruction.operandY instanceof ConstantResult)
            {
                DLX.assemble(DLX.ADDI, ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandX).register,
                                       ((ConstantResult)instruction.operandY).constant);
            }
            else
            {
                DLX.assemble(DLX.ADD , ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandY).register);
            }
        }
        else if(opcode == OperatorCode.sub)
        {
            if(instruction.operandY instanceof ConstantResult)
            {
                DLX.assemble(DLX.SUBI, ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandX).register,
                                       ((ConstantResult)instruction.operandY).constant);
            }
            else
            {
                DLX.assemble(DLX.SUB , ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandY).register);
            }
        }
        else if(opcode == OperatorCode.mul)
        {
            if(instruction.operandY instanceof ConstantResult)
            {
                DLX.assemble(DLX.MULI, ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandX).register,
                                       ((ConstantResult)instruction.operandY).constant);
            }
            else
            {
                DLX.assemble(DLX.MUL , ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandY).register);
            }
        }
        else if(opcode == OperatorCode.div)
        {
            if(instruction.operandY instanceof ConstantResult)
            {
                DLX.assemble(DLX.DIVI, ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandX).register,
                                       ((ConstantResult)instruction.operandY).constant);
            }
            else
            {
                DLX.assemble(DLX.DIV , ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandY).register);
            }
        }
        else if(opcode == OperatorCode.cmp)
        {
            if(instruction.operandY instanceof ConstantResult)
            {
                DLX.assemble(DLX.CMPI, ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandX).register,
                                       ((ConstantResult)instruction.operandY).constant);
            }
            else
            {
                DLX.assemble(DLX.CMP , ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandX).register,
                                       ((RegisterResult)instruction.operandY).register);
            }
        }
    }

    public static ArrayList<Integer> getProgram()
    {
        return program;
    }
}