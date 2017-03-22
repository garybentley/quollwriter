package com.quollwriter.data;

import java.util.*;

import javax.swing.*;

import org.jdom.*;

import com.quollwriter.*;

public class UserConfigurableObjectType extends NamedObject
{
    
    public static final String ICON16 = "icon16";
    public static final String ICON24 = "icon24";
    public static final String OBJECT_TYPE_NAME_PLURAL = "objectTypeNamePlural";
    public static final String OBJECT_TYPE_NAME = "objectTypeName";
    public static final String OBJECT_TYPE = "userconfigobjtype";
    
    private String objectTypeNamePlural = null;
    private ImageIcon icon24x24 = null;
    private ImageIcon icon16x16 = null;
    private Set<UserConfigurableObjectTypeField> fields = new LinkedHashSet ();
    private String layout = null;
    private String userObjectType = null;
    private boolean isAsset = false;
    private KeyStroke createShortcutKeyStroke = null;
    
    public UserConfigurableObjectType ()
    {
        
        super (OBJECT_TYPE);
        
    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "objectTypeId",
                                    this.getObjectTypeId ());
        this.addToStringProperties (props,
                                    "objectTypeName",
                                    this.getName ());
        this.addToStringProperties (props,
                                    "objectTypeNamePlural",
                                    this.getObjectTypeNamePlural ());
        this.addToStringProperties (props,
                                    "isAsset",
                                    this.isAssetObjectType ());
        this.addToStringProperties (props,
                                    "layout",
                                    this.getLayout ());
        this.addToStringProperties (props,
                                    "createShortcutKey",
                                    this.createShortcutKeyStroke);
        this.addToStringProperties (props,
                                    "fields",
                                    this.fields.size ());

    }
     
    public Set<UserConfigurableObjectTypeField> getSortableFields ()
    {
        
        Set<UserConfigurableObjectTypeField> sfs = new LinkedHashSet ();
        
        for (UserConfigurableObjectTypeField f : this.fields)
        {
            
            if (f.isSortable ())
            {
                
                sfs.add (f);
                
            }
            
        }
        
        return sfs;
     
    }
    
    public void setCreateShortcutKeyStroke (KeyStroke k)
    {
        
        this.createShortcutKeyStroke = k;
     
    }
    
    public KeyStroke getCreateShortcutKeyStroke ()
    {
        
        return this.createShortcutKeyStroke;
     
    }
    
    public boolean isLegacyObjectType ()
    {
        
        return this.getUserObjectType () != null;
     
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
        
        // Order the fields.
        TreeMap<Integer, UserConfigurableObjectTypeField> s = new TreeMap ();
                                
        for (UserConfigurableObjectTypeField f : this.fields)
        {  
            
            if (f.getOrder () == -1)
            {
                
                throw new IllegalStateException ("Field must have an order value: " +
                                                 f);
                
            }
            
            if (s.containsKey (f.getOrder ()))
            {
                
                throw new IllegalStateException ("Already have a field with order: " +
                                                 f.getOrder () +
                                                 ", " +
                                                 s.get (f.getOrder ()));
                
            }
            
            s.put (f.getOrder (),
                   f);
            
        }
        
        return new LinkedHashSet (s.values ());
                
    }
    
    public void addConfigurableField (UserConfigurableObjectTypeField f)
    {
                
        this.fields.add (f);
        
        f.setUserConfigurableObjectType (this);
        
        if (f.getOrder () == -1)
        {
        
            f.setOrder (this.fields.size () - 1);
            
        }
        
    }
    
    public void removeConfigurableField (UserConfigurableObjectTypeField f)
    {
        
        if (f instanceof ObjectNameUserConfigurableObjectTypeField)
        {
            
            throw new IllegalArgumentException ("Cant remove the object name field.");
            
        }
        
        this.fields.remove (f);
        
        f.setUserConfigurableObjectType (null);
        
        int i = 0;         
        
        for (UserConfigurableObjectTypeField _f : this.fields)
        {  

            _f.setOrder (i);
            
            i++;
        
        }
                
    }
