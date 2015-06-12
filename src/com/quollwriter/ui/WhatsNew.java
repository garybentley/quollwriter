package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.util.Set;
import java.util.Random;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.jdom.*;

import com.gentlyweb.xml.*;
import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.achievements.*;
import com.quollwriter.achievements.rules.*;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.whatsnewcomps.*;

public class WhatsNew extends Wizard //PopupWizard
{
    
    private class XMLConstants
    {
        
        public static final String id = "id";
        public static final String version = "version";
        public static final String clazz = "class";
        public static final String beta = "beta";
        
    }
    
    //private int currStage = 0;
    //private int maxStage = 2;
    
    private TreeMap<Integer, WhatsNewItem> items = new TreeMap ();
    
    
    public WhatsNew (AbstractProjectViewer pv,
                     boolean               onlyShowCurrentVersion)
                     throws                GeneralException
    {
        
        super (pv);

        // Get the current whats new version (i.e. old).
        String lastWhatsNewVersion = Environment.getProperty (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME);

        int currVer = Environment.getVersionAsInt (Environment.getQuollWriterVersion ());
        
        boolean betasAllowed = Environment.getUserProperties ().getPropertyAsBoolean (Constants.OPTIN_TO_BETA_VERSIONS_PROPERTY_NAME);
        
        try
        {
        
            String whatsNew = Environment.getResourceFileAsString (Constants.WHATS_NEW_FILE);
             
            // Load up all the whats new for greater versions.
            Element root = JDOMUtils.getStringAsElement (whatsNew);
            
            java.util.List verEls = JDOMUtils.getChildElements (root,
                                                                XMLConstants.version,
                                                                false);
            
            for (int i = 0; i < verEls.size (); i++)
            {
                
                Element vEl = (Element) verEls.get (i);
                
                String id = JDOMUtils.getAttributeValue (vEl,
                                                         XMLConstants.id,
                                                         true);
                
                if (JDOMUtils.getAttributeValueAsBoolean (vEl,
                                                          XMLConstants.beta,
                                                          false))
                {

                    if (!betasAllowed)
                    {

                        // Ignore, the user isn't interested in betas.
                        continue;
                        
                    }
                    
                }
                
                id = Environment.expandVersion (id);
                
                int verNum = Environment.getVersionAsInt (id);                
                
                if ((Environment.isNewVersionGreater (lastWhatsNewVersion,
                                                      id))
                    ||
                    ((onlyShowCurrentVersion)
                     &&
                     (verNum == currVer)
                    )
                   )
                {
                
                    String c = WhatsNewComponentProvider.class.getName ();
                    
                    int ind = c.lastIndexOf (".");
                    
                    if (ind > 0)
                    {
                        
                        c = c.substring (0,
                                         ind);
                        
                    }

                    WhatsNewComponentProvider compProv = null;
                    
                    String cl = JDOMUtils.getAttributeValue (vEl,
                                                             XMLConstants.clazz,
                                                             false);
                    
                    if (!cl.equals (""))
                    {
                    
                        Class clz = null;
                        
                        try
                        {
                            
                            clz = Class.forName (cl);

                            if (WhatsNewComponentProvider.class.isAssignableFrom (clz))
                            {
                                
                                compProv = (WhatsNewComponentProvider) clz.newInstance ();
                            
                            }
                            
                        } catch (Exception e) {
                            
                        }
                        
                    }
                    
                    // This is a version we are interested in.
                    java.util.List itemEls = JDOMUtils.getChildElements (vEl,
                                                                         WhatsNewItem.XMLConstants.root,
                                                                         true);
                    
                    for (int j = 0; j < itemEls.size (); j++)
                    {
                    
                        Element itEl = (Element) itemEls.get (j);
                    
                        // Add this to the list.
                        int itemNum = (verNum * 100) + j;
                         
                        WhatsNewItem it = new WhatsNewItem (itEl,
                                                            compProv,
                                                            pv);
                        
                        if (it.onlyIfCurrentVersion)
                        {
                            
                            if (verNum != currVer)
                            {
                                
                                continue;
                                
                            }
                            
                        }
                        
                        if ((it.description == null)
                            &&
                            (it.component == null)
                           )
                        {
                            
                            Environment.logMessage ("Whats new item has no description or component, referenced by: " +
                                                    JDOMUtils.getPath (itEl));
                            
                            continue;
                            
                        }
                    
                        this.items.put (itemNum,
                                        it);
                        
                    }
                    
                }
                                
            }

        } catch (Exception e) {
            
            throw new GeneralException ("Unable to init whats new",
                                        e);
            
        }
        
    }
/*
    public String getWindowTitle ()
    {

        return "What's new in this version - " + Environment.getQuollWriterVersion ();

    }

    public String getHeaderTitle ()
    {

        return this.getWindowTitle ();

    }

    public String getHeaderIconType ()
    {

        return null;//"whatsnew";

    }
*/
    public String getFirstHelpText ()
    {

        return "Welcome to version <b>" + Environment.getQuollWriterVersion () + "</b>.  This window describes the various changes that have been made since the last version and lets you setup new features.  You can also see the <a href='help://version-changes/" + Environment.getQuollWriterVersion ().replace ('.', '_') + "'>full list of changes online</a>.";

    }

