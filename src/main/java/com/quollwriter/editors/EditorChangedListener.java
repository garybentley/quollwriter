package com.quollwriter.editors;

import java.util.EventListener;

public interface EditorChangedListener extends EventListener
{
    
    public void editorChanged (EditorChangedEvent ev);
        
}
