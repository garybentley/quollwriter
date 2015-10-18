package com.quollwriter.events;

import java.util.EventObject;

import com.quollwriter.data.*;

public class ProjectInfoChangedEvent extends EventObject
{

    public static final String ADDED = "added";
    public static final String CHANGED = "changed";
    public static final String DELETED = "deleted";

    private ProjectInfo value = null;
    private String changeType = null;
    
    public ProjectInfoChangedEvent (ProjectInfo value,
                                    String      changeType)
    {

        super (value);

        this.value = value;
        this.changeType = changeType;

    }

    public String getChangeType ()
    {
        
        return this.changeType;
        
    }
    
    public ProjectInfo getProjectInfo ()
    {

        return this.value;

    }

}
