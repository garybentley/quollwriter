package com.quollwriter.data;

import java.util.*;

import org.josql.*;

import com.quollwriter.*;

public abstract class LegacyUserConfigurableObject extends UserConfigurableObject
{

    public static final String NAME_LEGACY_FIELD_ID = "name";
    public static final String DESCRIPTION_LEGACY_FIELD_ID = "description";
    public static final String ALIASES_LEGACY_FIELD_ID = "aliases";

    public LegacyUserConfigurableObject (UserConfigurableObjectType objType)
    {

        super (objType);

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "legacyObject",
                                    true);

    }

    public boolean isFieldSupported (String id)
    {

        if (id.equals (ALIASES_LEGACY_FIELD_ID))
        {

            return true;

        }

        return false;

    }

    @Override
    public void setAliases (String a)
    {

        if (this.isFieldSupported (ALIASES_LEGACY_FIELD_ID))
        {

            UserConfigurableObjectField f = this.getLegacyField (ALIASES_LEGACY_FIELD_ID);

            if (f == null)
            {

                UserConfigurableObjectTypeField type = this.getLegacyTypeField (ALIASES_LEGACY_FIELD_ID);

                f = new UserConfigurableObjectField (type);

                f.setParent (this);
                this.fields.add (f);

            }

            f.setValue (a);

        }

        super.setAliases (a);

    }

    public UserConfigurableObjectTypeField getLegacyTypeField (String id)
    {

        if (this.userConfigObjType == null)
        {

            throw new IllegalStateException ("No configurable object type set");

        }

        return this.userConfigObjType.getLegacyField (id);

    }

    public UserConfigurableObjectField getLegacyField (String id)
    {

        for (UserConfigurableObjectField f : this.fields)
        {

            if ((f.getLegacyFieldId () != null)
                &&
                (f.getLegacyFieldId ().equals (id))
               )
            {

                return f;

            }

        }

        return null;

    }

}
