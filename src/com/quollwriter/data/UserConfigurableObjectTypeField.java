package com.quollwriter.data;

import java.util.*;

import org.jdom.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.userobjects.*;

public abstract class UserConfigurableObjectTypeField extends NamedObject
{
    
    public enum Type
    {
        
        text ("text"),
        multitext ("multitext"),
        image ("image"),
        select ("select"),
        date ("date"),
        number ("number"),
        file ("file"),
        webpage ("webpage"),
        objectname ("objectname"),
        objectdesc ("objectdesc"),
        objectimage ("objectimage");
        
        private String type = null;
        
        Type (String type)
        {
            
            this.type = type;
            
        }
        
        public String getType ()
        {
            
            return this.type;
            
        }
        
        public static UserConfigurableObjectTypeField getNewFieldForType (Type t)
        {
            
            switch (t)
            {
                case text : return new TextUserConfigurableObjectTypeField ();
                case multitext : return new MultiTextUserConfigurableObjectTypeField ();
                case select : return new SelectUserConfigurableObjectTypeField ();
                case date : return new DateUserConfigurableObjectTypeField ();
                case number : return new NumberUserConfigurableObjectTypeField ();
                case webpage : return new WebpageUserConfigurableObjectTypeField ();
                case file : return new FileUserConfigurableObjectTypeField ();
                case image : return new ImageUserConfigurableObjectTypeField ();
                case objectname : return new ObjectNameUserConfigurableObjectTypeField ();
                case objectdesc : return new ObjectDescriptionUserConfigurableObjectTypeField ();
                case objectimage : return new ObjectImageUserConfigurableObjectTypeField ();
                default : return null;
                
            }            
            
        }            

        public String getName ()
        {
            
            java.util.List<String> prefix = new ArrayList ();
            prefix.add (LanguageStrings.form);
            prefix.add (LanguageStrings.types);

            return Environment.getUIString (prefix,
                                            this.type);
            
        }
        
    }
    
    public static final String OBJECT_TYPE = "userconfigobjfield";
    
    private String formName = null;
    private Type type = null;
    private UserConfigurableObjectType userConfigType = null;
    private Map<String, Object> definition = new HashMap ();
    private String defValue = null;
    private int order = -1;
    
    protected UserConfigurableObjectTypeField (Type type)
    {
        
        super (OBJECT_TYPE);
        
        this.type = type;
        
    }
           
    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "formName",
                                    this.formName);
        this.addToStringProperties (props,
                                    "type",
                                    this.type.getName ());
        this.addToStringProperties (props,
                                    "default",
                                    this.defValue);
        this.addToStringProperties (props,
                                    "order",
                                    this.order);
        this.addToStringProperties (props,
                                    "definition",
                                    this.definition);
                        
    }

    /**
     * The view/edit handler is used for viewing/editing the data of a field of this type.
     *
     * @return The view/edit handler for this type of field.
     */
    // TODO: Check to see if this needs to be generic.
    // TODO: Have this be configurable and use class loading...
    public abstract UserConfigurableObjectFieldViewEditHandler getViewEditHandler (UserConfigurableObject      obj,
                                                                                   UserConfigurableObjectField field,
                                                                                   AbstractProjectViewer       viewer);
                      
    /**
     * The config handler is used for editing/configuration of this field.
     *
     * Implementing classes should return a suitable sub-type that can handle this field.
     *
     * @return The config handler for this field.
     */
    public abstract UserConfigurableObjectTypeFieldConfigHandler getConfigHandler ();           
           
    public boolean isSortable ()
    {
        
        return false;
           
    }
    
    public boolean canEdit ()
    {
        
        return true;
           
    }
    
    public boolean canDelete ()
    {
        
        return true;
        
    }
    
    public void setDefinitionValue (String id,
                                    Object v)
    {
        
        this.definition.put (id,
                             v);
                        
    }
    
    public void setDefinitionValue (String id,
                                    Date   v)
    {
        
        this.definition.put (id,
                             Environment.formatDate (v));
        
    }
    
    public void setDefinitionValue (String id,
                                    Double v)
    {
    
        this.definition.put (id,
                             (v != null ? Environment.formatNumber (v) : null));
        
    }
    
    public Double getDoubleDefinitionValue (String id)
                                     throws GeneralException
    {
        
        Object o = this.getDefinitionValue (id);
        
        if (o == null)
        {
            
            return null;
            
        }
        
        return Environment.parseToDouble (o.toString ());
        
    }
    
    public Date getDateDefinitionValue (String id)
    {
        
        Object v = this.getDefinitionValue (id);
        
        if (v == null)
        {
            
            return null;
            
        }
        
        return Environment.parseDate (v.toString ());
        
    }
    
    public boolean getBooleanDefinitionValue (String id)
    {
        
        Boolean b = (Boolean) this.getDefinitionValue (id);
        
        if (b == null)
        {
            
            return false;
            
        }
        
        return b;
        
    }
    
    public Object getDefinitionValue (String id)
    {
        
        return this.definition.get (id);
        
    }
    
    public void removeDefinitionValue (String id)
    {
        
        this.definition.remove (id);
        
    }
        
    public boolean isLegacyField ()
    {
        
        return this.getLegacyFieldId () != null;
                        
    }
    
    public boolean isSearchable ()
    {
        
        return this.getBooleanDefinitionValue ("searchable");
                        
    }
    
    public void setSearchable (boolean v)
    {
                
        this.setDefinitionValue ("searchable", true);
        
    }
    
    public void setLegacyFieldId (String i)
    {
        
        this.setDefinitionValue ("legacyFieldId", i);
                        
    }
    
    public String getLegacyFieldId ()
    {
        
        return (String) this.getDefinitionValue ("legacyFieldId");
        
    }
    
    public void setOrder (int i)
    {
        
        int c = this.order;
        
        this.order = i;
        /*
        if ((this.userConfigType != null)
            &&
            (i != c)
           )
        {
            
            this.userConfigType.reorderFields ();
            
        }
          */              
    }
    
    public int getOrder ()
    {
        
        return this.order;
        
    }
    
    public Map getDefinition ()
    {
        
        return this.definition;
                        
    }
    
    public void setDefinition (Map m)
    {
        
        this.definition = m;
        
    }
    
    public String getDefaultValue ()
    {
        
        return (String) this.getDefinitionValue ("default");
        
    }
    
    public void setDefaultValue (String v)
    {
        
        this.setDefinitionValue ("default", v);
        
    }
    
    public void setUserConfigurableObjectType (UserConfigurableObjectType t)
    {
        
        this.userConfigType = t;
        
        this.setParent (t);
            
    }
    
    public UserConfigurableObjectType getUserConfigurableObjectType ()
    {
        
        return this.userConfigType;
        
    }
    
    public Set<NamedObject> getAllNamedChildObjects ()
    {
        
        return null;
        
    }

    public void getChanges (NamedObject old,
                            Element     root)
    {
        
    }

    public boolean isNameField ()
    {
        
        Object o = this.getDefinitionValue ("nameField");
        
        if (o == null)
        {
            
            return false;
            
        }
        
        return (Boolean) o;
            
    }
    
    public void setNameField (boolean v)
    {
        
        this.setDefinitionValue ("nameField", v);
        
    }
                                
    public DataObject getObjectForReference (ObjectReference ref)
    {
        
        return null;
        
    }
    
    public void setFormName (String n)
    {
        
        super.setName (n);
        
    }
    
    public String getFormName ()
    {
        
        return this.getName ();
        
    }
            
    public Type getType ()
    {
        
        return this.type;
        
    }
    
}
