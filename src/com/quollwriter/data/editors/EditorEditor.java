package com.quollwriter.data.editors;

import java.io.*;
import java.util.*;

import javax.activation.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;

public class EditorEditor extends AbstractEditorObject
{

    public enum Status
    {
        
        online ("online"),
        offline ("offline"),
        busy ("busy");
        
        private String type = null;
        
        Status (String type)
        {
            
            this.type = type;
            
        }
        
        public String getType ()
        {
            
            return this.type;
            
        }
        
        public String getName ()
        {
            
            switch (this)
            {
                case online : return "Online";
                case offline : return "Offline";
                case busy : return "Busy";
                default : return "Unknown";
                
            }
            
        }
        
    }
    
    public enum EditorStatus
    {
        
        current ("current"),
        previous ("previous"),
        pending ("pending"),
        rejected ("rejected");
        
        private String type = null;
        
        EditorStatus (String type)
        {
            
            this.type = type;
            
        }
        
        public String getType ()
        {
            
            return this.type;
            
        }
        
    }

    public static final String OBJECT_TYPE = "editoreditor";
    
    public class XMLConstants
    {
        
        public static final String root = "editor";
        public static final String about = "about";
        public static final String genres = "genres";
        public static final String wordCountLengths = "wordCountLengths";
        
    }
    
    private String about = null;
    private Set<EditorProject.WordCountLength> wcLengths = null;
    private Set<String> genres = null;
    private Status status = null;
    private EditorStatus editorStatus = null;
    private List<ChatMessage> messages = null;
    private DataSource avatar = null;
    private String avatarImageFileType = null;
    
    public EditorEditor ()
    {
        
        super (OBJECT_TYPE);
        
    }

    public EditorEditor (String       name,
                         String       shortName,
                         DataSource   avatar,
                         Status       status,
                         EditorStatus editorStatus)
    {
        
        super (OBJECT_TYPE);
        
        this.name = name;
        //this.shortName = shortName;
        this.avatar = avatar;
        this.status = status;
        this.editorStatus = editorStatus;
        
    }
    
    public EditorEditor (Element root)
                         throws  Exception
    {
        
        super (root,
               OBJECT_TYPE);
        
        this.about = JDOMUtils.getChildElementContent (root,
                                                       XMLConstants.about,
                                                       true);
                
        String genres = JDOMUtils.getAttributeValue (root,
                                                     XMLConstants.genres,
                                                     false);
        
        if (!genres.equals (""))
        {
        
            this.genres = new TreeSet ();
        
            StringTokenizer t = new StringTokenizer (genres,
                                                     ",");
            
            while (t.hasMoreTokens ())
            {
                
                this.genres.add (t.nextToken ().trim ());
                
            }

        }
        
        String wcls = JDOMUtils.getAttributeValue (root,
                                                   XMLConstants.wordCountLengths,
                                                   false);
        
        if (!wcls.equals (""))
        {

            this.wcLengths = new TreeSet ();
            
            StringTokenizer t = new StringTokenizer (wcls,
                                                     ",");
            
            while (t.hasMoreTokens ())
            {
                
                this.wcLengths.add (EditorProject.WordCountLength.getWordCountLengthByType (t.nextToken ().trim ()));
                
            }
        
        }
                
    }

    public void setAvatarImageFileType (String t)
    {
        
        this.avatarImageFileType = t;
        
    }
    
    public String getAvatarImageFileType ()
    {
        
        return this.avatarImageFileType;
        
    }
    
    public String getShortName ()
    {
        
        return this.name;
        
    }
    
    public EditorStatus getEditorStatus ()
    {
        
        return this.editorStatus;
        
    }
    
    public Status getStatus ()
    {
        
        return this.status;
        
    }

    public void setMessages (List<ChatMessage> messages)
    {
        
        this.messages = messages;
        
    }
    
    public List<ChatMessage> getMessages ()
    {
        
        return this.messages;
        
    }
    
    public void setWordCountLengths (Set<EditorProject.WordCountLength> wcs)
    {
        
        this.wcLengths = wcs;
        
    }
    
    public Set<EditorProject.WordCountLength> getWordCountLengths ()
    {
        
        return this.wcLengths;
        
    }
    
    public Set<String> getGenres ()
    {
        
        return this.genres;
        
    }
        
    public void setGenres (Set<String> s)
    {
        
        this.genres = s;
        
    }
    
    public Element getAsJDOMElement ()
    {
        
        Element root = new Element (XMLConstants.root);
        
        this.fillJDOMElement (root);
                
        Element about = new Element (XMLConstants.about);
        about.addContent (this.about);

        root.addContent (about);
        
        if (this.genres != null)
        {
        
            String gs = Utils.toString (this.genres,
                                        ",");
        
            if (gs.length () > 0)
            {
                        
                root.setAttribute (XMLConstants.genres,
                                   gs);
                
            }
            
        }
        
        if (this.wcLengths != null)
        {
        
            String wcs = Utils.toString (this.wcLengths,
                                         ",");
        
            if (wcs.length () > 0)
            {
                        
                root.setAttribute (XMLConstants.wordCountLengths,
                                   wcs);
                
            }
            
        }

        return root;
        
    }
/*
    public String getAvatarImageFileType ()
    {
        
        return this.avatarImageFileType;
        
    }
  */

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
        
        return Environment.getEditorsEditorAvatarImageFile ();
        
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