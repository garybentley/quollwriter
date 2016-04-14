package com.quollwriter.events;

import java.util.EventListener;

public interface UserPropertyListener extends EventListener
{
    
    public void propertyChanged (UserPropertyEvent ev);

}