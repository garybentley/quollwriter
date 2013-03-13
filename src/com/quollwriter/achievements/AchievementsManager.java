package com.quollwriter.achievements;

import java.util.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.event.*;

import javax.sound.sampled.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;

import com.quollwriter.achievements.rules.*;
import com.quollwriter.ui.components.ActionAdapter;

public class AchievementsManager implements ProjectEventListener
{
    
    public static final String USER = "user";
    public static final String PROJECT = "project";
    public static final String CHAPTER = "chapter";
    
    public class XMLConstants
    {
        
        public static final String item = "item";
        public static final String id = "id";
        public static final String category = "category";
        public static final String state = "state";
        
    }
    
    // The key here is the AchievementRule.getEventId
    private Map<String, Set<AchievementRule>> userRules = new HashMap ();
    
    // The inner map key is the AchievementRule.getEventId.
    private Map<AbstractProjectViewer, Map<String, Set<AchievementRule>>> eventRules = new HashMap ();
    private Map<AbstractProjectViewer, AchievementsChecker> checkers = new HashMap ();
    
    // The key is the id attribute of the element.
    private Map<String, Element> ruleEls = new LinkedHashMap ();
        
    private Timer checker = new Timer (true);
        
    private Clip achievementSound = null;
        
    private Set<AchievementReachedListener> listeners = new HashSet ();
        
    private boolean soundRunning = false;
        
    public AchievementsManager ()
                                throws Exception
    {
        
        // Load our list of user achieved achievements.
        Set<String> achieved = this.getAchievedIds (Environment.getProperty (Constants.USER_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME));      
                        
        // Load the state for the user achievements.
        Map<String, Element> initEls = this.getInitElements (Environment.getProperty (Constants.USER_ACHIEVEMENTS_STATE_PROPERTY_NAME));
                        
        // Create the achievements for the project and chapter categories.
        String achFile = Environment.getResourceFileAsString (Constants.ACHIEVEMENTS_FILE);
        
        if (achFile == null)
        {
            
            return;
            
        }
        
        Element root = JDOMUtils.getStringAsElement (achFile);
        
        List els = JDOMUtils.getChildElements (root,
                                               XMLConstants.item,
                                               true);
        
        Set<AchievementRule> userSessionRules = new HashSet ();
        
        // Get the user ones.
        for (int i = 0; i < els.size (); i++)
        {
            
            Element el = (Element) els.get (i);
            
            String id = JDOMUtils.getAttributeValue (el,
                                                     XMLConstants.id,
                                                     true).toLowerCase ().trim ();
            
            if (JDOMUtils.getAttributeValue (el,
                                             XMLConstants.category).equals (USER))
            {
                
                // Have we achieved this one.
                if (achieved.contains (id))
                {
                    
                    continue;
                    
                }
                                
                // Load the rule.
                AchievementRule ar = AchievementRuleFactory.createRule (el);

                if (ar == null)
                {
                    
                    throw new JDOMException ("Unable to create rule from element: " +
                                             JDOMUtils.getPath (el));
                    
                }

                // Init.
                Element initEl = initEls.get (ar.getId ());
                
                if (initEl != null)
                {
                    
                    ar.init (initEl);
                    
                }
                
                if (ar.isEventTrigger ())
                {
                    
                    for (String eid : ar.getEventIds ())
                    {
                    
                        Set<AchievementRule> rs = this.userRules.get (eid);
                        
                        if (rs == null)
                        {
                            
                            rs = new LinkedHashSet ();
                            
                            this.userRules.put (eid,
                                                rs);
                            
                        }
                        
                        rs.add (ar);
    
                    }

                } else {
                    
                    userSessionRules.add (ar);
                                
                }
                
            } else {
                
                this.ruleEls.put (id,
                                  el);
                
            }
            
        }
        
        if (userSessionRules.size () > 0)
        {
            
            AchievementsChecker ac = new AchievementsChecker (null,
                                                              userSessionRules,
                                                              this);
            
            this.checker.schedule (ac,
                                   10 * 1000,
                                   10 * 1000);
            
            this.checkers.put (null,
                               ac);            
            
        }
        
    }
    
