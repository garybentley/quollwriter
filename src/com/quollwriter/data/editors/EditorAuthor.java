package com.quollwriter.data.editors;

import java.io.*;

import org.jdom.*;

import javax.activation.*;

import com.gentlyweb.xml.*;

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
    private DataSource avatar = null;
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
        
        this.about = JDOMUtils.getChildElementContent (root,
                                                       XMLConstants.about,
                                                       true);
                
    }
    
    public Element getAsJDOMElement ()
    {
        
        Element root = new Element (XMLConstants.root);
        
        this.fillJDOMElement (root);
                
        Element about = new Element (XMLConstants.about);
        about.addContent (this.about);

        root.addContent (about);
        
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
    
    public DataSource getAvatar ()
    {
        
        return this.avatar;
        
    }
  
    public void setAvatar (DataSource ds)
    {
        
        this.avatar = ds;
        
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