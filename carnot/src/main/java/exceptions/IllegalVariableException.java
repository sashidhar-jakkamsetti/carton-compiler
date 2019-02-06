package exceptions;

public class IllegalVariableException extends Exception
{
    private static final long serialVersionUID = 1L; 

    public IllegalVariableException(String message)
    {
        super(message);
    }
}