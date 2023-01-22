package com.quollwriter.data.editors;

import java.util.*;

import org.dom4j.*;
import org.dom4j.tree.*;

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

    public AbstractEditorObject (String objType,
                                 String name)
    {

        super (objType,
               name);

    }

    public AbstractEditorObject (Element root,
                                 String  objType)
                                 throws  Exception
    {

        super (objType);

        this.setName (root.element (XMLConstants.name).getTextTrim ());

        try
        {

            this.setLastModified (new Date (Long.parseLong (root.attributeValue (XMLConstants.lastModified))));

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

    @Override
    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

    public Set<NamedObject> getAllNamedChildObjects (Class ofType)
    {

        return null;

    }

    @Override
    public Set<NamedObject> getAllNamedChildObjects ()
    {

        return null;

    }

    public void fillElement (Element root)
    {

        Element name = new DefaultElement (XMLConstants.name);
        name.add (new DefaultCDATA (this.getName ()));
        Element id = new DefaultElement (XMLConstants.id);
        id.add (new DefaultCDATA (this.getId ()));

        root.add (id);
        root.add (name);

        root.addAttribute (XMLConstants.lastModified,
                           "" + this.getLastModified ().getTime ());

    }

}
