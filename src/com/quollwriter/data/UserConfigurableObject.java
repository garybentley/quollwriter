package com.quollwriter.data;

import java.util.*;

import org.jdom.*;
import org.josql.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;

public class UserConfigurableObject extends NamedObject
{
                
    protected UserConfigurableObjectType userConfigObjType = null;
    private Project proj = null;
    protected Set<UserConfigurableObjectField> fields = new LinkedHashSet ();
    
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
            
            if (f.isNameField ())
            {

                Object v = f.getValue ();
                
                if (v != null)
                {
            
                    Set<String> names = new LinkedHashSet ();

                    // TODO: Change to return a list of names from the field.
                    StringTokenizer t = new StringTokenizer (v.toString (),
                                                             "\n;,");
            
                    while (t.hasMoreTokens ())
                    {
                        
                        names.add (t.nextToken ());
                        
                    }
            
                    l.addAll (names);
            
                }
                                
            }
            
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
    
    public ObjectNameUserConfigurableObjectFieldViewEditHandler getPrimaryNameViewEditHandler (com.quollwriter.ui.ProjectViewer viewer)
    {
        
        return (ObjectNameUserConfigurableObjectFieldViewEditHandler) this.getPrimaryNameTypeField ().getViewEditHandler (this,
                                                                                                                          null,
                                                                                                                          viewer);
        
    }

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
    
    public Project getProject ()
    {

        return this.proj;

    }

    public String getObjectTypeName ()
    {
        
        return this.userConfigObjType.getObjectTypeName ();
        
    }
    
    public String getObjectTypePluralName ()
    {
        
        return this.userConfigObjType.getObjectTypeNamePlural ();
        
    }

    public void setProject (Project p)
    {

        this.proj = p;
        
        this.setParent (this.proj);

    }
    
    public UserConfigurableObjectType getUserConfigurableObjectType ()
    {
        
        return this.userConfigObjType;
        
    }
    
    public void setUserConfigurableObjectType (UserConfigurableObjectType t)
    {
        
        this.userConfigObjType = t;
        
    }

    public Set<UserConfigurableObjectFieldViewEditHandler> getViewEditHandlers (com.quollwriter.ui.ProjectViewer viewer)
    {
        
        Map<UserConfigurableObjectTypeField, UserConfigurableObjectField> typeFieldMap = new HashMap ();
        
        for (UserConfigurableObjectField f : this.fields)
        {

            typeFieldMap.put (f.getUserConfigurableObjectTypeField (),
                              f);
        
        }            

        Set<UserConfigurableObjectFieldViewEditHandler> handlers = new LinkedHashSet ();
        
        for (UserConfigurableObjectTypeField tf : this.userConfigObjType.getConfigurableFields ())
        {
                      
            UserConfigurableObjectField f = typeFieldMap.get (tf);

            UserConfigurableObjectFieldViewEditHandler h = tf.getViewEditHandler (this,
                                                                                  f,
                                                                                  viewer);
            
            handlers.add (h);
            
        }

        return handlers;        
            
    }
    
}