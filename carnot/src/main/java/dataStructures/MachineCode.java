package dataStructures;

import machineCodeGeneration.DLX;

public class MachineCode
{
    public Integer id;
    public Integer op;
    public Integer a;
    public Integer b;
    public Integer c;

    public MachineCode(Integer id, Integer op)
    {
        this(id, op, null, null, null);
    }

    public MachineCode(Integer id, Integer op, Integer a)
    {
        this(id, op, a, null, null);
    }

    public MachineCode(Integer id, Integer op, Integer a, Integer b)
    {
        this(id, op, a, b, null);
    }

    public MachineCode(Integer id, Integer op, Integer a, Integer b, Integer c)
    {
        this.id = id;
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
        else if((b == null && c == null))
        {
            return DLX.assemble(op, a);
        }
        else if(a == null && c == null)
        {
            return DLX.assemble(op, b);
        }
        else if(a == null && b == null)
        {
            return DLX.assemble(op, c);
        }
        else if(c == null)
        {
            return DLX.assemble(op, a, b);
        }
        else if(b == null)
        {
            return DLX.assemble(op, a, c);
        }
        else if(a == null)
        {
            return DLX.assemble(op, b, c);
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