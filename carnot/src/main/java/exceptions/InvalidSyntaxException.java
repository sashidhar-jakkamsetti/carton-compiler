package exceptions;

public class InvalidSyntaxException extends Exception
{
    private static final long serialVersionUID = 1L; 

    public InvalidSyntaxException(String message)
    {
        super(message);
    }
}