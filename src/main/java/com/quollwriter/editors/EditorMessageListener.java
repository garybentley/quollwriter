package com.quollwriter.editors;

import java.util.EventListener;

public interface EditorMessageListener extends EventListener
{
    
    public void handleMessage (EditorMessageEvent ev);
        
}
