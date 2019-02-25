package utility;

import intermediateCodeRepresentation.ControlFlowGraph;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;
import java.util.*;
import java.io.*;

public class GraphViz
{
    private ControlFlowGraph cfg;
    private Stack<IBlock> blockStack;
    private ArrayList<String> edges;
    private String program;

    public GraphViz(ControlFlowGraph cfg, String program)
    {
        this.cfg = cfg;
        blockStack = new Stack<IBlock>();
        blockStack.push(ControlFlowGraph.head);
        edges = new ArrayList<String>();

        Integer len = program.split("/").length;
        this.program = program.split("/")[len - 1].replace(".txt", "");
    }

    public void print()
    {
        try
        {
            String graphFileName = "graphs/" + program + "Graph.gv";
            File file = new File(graphFileName);
            FileWriter out = new FileWriter(file);
            out.write("digraph g {\n");
            out.write("node [shape=box, height=.1, nojustify=true];\n");

            try {
                while(!blockStack.isEmpty())
                {
                    Block cBlock = (Block)blockStack.pop();
                    // System.out.println("node" + cBlock.getId() + " [");
                    out.write("node" + cBlock.getId() + " [label=\"");
                    if(cBlock instanceof IfBlock)
                    {
                        IfBlock ifBlock = (IfBlock)cBlock;
                        blockStack.push(ifBlock.getThenBlock());
                        edges.add(addEdge(ifBlock, ifBlock.getThenBlock()));
                        blockStack.push(ifBlock.getElseBlock());
                        edges.add(addEdge(ifBlock, ifBlock.getElseBlock()));
                    }
                    else if(cBlock instanceof WhileBlock)
                    {
                        WhileBlock whileBlock = (WhileBlock)cBlock;
                        blockStack.push(whileBlock.getLoopBlock());
                        edges.add(addEdge(whileBlock, whileBlock.getLoopBlock()));
                        blockStack.push(whileBlock.getFollowBlock());
                        edges.add(addEdge(whileBlock, whileBlock.getFollowBlock()));
                    }
                    else
                    {
                        if(cBlock.getChild() != null)
                        {
                            blockStack.push(cBlock.getChild());
                            edges.add(addEdge(cBlock, cBlock.getChild()));
                        }
                    }
                    for(Instruction instruction : cBlock.getInstructions())
                    {
                        // System.out.println(instruction.toString());
                        out.write(instruction.toString() + "\\l");
                    }
                    // System.out.println("]");
                    out.write("\"];\n");
                }
                for(String edge : edges)
                {
                    // System.out.println(edge);
                    out.write(edge + ";\n");
                }
                out.write("}");
            }
            finally
            {
                if (out != null)
                {
                    try
                    {
                        out.close();
                    }
                    catch(Exception e)
                    {
                        System.out.println(e);
                    }
                }
            }
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    public String addEdge(IBlock x, IBlock y)
    {
        String edge = "node" + x.getId() + " -> node" + y.getId();
        return edge;
    }
}