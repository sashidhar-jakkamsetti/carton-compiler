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
    boolean allocateRegister;
    boolean instructionScheduling;
    boolean generateMachineCode;
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

    public boolean getInstructionScheduling() 
    {
        return instructionScheduling;
    }

    @XmlElement
    public void setInstructionScheduling(boolean instructionScheduling) 
    {
        this.instructionScheduling = instructionScheduling;
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

}