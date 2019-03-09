package com.quollwriter.editors;

import java.util.EventListener;

public interface UserOnlineStatusListener extends EventListener
{
    
    public void userOnlineStatusChanged (UserOnlineStatusEvent ev);
    
}