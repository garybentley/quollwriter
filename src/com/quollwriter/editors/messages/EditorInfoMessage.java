package com.quollwriter.editors.messages;

import java.util.*;
import java.awt.image.*;

import com.quollwriter.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;

public class EditorInfoMessage extends EditorMessage
{
    
    public static final String MESSAGE_TYPE = "editor-information";
    
    private String name = null;
    private BufferedImage avatar = null;
    
    public EditorInfoMessage ()
    {
                        
    }
    
    public EditorInfoMessage (EditorAccount acc)
    {
        
        this.name = acc.getName ();
        this.avatar = acc.getAvatar ();
                        
    }
        
    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {
 
        super.fillToStringProperties (props);
 
        this.addToStringProperties (props,
                                    "name",
                                    this.name);
        this.addToStringProperties (props,
                                    "hasAvatar",
                                    (this.avatar != null));

    }
                
    protected void fillMap (Map data)
                     throws Exception
    {
                               
        if (this.name != null)
        {                                
        
            data.put (MessageFieldNames.name,
                      this.name);
        
        }
        
        if (this.avatar != null)
        {

            data.put (MessageFieldNames.avatar,
                      EditorsUtils.getImageAsBase64EncodedString (this.avatar));
            
        }
        
    }

    public BufferedImage getAvatar ()
    {
        
        return this.avatar;
        
    }
    
    public String getName ()
    {
        
        return this.name;
        
    }
    
    public String getMessage ()
    {
        
        return this.name;
        
    }
    
    public void setMessage (String s)
    {
        
        // Nothing to do.  No need to construct.
        
    }
    
    protected void doInit (Map          data,
                           EditorEditor from)
                    throws Exception
    {
        
        String n = this.getString (MessageFieldNames.name,
                                   data,
                                   false);
        
        if (n != null)
        {
            
            this.name = n;
            
        }
        
        String avatar = this.getString (MessageFieldNames.avatar,
                                        data,
                                        false);
        
        if (avatar != null)
        {
            
            this.avatar = EditorsUtils.getImageFromBase64EncodedString (avatar);
            
        }
                
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