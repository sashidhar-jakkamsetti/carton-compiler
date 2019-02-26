import java.io.File;

import intermediateCodeRepresentation.ControlFlowGraph;
import parser.Parser;
import utility.GraphViz;

public class Engine 
{
    public static void run(String program) throws Exception
    {
        Parser parser = Parser.getInstance(program);
        if(parser != null)
        {
            boolean success = false;
            ControlFlowGraph cfg = parser.parse(success);
            if(success)
            {
                GraphViz graphPrinter = new GraphViz(cfg, program);
                graphPrinter.print();
                Process p = Runtime.getRuntime().exec(
                        String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), graphPrinter.getGraphFileName() + ".png")
                );
            }
        }
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
            program = "testprograms/test003.txt";
        }

        if(program.endsWith("/"))
        {
            File folder = new File(program);
            File[] listOfFiles = folder.listFiles();
            
            for (File file : listOfFiles) 
            {
                if(file.getAbsolutePath().endsWith(".txt"))
                {
                    try
                    {
                        run(file.getAbsolutePath());
                    }
                    catch(Exception exception)
                    {
                        System.out.println(String.format("%s : %s\n%s", exception.toString(), exception.getMessage(),
                            exception.getStackTrace()));
                    }
                }
            }
        }
        else
        {
            try
            {
                run(program);
            }
            catch(Exception exception)
            {
                System.out.println(String.format("%s : %s\n%s", exception.toString(), exception.getMessage(),
                    exception.getStackTrace()));
            }
        }
    }
}