/*                
    public void reorderFields ()
    {
                
        TreeMap<Integer, UserConfigurableObjectTypeField> s = new TreeMap ();
                                
        for (UserConfigurableObjectTypeField f : this.fields)
        {  
            
            if (f.getOrder () == -1)
            {
                
                throw new IllegalStateException ("Field must have an order value: " +
                                                 f);
                
            }
            
            s.put (f.getOrder (),
                   f);
            
        }
        
        this.fields = new LinkedHashSet (s.values ());
                
    }
*/
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
    
    /**
     * Return how many non object name/object description fields there are.
     *
     * @return The count.
     */
    public int getNonCoreFieldCount ()
    {
        
        int c = 0;
        
        for (UserConfigurableObjectTypeField f : this.fields)
        {
            
            // TODO: Need a better way!
            if (f instanceof ObjectNameUserConfigurableObjectTypeField)
            {
                
                continue;
                
            }
            
            if (f instanceof ObjectDescriptionUserConfigurableObjectTypeField)
            {
                
                continue;
                
            }
            
            c++;

        }
        
        return c;
    
    }
    
    public ObjectNameUserConfigurableObjectTypeField getPrimaryNameField ()
    {
        
        for (UserConfigurableObjectTypeField f : this.fields)
        {
            
            // TODO: Need a better way!
            if (f instanceof ObjectNameUserConfigurableObjectTypeField)
            {
                
                return (ObjectNameUserConfigurableObjectTypeField) f;
                
            }
            
        }
        
        return null;
        
    }
        
    public ObjectDescriptionUserConfigurableObjectTypeField getObjectDescriptionField ()
    {
        
        for (UserConfigurableObjectTypeField f : this.fields)
        {
            
            // TODO: Need a better way!
            if (f instanceof ObjectDescriptionUserConfigurableObjectTypeField)
            {
                
                return (ObjectDescriptionUserConfigurableObjectTypeField) f;
                
            }
            
        }
        
        return null;
        
    }

    public ObjectImageUserConfigurableObjectTypeField getObjectImageField ()
    {
        
        for (UserConfigurableObjectTypeField f : this.fields)
        {
            
            // TODO: Need a better way!
            if (f instanceof ObjectImageUserConfigurableObjectTypeField)
            {
                
                return (ObjectImageUserConfigurableObjectTypeField) f;
                
            }
            
        }
        
        return null;
        
    }

    public void setIcon24x24 (ImageIcon ic)
    {
        
        ImageIcon oldIc = this.icon24x24;
        
        this.icon24x24 = ic;

        this.firePropertyChangedEvent (ICON24,
                                       oldIc,
                                       ic);
        
    }
    
    public ImageIcon getIcon24x24 ()
    {
        
        return this.icon24x24;
        
    }
    
    public void setIcon16x16 (ImageIcon ic)
    {
        
        ImageIcon oldIc = this.icon16x16;
        
        this.icon16x16 = ic;

        this.firePropertyChangedEvent (ICON16,
                                       oldIc,
                                       ic);
        
    }
    
    public ImageIcon getIcon16x16 ()
    {
        
        return this.icon16x16;
        
    }

    public void setObjectTypeName (String n)
    {
        
        String oldName = this.getName ();
        
        this.setName (n);
                
        this.firePropertyChangedEvent (OBJECT_TYPE_NAME,
                                       oldName,
                                       n);
                
    }
    
    public String getObjectTypeName ()
    {
        
        return this.getName ();
        
    }
    
    public String getObjectTypeNamePlural ()
    {
        
        return this.objectTypeNamePlural;
        
    }
    
    public void setObjectTypeNamePlural (String n)
    {

        String oldName = this.objectTypeNamePlural;
        
        this.objectTypeNamePlural = n;

        this.firePropertyChangedEvent (OBJECT_TYPE_NAME_PLURAL,
                                       oldName,
                                       n);
                
    }
    
    public String getObjectTypeId ()
    {
        
        if (this.getUserObjectType () != null)
        {
            
            return this.getUserObjectType ();
            
        } else {
            
            if (this.isAssetObjectType ())
            {
                
                return "asset:" + this.getKey ();
                
            } else {
                
                return this.getObjectReference ().asString ();
                
            }
            
        }
        
    }
    
    public void setAssetObjectType (boolean v)
    {
        
        this.isAsset = v;
        
    }
    
    public boolean isAssetObjectType ()
    {
        
        return this.isAsset;
     
    }
    
}