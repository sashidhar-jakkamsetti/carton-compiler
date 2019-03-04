package dataStructures.Instructions;

import dataStructures.*;
import dataStructures.Operator.OperatorCode;
import dataStructures.Results.*;

public class PhiInstruction extends Instruction
{
    public Variable variable;
    
    public PhiInstruction(Integer programCounter)
    {
        super(programCounter);
        opcode = OperatorCode.phi;
        deleteMode = DeleteMode._NotDeleted;
        akaI = new Instruction(programCounter);
        akaI.opcode = opcode;
    }
    
    public PhiInstruction(Integer programCounter, Variable variable, IResult x, IResult y)
    {
        super(programCounter, OperatorCode.phi, x, y);
        this.variable = variable;
    }

    @Override
    public PhiInstruction clone()
    {
        PhiInstruction instr = new PhiInstruction(id);
        instr.opcode = opcode;
        if(operandX != null)
        {
            instr.operandX = operandX.clone();
        }
        if(operandY != null)
        {
            instr.operandY = operandY.clone();
        }
        instr.deleteMode = deleteMode;
        instr.akaI = new Instruction(id);
        if(akaI != null)
        {
            instr.akaI.opcode = akaI.opcode;
            instr.akaI.operandX = akaI.operandX.clone();
            instr.akaI.operandY = akaI.operandY.clone();
        }
        return instr;
    }

    @Override
    public String toString()
    {
        return String.format("%s : PHI %s := %s %s",
                             this.id,
                             this.variable.toString(),
                             this.operandX.toString(),
                             this.operandY.toString()
        );
    }

    @Override
    public Boolean equals(Instruction instruction)
    {
        
        if(instruction.opcode == OperatorCode.phi)
        {
            PhiInstruction phiI = (PhiInstruction)instruction;
            if(variable.address == phiI.variable.address && variable.version == phiI.variable.version)
            {
                return super.equals(instruction);
            }
        }

        return false;
    }
}