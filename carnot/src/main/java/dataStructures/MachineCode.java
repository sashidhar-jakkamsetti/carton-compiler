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
        this(op, null, null, null);
    }

    public MachineCode(Integer op, Integer a)
    {
        this.op = op;
        this.a = a;
        this.b = null;
        this.c = null;
    }

    public MachineCode(Integer op, Integer a, Integer b)
    {
        this.op = op;
        this.a = a;
        this.b = b;
        this.c = null;
    }

    public MachineCode(Integer op, Integer a, Integer b, Integer c)
    {
        this.op = op;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public int toInteger()
    {
        if(a == null && b == null && c == null)
        {
            return DLX.assemble(op);
        }
        else if(b == null && c == null)
        {
            return DLX.assemble(op, a);
        }
        else if(c == null)
        {
            return DLX.assemble(op, a, b);
        }

        return DLX.assemble(op, a, b, c);
    }

    public String toString()
    {
        return String.format(
            "%s    %s  %s  %s", 
            DLX.mnemo[op], 
            a != null ? a.toString() : "",  
            b != null ? b.toString() : "", 
            c != null ? c.toString() : ""
        );
    }
}