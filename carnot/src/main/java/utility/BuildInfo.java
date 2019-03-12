package utility;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="buildinfo")
public class BuildInfo
{
    String program;
    boolean abstractControlFlowGraph;
    boolean optimize;
    boolean eliminateDeadCode;
    int regsize;
    boolean allocateRegister;
    boolean generateMachineCode;
    boolean execute;
    String outputpath;

    public String getProgram() 
    {
        return program;
    }

    @XmlElement
    public void setProgram(String program) 
    {
        this.program = program;
    }

    public boolean getAbstractControlFlowGraph() 
    {
        return abstractControlFlowGraph;
    }

    @XmlElement
    public void setAbstractControlFlowGraph(boolean abstractControlFlowGraph) 
    {
        this.abstractControlFlowGraph = abstractControlFlowGraph;
    }

    public boolean getOptimize() 
    {
        return optimize;
    }

    @XmlElement
    public void setOptimize(boolean optimize) 
    {
        this.optimize = optimize;
    }

    public boolean getEliminateDeadCode() 
    {
        return eliminateDeadCode;
    }

    @XmlElement
    public void setEliminateDeadCode(boolean eliminateDeadCode) 
    {
        this.eliminateDeadCode = eliminateDeadCode;
    }

    public boolean getAllocateRegister() 
    {
        return allocateRegister;
    }

    @XmlElement
    public void setAllocateRegister(boolean allocateRegister) 
    {
        this.allocateRegister = allocateRegister;
    }

    public boolean getGenerateMachineCode() 
    {
        return generateMachineCode;
    }

    @XmlElement
    public void setGenerateMachineCode(boolean generateMachineCode) 
    {
        this.generateMachineCode = generateMachineCode;
    }

    public String getOutputpath() 
    {
        return outputpath;
    }

    @XmlElement
    public void setOutputpath(String outputpath) 
    {
        this.outputpath = outputpath;
    }

    public int getRegsize()
    {
        return regsize;
    }

    @XmlElement
    public void setRegsize(int regsize)
    {
        this.regsize = regsize;
    }

    public boolean getExecute() 
    {
        return execute;
    }

    @XmlElement
    public void setExecute(boolean execute) 
    {
        this.execute = execute;
    }
}