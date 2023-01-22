package com.quollwriter.editors;

import java.util.EventObject;

import com.quollwriter.data.editors.*;
import com.quollwriter.editors.messages.*;

public class EditorInteractionEvent extends EditorEvent
{

    private InteractionMessage.Action action = null;
    
    public EditorInteractionEvent (EditorEditor              editor,
                                   InteractionMessage.Action action)
    {
        
        super (editor);
        
        this.action = action;
        
    }
        
    public InteractionMessage.Action getAction ()
    {
        
        return this.action;
        
    }
    
}
