package machineCodeGeneration;

import dataStructures.Instructions.*;
import dataStructures.Results.*;
import intermediateCodeRepresentation.RegisterAllocator;

public class MachineCodeGenerator 
{
    private static RegisterAllocator registerAllocator;
    private static MachineCodeGenerator mCodeGenerator;

    private MachineCodeGenerator()
    {
        registerAllocator = RegisterAllocator.getInstance();
    }

    public static MachineCodeGenerator MachineCodeGenerator()
    {
        if(mCodeGenerator == null)
        {
            mCodeGenerator = new MachineCodeGenerator();
        }

        return mCodeGenerator;
    }

    private void load(IResult result)
    {
        
    }

    public void Compute(Instruction instruction)
    {

    }
}