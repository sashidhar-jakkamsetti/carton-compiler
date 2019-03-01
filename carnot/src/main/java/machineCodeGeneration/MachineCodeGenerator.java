package machineCodeGeneration;

import dataStructures.Instructions.*;
import dataStructures.Results.*;
import registerAllocation.*;

public class MachineCodeGenerator 
{
    private RegisterAllocator registerAllocator;

    public MachineCodeGenerator()
    {
        registerAllocator = RegisterAllocator.getInstance();
    }

    private void load(IResult result)
    {
        
    }

    public void Compute(Instruction instruction)
    {

    }
}