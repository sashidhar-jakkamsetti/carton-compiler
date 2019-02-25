import intermediateCodeRepresentation.ControlFlowGraph;
import parser.Parser;
import utility.GraphViz;

public class Engine 
{
    public static void main( String[] args )
    {
        String program;
        if(args.length > 0) 
        {
            program = args[0];
        }
        else
        {
            program = "testprograms/test001.txt";
        }

        Parser parser = Parser.getInstance(program);
        if(parser != null)
        {
            ControlFlowGraph cfg = parser.parse();
            GraphViz graphPrinter = new GraphViz(cfg, "test001");
            graphPrinter.print();
            try
            {
                Process p = Runtime.getRuntime().exec("dot -Tpng graphs/test001Graph.gv -o graphs/test001Graph.png");
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
        }
    }
}
