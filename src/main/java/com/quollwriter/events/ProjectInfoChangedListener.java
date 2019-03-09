package com.quollwriter.events;

import java.util.EventListener;

public interface ProjectInfoChangedListener extends EventListener
{
    
    public void projectInfoChanged (ProjectInfoChangedEvent ev);
    
}