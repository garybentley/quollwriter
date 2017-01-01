package com.quollwriter.data;

import java.util.*;

import org.jdom.*;

public class UserConfigurableObjectField extends NamedObject
{
    
    private UserConfigurableObjectTypeField field = null;
    private Object value = null;
    
    public UserConfigurableObjectField (UserConfigurableObjectTypeField type)
    {
        
        super (type.getUserConfigurableObjectType ().getUserObjectType () + "objectfield");
        
        this.field = type;
        
    }
    
    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "value",
                                    this.value);
        this.addToStringProperties (props,
                                    "typeField",
                                    this.field);
                        
    }
    
    public Set<NamedObject> getAllNamedChildObjects ()
    {
        
        return null;
        
    }

    public boolean isNameField ()
    {
        
        return this.field.isNameField ();
        
    }
    
    public boolean isPrimaryNameField ()
    {
        
        return this.field.isPrimaryNameField ();
        
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
    
    public void setValue (Object v)
    {
        
        this.value = v;
        
    }
    
    public Object getValue ()
    {
        
        return this.value;
        
    }
        
}