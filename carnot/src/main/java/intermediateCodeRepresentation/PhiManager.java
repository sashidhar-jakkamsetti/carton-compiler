package intermediateCodeRepresentation;

import java.util.*;
import dataStructures.*;
import dataStructures.Instructions.*;
import dataStructures.Results.*;

public class PhiManager
{
    private HashMap<Integer, Instruction> phis;

    public PhiManager()
    {
        phis = new HashMap<Integer, Instruction>();
    }

    public void addPhi(Variable x, IResult x1, IResult x2) 
    {

    }

    public void updatePhi(Variable x, IResult x1, IResult x2)
    {
        
    }

    public ArrayList<Instruction> getPhis()
    {
        return (ArrayList<Instruction>)phis.values();
    }
}