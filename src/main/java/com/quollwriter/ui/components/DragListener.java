package com.quollwriter.ui.components;

import java.util.EventListener;

public interface DragListener extends EventListener
{
    
    public void dragFinished (DragEvent ev);
    
    public void dragStarted (DragEvent ev);
    
    public void dragInProgress (DragEvent ev);
    
}