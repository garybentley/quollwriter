package com.quollwriter.editors;

import java.util.EventObject;

import com.quollwriter.data.editors.*;
import com.quollwriter.editors.messages.*;

public class EditorMessageEvent extends EditorEvent
{

    public static final int MESSAGE_CHANGED = 1;
    public static final int MESSAGE_DELETED = 2;
    public static final int MESSAGE_ADDED = 3;
    
    private EditorMessage message = null;
    private int type = -1;
    
    public EditorMessageEvent (EditorEditor  editor,
                               EditorMessage message,
                               int           type)
    {
        
        super (editor);

        this.type = type;        
        this.message = message;
        
    }
    
    public EditorMessage getMessage ()
    {
        
        return this.message;
        
    }

    public int getType ()
    {
        
        return this.type;
        
    }
        
}
