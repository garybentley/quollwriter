package com.quollwriter.data;

import java.util.*;

import org.jdom.*;

import com.quollwriter.ui.components.FormItem;

public abstract class UserConfigurableObjectTypeField extends NamedObject
{
    
    public enum Type
    {
        
        text ("text"),
        multitext ("multitext"),
        select ("select"),
        date ("date"),
        number ("number"),
        file ("file"),
        webpage ("webpage");
        
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
                case date : return new DateUserObjectTypeField ();
                case number : return new NumberUserObjectTypeField ();
                case webpage : return new WebpageUserConfigurableObjectTypeField ();
                default : return null;
                
            }            
            
        }            
        
        public static Type getTypeForName (String n)
        {
            
            switch (n)
            {
                case "Text" : return Type.text;
                case "Multi-line text" : return Type.multitext;
                case "List" : return Type.select;
                case "Date" : return Type.date;
                case "Number" : return Type.number;
                case "Web page" : return Type.webpage;
                case "File" : return Type.file;
                default : return null;
                
            }            
            
        }
        
        public String getName ()
        {
            
            switch (this)
            {
                case text : return "Text";
                case multitext : return "Multi-line text";
                case select : return "List";
                case date : return "Date";
                case number : return "Number";
                case webpage : return "Web page";
                case file : return "File";
                default : return "Unknown";
                
            }
            
        }
        
    }
    
    public static final String OBJECT_TYPE = "userconfigobjfield";
    
    private String formName = null;
    private Type type = null;
    private UserConfigurableObjectType userConfigType = null;
    private Map<String, Object> definition = new HashMap ();
    private String defValue = null;
    private int order = 0;
    
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
           
    public void setDefinitionValue (String id,
                                    Object v)
    {
        
        this.definition.put (id,
                             v);
                        
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
        
        return (Boolean) this.getDefinitionValue ("searchable");
                        
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
        
        this.order = i;
                        
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

    public final boolean isNameField ()
    {
        
        Object o = this.getDefinitionValue ("nameField");
        
        if (o == null)
        {
            
            return false;
            
        }
        
        return (Boolean) o;
            
    }
    
    public final void setNameField (boolean v)
    {
        
        this.setDefinitionValue ("nameField", v);
        
    }
    
    public final void setPrimaryNameField (boolean v)
    {
                
        this.setDefinitionValue ("primaryNameField", true);
        this.setDefinitionValue ("nameField", true);
                        
    }
    
    public final boolean isPrimaryNameField ()
    {
        
        Object o = this.getDefinitionValue ("primaryNameField");
        
        if (o == null)
        {
            
            return false;
            
        }
        
        return (Boolean) o;
        
    }
                
    public abstract void initConfiguration (Map initVal);
    
    public abstract void fillConfiguration (Map fillVal);
    
    public abstract String getConfigurationDescription ();
    
    public abstract Set<FormItem> getExtraFormItems ();
    
    public abstract boolean updateFromExtraFormItems ();
    
    public abstract Set<String> getExtraFormItemErrors ();
        
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
