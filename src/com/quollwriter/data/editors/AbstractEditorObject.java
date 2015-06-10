package com.quollwriter.data.editors;

import java.util.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import com.quollwriter.data.*;

public abstract class AbstractEditorObject extends NamedObject
{

    public class XMLConstants
    {
        
        public static final String id = "id";
        public static final String name = "name";
        public static final String lastModified = "lastModified";
        
    }
    
    //private String id = null;

    public AbstractEditorObject (String objType)
    {
        
        super (objType);
        
    }
    
    public AbstractEditorObject (Element root,
                                 String  objType)
                                 throws  Exception
    {
        
        super (objType);
        
        this.setName (JDOMUtils.getChildElementContent (root,
                                                        XMLConstants.name,
                                                        true));
        
        try
        {
            
            this.lastModified = new Date (Long.parseLong (JDOMUtils.getAttributeValue (root,
                                                                                       XMLConstants.lastModified)));
            
        } catch (Exception e) {
                        
        }
        
    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);
/*
        this.addToStringProperties (props,
                                    "id",
                                    this.id);
  */                      
    }    
    
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public Set<NamedObject> getAllNamedChildObjects (Class ofType)
    {

        return null;
    
    }

    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return null;
    
    }
    
    public void fillJDOMElement (Element root)
    {
        
        Element name = new Element (XMLConstants.name);
        name.addContent (this.getName ());
        Element id = new Element (XMLConstants.id);
        id.addContent (this.getId ());
        
        root.addContent (id);
        root.addContent (name);
        
        root.setAttribute (XMLConstants.lastModified,
                           "" + this.lastModified.getTime ());
        
    }
            
}