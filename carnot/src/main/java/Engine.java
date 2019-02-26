import java.io.File;

import intermediateCodeRepresentation.ControlFlowGraph;
import parser.Parser;
import utility.GraphViz;

public class Engine 
{
    public static void run(String program)
    {
        Parser parser = Parser.getInstance(program);
        if(parser != null)
        {
            ControlFlowGraph cfg = parser.parse();
            GraphViz graphPrinter = new GraphViz(cfg, program);
            graphPrinter.print();
            try
            {
                Process p = Runtime.getRuntime().exec(
                        String .format("dot -Tpng %s -o %s", graphPrinter.getGraphFileName(), graphPrinter.getGraphFileName() + ".png")
                );
            }
            catch(Exception e)
            {
                System.out.println(String.format("%s : %s\n%s", e.toString(), e.getMessage(), e.getStackTrace()));
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
            program = "testprograms/";
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
                    catch(Exception e)
                    {

                    }
                }
            }
        }
        else
        {
            run(program);
        }
    }
}
