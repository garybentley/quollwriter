package com.quollwriter.data;

import java.util.*;

import org.jdom.*;
import org.josql.*;

import com.quollwriter.*;

public abstract class LegacyUserConfigurableObject extends UserConfigurableObject
{
        
    public static final String NAME_LEGACY_FIELD_ID = "name";
    public static final String NAME_LEGACY_FIELD_FORM_NAME = "Name";
    public static final String DESCRIPTION_LEGACY_FIELD_ID = "description";
    public static final String DESCRIPTION_LEGACY_FIELD_FORM_NAME = "Description";
    public static final String ALIASES_LEGACY_FIELD_ID = "aliases";
    public static final String ALIASES_LEGACY_FIELD_FORM_NAME = "Aliases";
        
    public LegacyUserConfigurableObject (String objType)
    {
        
        super (objType);
        
    }
                
    public boolean isFieldSupported (String id)
    {
        
        if ((id.equals (NAME_LEGACY_FIELD_ID))
            ||
            (id.equals (DESCRIPTION_LEGACY_FIELD_ID))
            ||
            (id.equals (ALIASES_LEGACY_FIELD_ID))
           )
        {
            
            return true;
                
        }
        
        return false;
        
    }
    /*
    @Override    
    public void addField (UserConfigurableObjectField f)
    {
                         
        if (f.isLegacyField ())
        {

            if (f.getLegacyFieldId ().equals (DESCRIPTION_LEGACY_FIELD_ID))
            {
                
                Map m = (Map) JSONDecoder.decode (f.getValue ().toString ());
                
                // TODO: Have better constants.  See JSONEncoder
                this.setDescription (new StringWithMarkup ((String) m.get ("text"), (String) m.get ("markup")));
                
            }
        
            if (f.getLegacyFieldId ().equals ("aliases"))
            {
                
                this.setAliases (f.getValue ().toString ());
                
            }
            
        } else {
            
            super.addField (f);
            
        }
                        
    }
      */      
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
    
    @Override
    public void setName (String n)
    {
        
        if (this.isFieldSupported (NAME_LEGACY_FIELD_ID))
        {

            UserConfigurableObjectField f = this.getPrimaryNameField ();
            
            if (f == null)
            {
                
                UserConfigurableObjectTypeField type = this.getPrimaryNameTypeField ();
                
                f = new UserConfigurableObjectField (type);
                f.setParent (this);
                this.fields.add (f);
                
            }
        
            f.setValue (n);
            
        }
        
        super.setName (n);
        
    }
    
    @Override
    public void setDescription (StringWithMarkup s)
    {
        
        if (this.isFieldSupported (DESCRIPTION_LEGACY_FIELD_ID))
        {
        
            UserConfigurableObjectField f = this.getLegacyField ("description");
                    
            if (f == null)
            {
                
                UserConfigurableObjectTypeField type = this.getLegacyTypeField ("description");            
                
                f = new UserConfigurableObjectField (type);
                f.setParent (this);
                this.fields.add (f);
                
            }
                
            try
            {
        
                f.setValue (JSONEncoder.encode (s));
                
            } catch (Exception e) {
                
                throw new IllegalArgumentException ("Unable to json encode string with markup: " +
                                                    s,
                                                    e);
                
            }

        }
                                    
        super.setDescription (s);
        
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