    public void removeAchievementReachedListener (AchievementReachedListener l)
    {
        
        this.listeners.remove (l);
        
    }
    
    public void addAchievementReachedListener (AchievementReachedListener l)
    {
        
        this.listeners.add (l);        
        
    }
    
    protected void fireAchievementReachedEvent (AchievementRule ar)
    {
        
        AchievementReachedEvent ev = new AchievementReachedEvent (ar);
        
        for (AchievementReachedListener l : this.listeners)
        {
            
            l.achievementReached (ev);            
            
        }
        
    }
    
    public Set<String> getUserAchievedIds ()
    {
        
        return this.getAchievedIds (Environment.getProperty (Constants.USER_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME));
        
    }

    private Set<AchievementRule> getRules (String  type,
                                           boolean includeHidden)
                                           throws  Exception
    {

        // Create the achievements for the project and chapter categories.
        String achFile = Environment.getResourceFileAsString (Constants.ACHIEVEMENTS_FILE);
        
        if (achFile == null)
        {
            
            return null;
            
        }

        Element root = null;
        
        try
        {
            
            root = JDOMUtils.getStringAsElement (achFile);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to convert file: " +
                                  achFile +
                                  " to an element",
                                  e);
            
            return null;
                
        }
        
        Set<AchievementRule> rules = new HashSet ();

        List els = JDOMUtils.getChildElements (root,
                                               XMLConstants.item,
                                               true);
        
        // Get the user ones.
        for (int i = 0; i < els.size (); i++)
        {
            
            Element el = (Element) els.get (i);
            
            try
            {
            
                String id = JDOMUtils.getAttributeValue (el,
                                                         XMLConstants.id,
                                                         true).toLowerCase ().trim ();
                
                if (JDOMUtils.getAttributeValue (el,
                                                 XMLConstants.category).equals (type))
                {
                    
                    AchievementRule ar = AchievementRuleFactory.createRule (el);
                    
                    if ((!includeHidden)
                        &&
                        (ar.isHidden ())
                       )
                    {
                        
                        continue;
                        
                    }
                    
                    rules.add (ar);
                                                    
                }

            } catch (Exception e) {
                
                Environment.logError ("Unable to convert element: " +
                                      JDOMUtils.getPath (el) +
                                      " to a rule",
                                      e);                
                
            }
            
        }
                
