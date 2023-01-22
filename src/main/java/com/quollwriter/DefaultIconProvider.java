package com.quollwriter;

import java.util.*;

import javax.swing.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.components.*;


public class DefaultIconProvider implements IconProvider
{

    private Map<String, ImageIcon> cache = new HashMap ();

    public ImageIcon getIcon (String name,
                              int    type)
    {
        
        if (name == null)
        {

            return null;

        }
        
        ImageIcon ii = this.cache.get (name + "-" + type);
        
        if (ii == null)
        {
        
            ii = Environment.getIcon (name,
                                      type);
            
            this.cache.put (name + "-" + type,
                            ii);
            
        }

        return ii;
        
    }
    
    public ImageIcon getIcon (DataObject d,
                              int        type)
    {
        
        return this.getIcon (d.getObjectType (),
                             type);
        
    }
    
    public ImageIcon getIcon (Object obj,
                              int    type)
    {

        if (obj == null)
        {

            return null;

        }
        
        if (obj instanceof String)
        {
            
            return this.getIcon (obj.toString (),
                                 type);
            
        }
        
        if (obj instanceof Note)
        {
            
            Note note = (Note) obj;
            
            if (note.isEditNeeded ())
            {

                return this.getIcon (Constants.EDIT_NEEDED_NOTE_ICON_NAME,
                                     type);
                
            }
        
        }
        
        if (obj instanceof DataObject)
        {
            
            return this.getIcon ((DataObject) obj,
                                 type);
            
        }
        
        throw new IllegalArgumentException ("Class: " +
                                            obj.getClass ().getName () +
                                            " not supported.");
    
    }

}
