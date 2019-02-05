package exceptions;

public class IncorrectSyntaxException extends Exception
{
    private static final long serialVersionUID = 1L; 

    public IncorrectSyntaxException(String message)
    {
        super(message);
    }
}