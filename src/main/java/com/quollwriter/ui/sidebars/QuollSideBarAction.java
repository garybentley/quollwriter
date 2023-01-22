package com.quollwriter.ui.sidebars;

public interface QuollSideBarAction<E extends AbstractSideBar>
{
    
    public void doAction (E sidebar);
    
}