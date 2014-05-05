package com.quollwriter.data;

import org.jdom.*;


public class QObject extends Asset
{

    public static final String OBJECT_TYPE = "object";
    public static final String TYPE = "type";
    
    private String type = null;

    public QObject()
    {

        super (QObject.OBJECT_TYPE);

    }

    public String getType ()
    {

        return this.type;

    }

    public void setType (String t)
    {

        String oldType = this.type;
    
        this.type = t;

        this.firePropertyChangedEvent (TYPE,
                                       oldType,
                                       this.type);        
        
    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

        QObject o = (QObject) old;

        this.addFieldChangeElement (root,
                                    "type",
                                    ((old != null) ? o.getType () : null),
                                    this.type);

    }

}
