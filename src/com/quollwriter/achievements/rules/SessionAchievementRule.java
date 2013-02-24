package com.quollwriter.achievements.rules;

import java.util.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import com.quollwriter.ui.*;

import com.quollwriter.data.*;

import com.quollwriter.*;

public class SessionAchievementRule extends AbstractAchievementRule
{
    
    public static final String RULE_TYPE = "session";
    
    public static final String EDIT = "edit";
    public static final String NO_EDIT = "noedit";
    
    public class XMLConstants
    {


        public static final String mins = "mins";
        public static final String wordCount = "wordCount";
        public static final String currentSession = "currentSession";
        public static final String count = "count";
        public static final String days = "days";
        public static final String action = "action";
        public static final String lastChecked = "lastChecked";

    }
    
    private int mins = -1;
    private int wordCount = 0;
    private int count = -1;
    private int days = 0;
    private String action = null;
    private boolean currentSession = false;
    
    private long lastChecked = 0;

    private List<SessionData> previousSessions = new ArrayList ();

    private SessionData currData = new SessionData ();
            
    private long lastEdit = -1;
    private long startEdit = -1;
    
    private Set<Long> editedChapters = new HashSet ();
        
    public SessionAchievementRule (Element root)
                                   throws  JDOMException
    {
        
        super (root);
        
        this.action = JDOMUtils.getAttributeValue (root,
                                                   XMLConstants.action,
                                                   false).toLowerCase ();
        
        this.currentSession = JDOMUtils.getAttributeValueAsBoolean (root,
                                                                    XMLConstants.currentSession,
                                                                    false);
        
        this.count = JDOMUtils.getAttributeValueAsInt (root,
                                                       XMLConstants.count,
                                                       false);

        this.wordCount = JDOMUtils.getAttributeValueAsInt (root,
                                                           XMLConstants.wordCount,
                                                           false);

        this.days = JDOMUtils.getAttributeValueAsInt (root,
                                                      XMLConstants.days,
                                                      false);

        this.mins = JDOMUtils.getAttributeValueAsInt (root,
                                                      XMLConstants.mins,
                                                      false);
                                
    }

    public String toString ()
    {
        
        return super.toString () + "(current session: " + this.currentSession + ", action: " + this.action + ", count: " + this.count + ", wordCount: " + this.wordCount + ", days: " + this.days + ", mins: " + this.mins + ", previous session: " + this.previousSessions + ")";
        
    }
            
    public boolean shouldPersistState ()
    {
        
        return !this.currentSession || this.lastChecked != 0;
                
    }

    public boolean achieved (AbstractProjectViewer viewer,
                             ProjectEvent          ev)
    {
        
        if (ev.getType ().equals (Project.OBJECT_TYPE))
        {
            
            if (ev.getAction ().equals (ProjectEvent.OPEN))
            {

                this.currData.start = System.currentTimeMillis ();

            }

            if (ev.getAction ().equals (ProjectEvent.CLOSE))
            {
                
                this.currData.end = System.currentTimeMillis ();
                
                this.currData.wordCount = viewer.getSessionWordCount ();

                this.lastChecked = System.currentTimeMillis ();
                
            }

        }

        long now = System.currentTimeMillis ();
               
        if (this.currentSession)
        {
            
            if ((ev.getType ().equals (Chapter.OBJECT_TYPE))
                &&
                (ev.getAction ().equals (ProjectEvent.EDIT))
               )
            {
                
                if (this.count > 0)
                {
                
                    Object o = ev.getContextObject ();
    
                    if (o != null)
                    {
    
                        if (o instanceof Chapter)
                        {
    
                            this.editedChapters.add (((Chapter) o).getKey ());
                            
                        }
        
                        if (this.editedChapters.size () >= this.count)
                        {
                            
                            return true;
                            
                        }
    
                    }

                }

                if (this.mins > 0)
                {
                    
                    if (this.lastEdit < 0)
                    {
                        
                        this.lastEdit = now;
                        this.startEdit = now;
                        
                        return false;
                        
                    } else {
                        
                        if ((now - this.lastEdit) > 300000)
                        {
                            
                            this.startEdit = now;
                                                    
                        } else {
                            
                            if ((now - this.startEdit) >= (this.mins * 60000))
                            {

                                return true;
                                
                            }
                            
                        }
                        
                        this.lastEdit = now;
                                    
                    }

                }
               
            }
            
        }
        
        if (ev.getType ().equals (Project.OBJECT_TYPE))
        {
            
            if (ev.getAction ().equals (ProjectEvent.OPEN))
            {

                if (this.action.equals (NO_EDIT))
                {
                    
                    if (viewer != null)
                    {
                        
                        if (this.lastChecked > 0)
                        {
                        
                            if (this.days > 0)
                            {
                            
                                long diff = now - this.lastChecked;
                                
                                long time = (long) 86400000 * (long) this.days;
                                
                                if (diff > time)
                                {
                                    
                                    return true;
                                    
                                }
                                
                            }
                            
                        } 
                                                    
                    }
                    
                }

                int sessSize = this.previousSessions.size ();
                
                if ((this.count > 0)               
                    &&
                    (sessSize < this.count)
                   )
                {
                    
                    return false;
                    
                }
                        
                // Check the days.
                if (this.days > 0)
                {
                        
                    long diff = this.previousSessions.get (sessSize - 1).end - this.previousSessions.get (0).start;
                    
                    int days = (int) (diff / (24 * 60 * 60 * 1000));

                    if (days != this.days)
                    {
                        
                        return false;
                        
                    }

                }                
                
                if (this.wordCount > 0)
                {
                    
                    // Check each session.
                    for (SessionData sd : this.previousSessions)
                    {
                        
                        if (sd.wordCount < this.wordCount)
                        {
                            
                            return false;
                            
                        }
                        
                    }
                    
                }
                
                return true;
                
            }
            
        }
        
        return false;
        
    }
    
