package com.quollwriter.data.editors;

import java.io.*;

import org.dom4j.*;
import org.dom4j.tree.*;

import com.quollwriter.*;

public class EditorAuthor extends AbstractEditorObject
{

    public static final String OBJECT_TYPE = "editorauthor";

    public class XMLConstants
    {

        public static final String root = "author";
        public static final String about = "about";

    }

    private String about = null;
    private String avatarImageFileType = null;

    public EditorAuthor ()
    {

        super (OBJECT_TYPE);

    }

    public EditorAuthor (Element root)
                         throws  Exception
    {

        super (root,
               OBJECT_TYPE);

        this.about = DOM4JUtils.childElementContent (root,
                                                     XMLConstants.about);

    }

    public Element getAsElement ()
    {

        Element root = new DefaultElement (XMLConstants.root);

        this.fillElement (root);

        Element about = new DefaultElement (XMLConstants.about);
        about.add (new DefaultCDATA (this.about));

        root.add (about);

        return root;

    }

    public void setAvatarImageFileType (String t)
    {

        this.avatarImageFileType = t;

    }

    public String getAvatarImageFileType ()
    {

        return this.avatarImageFileType;

    }

  /*
    public File getAvatarImage ()
    {

        return Environment.getEditorsAuthorAvatarImageFile ();

    }
    */
    public String getAbout ()
    {

        return this.about;

    }

    public void setAbout (String a)
    {

        this.about = a;

    }

}
