package com.quollwriter.data;

import java.util.*;

import org.jdom.*;
import org.josql.*;

import com.quollwriter.*;

public class UserConfigurableObject extends NamedObject
{
    
    protected UserConfigurableObjectType userConfigObjType = null;
    private Project proj = null;
    protected Set<UserConfigurableObjectField> fields = new LinkedHashSet ();
    
    public UserConfigurableObject (String objType)
    {
        
        super (objType);
        
    }
    
    public UserConfigurableObject (String objType,
                                   String name)
    {
        
        super (objType,
               name);
        
    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {
        
        return new LinkedHashSet (this.fields);
        
    }

    public void getChanges (NamedObject old,
                            Element     root)
    {
        
    }
    
    public UserConfigurableObjectField getPrimaryNameField ()
    {
        
        for (UserConfigurableObjectField f : this.fields)
        {
            
            if ((f.isNameField ())
                &&
                (f.isPrimaryNameField ())
               )
            {

                return f;
            
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
        
        this.fields.remove (f);
        
    }
    
    public void setFields (Set<UserConfigurableObjectField> fields)
    {
        
        if (this.userConfigObjType == null)
        {
            
            throw new IllegalStateException ("No configurable object type set");
            
        }
        
        for (UserConfigurableObjectField f : fields)
        {
            
            this.addField (f);
            
        }
        
    }
    
    public void addField (UserConfigurableObjectField f)
    {
               
        if (this.userConfigObjType == null)
        {
            
            throw new IllegalStateException ("No configurable object type set");
            
        }
                
        f.setParent (this);
                
        this.fields.add (f);
                         
        if (f.isPrimaryNameField ())
        {
            
            this.setName (f.getValue ().toString ());
            
        }
                                
    }
        
    /*
    private void sortFields ()
    {
        
        Query q = new Query ();
        
        try
        {
        
            q.parse (String.format ("SELECT * FROM %s ORDER BY field.order",
                                    UserConfigurableObjectField.class.getName ()));
    
            QueryResults qr = q.execute (this.fields);
    
            this.fields = new LinkedHashSet (qr.getResults ());

        } catch (Exception e) {
            
            Environment.logError ("Unable to order fields",
                                  e);
            
        }
        
    }
    */
                
    public UserConfigurableObjectTypeField getPrimaryNameTypeField ()
    {
        
        if (this.userConfigObjType == null)
        {
            
            throw new IllegalStateException ("No configurable object type set");
            
        }
        
        return this.userConfigObjType.getPrimaryNameField ();

    }
    
    public Set<UserConfigurableObjectField> getFields ()
    {
        
        return this.fields;
        
    }
    
    public Project getProject ()
    {

        return this.proj;

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
    
}