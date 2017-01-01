package com.quollwriter.data;

import java.util.*;

import org.jdom.*;

import com.quollwriter.*;

public class UserConfigurableObjectType extends NamedObject
{
    
    public static final String OBJECT_TYPE = "userconfigobjtype";
    
    private String objectTypePluralName = null;
    private String iconName = null;
    private Set<UserConfigurableObjectTypeField> fields = new LinkedHashSet ();
    private String layout = null;
    private String userObjectType = null;
    
    public UserConfigurableObjectType ()
    {
        
        super (OBJECT_TYPE);
        
    }
     
    public String getUserObjectType ()
    {
        
        return this.userObjectType;        
     
    }
    
    public void setUserObjectType (String t)
    {
        
        this.userObjectType = t;
        
    }
    
    public Set<NamedObject> getAllNamedChildObjects ()
    {
        
        return new LinkedHashSet (this.fields);
        
    }

    public void getChanges (NamedObject old,
                            Element     root)
    {
        
    }
     
    public String getLayout ()
    {
        
        return this.layout;
        
    }
    
    public void setLayout (String l)
    {
        
        this.layout = l;
        
    }
    
    public Set<UserConfigurableObjectTypeField> getConfigurableFields ()
    {
        
        return new LinkedHashSet (this.fields);
        
    }
    
    public void addConfigurableField (UserConfigurableObjectTypeField f)
    {
                
        this.fields.add (f);
        
        f.setOrder (this.fields.size () - 1);
        
        f.setUserConfigurableObjectType (this);
        
    }
    
    public void removeConfigurableField (UserConfigurableObjectTypeField f)
    {
        
        if (f.isNameField ())
        {
            
            throw new IllegalArgumentException ("Cant remove the name field.");
            
        }
        
        this.fields.remove (f);
        
        f.setUserConfigurableObjectType (null);
        
        this.reorderFields ();
        
    }
            
    public void reorderFields ()
    {
                
        int i = 0; 
                
        for (UserConfigurableObjectTypeField f : this.fields)
        {  
            
            f.setOrder (i);
            
            i++;
            
        }
                
    }

    public UserConfigurableObjectTypeField getLegacyField (String id)
    {
        
        for (UserConfigurableObjectTypeField f : this.fields)
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
    
    public UserConfigurableObjectTypeField getPrimaryNameField ()
    {
        
        for (UserConfigurableObjectTypeField f : this.fields)
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
    
    public String getIconName ()
    {
        
        return this.iconName;
        
    }
    
    public void setIconName (String n)
    {
        
        this.iconName = n;
        
    }
    
    public void setObjectTypeName (String n)
    {
        
        this.setName (n);
                
    }
    
    public String getObjectTypeName ()
    {
        
        return this.getName ();
        
    }
    
    public String getObjectTypePluralName ()
    {
        
        return this.objectTypePluralName;
        
    }
    
    public void setObjectTypePluralName (String n)
    {
        
        this.objectTypePluralName = n;
                
    }
     
}