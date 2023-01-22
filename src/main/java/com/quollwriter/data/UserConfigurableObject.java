package com.quollwriter.data;

import java.util.*;

import javafx.beans.property.*;

import org.dom4j.*;
import org.josql.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.userobjects.*;

public class UserConfigurableObject extends NamedObject
{

    protected UserConfigurableObjectType userConfigObjType = null;
    protected Set<UserConfigurableObjectField> fields = new LinkedHashSet<> ();

    public UserConfigurableObject (UserConfigurableObjectType objType)
    {

        super (objType.getObjectTypeId ());

        this.userConfigObjType = objType;

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "userConfigObjType",
                                    this.userConfigObjType);
        this.addToStringProperties (props,
                                    "fields",
                                    this.fields.size ());

    }

    @Override
    public Set<String> getAllNames ()
    {

        Set<String> l = new HashSet ();

        l.add (this.getName ());

        // Find all name fields and add to the list.
        for (UserConfigurableObjectField f : this.fields)
        {

            l.addAll (f.getNames ());

        }

        return l;

    }

    @Override
    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return new LinkedHashSet (this.fields);

    }

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }
/*
TODO Remove
    public ObjectNameUserConfigurableObjectFieldViewEditHandler getPrimaryNameViewEditHandler (com.quollwriter.ui.ProjectViewer viewer)
    {

        return (ObjectNameUserConfigurableObjectFieldViewEditHandler) this.getPrimaryNameTypeField ().getViewEditHandler (this,
                                                                                                                          null,
                                                                                                                          viewer);

    }
*/
/*
TODO Remove
    public ObjectDescriptionUserConfigurableObjectFieldViewEditHandler getObjectDescriptionViewEditHandler (com.quollwriter.ui.ProjectViewer viewer)
    {

        if (this.getObjectDescriptionTypeField () == null)
        {

            return null;

        }

        return (ObjectDescriptionUserConfigurableObjectFieldViewEditHandler) this.getObjectDescriptionTypeField ().getViewEditHandler (this,
                                                                                                                                       null,
                                                                                                                                       viewer);

    }
*/

    public Object getValueForField (UserConfigurableObjectTypeField f)
    {

        for (UserConfigurableObjectField field : this.fields)
        {

            if (field.getUserConfigurableObjectTypeField () == f)
            {

                return field.getUserConfigurableObjectTypeField ().getViewEditHandler (this,
                                                                                       field,
                                                                                       null).getFieldValue ();

            }

        }

        return null;

    }

    public void removeField (UserConfigurableObjectField f)
    {

        if (this.userConfigObjType == null)
        {

            throw new IllegalStateException ("No configurable object type set");

        }

        if (f.isPrimaryNameField ())
        {

            throw new IllegalStateException ("Cannot remove primary name field.");

        }

        this.fields.remove (f);

    }

    public void addField (UserConfigurableObjectField f)
    {

        if (this.userConfigObjType == null)
        {

            throw new IllegalStateException ("No configurable object type set");

        }

        f.setParent (this);

        this.fields.add (f);

    }

    public ObjectNameUserConfigurableObjectTypeField getPrimaryNameTypeField ()
    {

        if (this.userConfigObjType == null)
        {

            throw new IllegalStateException ("No configurable object type set");

        }

        return this.userConfigObjType.getPrimaryNameField ();

    }

    public ObjectDescriptionUserConfigurableObjectTypeField getObjectDescriptionTypeField ()
    {

        if (this.userConfigObjType == null)
        {

            throw new IllegalStateException ("No configurable object type set");

        }

        return this.userConfigObjType.getObjectDescriptionField ();

    }

    public ObjectImageUserConfigurableObjectTypeField getObjectImageTypeField ()
    {

        if (this.userConfigObjType == null)
        {

            throw new IllegalStateException ("No configurable object type set");

        }

        return this.userConfigObjType.getObjectImageField ();

    }

    public Set<UserConfigurableObjectField> getFields ()
    {

        return this.fields;

    }

    public String getObjectTypeName ()
    {

        return this.userConfigObjType.getObjectTypeName ();

    }

    public StringProperty objectTypeNameProperty ()
    {

        return this.userConfigObjType.objectTypeNameProperty ();

    }

    public String getObjectTypePluralName ()
    {

        return this.userConfigObjType.getObjectTypeNamePlural ();

    }

    public StringProperty objectTypeNamePluralProperty ()
    {

        return this.userConfigObjType.objectTypeNamePluralProperty ();

    }

    public UserConfigurableObjectType getUserConfigurableObjectType ()
    {

        return this.userConfigObjType;

    }

    public void setUserConfigurableObjectType (UserConfigurableObjectType t)
    {

        this.userConfigObjType = t;

    }

    public Set<com.quollwriter.ui.userobjects.UserConfigurableObjectFieldViewEditHandler> getViewEditHandlers (com.quollwriter.ui.ProjectViewer viewer)
    {

        Map<UserConfigurableObjectTypeField, UserConfigurableObjectField> typeFieldMap = new HashMap<> ();

        for (UserConfigurableObjectField f : this.fields)
        {

            typeFieldMap.put (f.getUserConfigurableObjectTypeField (),
                              f);

        }

        Set<com.quollwriter.ui.userobjects.UserConfigurableObjectFieldViewEditHandler> handlers = new LinkedHashSet<> ();

        for (UserConfigurableObjectTypeField tf : this.userConfigObjType.getConfigurableFields ())
        {

            UserConfigurableObjectField f = typeFieldMap.get (tf);

            com.quollwriter.ui.userobjects.UserConfigurableObjectFieldViewEditHandler h = tf.getViewEditHandler (this,
                                                                                  f,
                                                                                  viewer);

            handlers.add (h);

        }

        return handlers;

    }

    public UserConfigurableObjectField getField (UserConfigurableObjectTypeField t)
    {

        return this.fields.stream ()
            .filter (f -> f.getUserConfigurableObjectTypeField ().equals (t))
            .findFirst ()
            .orElse (null);

    }

/*
TODO Remove
    public Set<UserConfigurableObjectFieldViewEditHandler> getViewEditHandlers2 (ProjectViewer viewer)
    {

        Map<UserConfigurableObjectTypeField, UserConfigurableObjectField> typeFieldMap = new HashMap<> ();

        for (UserConfigurableObjectField f : this.fields)
        {

            typeFieldMap.put (f.getUserConfigurableObjectTypeField (),
                              f);

        }

        Set<UserConfigurableObjectFieldViewEditHandler> handlers = new LinkedHashSet<> ();

        for (UserConfigurableObjectTypeField tf : this.userConfigObjType.getConfigurableFields ())
        {

            UserConfigurableObjectField f = typeFieldMap.get (tf);

            UserConfigurableObjectFieldViewEditHandler h = tf.getViewEditHandler2 (this,
                                                                                  f,
                                                                                  viewer);

            handlers.add (h);

        }

        return handlers;

    }
*/
}
