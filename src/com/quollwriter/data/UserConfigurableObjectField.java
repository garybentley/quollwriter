package com.quollwriter.data;

import java.util.*;

import org.jdom.*;

import com.quollwriter.*;

public class UserConfigurableObjectField extends NamedObject
{
    
    private UserConfigurableObjectTypeField field = null;
    private Object value = null;
    
    public UserConfigurableObjectField (UserConfigurableObjectTypeField type)
    {
        
        super (type.getUserConfigurableObjectType ().getUserObjectType () + "objectfield");
        
        this.field = type;
        
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
        
        if (this.value != null)
        {

            this.addToStringProperties (props,
                                        "valueClass",
                                        this.value.getClass ().getName ());
            
        }
        
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
                        
    public UserConfigurableObjectTypeField getUserConfigurableObjectTypeField ()
    {
        
        return this.field;
        
    }
    
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
                     
            try
            {
            
                return this.field.getViewEditHandler (this.getParentObject (),
                                                      this,
                                                      null).getNamesFromFieldValue ();
    
            } catch (Exception e) {
                
                Environment.logError ("Unable to get names for field: " +
                                      this.value,
                                      e);
                                
            }

        }
        
        return ret;
        
    }
    
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
    
    public void setValue (Object v)
    {
    
        this.value = v;
        
    }
    
    public Object getValue ()
    {
        
        return this.value;
        
    }
        
}