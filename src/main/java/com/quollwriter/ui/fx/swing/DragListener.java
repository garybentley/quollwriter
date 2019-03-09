package com.quollwriter.ui.fx.swing;

import java.util.EventListener;

public interface DragListener extends EventListener
{

    public void dragFinished (DragEvent ev);

    public void dragStarted (DragEvent ev);

    public void dragInProgress (DragEvent ev);

}
