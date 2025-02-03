package com.quollwriter.data;

import java.util.*;

import org.dom4j.*;

import com.quollwriter.*;

public class QObject extends LegacyAsset
{

    public static final String TYPE_LEGACY_FIELD_ID = "type";
    public static final String TYPE_LEGACY_FIELD_FORM_NAME = "Type";

    public static final String OBJECT_TYPE = "object";
    public static final String TYPE = "type";

    private String type = null;

    public QObject (UserConfigurableObjectType objType)
    {

        super (objType);

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

        Set<String> vals = new LinkedHashSet<> ();
        vals.add (t);

        try
        {

            f.setValue (JSONEncoder.encode (vals));

        } catch (Exception e) {

            Environment.logError ("Unable to encode: " + vals,
                                  e);

        }

        this.type = t;

        this.firePropertyChangedEvent (TYPE,
                                       oldType,
                                       this.type);

    }

    @Override
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
