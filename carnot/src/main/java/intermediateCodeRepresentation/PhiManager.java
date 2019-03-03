package intermediateCodeRepresentation;

import java.util.*;
import dataStructures.*;
import dataStructures.Blocks.IBlock;
import dataStructures.Instructions.*;
import dataStructures.Results.*;
import optimization.Optimizer;

public class PhiManager
{
    public HashMap<Integer, PhiInstruction> phis;
    private static IntermediateCodeGenerator iCodeGenerator;
    private static Optimizer optimizer;

    public PhiManager()
    {
        phis = new HashMap<Integer, PhiInstruction>();
        iCodeGenerator = IntermediateCodeGenerator.getInstance();
        optimizer = Optimizer.getInstance();
    }

    public void addPhi(IBlock block, Variable x, IResult x1, IResult x2) 
    {
        if(!isExists(x))
        {
            PhiInstruction phiInstruction = new PhiInstruction(iCodeGenerator.getPC());
            iCodeGenerator.incrementPC();

            x.version = phiInstruction.id;
            phiInstruction.variable = x;
            phiInstruction.operandX = x1;
            phiInstruction.operandY = x2;
            phis.put(x.address, phiInstruction);
            optimizer.optimize(block, phiInstruction);
        }
        else
        {
            updatePhi(block, x, x1, x2);
        }
    }

    public void updatePhi(IBlock block, Variable x, IResult x1, IResult x2)
    {
        if(isExists(x))
        {
            if(x1 != null && x2 != null)
            {
                phis.get(x.address).operandX = x1;
                phis.get(x.address).operandY = x2;
                phis.get(x.address).variable.version = x.version;                
            }
            else if(x1 == null)
            {
                phis.get(x.address).operandY = x2;
                phis.get(x.address).variable.version = x.version;
            }
            else if(x2 == null)
            {
                phis.get(x.address).operandY = x1;
                phis.get(x.address).variable.version = x.version;
            }
            else 
            {
                phis.get(x.address).variable.version = x.version;
            }
            optimizer.optimize(block, phis.get(x.address));
        }
    }

    public boolean isExists(Variable x)
    {
        return phis.containsKey(x.address);
    }
}