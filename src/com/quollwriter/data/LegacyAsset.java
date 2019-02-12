package com.quollwriter.data;

import java.util.*;

import org.jdom.*;
import org.josql.*;

import com.quollwriter.*;

public abstract class LegacyAsset extends Asset
{

    public static final String NAME_LEGACY_FIELD_ID = "name";
    public static final String DESCRIPTION_LEGACY_FIELD_ID = "description";
    public static final String ALIASES_LEGACY_FIELD_ID = "aliases";

    public static Map<String, Class> supportedLegacyTypes = new LinkedHashMap ();

    static
    {

        Map m = LegacyAsset.supportedLegacyTypes;

        m.put (QCharacter.OBJECT_TYPE,
               QCharacter.class);
        m.put (Location.OBJECT_TYPE,
               Location.class);
        m.put (QObject.OBJECT_TYPE,
               QObject.class);
        m.put (ResearchItem.OBJECT_TYPE,
               ResearchItem.class);

    }

    public LegacyAsset (String objType)
    {

        super (Environment.getUserConfigurableObjectType (objType));

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "legacyAsset",
                                    true);

    }

    public static Asset createLegacyAsset (UserConfigurableObjectType objectType)
                                    throws GeneralException
    {

        Class cl = LegacyAsset.supportedLegacyTypes.get (objectType.getUserObjectType ());

        if (cl == null)
        {

            return null;

        }

        try
        {

            return (Asset) cl.getDeclaredConstructor ().newInstance ();

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to create new instance of: " +
                                        cl.getName () +
                                        " for object type: " +
                                        objectType,
                                        e);

        }

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

        this.setAliases (new StringWithMarkup (a));

    }

    public void setAliases (StringWithMarkup a)
    {

        if (this.isFieldSupported (ALIASES_LEGACY_FIELD_ID))
        {

            UserConfigurableObjectField f = this.getLegacyField (ALIASES_LEGACY_FIELD_ID);

            if (f == null)
            {

                UserConfigurableObjectTypeField type = this.getLegacyTypeField (ALIASES_LEGACY_FIELD_ID);

                if (type == null)
                {
                    return;
                }

                f = new UserConfigurableObjectField (type);

                this.addField (f);

            }

            f.setValue (a);

        }

        super.setAliases (a.getText ());

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
