package com.quollwriter.events;

import java.util.EventListener;

public interface MainPanelListener extends EventListener
{
    
    public void panelShown (MainPanelEvent ev);

}