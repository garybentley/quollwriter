package com.quollwriter.editors.messages;

import java.util.*;

import com.quollwriter.*;
import com.quollwriter.text.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.editors.*;

/**
 */
public class ProjectEditStopMessage extends EditorMessage
{
    
    public static final String MESSAGE_TYPE = "project-edit-stop";
        
    private String reason = null;
    
    public ProjectEditStopMessage ()
    {
                        
    }
    
    public ProjectEditStopMessage (Project      project,
                                   String       reason,
                                   EditorEditor editor)
    {
                        
        this.reason = reason;
        this.setEditor (editor);
        this.setForProjectName (project.getName ());
        this.setForProjectId (project.getId ());
        
    }
        
    public ProjectEditStopMessage (ProjectInfo  projInfo,
                                   String       reason,
                                   EditorEditor editor)
    {
                        
        this.reason = reason;
        this.setEditor (editor);
        this.setForProjectName (projInfo.getName ());
        this.setForProjectId (projInfo.getId ());
        
    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {
 
        super.fillToStringProperties (props);
 
        this.addToStringProperties (props,
                                    "reason",
                                    this.reason);
                
    }
                            
    public String getReason ()
    {
        
        return this.reason;        
                
    }
        
    protected void fillMap (Map data)
                     throws GeneralException
    {
        
        if (this.getForProjectId () == null)
        {
            
            throw new GeneralException ("No project id set");
            
        }
            
        if (this.reason != null)
        {
        
            data.put (MessageFieldNames.message,
                      this.reason);
            
        }
                
    }
        
    protected void doInit (Map          data,
                           EditorEditor from)
                    throws GeneralException
    {
                                
        this.reason = this.getString (MessageFieldNames.message,
                                      data,
                                      false);

    }
        
    public String getMessage ()
                       throws GeneralException
    {
        
        // Return a summary, should be json.
        Map m = new HashMap ();
        m.put (MessageFieldNames.projectid,
               this.getForProjectId ());
        
        if (this.reason != null)
        {
            
            m.put (MessageFieldNames.message,
                   this.reason);
            
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
        
        this.setForProjectId (this.getString (MessageFieldNames.projectid,
                                              data));
        
        this.reason = this.getString (MessageFieldNames.message,
                                      data,
                                      false);

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