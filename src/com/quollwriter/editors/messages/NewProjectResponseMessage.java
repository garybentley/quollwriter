package com.quollwriter.editors.messages;

import java.util.*;
import java.awt.image.*;

import com.quollwriter.*;
import com.quollwriter.editors.*;
import com.quollwriter.text.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;

public class NewProjectResponseMessage extends EditorMessage
{
    
    public static final String MESSAGE_TYPE = "project-new-response";
        
    private String responseMessage = null;
    private boolean accepted = false;
    private String editorName = null;
    private BufferedImage editorAvatar = null;
    
    public NewProjectResponseMessage ()
    {
                        
    }
    
    public NewProjectResponseMessage (String       projectId,
                                      boolean      accepted,
                                      String       message,
                                      EditorEditor  ed,
                                      EditorAccount acc)
    {
                
        if (projectId == null)
        {
            
            throw new IllegalArgumentException ("Project id must be specified.");
                
        }
        
        this.setEditor (ed);
        this.setForProjectId (projectId);
        this.responseMessage = message;
        this.accepted = accepted;

        if (this.accepted)
        {
        
            this.editorName = acc.getName ();
            this.editorAvatar = acc.getAvatar ();

        }
        
    }
        
    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {
 
        super.fillToStringProperties (props);
 
        this.addToStringProperties (props,
                                    "accepted",
                                    this.accepted);
        this.addToStringProperties (props,
                                    "message",
                                    this.responseMessage);
                
    }
        
    public boolean isAccepted ()
    {
        
        return this.accepted;
        
    }
    
    public BufferedImage getEditorAvatar ()
    {
        
        return this.editorAvatar;
        
    }
    
    public String getEditorName ()
    {
        
        return this.editorName;
        
    }
    
    public String getResponseMessage ()
    {
        
        return this.responseMessage;
        
    }
        
    protected void fillMap (Map data)
                     throws GeneralException
    {
        
        if (this.getForProjectId () == null)
        {
            
            throw new GeneralException ("No project id set");
            
        }
                            
        if (this.responseMessage != null)
        {
            
            data.put (MessageFieldNames.message,
                      this.responseMessage);
        
        }
        
        data.put (MessageFieldNames.accepted,
                  this.accepted);
        
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
            
            try
            {
            
                edInf.put (MessageFieldNames.avatar,
                           EditorsUtils.getImageAsBase64EncodedString (this.editorAvatar));

            } catch (Exception e) {
                
                throw new GeneralException ("Unable to get encoded avatar for editor.");
                               
            }
            
        }
        
    }
    
    protected void doInit (Map          data,
                           EditorEditor from)
                    throws GeneralException
    {
                                
        this.responseMessage = this.getString (MessageFieldNames.message,
                                               data,
                                               false);
        
        this.accepted = this.getBoolean (MessageFieldNames.accepted,
                                         data);

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
                
                try
                {
                
                    this.editorAvatar = EditorsUtils.getImageFromBase64EncodedString (avatar);
                    
                } catch (Exception e) {
                    
                    throw new GeneralException ("Unable to get image from encoded avatar.");
                    
                }
                
            }
            
        }
                                         
    }
        
    public String getMessage ()
                       throws GeneralException
    {
        
        // Return a summary, should be json.
        Map m = new HashMap ();
        m.put (MessageFieldNames.projectid,
               this.getForProjectId ());
        m.put (MessageFieldNames.accepted,
               this.accepted);
        
        if (this.responseMessage != null)
        {
        
            m.put (MessageFieldNames.message,
                   this.responseMessage);

        }
                
        try
        {
        
            return JSONEncoder.encode (m);
        
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to json encode message: " +
                                        m,
                                        e);
            
        }
        
    }
    
    public void setMessage (String m)
                     throws GeneralException
    {
        
        // This is a summary of what was sent/received.
        Map data = (Map) JSONDecoder.decode (m);
        
        this.doInit (data,
                     this.getEditor ());
        
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