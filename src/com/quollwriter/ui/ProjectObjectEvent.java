package com.quollwriter.ui;

import com.quollwriter.data.*;

public class ProjectObjectEvent extends ProjectEvent
{
    
    private DataObject object = null;
    
    public ProjectObjectEvent (AbstractProjectViewer viewer,
                               String                type,
                               String                action,
                               DataObject            obj)
    {
        
        super (viewer,
               type,
               action);
        
        this.object = obj;
        
    }
    
    public DataObject getContextObject ()
    {
        
        return this.getObject ();
        
    }
    
    public DataObject getObject ()
    {
        
        return this.object;
        
    }
        
}