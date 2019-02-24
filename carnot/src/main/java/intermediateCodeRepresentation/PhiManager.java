package intermediateCodeRepresentation;

import java.util.*;
import dataStructures.*;
import dataStructures.Blocks.IBlock;
import dataStructures.Instructions.*;
import dataStructures.Results.*;

public class PhiManager
{
    public HashMap<Integer, Instruction> phis;
    private IBlock block;
    private IntermediateCodeGenerator iCodeGenerator;

    public PhiManager(IBlock block)
    {
        phis = new HashMap<Integer, Instruction>();
        this.block = block;
        iCodeGenerator = IntermediateCodeGenerator.getInstance();
    }

    public PhiInstruction addPhi(Variable x, IResult x1, IResult x2) 
    {
        return null;
    }

    public PhiInstruction updatePhi(Variable x, IResult x1, IResult x2)
    {
        if(isExists(x))
        {
            if(x1 != null)
            {

            }

            if(x2 != null)
            {
                
            }
        }

        return null;
    }

    public boolean isExists(Variable x)
    {
        return false;
    }
}