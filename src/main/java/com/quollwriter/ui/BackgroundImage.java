package com.quollwriter.ui;

import java.awt.Image;

import java.util.*;

import com.quollwriter.*;

public class BackgroundImage implements Comparable<BackgroundImage>
{
    
    private String name = null;
    
    public BackgroundImage (String n)
    {
        
        this.name = n;
        
    }
    
    public Image getImage ()
    {
        
        return Environment.getBackgroundImage (this.name);
        
    }
    
    public Image getThumb ()
    {
        
        return Environment.getBackgroundThumbImage (this.name);
        
    }
    
    public String getName ()
    {
        
        return this.name;
        
    }
    
    public int compareTo (BackgroundImage im)
    {
        
        return this.getName ().compareTo (im.getName ());
        
    }
    
    public boolean equals (Object o)
    {
        
        if (o instanceof BackgroundImage)
        {
            
            return ((BackgroundImage) o).getName ().equals (this.name);
            
        }
        
        return false;
        
    }
    
    public String toString ()
    {
        
        return this.name;
        
    }
    
}