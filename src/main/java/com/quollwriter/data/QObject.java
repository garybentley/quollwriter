package com.quollwriter.data;

import java.util.*;

import org.jdom.*;

public class QObject extends LegacyAsset
{

    public static final String TYPE_LEGACY_FIELD_ID = "type";
    public static final String TYPE_LEGACY_FIELD_FORM_NAME = "Type";

    public static final String OBJECT_TYPE = "object";
    public static final String TYPE = "type";
    
    private String type = null;

    public QObject ()
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
    
        UserConfigurableObjectField f = this.getLegacyField (TYPE_LEGACY_FIELD_ID);
                
        if (f == null)
        {
            
            UserConfigurableObjectTypeField type = this.getLegacyTypeField (TYPE_LEGACY_FIELD_ID);            
            
            // May not have the field.
            if (type == null)
            {
                
                return;
                
            }
            
            f = new UserConfigurableObjectField (type);
            
            this.addField (f);
            
        }
                    
        Set<String> vals = new LinkedHashSet ();
        vals.add (t);
        
        f.setValue (vals);
    
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
