package dataStructures.Instructions;

import dataStructures.*;
import dataStructures.Results.*;

public class PhiInstruction extends Instruction
{
    public Variable variable;
    
    public PhiInstruction(Integer programCounter, Variable variable, IResult x, IResult y)
    {
        super(programCounter, Operator.OperatorCode.phi, x, y);
        this.variable = variable;
    }

    @Override
    public String toString()
    {
        return "";
    }
}