package com.quollwriter.data.editors;

import java.io.*;
import java.util.*;

import javafx.collections.*;
import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.image.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import org.bouncycastle.openpgp.*;
import org.bouncycastle.bcpg.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.messages.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

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

        public StringProperty nameProperty ()
        {

            switch (this)
            {
                case online : return getUILanguageStringProperty (editors,statuses, LanguageStrings.online);//"Online";
                case offline : return getUILanguageStringProperty (editors,statuses, LanguageStrings.offline);//"Offline";
                case busy : return getUILanguageStringProperty (editors,statuses, LanguageStrings.busy);//"Busy";
                case away : return getUILanguageStringProperty (editors,statuses, LanguageStrings.away);//"Away";
                case snooze : return getUILanguageStringProperty (editors,statuses, LanguageStrings.snooze);//"Snooze";
                default : return new SimpleStringProperty ("Unknown");

            }

        }

        public String getName ()
        {

            return this.nameProperty ().getValue ();

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

    private StringProperty emailProp = new SimpleStringProperty ();
    private String about = null;
    private Set<EditorProject.WordCountLength> wcLengths = null;
    private Set<String> genres = null;
    private ObjectProperty<OnlineStatus> statusProp = new SimpleObjectProperty ();
    private ObjectProperty<EditorStatus> editorStatusProp = new SimpleObjectProperty (EditorStatus.pending);
    private ObservableSet<EditorMessage> messages = FXCollections.observableSet (new LinkedHashSet<> ());
    private Image avatar = null;
    private String avatarImageFileType = null;
    private boolean invitedByMe = false;
    private PGPPublicKey theirPublicKey = null;
    private boolean messagesLoaded = false;
    private StringProperty myNameForEditorProp = new SimpleStringProperty ();
    private Image myAvatarForEditor = null;
    private String serviceName = null;
    private String messagingUsername = null;
    private StringProperty mainNameProp = new SimpleStringProperty ();
    private ObjectProperty<Image> mainAvatarProp = new SimpleObjectProperty ();

    public EditorEditor ()
    {

        super (OBJECT_TYPE);
        this.mainNameProp.bind (Bindings.createStringBinding (() ->
        {

            if (this.myNameForEditorProp.getValue () != null)
            {

                return this.myNameForEditorProp.getValue ();

            }

            if (this.getName () != null)
            {

                return this.getName ();

            }

            return this.emailProp.getValue ();

        },
        this.nameProperty (),
        this.emailProperty (),
        this.myNameForEditorProperty ()));

        this.nameProperty ().addListener ((pr, oldv, newv) -> {});
        this.emailProp.addListener ((pr, oldv, newv) -> {});
        this.myNameForEditorProp.addListener ((pr, oldv, newv) -> {});

    }

    public EditorEditor (String        name,
                         Image         avatar,
                         OnlineStatus  status,
                         EditorStatus  editorStatus)
    {

        this ();
        this.setName (name);

        this.avatar = avatar;
        this.setOnlineStatus (status);
        this.setEditorStatus (editorStatus);

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
                                    this.emailProp.getValue ());
        this.addToStringProperties (props,
                                    "onlineStatus",
                                    (this.statusProp.getValue () != null ? this.statusProp.getValue ().getType () : "unknown"));
        this.addToStringProperties (props,
                                    "editorStatus",
                                    this.editorStatusProp.getValue ().getType ());
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
                                    this.myNameForEditorProp.getValue ());
        this.addToStringProperties (props,
                                    "hasMyAvatarForEditor",
                                    (this.myAvatarForEditor != null));

    }

    public ObjectProperty<EditorStatus> editorStatusProperty ()
    {

        return this.editorStatusProp;

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

    public StringProperty myNameForEditorProperty ()
    {

        return this.myNameForEditorProp;

    }

    public String getMyNameForEditor ()
    {

        return this.myNameForEditorProp.getValue ();

    }

    public void setMyNameForEditor (String n)
    {

        this.myNameForEditorProp.setValue (n);

    }
    /*
    public void setMyPrivateKey (PGPPrivateKey k)
    {

        this.myPrivateKey = k;

    }
    */

    public Image getMainAvatar ()
    {

        return this.mainAvatarProp.getValue ();

    }
    /*
    public BufferedImage getMainAvatar ()
    {

        if (this.myAvatarForEditor != null)
        {

            return this.myAvatarForEditor;

        }

        return this.avatar;

    }
*/
    public String getMainName ()
    {

        return this.mainNameProp.getValue ();

    }

    public StringProperty mainNameProperty ()
    {

        return this.mainNameProp;

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

        return this.emailProp.getValue ();

    }

    public void setEmail (String em)
    {

        this.emailProp.setValue (em);

    }

    public StringProperty emailProperty ()
    {

        return this.emailProp;

    }

    public void setAvatarImageFileType (String t)
    {

        this.avatarImageFileType = t;

    }

    public String getAvatarImageFileType ()
    {

        return this.avatarImageFileType;

    }

    public void setEditorStatus (EditorStatus s)
    {

        this.editorStatusProp.setValue (s);

    }

    public EditorStatus getEditorStatus ()
    {

        return this.editorStatusProp.getValue ();

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

        this.statusProp.setValue (status);

    }

    public OnlineStatus getOnlineStatus ()
    {

        return this.statusProp.getValue ();

    }

    public ObjectProperty<OnlineStatus> onlineStatusProperty ()
    {

        return this.statusProp;

    }

    public boolean messagesLoaded ()
    {

        return this.messagesLoaded;

    }

    public void removeMessage (EditorMessage m)
    {

        if (this.messages == null)
        {

            return;

        }

        this.messages.remove (m);

    }

    public void setMessages (Set<EditorMessage> messages)
    {

        this.messages.clear ();

        if (messages != null)
        {

            this.messages.addAll (messages);

        }

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
            ||
            (forProject == null)
           )
        {

            return null;

        }

        return this.getMessage (type,
                                forProject.getId ());

    }

    public EditorMessage getMessage (String type,
                                     String forProjectId)
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

                if (m.getForProjectId ().equals (forProjectId))
                {

                    return m;

                }

            }

        }

        return null;

    }

    public ObservableSet<EditorMessage> getMessages ()
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

        if (this.editorStatusProp.getValue () == null)
        {

            return false;

        }

        return this.editorStatusProp.getValue () == EditorStatus.pending;

    }

    public boolean isPrevious ()
    {

        if (this.editorStatusProp.getValue () == null)
        {

            return false;

        }

        return this.editorStatusProp.getValue () == EditorStatus.previous;

    }

    public boolean isRejected ()
    {

        if (this.editorStatusProp.getValue () == null)
        {

            return false;

        }

        return this.editorStatusProp.getValue () == EditorStatus.rejected;

    }

    public boolean isCurrent ()
    {

        if (this.editorStatusProp.getValue () == null)
        {

            return false;

        }

        return this.editorStatusProp.getValue () == EditorStatus.current;

    }

    public boolean isOffline ()
    {

        if (this.statusProp.getValue () == null)
        {

            return true;

        }

        return this.statusProp.getValue () == OnlineStatus.offline;

    }

    public void setAvatar (Image im)
    {

        this.avatar = im;

        if (this.myAvatarForEditor == null)
        {

            this.mainAvatarProp.setValue (this.avatar);

        }

    }

    public ObjectProperty<Image> mainAvatarProperty ()
    {

        return this.mainAvatarProp;

    }

    public Image getAvatar ()
    {

        return this.avatar;

    }

    public void setMyAvatarForEditor (Image im)
    {

        this.myAvatarForEditor = im;

        if (this.myAvatarForEditor != null)
        {

            this.mainAvatarProp.setValue (this.myAvatarForEditor);

        }

    }

    public Image getMyAvatarForEditor ()
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
