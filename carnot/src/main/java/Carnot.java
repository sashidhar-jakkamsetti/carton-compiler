import java.io.File;
import java.util.*;

import intermediateCodeRepresentation.*;
import parser.Parser;
import registerAllocation.*;
import utility.*;

public class Carnot 
{
    public static boolean run(BuildInfo buildInfo) throws Exception
    {
        Parser parser = Parser.getInstance(buildInfo.getProgram());
        if(parser != null)
        {
            ControlFlowGraph cfg = parser.parse();
            if(cfg.done)
            {
                GraphViz graphPrinter = new GraphViz(cfg, buildInfo.getProgram(), buildInfo.getOutputpath());

                if(buildInfo.getAbstractControlFlowGraph())
                {
                    graphPrinter.print(false, false, false, false);
                    Runtime.getRuntime().exec(
                            String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), 
                                            graphPrinter.getGraphFileName().replace(".gv", ".png"))
                    );

                    if(buildInfo.getOptimize())
                    {
                        graphPrinter.print(true, false, false, false);
                        Runtime.getRuntime().exec(
                                String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), 
                                                graphPrinter.getGraphFileName().replace(".gv", ".png"))
                        );

                        if(buildInfo.getEliminateDeadCode())
                        {
                            IntermediateCodeGenerator.optimizer.eliminateDeadCode(cfg);
                            graphPrinter.print(true, true, false, false);
                            Runtime.getRuntime().exec(
                                    String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), 
                                                    graphPrinter.getGraphFileName().replace(".gv", ".png"))
                            );
                        }
                    }

                    if(buildInfo.getAllocateRegister())
                    {
                        InterferenceGraph iGraph = new InterferenceGraph(cfg);
                        iGraph.construct();
                        RegisterAllocator rAllocator = new RegisterAllocator(iGraph, buildInfo.getRegsize());
                        Boolean success = rAllocator.allocate(cfg);
                        if(success)
                        {
                            graphPrinter.print(false, false, true, false);
                            Runtime.getRuntime().exec(
                                    String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), 
                                                    graphPrinter.getGraphFileName().replace(".gv", ".png"))
                            );

                            if(buildInfo.getGenerateMachineCode())
                            {
                                graphPrinter.print(false, false, false, true);
                            }
                        }
                    }
                }

                return true;
            }
        }

        return false;
    }

    public static void main( String[] args )
    {
        String buildFile;
        if(args.length > 0) 
        {
            buildFile = args[0];
        }
        else
        {
            buildFile = "carnot/build.config";
        }

        BuildConfigLoader loader = new BuildConfigLoader(buildFile);
        BuildInfo buildInfo;
        try
        {
            buildInfo = loader.load();
            if(buildInfo == null || buildInfo.getProgram().isEmpty())
            {
                return;
            }
        }
        catch(Exception exception)
        {
            System.out.println(String.format("%s : %s\n", exception.toString(), exception.getMessage()));
            exception.printStackTrace();
            return;
        }

        if(System.getProperty("user.dir").contains("carnot/target/classes"))
        {
            String prefix = System.getProperty("user.dir").replace("carnot/target/classes","");
            buildInfo.setProgram(prefix + buildInfo.getProgram());
            buildInfo.setOutputpath(prefix + buildInfo.getOutputpath());
        }
        else
        {
            buildInfo.setProgram(System.getProperty("user.dir") + "/" + buildInfo.getProgram());
            buildInfo.setOutputpath(System.getProperty("user.dir") + "/" + buildInfo.getOutputpath());
        }
        
        if(buildInfo.getProgram().endsWith("/"))
        {
            File folder = new File(buildInfo.getProgram());
            File[] listOfFiles = folder.listFiles();
            Arrays.sort(listOfFiles);
            Integer failCount = 0;
            ArrayList<String> failFiles = new ArrayList<String>();

            for (int i = 0; i < listOfFiles.length; i++) 
            {
                File file = listOfFiles[i];
                if(file.getAbsolutePath().endsWith(".txt"))
                {
                    Integer len = file.getAbsolutePath().split("/").length;
                    String filesuffix = file.getAbsolutePath().split("/")[len - 1];
                    buildInfo.setProgram(file.getAbsolutePath());
                    try
                    {
                        boolean status = run(buildInfo);
                        if(status)
                        {
                            System.out.println(filesuffix + " done.");
                        }
                        else
                        {
                            failCount += 1;
                            failFiles.add(filesuffix);
                            System.out.println(filesuffix + " FAILED.");
                        }
                    }
                    catch(Exception exception)
                    {
                        failCount += 1;
                        failFiles.add(filesuffix);
                        System.out.println(filesuffix + " FAILED.");
                        System.out.println(String.format("%s : %s\n", exception.toString(), exception.getMessage()));
                        exception.printStackTrace();
                    }
                }
            }

            Integer successCount = listOfFiles.length - failCount;
            System.out.println(""); 
            System.out.println("REPORT: " + "PASS: " + successCount.toString() + "  FAIL: " + failCount.toString());
            System.out.println(""); 
            if(failCount > 0)
            {
                System.out.println("FAILED files:"); 
                for (String fN : failFiles) 
                {
                    System.out.println(fN);    
                }
            }
        }
        else
        {
            try
            {
                Integer len = buildInfo.getProgram().split("/").length;
                String filesuffix = buildInfo.getProgram().split("/")[len - 1];
                if(run(buildInfo))
                {
                    System.out.println(filesuffix + " done.");
                }
                else
                {
                    System.out.println(filesuffix + " failed.");
                }
            }
            catch(Exception exception)
            {
                System.out.println(String.format("%s : %s\n", exception.toString(), exception.getMessage()));
                exception.printStackTrace();
            }
        }
    }
}
