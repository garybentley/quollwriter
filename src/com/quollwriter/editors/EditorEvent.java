package com.quollwriter.editors;

import java.util.EventObject;

import com.quollwriter.data.editors.*;

public abstract class EditorEvent extends EventObject
{

    private EditorEditor editor = null;
    
    public EditorEvent (EditorEditor editor)
    {
        
        super (editor);
        
        this.editor = editor;
        
    }
    
    public EditorEditor getEditor ()
    {
        
        return this.editor;
        
    }
        
}
