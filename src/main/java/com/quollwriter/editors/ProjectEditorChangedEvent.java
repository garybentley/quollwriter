package com.quollwriter.editors;

import java.util.EventObject;

import com.quollwriter.data.editors.*;

public class ProjectEditorChangedEvent extends EventObject
{

    public static final int PROJECT_EDITOR_CHANGED = 1;
    public static final int PROJECT_EDITOR_DELETED = 2;
    public static final int PROJECT_EDITOR_ADDED = 3;

    private int type = -1;
    private ProjectEditor pe = null;
    
    public ProjectEditorChangedEvent (ProjectEditor pe,
                                      int           type)
    {
        
        super (pe);
        
        this.pe = pe;
        
        this.type = type;
        
    }
        
    public ProjectEditor getProjectEditor ()
    {
        
        return this.pe;
        
    }
    
    public int getType ()
    {
        
        return this.type;
        
    }
    
}