    public boolean achieved (AbstractProjectViewer viewer)
    {
      
        if (this.currentSession)
        {
                        
            if (this.wordCount > 0)
            {
                
                if (viewer != null)
                {
                    
                    return viewer.getSessionWordCount () > this.wordCount;
                                        
                }
                
            }
            
        }
                        
        return false;
        
    }

    public void init (Element root)
                    
    {
                
        try
        {                
        
            List els = JDOMUtils.getChildElements (root,
                                                   SessionData.XMLConstants.root,
                                                   false);
            
            for (int i = 0; i < els.size (); i++)
            {
                
                Element el = (Element) els.get (i);
                
                this.previousSessions.add (new SessionData (el));
                    
            }

        } catch (Exception e) {
            
            // Ignore.
            Environment.logError ("Unable to init from: " +
                                  JDOMUtils.getPath (root),
                                  e);
            
        }
        
        try
        {
            
            String lc = JDOMUtils.getAttributeValue (root,
                                                     XMLConstants.lastChecked,
                                                     false);
            
            if (!lc.equals (""))
            {
            
                this.lastChecked = Long.parseLong (lc);
                    
            }

        } catch (Exception e) {
            
            // Ignore...
            
        }
        
    }
    
    public void fillState (Element root)
    {
        
        if (this.count > 0)
        {
            
            List<SessionData> sessions = new ArrayList (this.previousSessions);
            
            sessions.add (this.currData);
            
            for (SessionData sd : sessions)
            {
                
                root.addContent (sd.getAsElement ());
                
            }
            
        }
        
        if (this.lastChecked > 0)
        {
        
            root.setAttribute (XMLConstants.lastChecked,
                               String.valueOf (this.lastChecked));

        }

    }
     
    public class SessionData
    {
        
        public class XMLConstants
        {
            
            public static final String root = "session";
            public static final String start = "start";
            public static final String end = "end";
            public static final String wordCount = "wordCount";
            
        }
        
        public long start = 0;
        public long end = 0;
        public int wordCount = 0;

        public SessionData ()
        {
            
        }

        public SessionData (Element root)
                            throws  JDOMException
        {
            
            this.start = Long.parseLong (JDOMUtils.getAttributeValue (root,
                                                                      XMLConstants.start));
            this.end = Long.parseLong (JDOMUtils.getAttributeValue (root,
                                                                    XMLConstants.end));
            
            this.wordCount = JDOMUtils.getAttributeValueAsInt (root,
                                                               XMLConstants.wordCount);
            this.wordCount = 560;
        }
     
        public Element getAsElement ()
        {
            
            Element root = new Element (XMLConstants.root);
            
            root.setAttribute (XMLConstants.start,
                               String.valueOf (this.start));
            root.setAttribute (XMLConstants.end,
                               String.valueOf (this.end));
            root.setAttribute (XMLConstants.wordCount,
                               String.valueOf (this.wordCount));
            
            return root;
            
        }
 
        public String toString ()
        {
            
            return "start: " + new Date (this.start) + ", end: " + new Date (this.end) + ", word count: " + this.wordCount;
            
        }

    }
     
}