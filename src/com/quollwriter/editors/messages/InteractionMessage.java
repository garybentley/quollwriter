package com.quollwriter.editors.messages;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.data.editors.*;

public class InteractionMessage extends EditorMessage
{
    
    public static final String MESSAGE_TYPE = "interaction";

    public enum Action
    {
        typing ("typing"),
        normal ("normal");
        
        private final String action;
        
        Action (String t)
        {
            
            this.action = t;
            
        }
        
        public String getType ()
        {
            
            return this.action;
            
        }
        
    }
    
    private Action action = null;
    
    public InteractionMessage ()
    {
                        
    }
    
    public InteractionMessage (Action       action,
                               EditorEditor editor)
    {
                
        if (action == null)
        {
            
            throw new IllegalArgumentException ("No action provided.");
                
        }
        
        this.setEditor (editor);
        this.action = action;
                
    }
        
    public String toString ()
    {
        
        return MESSAGE_TYPE + "(action:" + this.action.getType () + ")";
        
    }
    
    public Action getAction ()
    {
        
        return this.action;
        
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
                
        data.put (MessageFieldNames.action,
                  this.action.getType ());
        
    }

    protected void doInit (Map          data,
                           EditorEditor from)
                    throws GeneralException
    {
        
        this.action = Action.valueOf (this.getString (MessageFieldNames.action,
                                                      data));
                        
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