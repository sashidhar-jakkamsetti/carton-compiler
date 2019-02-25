package intermediateCodeRepresentation;

import java.util.*;
import dataStructures.*;
import dataStructures.Blocks.IBlock;
import dataStructures.Instructions.*;
import dataStructures.Results.*;

public class PhiManager
{
    public HashMap<Integer, Instruction> phis;
    private IntermediateCodeGenerator iCodeGenerator;

    public PhiManager()
    {
        phis = new HashMap<Integer, Instruction>();
        iCodeGenerator = IntermediateCodeGenerator.getInstance();
    }

    public void addPhi(Variable x, IResult x1, IResult x2) 
    {

    }

    public void updatePhi(Variable x, IResult x1, IResult x2)
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
    }

    public boolean isExists(Variable x)
    {
        return phis.containsKey(x.address);
    }
}