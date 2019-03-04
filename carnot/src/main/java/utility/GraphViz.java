package utility;

import intermediateCodeRepresentation.ControlFlowGraph;
import dataStructures.Function;
import dataStructures.Blocks.*;
import java.util.*;
import java.io.*;

public class GraphViz
{
    private ControlFlowGraph cfg;
    private Stack<IBlock> blockStack;
    private boolean[] alreadyPrintedBlocks;
    private String program;
    private String graphFileName;

    public GraphViz(ControlFlowGraph cfg, String program)
    {
        this.cfg = cfg;
        blockStack = new Stack<IBlock>();
        this.program = program;
    }

    public String getGraphFileName()
    {
        return graphFileName;
    }

    public void print(Boolean optimized, Boolean dce)
    {
        try 
        {
            Integer len = program.split("/").length;
            String filesuffix = program.split("/")[len - 1].replace(".txt", "");
            
            if(optimized)
            {
                graphFileName = "graphs/" + filesuffix + ".optimized.cgf.gv";
            }
            else 
            {
                graphFileName = "graphs/" + filesuffix + ".cgf.gv";
            }
            File file = new File(graphFileName);
            FileWriter out = new FileWriter(file);

            out.write("digraph g {\n");
            out.write("node [shape=box, height=.1, nojustify=true];\n");

            blockStack.clear();
            alreadyPrintedBlocks = new boolean[cfg.getAllBlocks().size()];

            addFunction(cfg.head, "main", optimized, dce, out);
            for (Function function : cfg.functions) 
            {
                addFunction(function.head, function.name, optimized, dce, out);
            }

            out.write("}");
            out.close();
        }
        catch(Exception exception)
        {
            System.out.println(String.format("%s : %s\n%s", exception.toString(), exception.getMessage()));
            exception.printStackTrace();
        }
    }

    private void addFunction(IBlock head, String funcName, Boolean optimized, Boolean dce, FileWriter out) throws IOException
    {
        blockStack.push(head);
        alreadyPrintedBlocks[head.getId()] = true;
        ArrayList<String> edges = new ArrayList<String>();
        out.write("subgraph cluster" + head.getId().toString() + "{ \n label=" + funcName + ";\n");
        while(!blockStack.isEmpty())
        {
            Block cBlock = (Block)blockStack.pop();
            Integer id = cBlock.getId();
            out.write("node" + id.toString() + " [xlabel=" + id.toString() + ", label=\"");

            if(cBlock instanceof IfBlock)
            {
                IfBlock ifBlock = (IfBlock)cBlock;
                if(ifBlock.getThenBlock() != null)
                {
                    if(!alreadyPrintedBlocks[ifBlock.getThenBlock().getId()])
                    {
                        blockStack.push(ifBlock.getThenBlock());
                        alreadyPrintedBlocks[ifBlock.getThenBlock().getId()] = true;
                    }
                    edges.add(addEdge(ifBlock, ifBlock.getThenBlock()) + "[label=\"then\"]");
                }
                else if(ifBlock.getJoinBlock() != null)
                {
                    if(!alreadyPrintedBlocks[ifBlock.getJoinBlock().getId()])
                    {
                        blockStack.push(ifBlock.getJoinBlock());
                        alreadyPrintedBlocks[ifBlock.getJoinBlock().getId()] = true;
                    }
                    edges.add(addEdge(ifBlock, ifBlock.getJoinBlock()) + "[label=\"then\"]");
                }

                if(ifBlock.getElseBlock() != null)
                {
                    if(!alreadyPrintedBlocks[ifBlock.getElseBlock().getId()])
                    {
                        blockStack.push(ifBlock.getElseBlock());
                        alreadyPrintedBlocks[ifBlock.getElseBlock().getId()] = true;
                    }
                    edges.add(addEdge(ifBlock, ifBlock.getElseBlock()) + "[label=\"else\"]");
                }
                else if(ifBlock.getJoinBlock() != null)
                {
                    if(!alreadyPrintedBlocks[ifBlock.getJoinBlock().getId()])
                    {
                        blockStack.push(ifBlock.getJoinBlock());
                        alreadyPrintedBlocks[ifBlock.getJoinBlock().getId()] = true;
                    }
                    edges.add(addEdge(ifBlock, ifBlock.getJoinBlock()) + "[label=\"else\"]");
                }
            }
            else if(cBlock instanceof WhileBlock)
            {
                WhileBlock whileBlock = (WhileBlock)cBlock;
                if(whileBlock.getLoopBlock() != null)
                {
                    if(!alreadyPrintedBlocks[whileBlock.getLoopBlock().getId()])
                    {
                        blockStack.push(whileBlock.getLoopBlock());
                        alreadyPrintedBlocks[whileBlock.getLoopBlock().getId()] = true;
                    }
                    edges.add(addEdge(whileBlock, whileBlock.getLoopBlock()) + "[label=\"loop\"]");
                }

                if(whileBlock.getFollowBlock() != null)
                {
                    if(!alreadyPrintedBlocks[whileBlock.getFollowBlock().getId()])
                    {
                        blockStack.push(whileBlock.getFollowBlock());
                        alreadyPrintedBlocks[whileBlock.getFollowBlock().getId()] = true;
                    }
                    edges.add(addEdge(whileBlock, whileBlock.getFollowBlock()) + "[label=\"follow\"]");
                }
            }
            else
            {
                if(cBlock.getChild() != null)
                {
                    if(!alreadyPrintedBlocks[cBlock.getChild().getId()])
                    {
                        blockStack.push(cBlock.getChild());
                        alreadyPrintedBlocks[cBlock.getChild().getId()] = true;
                    }
                    edges.add(addEdge(cBlock, cBlock.getChild()));
                }
            }
            out.write(cBlock.toString(optimized, dce));
            out.write("\"];\n");
        }
        for(String edge : edges)
        {
            out.write(edge + ";\n");
        }
        out.write("}\n");
    }

    private String addEdge(IBlock x, IBlock y)
    {
        String edge = "node" + x.getId() + " -> node" + y.getId();
        return edge;
    }
}