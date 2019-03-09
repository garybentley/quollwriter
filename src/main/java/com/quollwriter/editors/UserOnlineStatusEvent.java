package com.quollwriter.editors;

import java.util.EventObject;

import com.quollwriter.data.editors.*;

public class UserOnlineStatusEvent extends EventObject
{

    private EditorEditor.OnlineStatus status = null;
    
    public UserOnlineStatusEvent (EditorEditor.OnlineStatus status)
    {
        
        super (status);
        
        this.status = status;
        
    }
        
    public EditorEditor.OnlineStatus getStatus ()
    {
        
        return this.status;
        
    }
    
}
