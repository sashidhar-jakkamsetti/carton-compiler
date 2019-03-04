import java.io.File;
import java.util.*;

import intermediateCodeRepresentation.ControlFlowGraph;
import parser.Parser;
import utility.GraphViz;

public class Engine 
{
    public static boolean run(String program) throws Exception
    {
        Parser parser = Parser.getInstance(program);
        if(parser != null)
        {
            ControlFlowGraph cfg = parser.parse();
            if(cfg.done)
            {
                GraphViz graphPrinter = new GraphViz(cfg, program);

                // Stage 1 control flow graph
                graphPrinter.print(false);
                Runtime.getRuntime().exec(
                        String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), graphPrinter.getGraphFileName() + ".png")
                );

                // Stage 2 optimized cfg
                graphPrinter.print(true);
                Runtime.getRuntime().exec(
                        String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), graphPrinter.getGraphFileName() + ".png")
                );

                return true;
            }
        }

        return false;
    }

    public static void main( String[] args )
    {
        String program;
        if(args.length > 0) 
        {
            program = args[0];
        }
        else
        {
            program = "testprograms/";
        }

        // If folder, take all the files and parse them.
        if(program.endsWith("/"))
        {
            File folder = new File(program);
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
                    try
                    {
                        boolean status = run(file.getAbsolutePath());
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
                Integer len = program.split("/").length;
                String filesuffix = program.split("/")[len - 1];
                if(run(program))
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
