package com.quollwriter;

public class GeneralException extends Exception
{

    public GeneralException(String message)
    {

        super (message);

    }

    public GeneralException(String    message,
                            Exception cause)
    {

        super (message,
               cause);

    }

}
