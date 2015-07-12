package com.quollwriter.editors;

import java.util.EventListener;

public interface ProjectEditorChangedListener extends EventListener
{
    
    public void projectEditorChanged (ProjectEditorChangedEvent ev);
        
}
