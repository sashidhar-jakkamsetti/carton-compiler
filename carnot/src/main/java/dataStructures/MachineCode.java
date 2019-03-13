package dataStructures;

import machineCodeGeneration.DLX;

public class MachineCode
{
    public Integer op;
    public Integer a;
    public Integer b;
    public Integer c;

    public MachineCode(int op)
    {
        this(op, -1, -1, -1);
    }

    public MachineCode(Integer op, Integer a, Integer b, Integer c)
    {
        this.op = op;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    // Fill this accordingly
    public int toInteger()
    {
        if(a == -1 && b == -1 && c == -1)
        {
            return DLX.assemble(op);
        }
        else if(b == -1 && c == -1)
        {
            return DLX.assemble(op, a);
        }

        return 0;
    }

    public String toString()
    {
        return String.format(
            "%s    %s  %s  %s", 
            DLX.mnemo[op], 
            a > -1? a.toString() : "",  
            b > -1? b.toString() : "", 
            c > -1? c.toString() : ""
        );
    }
}