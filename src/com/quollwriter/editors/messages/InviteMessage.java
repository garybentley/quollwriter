package com.quollwriter.editors.messages;

import java.util.*;
import java.awt.image.*;

import com.quollwriter.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;

//@Message(type=InviteMessage.MESSAGE_TYPE)
public class InviteMessage extends EditorMessage
{
    
    public static final String MESSAGE_TYPE = "invite";
    
    private String editorName = null;
    private BufferedImage editorAvatar = null;
    
    public InviteMessage ()
    {
                        
    }
    
    public InviteMessage (EditorAccount acc)
    {
        
        this.editorName = acc.getName ();
        this.editorAvatar = acc.getAvatar ();
                        
    }
        
    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {
 
        super.fillToStringProperties (props);
 
        this.addToStringProperties (props,
                                    "editorName",
                                    this.editorName);
        this.addToStringProperties (props,
                                    "hasEditorAvatar",
                                    (this.editorAvatar != null));

    }
        
    protected void fillMap (Map data)
                     throws Exception
    {
                               
        Map edInf = new HashMap ();
                               
        data.put (MessageFieldNames.editorinformation,
                  edInf);
        
        if (this.editorName != null)
        {
        
            edInf.put (MessageFieldNames.name,
                       this.editorName);

        }
        
        if (this.editorAvatar != null)
        {
            
            edInf.put (MessageFieldNames.avatar,
                       EditorsUtils.getImageAsBase64EncodedString (this.editorAvatar));
                               
        }
        
    }

    public BufferedImage getEditorAvatar ()
    {
        
        return this.editorAvatar;
        
    }
    
    public String getEditorName ()
    {
        
        return this.editorName;
        
    }
        
    public String getMessage ()
    {
        
        return null;
        
    }
    
    public void setMessage (String s)
    {
        
        // Nothing to do.  No need to construct.
        
    }
    
    protected void doInit (Map          data,
                           EditorEditor from)
                    throws Exception
    {
        
        Map edInf = (Map) data.get (MessageFieldNames.editorinformation);
        
        if (edInf != null)
        {
            
            String n = this.getString (MessageFieldNames.name,
                                       edInf,
                                       false);
            
            if (n != null)
            {
                
                this.editorName = n;
                
            }
            
            String avatar = this.getString (MessageFieldNames.avatar,
                                            edInf,
                                            false);
            
            if (avatar != null)
            {
                
                this.editorAvatar = EditorsUtils.getImageFromBase64EncodedString (avatar);
                
            }
            
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