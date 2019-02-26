package intermediateCodeRepresentation;

import java.util.*;
import dataStructures.*;
import dataStructures.Instructions.*;
import dataStructures.Operator.OperatorCode;
import dataStructures.Results.*;

public class PhiManager
{
    public HashMap<Integer, PhiInstruction> phis;
    private IntermediateCodeGenerator iCodeGenerator;

    public PhiManager()
    {
        phis = new HashMap<Integer, PhiInstruction>();
        iCodeGenerator = IntermediateCodeGenerator.getInstance();
    }

    public void addPhi(Variable x, IResult x1, IResult x2) 
    {
        if(!isExists(x))
        {
            PhiInstruction phiInstruction = (PhiInstruction)iCodeGenerator.compute(OperatorCode.phi, x1, x2);
            x.version = phiInstruction.id;
            phiInstruction.variable = x;
            phiInstruction.operandX = x1;
            phiInstruction.operandY = x2;
            phis.put(x.address, phiInstruction);
        }
        else
        {
            updatePhi(x, x1, x2);
        }
    }

    public void updatePhi(Variable x, IResult x1, IResult x2)
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
        }
    }

    public boolean isExists(Variable x)
    {
        return phis.containsKey(x.address);
    }
}