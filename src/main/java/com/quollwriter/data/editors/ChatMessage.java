package com.quollwriter.data.editors;

import java.util.*;

public class ChatMessage
{
    
    private String message = null;
    private String from = null;
    private Date when = null;
    
    public ChatMessage (String message,
                        String from,
                        Date   when)
    {
        
        this.message = message;
        this.from = from;
        this.when = when;
        
    }
    
    public Date getWhen ()
    {
        
        return this.when;
        
    }
    
    public String getFrom ()
    {
        
        return this.from;
        
    }
    
    public String getMessage ()
    {
        
        return this.message;
        
    }
    
}