import java.io.File;
import java.util.*;

import intermediateCodeRepresentation.ControlFlowGraph;
import parser.Parser;
import utility.BuildConfigLoader;
import utility.BuildInfo;
import utility.GraphViz;

public class Engine 
{
    public static boolean run(BuildInfo buildInfo) throws Exception
    {
        Parser parser = Parser.getInstance(buildInfo.getProgram());
        if(parser != null)
        {
            ControlFlowGraph cfg = parser.parse();
            if(cfg.done)
            {
                GraphViz graphPrinter = new GraphViz(cfg, buildInfo.getProgram());

                if(buildInfo.getAbstractControlFlowGraph())
                {
                    graphPrinter.print(false);
                    Runtime.getRuntime().exec(
                            String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), graphPrinter.getGraphFileName() + ".png")
                    );
                }

                if(buildInfo.getOptimize())
                {
                    graphPrinter.print(true);
                    Runtime.getRuntime().exec(
                            String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), graphPrinter.getGraphFileName() + ".png")
                    );
                }

                if(buildInfo.getEliminateDeadCode())
                {
                    graphPrinter.print(true);
                    Runtime.getRuntime().exec(
                            String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), graphPrinter.getGraphFileName() + ".png")
                    );
                }

                if(buildInfo.getAllocateRegister())
                {
                    graphPrinter.print(true);
                    Runtime.getRuntime().exec(
                            String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), graphPrinter.getGraphFileName() + ".png")
                    );
                }

                if(buildInfo.getInstructionScheduling())
                {
                    graphPrinter.print(true);
                    Runtime.getRuntime().exec(
                            String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), graphPrinter.getGraphFileName() + ".png")
                    );
                }

                if(buildInfo.getGenerateMachineCode())
                {
                    graphPrinter.print(true);
                    Runtime.getRuntime().exec(
                            String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), graphPrinter.getGraphFileName() + ".png")
                    );
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
            buildFile = "carnot/carnot.xml";
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

        // If folder, take all the files and parse them.
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