    public boolean handleFinish ()
    {

        return true;

    }
    
    public void handleCancel ()
    {

        this.handleFinish ();

    }

    public String getNextStage (String currStage)
    {

        if (currStage == null)
        {
            
            return this.getStartStage ();
            
        }

        int cs = -1;
        
        try
        {
            
            cs = Integer.parseInt (currStage);
                        
        } catch (Exception e) {
                        
        }

        Integer n = this.items.higherKey (cs);
        
        if (n == null)
        {
            
            return null;
            
        }

        return n.toString ();
        
    }
    
    public String getPreviousStage (String currStage)
    {

        if (currStage == null)
        {
            
            return null;
            
        }

        int cs = -1;
        
        try
        {
            
            cs = Integer.parseInt (currStage);
                                    
        } catch (Exception e) {
                        
        }

        Integer p = this.items.lowerKey (cs);
        
        if (p == null)
        {
            
            return null;
            
        }

        return p.toString ();
        
    }    
    
    public boolean handleStageChange (String oldStage,
                                      String newStage)
    {

        return true;

    }
    /*
    public int getMaximumContentHeight ()
    {

        return 350;

    }
*/
    public String getStartStage ()
    {

        try
        {
    
            return this.items.firstKey ().toString ();
        
        } catch (Exception e) {
            
            return null;
            
        }

    }

    public WizardStep getStage (String stage)
    {

        final WhatsNew _this = this;

        WizardStep ws = new WizardStep ();

        int ind = Integer.parseInt (stage);
        
        WhatsNewItem item = this.items.get (ind);
        
        if (item == null)
        {
            
            return null;
            
        }
        
        ws.title = item.title;
        ws.helpText = this.getFirstHelpText ();
        
        if ((item.description != null)
            ||
            (item.component != null)
           )
        {
            
            final Box b = new Box (BoxLayout.Y_AXIS);

            if (item.description != null)
            {

                JTextPane hp = UIUtils.createHelpTextPane (item.description,
                                                           this.projectViewer);
                
                hp.setBorder (null);
    
                b.add (hp);

            }
                
            if (item.component != null)
            {
    
                if (item.description != null)
                {

                    b.add (Box.createVerticalStrut (5));
                    
                }
                
                b.add (item.component);                
                
            }
            
            b.add (Box.createVerticalGlue ());
    
            ws.panel = b;            
            
        }
        
        return ws;
        
    }
    
    public void init ()
    {
                
        super.init ();
        
        try
        {
    
            Environment.setUserProperty (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME,
                                         new StringProperty (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME,
                                                             Environment.getQuollWriterVersion ()));

        } catch (Exception e) {
            
            Environment.logError ("Unable to set the whats new version viewed property",
                                  e);
            
        }
        
    }
    
    private class WhatsNewInit
    {
        
        public WhatsNewInit (Element root)
        {
            
            
        }
        
        public void init (AbstractProjectViewer pv)
        {
            
            
            
        }
        
    }
    
    private class WhatsNewItem
    {
    
        public class XMLConstants
        {
            
            public static final String root = "item";
            public static final String title = "title";
            public static final String description = "description";
            public static final String onlyIfCurrentVersion = "onlyIfCurrentVersion";
            public static final String id = "id";
            
        }
    
        public String title = null;
        public String description = null;
        public String id = null;
        public boolean onlyIfCurrentVersion = false;
        public JComponent component = null;
    
        public WhatsNewItem (Element                   root,
                             WhatsNewComponentProvider prov,
                             AbstractProjectViewer     pv)
                             throws                    Exception
        {
            
            this.id = JDOMUtils.getAttributeValue (root,
                                                   XMLConstants.id,
                                                   false);
            
            if ((!this.id.equals (""))
                &&
                (prov != null)
               )
            {
                
                this.component = prov.getComponent (pv,
                                                    this.id);
                                
            } else {
                
                this.id = null;
                
            }
            
            this.onlyIfCurrentVersion = JDOMUtils.getAttributeValueAsBoolean (root,
                                                                              XMLConstants.onlyIfCurrentVersion,
                                                                              false);
            this.title = JDOMUtils.getChildElementContent (root,
                                                           XMLConstants.title);
            this.description = JDOMUtils.getChildElementContent (root,
                                                                 XMLConstants.description,
                                                                 false);
                                                
            if (this.description.equals (""))
            {
                
                this.description = null;
                                                
            }
                        
        }
        
    }
    
    
}