        return rules;        
        
    }
    
    public Set<AchievementRule> getUserRules ()
    {
        
        try
        {
        
            return this.getRules (USER,
                                  true);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to get user rules",
                                  e);
            
            return null;
            
        }
        
    }
    
    public Set<AchievementRule> getPerProjectRules ()
    {

        try
        {

            return this.getRules (PROJECT,
                                  true);
            
        } catch (Exception e) {
            
            Environment.logError ("Unable to get project rules",
                                  e);
            
            return null;
            
        }
        
    }

    
    private Set<String> getAchievedIds (String v)
    {
        
        Set<String> achievedM = new HashSet ();

        if (v != null)
        {
            
            // Split on ,
            StringTokenizer t = new StringTokenizer (v,
                                                     ",");
            
            while (t.hasMoreTokens ())
            {
                
                achievedM.add (t.nextToken ().toLowerCase ().trim ());
                
            }
            
        }
        
        return achievedM;
        
    }
    
    private String getState (Set<AchievementRule> rules)
                             throws               Exception
    {
        
        Element root = new Element (XMLConstants.state);
        
        // Now persist any user rules.
        for (AchievementRule ar : rules)
        {
            
            if (ar.shouldPersistState ())
            {
                
                Element el = new Element (XMLConstants.item);
                
                el.setAttribute (XMLConstants.id,
                                 ar.getId ());
                ar.fillState (el);
                
                root.addContent (el);
                
            }
            
        }

        return JDOMUtils.getElementAsString (root);
        
    }
    
    public void stop ()
                      throws Exception
    {
        
        Set<AbstractProjectViewer> viewers = new HashSet (this.eventRules.keySet ());
        
        for (AbstractProjectViewer pv : viewers)
        {
            
            this.removeProjectViewer (pv);
            
        }
        
        Environment.setUserProperty (Constants.USER_ACHIEVEMENTS_STATE_PROPERTY_NAME,
                                     this.getState (new HashSet (this.userRules.values ())));                                     
        
    }
    
    public Map<String, Set<String>> getAchievedAchievementIds (AbstractProjectViewer viewer)
    {
        
        Map<String, Set<String>> achieved = new HashMap ();
        
        achieved.put ("user",
                      this.getAchievedIds (Environment.getProperty (Constants.USER_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME)));
        
        if (viewer != null)
        {
            
            achieved.put ("project",
                          this.getAchievedIds (viewer.getProject ().getProperty (Constants.PROJECT_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME)));

        }
        
        return achieved;

    }
    
    private Map<String, Element> getInitElements (String v)
                                                  throws Exception
    {

        Map<String, Element> initEls = new HashMap ();

        if (v != null)
        {
            
            try
            {
            
                Element root = JDOMUtils.getStringAsElement (v);
                
                List els = JDOMUtils.getChildElements (root,
                                                       XMLConstants.item,
                                                       false);
                
                for (int i = 0; i < els.size (); i++)
                {
                    
                    Element el = (Element) els.get (i);
                    
                    initEls.put (JDOMUtils.getAttributeValue (el,
                                                              XMLConstants.id,
                                                              true),
                                 el);
                    
                }

            } catch (Exception e) {
                
                Environment.logError ("Unable to get achievement init elements for string: " +
                                      v,
                                      e);
                
            }
            
        }
        
        return initEls;        
                
    }

    public void removeProjectViewer (AbstractProjectViewer v)
                                     throws                Exception
    {
        
        if (v == null)
        {
            
            return;
            
        }
        
        Map<String, Set<AchievementRule>> rules = this.eventRules.get (v);
        
        if (rules == null)
        {
            
            // Already removed.
            return;
            
        }

        Set<AchievementRule> rs = new HashSet ();
        
        if (rules != null)
        {

            for (String id : rules.keySet ())
            {
                
                rs.addAll (rules.get (id));
                
            }

        }
           
        this.eventRules.remove (v);

        // Check to see if we meet any of the achievements.
        AchievementsChecker t = this.checkers.get (v);
        
        if (t != null)
        {
        
            // Get the rules.
            rs.addAll (t.getRules ());

        }

        // Save the state.
        v.getProject ().setProperty (Constants.PROJECT_ACHIEVEMENTS_STATE_PROPERTY_NAME,
                                     this.getState (rs));
        
        v.saveProject ();
     
        if (t != null)
        {
            
            t.cancel ();
            
        }
        
        this.checkers.remove (v);
        
    }
    
    public void addProjectViewer (AbstractProjectViewer v)
                                  throws                Exception
    {
        
        // Get the list of project/chapter achieved achievements.
        Set<String> achieved = this.getAchievedIds (v.getProject ().getProperty (Constants.PROJECT_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME));

        // Load the state for the achievements and init.
        Map<String, Element> initEls = this.getInitElements (v.getProject ().getProperty (Constants.PROJECT_ACHIEVEMENTS_STATE_PROPERTY_NAME));
        
        // The key is the event id.
        Map<String, Set<AchievementRule>> evRules = new HashMap ();
                
        Set<AchievementRule> constantRules = new LinkedHashSet ();       
        
        // Load the project/chapter achievements (not in the list above).
        for (String id : this.ruleEls.keySet ())
        {
                        
            if (achieved.contains (id))
            {
                
                continue;
                
            }

            Element el = this.ruleEls.get (id);
            
            // Load the rule.
            AchievementRule ar = AchievementRuleFactory.createRule (el);

            if (ar == null)
            {
                
                throw new JDOMException ("Unable to create rule from element: " +
                                         JDOMUtils.getPath (el));
                
            }

            // Init.
            Element initEl = initEls.get (ar.getId ());
            
            if (initEl != null)
            {
                
                ar.init (initEl);
                
            }
            
            if (ar.isEventTrigger ())
            {

                // Get the event ids, there can be more than one for example
                // if we are looking for new and/or edit events for an object type.
                Set<String> eventIds = ar.getEventIds ();
                
                for (String eid : eventIds)
                {

                    Set<AchievementRule> rules = evRules.get (eid);
                    
                    if (rules == null)
                    {
                    
                        rules = new LinkedHashSet ();
                        
                        evRules.put (eid,
                                     rules);
                        
                    }
                
                    rules.add (ar);

                }
                            
            } else {
                
                constantRules.add (ar);                
        
            }
            
        }
        
        this.eventRules.put (v,
                             evRules);
        
        if (constantRules.size () > 0)
        {
        
            // Create the achievements checker and schedule.
            AchievementsChecker ac = new AchievementsChecker (v,
                                                              constantRules,
                                                              this);
            
            this.checker.schedule (ac,
                                   10 * 1000,
                                   10 * 1000);
            
            this.checkers.put (v,
                               ac);

        }
        
        v.addProjectEventListener (this);        

    }
     
    private void logAchievementReached ()
    {
        
     
    }
    
    public void achievementReached (AbstractProjectViewer viewer,
                                    AchievementRule       ar)
    {

        if (Environment.debugMode)
        {
            
            Environment.logMessage ("Achievement reached: " + ar + ", enabled: " + this.isAchievementsEnabled ());
            
        }

        if (!this.isAchievementsEnabled ())
        {
            
            return;            
            
        }
              
        try
        {                

            // Add to the list of project achievements.
            Set<String> achieved = this.getAchievedIds (viewer.getProject ().getProperty (Constants.PROJECT_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME));
    
            achieved.add (ar.getId ());
            
            viewer.getProject ().getProperties ().setProperty (Constants.PROJECT_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME,
                                                               new StringProperty (Constants.PROJECT_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME,
                                                                                   this.getAsString (achieved)));
            
            viewer.saveProject ();
                
            viewer.showAchievement (ar);

            this.fireAchievementReachedEvent (ar);

            this.playAchievementSound ();

            viewer.createActionLogEntry (viewer.getProject (),
                                         "Achievement: " + ar.getId () + "[" + ar.getName () + "] reached.");            

        } catch (Exception e) {
            
            Environment.logError ("Unable to set achievement achieved: " +
                                  ar,
                                  e);            
                        
        }
        
    }

    private void removeRule (AchievementRule       ar,
                             AbstractProjectViewer viewer)
    {
        
        Map<String, Set<AchievementRule>> rules = this.eventRules.get (viewer);
        
        Set<String> eventIds = ar.getEventIds ();
    
        for (String eid : eventIds)
        {
        
            Set<AchievementRule> rs = rules.get (eid);
            
            rs.remove (ar);

        }
        
    }

    public void userAchievementReached (AchievementRule ar)
    {

        if (Environment.debugMode)
        {
            
            Environment.logMessage ("User achievement reached: " + ar + ", enabled: " + this.isAchievementsEnabled ());
            
        }

        if (!this.isAchievementsEnabled ())
        {
            
            return;            
            
        }

        try
        {                        
        
            // Add to the list of user achievements.
            Set<String> achieved = this.getAchievedIds (Environment.getProperty (Constants.USER_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME));
            
            achieved.add (ar.getId ());
            
            Environment.setUserProperty (Constants.USER_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME,
                                         this.getAsString (achieved));

            this.fireAchievementReachedEvent (ar);
                        
            Environment.showAchievement (ar);

            this.playAchievementSound ();

        } catch (Exception e) {
            
            // Log the error.
            Environment.logError ("Unable to set user achievement as reached: " +
                                  ar,
                                  e);
                        
        }
        
    }

    public void setAchievementsEnabled (boolean v)
    {
        
        try
        {

            Environment.setUserProperty (Constants.ACHIEVEMENTS_ENABLED_PROPERTY_NAME,
                                         new BooleanProperty (Constants.ACHIEVEMENTS_ENABLED_PROPERTY_NAME,
                                                              v));

        } catch (Exception e) {
            
            Environment.logError ("Unable to set achievements enabled property",
                                  e);
                            
        }        
        
    }

    public boolean isAchievementsEnabled ()
    {
        
        return Environment.getUserProperties ().getPropertyAsBoolean (Constants.ACHIEVEMENTS_ENABLED_PROPERTY_NAME);
        
    }

    public boolean isSoundsInFullScreenEnabled ()
    {
        
        return Environment.getUserProperties ().getPropertyAsBoolean (Constants.ACHIEVEMENTS_SOUND_IN_FULL_SCREEN_ENABLED_PROPERTY_NAME);
        
    }

    public void setSoundsInFullScreenEnabled (boolean v)
    {
        
        try
        {
        
            Environment.setUserProperty (Constants.ACHIEVEMENTS_SOUND_IN_FULL_SCREEN_ENABLED_PROPERTY_NAME,
                                         new BooleanProperty (Constants.ACHIEVEMENTS_SOUND_IN_FULL_SCREEN_ENABLED_PROPERTY_NAME,
                                                              v));

        } catch (Exception e) {
            
            Environment.logError ("Unable to set achievements sound in full screen property",
                                  e);
                            
        }        
        
    }

    public void setSoundEnabled (boolean v)
    {
        
        try
        {
        
            Environment.setUserProperty (Constants.ACHIEVEMENTS_SOUND_ENABLED_PROPERTY_NAME,
                                         new BooleanProperty (Constants.ACHIEVEMENTS_SOUND_ENABLED_PROPERTY_NAME,
                                                              v));

        } catch (Exception e) {
            
            Environment.logError ("Unable to set achievements sound property",
                                  e);
                            
        }        
        
    }

    public boolean isSoundEnabled ()
    {
        
        return Environment.getUserProperties ().getPropertyAsBoolean (Constants.ACHIEVEMENTS_SOUND_ENABLED_PROPERTY_NAME);
        
    }

    public void playAchievementSound ()
    {

        if (!this.isSoundEnabled ())
        {
            
            return;
            
        }

        if ((Environment.isInFullScreen ())
            &&
            (!this.isSoundsInFullScreenEnabled ())
           )
        {
            
            return;
            
        }

        if (this.achievementSound == null)
        {

            try
            {
    
                InputStream is = new BufferedInputStream (Environment.getResourceStream (Constants.DEFAULT_ACHIEVEMENT_SOUND_FILE));
            
                // Get the clip.
                AudioInputStream ais = AudioSystem.getAudioInputStream (is);
    
                this.achievementSound = AudioSystem.getClip ();
    
                this.achievementSound.open (ais);
    
            } catch (Exception e)
            {
    
                Environment.logError ("Unable to get sound file to play on achievement",
                                      e);
    
                return;
    
            }

        }

        if (this.soundRunning)
        {
            
            return;
            
        }

        this.soundRunning = true;

        final AchievementsManager _this = this;

        javax.swing.Timer t = new javax.swing.Timer (1000,
        new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                // Play after 1s.
                try
                {
                
                    _this.achievementSound.setFramePosition (0);
        
                    _this.achievementSound.start ();
        
                    _this.soundRunning = false;
        
                } catch (Exception e)
                {
        
                    Environment.logError ("Unable to play achievement sound",
                                          e);
        
                }        

            }

        });
        
        t.setRepeats (false);
        
        t.start ();
        
    }

    private String getAsString (Set<String> ids)
    {
        
        StringBuilder b = new StringBuilder ();
        
        for (String id : ids)
        {
            
            if (b.length () > 0)
            {
                
                b.append (",");
                
            }
            
            b.append (id);
            
        }
        
        return b.toString ();
        
    }
 
    public void eventOccurred (ProjectEvent ev)
    {
                
        // Get our matching user rules for the event.
        
        if (Environment.debugMode)
        {
            
            Environment.logMessage ("Event occurred: " + ev);
            
        }        
        
        long start = System.currentTimeMillis ();
        
        Set<AchievementRule> rules = this.userRules.get (ev.getEventId ());

        if (rules != null)
        {
            
            Set<AchievementRule> achieved = new HashSet ();
            
            for (AchievementRule ar : rules)
            {
                
                try
                {
                
                    if (ar.achieved (null,
                                     ev))
                    {
                        
                        // Just pick a viewer and display there.
                        this.userAchievementReached (ar);
                        
                        achieved.add (ar);
                        
                    }
    
                } catch (Exception e) {
                    
                    Environment.logError ("Unable to check for achievement: " +
                                          ar,
                                          e);
                    
                }
                
            }

            for (AchievementRule ar : achieved)
            {

                for (String eid : ar.getEventIds ())
                {
                
                    this.userRules.remove (eid);
                                
                }

            }

        }
                
        for (AbstractProjectViewer pv : this.eventRules.keySet ())
        {
            
            Map<String, Set<AchievementRule>> evRules = this.eventRules.get (pv);
            
            Set<AchievementRule> rs = evRules.get (ev.getEventId ());
            
            if (rs != null)
            {

                Set<AchievementRule> achieved = new HashSet ();
                
                for (AchievementRule ar : rs)
                {
                
                    try
                    {
                    
                        if (ar.achieved (pv,
                                         ev))
                        {
                            
                            this.achievementReached (pv,
                                                     ar);
                            
                            achieved.add (ar);
                            
                        }

                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to check for achievement: " +
                                              ar,
                                              e);
                        
                    }
                    
                }

                for (AchievementRule ar : achieved)
                {
                    
                    this.removeRule (ar,
                                     pv);
                    
                }
                
            }
                     
        }
        
        if ((System.currentTimeMillis () - start) > 500)
        {
            
            Environment.logMessage ("Warning! Achievements event processing took: " + (System.currentTimeMillis () - start));
            
        }
        
    }
    
    public class AchievementsChecker extends TimerTask
    {
        
        public AbstractProjectViewer viewer = null;
        private boolean running = false;
        public AchievementsManager   manager = null;
        public Set<AchievementRule> rules = null;
        private int count = 0;
        public AchievementsChecker (AbstractProjectViewer viewer,
                                    Set<AchievementRule>  rules,
                                    AchievementsManager   manager)
        {
            
            this.viewer = viewer;
            this.rules = rules;
            this.manager = manager;
            
        }
        
        public Set<AchievementRule> getRules ()
        {
            
            return this.rules;
            
        }
        
        public boolean isRunning ()
        {
            
            return this.running;
            
        }
        
        public void run ()
        {
            
            Thread.currentThread ().setPriority (Thread.MIN_PRIORITY);
            
            try{
            this.count++;

            if (this.running)
            {
                
                return;
                
            }
            
            this.running = true;
            
            if (rules.size () == 0)
            {
                
                try
                {
                
                    manager.removeProjectViewer (this.viewer);
                    
                } catch (Exception e) {
                    
                    // Ignore.
                    
                }

                this.running = false;
                
                return;
                
            }
            
            Iterator<AchievementRule> iter = this.rules.iterator ();
            
            while (iter.hasNext ())
            {

                AchievementRule ar = iter.next ();                

                try
                {
        
                    if (ar.achieved (this.viewer))
                    {
                        
                        if (ar.getCategory ().equals (USER))
                        {
                            
                            manager.userAchievementReached (ar); 
                            
                        } else {
                        
                            manager.achievementReached (this.viewer,
                                                        ar);

                        }
                        
                        iter.remove ();
                        
                    }

                } catch (Exception e) {
                    
                    Environment.logError ("Unable to check achievement: " + ar,
                                          e);
                    
                }
                
            }
                            
            this.running = false;
            
            }catch(Exception e) {
                
                Environment.logError ("Unable to check for achievements for project: " +
                                      this.viewer.getProject (),
                                      e);

            }
                        
        }
        
    }

    public void removeAchievedAchievement (String                type,
                                           String                id,
                                           AbstractProjectViewer viewer)
                                           throws                Exception
    {

        if (type.toLowerCase ().equals (USER))
        {

            Set<String> achieved = this.getAchievedIds (Environment.getProperty (Constants.USER_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME));
            
            achieved.remove (id.toLowerCase ());
            
            Environment.setUserProperty (Constants.USER_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME,
                                         this.getAsString (achieved));

        } else {
            
            Set<String> achieved = this.getAchievedIds (viewer.getProject ().getProperty (Constants.PROJECT_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME));
    
            achieved.remove (id);
            
            viewer.getProject ().getProperties ().setProperty (Constants.PROJECT_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME,
                                                               new StringProperty (Constants.PROJECT_ACHIEVEMENTS_ACHIEVED_PROPERTY_NAME,
                                                                                   this.getAsString (achieved)));
            
        }

    }
    
}