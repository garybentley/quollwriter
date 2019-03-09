package com.quollwriter.ui;

public interface ProjectViewerAction<E extends AbstractProjectViewer>
{
    
    public void doAction (E viewer);
    
}