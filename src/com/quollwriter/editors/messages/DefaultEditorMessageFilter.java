package com.quollwriter.editors.messages;

import java.util.*;

public class DefaultEditorMessageFilter implements EditorMessageFilter
{
    
    private Set<String> acceptTypes = new HashSet ();
    private String projId = null;
        
    public DefaultEditorMessageFilter (String    projId,
                                       String... acceptTypes)
    {
        
        this.acceptTypes = new HashSet (Arrays.asList (acceptTypes));
                
        this.projId = projId;
                
    }

    public boolean accept (EditorMessage m)
    {
        
        if (this.projId != null)
        {
            
            if ((m.getForProjectId () == null)
                ||
                (!m.getForProjectId ().equals (this.projId))
               )
            {
                
                return false;
                
            }
            
        }
        
        return this.acceptTypes.contains (m.getMessageType ());
        
    }
    
}