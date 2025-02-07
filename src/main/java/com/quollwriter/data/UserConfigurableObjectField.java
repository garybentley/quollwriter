package com.quollwriter.data;

import java.util.*;

import org.dom4j.*;

import com.quollwriter.*;

public class UserConfigurableObjectField extends NamedObject
{

    private UserConfigurableObjectTypeField field = null;
    private String value = null;

    public UserConfigurableObjectField (UserConfigurableObjectTypeField type)
    {

        super (type.getUserConfigurableObjectType ().getObjectTypeId () + "objectfield");

        this.field = type;

    }

    // TODO: Make this better...!
    public Set<String> getProjectFileNames ()
    {

        Set<String> files = new LinkedHashSet<> ();

        if (this.value == null)
        {

            return files;

        }

        if ((this.field instanceof ImageUserConfigurableObjectTypeField)
            ||
            (this.field instanceof FileUserConfigurableObjectTypeField)
           )
        {

            files.add (this.value);

        }

        return files;

    }

    public UserConfigurableObject getParentObject ()
    {

        return (UserConfigurableObject) this.getParent ();

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "value",
                                    this.value);
/*
        if (this.value != null)
        {

            this.addToStringProperties (props,
                                        "value",
                                        this.value);

        }
*/
        this.addToStringProperties (props,
                                    "typeField",
                                    this.field);

    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return null;

    }

    public boolean isPrimaryNameField ()
    {

        return (this.field instanceof ObjectNameUserConfigurableObjectTypeField);

    }

    public boolean isNameField ()
    {

        return (this.field.isNameField () || this.isPrimaryNameField ());

    }

    public boolean isLegacyField ()
    {

        return this.field.isLegacyField ();

    }

    public String getLegacyFieldId ()
    {

        return this.field.getLegacyFieldId ();

    }

    // A very dangerous method to use, don't use it!
    public void setUserConfigurableObjectTypeField (UserConfigurableObjectTypeField f)
    {

        this.field = f;

    }

    public UserConfigurableObjectTypeField getUserConfigurableObjectTypeField ()
    {

        return this.field;

    }

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public Set<String> getNames ()
    {

        Set<String> ret = new LinkedHashSet ();

        if ((this.isNameField ())
            &&
            (this.value != null)
           )
        {

            String st = null;

            try
            {

                StringWithMarkup sm = JSONDecoder.decodeToStringWithMarkup (this.value);

                st = sm.getText ();

            } catch (Exception e) {

                st = this.value;

            }

            if (st != null)
            {

                StringTokenizer t = new StringTokenizer (st,
                                                         ";,");

                while (t.hasMoreTokens ())
                {

                    ret.add (t.nextToken ().trim ());

                }

            }

        }

        return ret;

    }
/*
    public String getValueAsString ()
    {

        if (this.value == null)
        {

            return null;

        }

        try
        {

            return this.field.getViewEditHandler (this.getParentObject (),
                                                  this,
                                                  null).valueToString (this.value);

        } catch (Exception e) {

            Environment.logError ("Unable to get value as string: " +
                                  this.value,
                                  e);

            return this.value.toString ();

        }

    }
*/
    public void setValue (String v)
    {

        this.value = v;

    }

    public String getValue ()
    {

        return this.value;

    }

}
