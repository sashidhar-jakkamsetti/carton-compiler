package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileReader {
    private static FileReader fileReader;
    private BufferedReader bufferedReader;
    private Integer charPosition;
    private Integer lineNo;
    private String line;
    private static String cfileName;
    public static final int EOF = 255;

    public static FileReader getInstance(String fileName) 
    {
        if (fileReader == null || cfileName != fileName) 
        {
            fileReader = new FileReader(fileName);
            if (fileReader.bufferedReader == null) 
            {
                return null;
            }

            return fileReader;
        }

        return fileReader;
    }

    private FileReader(String fileName) 
    {
        cfileName = fileName;

        try 
        {
            File file = new File(fileName);
            bufferedReader = new BufferedReader(new java.io.FileReader(file));
            lineNo = 0;
            charPosition = 0;
        } 
        catch (FileNotFoundException e) 
        {
            error(e);
            bufferedReader = null;
        }
    }

    public char getSym() 
    {
        int currentChar = EOF;
        if (line != null && charPosition < line.length()) 
        {
            return line.charAt(charPosition++);
        }
        else 
        {
            nextLine();
            if (line != null) 
            {
                return '\n';
            }
        }
        
        return (char)currentChar;
    }

    public void nextLine()
    {
        try
        {
            line = bufferedReader.readLine();
            lineNo += 1;
            charPosition = 0;

            while(line != null && line.trim().length() == 0) 
            {
                line = bufferedReader.readLine();
                lineNo += 1;
            }
        }
        catch(IOException e)
        {
            error(e);
        }
    }

    public void error(Exception exception) 
    {
        System.out.println("Caught exception while parsing file: " 
                + cfileName + " at line: " + lineNo + " column: " + charPosition);

        System.out.println(String.format("%s : %s\n%s", exception.toString(), exception.getMessage(),
                exception.getStackTrace()));
    }
}