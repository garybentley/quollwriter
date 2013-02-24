package com.quollwriter.ui.components;

import javax.swing.event.*;

public class StyleChangeEvent extends ChangeEvent
{
    
    public static final String BOLD = "bold";
    public static final String ITALIC = "italic";
    public static final String UNDERLINE = "underline";
    
    private int from = -1;
    private int to = -1;
    private String type = null;
    private boolean on = false;
    
    public StyleChangeEvent (Object  source,
                             int     from,
                             int     to,
                             String  styleType,
                             boolean on)
    {
        
        super (source);
        
        this.from = from;
        this.to = to;
        this.type = styleType;
        this.on = on;
        
    }
    
    public boolean isOn ()
    {
        
        return this.on;
        
    }
    
    public String getType ()
    {
        
        return this.type;
        
    }
    
    public int getTo ()
    {
        
        return this.to;
        
    }
    
    public int getFrom ()
    {
        
        return this.from;
        
    }
    
}