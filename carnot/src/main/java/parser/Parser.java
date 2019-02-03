package parser;

import dataStructures.Token;

public class Parser 
{
    private static Parser parser;
    private Scanner scanner;
    private Token inputSym;

    public static Parser getInstance(String fileName)
    {
        if(parser == null)
        {
            parser = new Parser(fileName);

            if(parser.scanner == null)
            {
                return null;
            }
        }

        return parser;
    }

    private Parser(String fileName)
    {
        scanner = Scanner.getInstance(fileName);
        next();
    }

    private void next()
    {
        if(scanner != null)
        {
            inputSym = scanner.getSym();
        }
    }

    public void error(Exception exception)
    {
        scanner.error(exception);
    }
}
