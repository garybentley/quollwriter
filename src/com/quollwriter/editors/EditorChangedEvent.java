package com.quollwriter.editors;

import java.util.EventObject;

import com.quollwriter.data.editors.*;

public class EditorChangedEvent extends EditorEvent
{

    public static final int EDITOR_CHANGED = 1;
    public static final int EDITOR_DELETED = 2;
    public static final int EDITOR_ADDED = 3;

    private int type = -1;
    
    public EditorChangedEvent (EditorEditor editor,
                               int          type)
    {
        
        super (editor);
        
        this.type = type;
        
    }
        
    public int getType ()
    {
        
        return this.type;
        
    }
    
}
