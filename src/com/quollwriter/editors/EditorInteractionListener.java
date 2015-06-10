package com.quollwriter.editors;

import java.util.EventListener;

public interface EditorInteractionListener extends EventListener
{
    
    public void handleInteraction (EditorInteractionEvent ev);
        
}
