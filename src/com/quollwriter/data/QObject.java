package com.quollwriter.data;

import org.jdom.*;


public class QObject extends Asset
{

    public static final String OBJECT_TYPE = "object";

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

        this.type = t;

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
