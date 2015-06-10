package com.quollwriter.data.editors;

import java.io.*;
import java.util.*;
import java.awt.image.*;

import javax.activation.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import org.bouncycastle.openpgp.*;
import org.bouncycastle.bcpg.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.messages.*;

public class EditorEditor extends AbstractEditorObject
{

    public enum OnlineStatus
    {
        
        online ("online"),
        busy ("busy"),
        offline ("offline"),
        away ("away"),
        snooze ("snooze");
        
        private String type = null;
        
        OnlineStatus (String type)
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
                case away : return "Away";
                case snooze : return "Snooze";
                default : return "Unknown";
                
            }
            
        }
        
    }
    
    public enum EditorStatus
    {
        
        current ("current"),
        previous ("previous"),
        pending ("pending"),
        pending_inviteaccepted ("pending_inviteaccepted"),
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

    public static final String OBJECT_TYPE = "editor";
    
    public class XMLConstants
    {
        
        public static final String root = "editor";
        public static final String about = "about";
        public static final String genres = "genres";
        public static final String wordCountLengths = "wordCountLengths";
        
    }
    
    private String email = null;
    private String about = null;
    private Set<EditorProject.WordCountLength> wcLengths = null;
    private Set<String> genres = null;
    private OnlineStatus status = null;
    private EditorStatus editorStatus = EditorStatus.pending;
    private Set<EditorMessage> messages = null;
    //private DataSource avatar = null;
    private BufferedImage avatar = null;
    private String avatarImageFileType = null;
    private boolean invitedByMe = false;
    private PGPPublicKey theirPublicKey = null;
    private boolean messagesLoaded = false;
    private String myNameForEditor = null;
    private BufferedImage myAvatarForEditor = null;
    private String serviceName = null;
    private String messagingUsername = null;
    
    public EditorEditor ()
    {
        
        super (OBJECT_TYPE);
        
    }

    public EditorEditor (String        name,
                         String        shortName,
                         BufferedImage avatar,
                         OnlineStatus  status,
                         EditorStatus  editorStatus)
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
        
    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "email",
                                    this.email);
        this.addToStringProperties (props,
                                    "onlineStatus",
                                    (this.status != null ? this.status.getType () : "unknown"));
        this.addToStringProperties (props,
                                    "editorStatus",
                                    this.editorStatus.getType ());
        this.addToStringProperties (props,
                                    "serviceName",
                                    this.serviceName);
        this.addToStringProperties (props,
                                    "messagingUsername",
                                    this.messagingUsername);
        this.addToStringProperties (props,
                                    "hasPublicKey",
                                    (this.theirPublicKey != null));
        this.addToStringProperties (props,
                                    "invitedByMe",
                                    this.invitedByMe);
        this.addToStringProperties (props,
                                    "messages",
                                    (this.messages != null ? this.messages.size () : 0));
        this.addToStringProperties (props,
                                    "myNameForEditor",
                                    this.myNameForEditor);
        this.addToStringProperties (props,
                                    "hasMyAvatarForEditor",
                                    (this.myAvatarForEditor != null));
                        
    }    
    
    public String getMessagingUsername ()
    {
        
        return this.messagingUsername;
        
    }
    
    public void setMessagingUsername (String u)
    {
        
        this.messagingUsername = u;
        
    }
    
    public String getServiceName ()
    {
        
        return this.serviceName;
        
    }
    
    public void setServiceName (String n)
    {
        
        this.serviceName = n;
        
    }
    
    public String getMyNameForEditor ()
    {
        
        return this.myNameForEditor;
        
    }
    
    public void setMyNameForEditor (String n)
    {
        
        this.myNameForEditor = n;
        
    }
    /*
    public void setMyPrivateKey (PGPPrivateKey k)
    {
        
        this.myPrivateKey = k;
        
    }
    */
    public BufferedImage getMainAvatar ()
    {
        
        if (this.myAvatarForEditor != null)
        {
            
            return this.myAvatarForEditor;
            
        }
        
        return this.avatar;
        
    }
    
    public String getMainName ()
    {
        
        if (this.myNameForEditor != null)
        {
            
            return this.myNameForEditor;
            
        }
        
        String n = super.getName ();
        
        if (n != null)
        {
            
            return n;
            
        }
        
        return this.email;
        
    }
    
    public PGPPublicKey getTheirPublicKey ()
    {
        
        return this.theirPublicKey;
        
    }
    
    public void setTheirPublicKey (PGPPublicKey k)
    {
        
        this.theirPublicKey = k;
        
    }

    public boolean isInvitedByMe ()
    {
        
        return this.invitedByMe;
        
    }
    
    public void setInvitedByMe (boolean v)
    {
        
        this.invitedByMe = v;
        
    }
    
    public String getEmail ()
    {
        
        return this.email;
        
    }
    
    public void setEmail (String em)
    {
        
        this.email = em;
        
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
        
        if (this.getMyNameForEditor () != null)
        {
            
            return this.getMyNameForEditor ();
            
        }
        
        return this.getMainName ();
        
    }
    
    public void setEditorStatus (EditorStatus s)
    {
        
        this.editorStatus = s;
        
    }
    
    public EditorStatus getEditorStatus ()
    {
        
        return this.editorStatus;
        
    }
    
    /**
     * Do we have an editor-info message sent by the editor?
     * We can't assume that the messages have been loaded here.
     *
     * @return true If the info has been sent/recevied.
     */
    public boolean hasSentInfo ()
    {
        
        if (this.messages == null)
        {
            
            // This is kind of a false positive since we aren't querying the live db.
            return false;
            
        }
        
        for (EditorMessage m : this.messages)
        {
            
            if ((m instanceof EditorInfoMessage)
                &&
                (!m.isSentByMe ())
               )
            {
                
                // It doesn't matter how many times it has been sent.
                return true;
                
            }
            
        }
        
        return false;
        
    }
    
    public void setOnlineStatus (OnlineStatus status)
    {
        
        this.status = status;
        
    }
    
    public OnlineStatus getOnlineStatus ()
    {
        
        return this.status;
        
    }

    public boolean messagesLoaded ()
    {
        
        return this.messagesLoaded;
        
    }
    
    public void setMessages (Set<EditorMessage> messages)
    {
        
        this.messages = messages;

        this.messagesLoaded = (messages != null);
        
    }
    
    public void addMessage (EditorMessage m)
    {
        
        if (this.messagesLoaded)
        {
            
            this.messages.add (m);
            
        }
        
    }
    
    /**
     * Get all the messages for the specified accept types (message types).
     *
     * @param acceptTypes The types of messages we are looking for.
     * @param projId Limit to the specified project, if provided.
     * @return The matched messages in date created order, or null if no messages match.
     */
    public SortedSet<EditorMessage> getMessages (String    projId,
                                                 String... acceptTypes)
    {
        
        Set<EditorMessage> mess = this.getMessages (new DefaultEditorMessageFilter (projId,
                                                                                    acceptTypes));
        
        if (mess == null)
        {
            
            return null;
            
        }
        
        SortedSet ss = new TreeSet (new Comparator<EditorMessage> ()
        {
           
            @Override
            public int compare (EditorMessage m1,
                                EditorMessage m2)
            {
            
                return m1.getWhen ().compareTo (m2.getWhen ());
             
            }
            
            @Override
            public boolean equals (Object o)
            {
                
                return super.equals (o);
                
            }
           
        });
        
        ss.addAll (mess);
    
        return ss;
    
        
    }
    
    public Set<EditorMessage> getMessages (EditorMessageFilter filter)
    {
        
        Set<EditorMessage> ret = new LinkedHashSet ();
        
        if ((this.messages == null)
            ||
            (!this.messagesLoaded)
           )
        {
            
            return ret;
            
        }
        
        for (EditorMessage m : this.messages)
        {
            
            if (filter.accept (m))
            {
                
                ret.add (m);
                
            }
            
        }

        return ret;
        
    }
    
    public EditorMessage getMessage (String  type,
                                     Project forProject)
    {
        
        if ((this.messages == null)
            ||
            (!this.messagesLoaded)
           )
        {
            
            return null;
            
        }
        
        for (EditorMessage m : this.messages)
        {
            
            if (m.getMessageType ().equals (type))
            {
                
                if (m.getForProjectId ().equals (forProject.getId ()))
                {
                    
                    return m;
                    
                }
                
            }
            
        }
        
        return null;
        
    }
    
    public Set<EditorMessage> getMessages ()
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

    public boolean isPending ()
    {
        
        if (this.editorStatus == null)
        {
            
            return false;
            
        }
        
        return this.editorStatus == EditorStatus.pending;
        
    }
  
    public boolean isPrevious ()
    {
        
        if (this.editorStatus == null)
        {
            
            return false;
            
        }
        
        return this.editorStatus == EditorStatus.previous;
        
    }

    public boolean isRejected ()
    {
        
        if (this.editorStatus == null)
        {
            
            return false;
            
        }
        
        return this.editorStatus == EditorStatus.rejected;
        
    }
  
    public boolean isCurrent ()
    {
        
        if (this.editorStatus == null)
        {
            
            return false;
            
        }
        
        return this.editorStatus == EditorStatus.current;
        
    }
    
    public boolean isOffline ()
    {
        
        if (this.status == null)
        {
            
            return true;
            
        }
        
        return this.status == OnlineStatus.offline;
        
    }
    
/*
    public DataSource getAvatar ()
    {
        
        return this.avatar;
        
    }
  
    public void setAvatar (DataSource ds)
    {
        
        this.avatar = ds;
        
    }
    */

    public BufferedImage getDisplayAvatar ()
    {
        
        return (this.myAvatarForEditor != null ? this.myAvatarForEditor : this.avatar);
        
    }
    
    public void setAvatar (BufferedImage im)
    {
        
        this.avatar = im;
        
    }
    
    public BufferedImage getAvatar ()
    {
                
        return this.avatar;
        
    }
    
    public void setMyAvatarForEditor (BufferedImage im)
    {
        
        this.myAvatarForEditor = im;
        
    }
    
    public BufferedImage getMyAvatarForEditor ()
    {
        
        return this.myAvatarForEditor;
        
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