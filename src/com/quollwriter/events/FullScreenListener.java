package com.quollwriter.events;

import java.util.EventListener;

public interface FullScreenListener extends EventListener
{
    
    public void fullScreenEntered (FullScreenEvent ev);

    public void fullScreenExited (FullScreenEvent ev);

}