package com.quollwriter.data;

import java.util.*;

import org.jdom.*;

public class ProjectVersion extends NamedObject
{
    
    public static final String OBJECT_TYPE = "projectversion";
    
    private Date due = null;
    
    public ProjectVersion ()
    {
        
        super (ProjectVersion.OBJECT_TYPE);
        
    }
    
    public ProjectVersion (String name)
    {
        
        super (ProjectVersion.OBJECT_TYPE,
               name);
        
    }
        
    public Date getDueDate ()
    {
        
        return this.due;
        
    }
    
    public void setDueDate (Date d)
    {
        
        this.due = d;
        
    }
    
    public Set<NamedObject> getAllNamedChildObjects ()
    {
        
        return null;
        
    }
    
    public void getChanges (NamedObject old,
                            Element     root)
    {
        
    }
    
}