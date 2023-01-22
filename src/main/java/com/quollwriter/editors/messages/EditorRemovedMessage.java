package com.quollwriter.editors.messages;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.data.editors.*;

public class EditorRemovedMessage extends EditorMessage
{
    
    public static final String MESSAGE_TYPE = "editor-removed";
        
    public EditorRemovedMessage ()
    {
                        
    }
    
    public EditorRemovedMessage (EditorEditor editor)
    {
                        
        this.setEditor (editor);
        
    }
        
    public String toString ()
    {
        
        return MESSAGE_TYPE;
        
    }
        
    public void setMessage (String m)
    {
                
    }
    
    public String getMessage ()
    {
        
        return null;
        
    }
        
    protected void fillMap (Map data)
                     throws GeneralException
    {                                
        
    }

    protected void doInit (Map          data,
                           EditorEditor from)
                    throws GeneralException
    {
                        
    }
    
    public boolean isEncrypted ()
    {
        
        return true;
        
    }
    
    public String getMessageType ()
    {
        
        return MESSAGE_TYPE;
        
    }    
    
}