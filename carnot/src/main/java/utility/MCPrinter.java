package utility;

import java.io.File;
import java.io.FileWriter;

import dataStructures.MachineCode;

public class MCPrinter
{
    private MachineCode[] code;
    private Integer length;
    private String program;
    private String outputPath;

    public MCPrinter(MachineCode[] code, Integer length, String program, String outputPath)
    {
        this.code = code;
        this.length = length;
        this.program = program;
        this.outputPath = outputPath;
    }

    public void print()
    {
        try
        {
            Integer len = program.split("/").length;
            String codeFileName = outputPath + program.split("/")[len - 1].replace(".txt", ".mc");
            File file = new File(codeFileName);
            FileWriter out = new FileWriter(file);

            for(int i = 0; i < length; i++)
            {
                out.write(code[i].toString() + "\n");
            }
            out.close();
        }
        catch(Exception exception)
        {
            System.out.println(String.format("%s : %s\n", exception.toString(), exception.getMessage()));
            exception.printStackTrace();
        }
    }
}