package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.util.Set;
import java.util.Random;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;

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
        
    //private TreeMap<Integer, WhatsNewItem> items = new TreeMap ();
    private TreeMap<Version, java.util.List<WhatsNewItem>> items = new TreeMap ();//Collections.reverseOrder ());
        
    public WhatsNew (AbstractProjectViewer pv,
                     boolean               onlyShowCurrentVersion)
                     throws                GeneralException
    {
        
        super (pv);

        // Get the current whats new version (i.e. old).
        Version lastWhatsNewVersion = new Version (Environment.getProperty (Constants.WHATS_NEW_VERSION_VIEWED_PROPERTY_NAME));
        
        boolean betasAllowed = Environment.getUserProperties ().getPropertyAsBoolean (Constants.OPTIN_TO_BETA_VERSIONS_PROPERTY_NAME);
        
        try
        {
        
            String whatsNew = Environment.getResourceFileAsString (Constants.WHATS_NEW_FILE);
             
            // Load up all the whats new for greater versions.
            Element root = JDOMUtils.getStringAsElement (whatsNew);
            
            java.util.List verEls = JDOMUtils.getChildElements (root,
                                                                XMLConstants.version,
                                                                false);
            
            // Assume they are in the right order
            // TODO: Enforce the order and/or sort.
            for (int i = 0; i < verEls.size (); i++)
            {
                
                Element vEl = (Element) verEls.get (i);
                
                String id = JDOMUtils.getAttributeValue (vEl,
                                                         XMLConstants.id,
                                                         true);
                
                Version v = new Version (id);
/*
                if ((v.isBeta ())
                    &&
                    (!betasAllowed)
                   )
                {

                    // Ignore, the user isn't interested in betas.
                    continue;
                    
                }
  */              
                if ((lastWhatsNewVersion.isNewer (v))
                    ||
                    ((onlyShowCurrentVersion)
                     &&
                     (v.isSame (Environment.getQuollWriterVersion ()))
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
                    
                    java.util.List<WhatsNewItem> its = new ArrayList ();
                    
                    for (int j = 0; j < itemEls.size (); j++)
                    {
                    
                        Element itEl = (Element) itemEls.get (j);
                                             
                        WhatsNewItem it = new WhatsNewItem (itEl,
                                                            compProv,
                                                            pv);
                        
                        if (it.onlyIfCurrentVersion)
                        {
                            
                            if (!Environment.getQuollWriterVersion ().isSame (v))
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
                    
                        its.add (it);
                        
                    }
                    
                    if (its.size () > 0)
                    {
                        
                        this.items.put (v,
                                        its);
                        
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

        return String.format ("Welcome to version <b>%s</b>.  This window describes the various changes that have been made since the last version and lets you setup new features.  You can also see the <a href='help://version-changes/%s'>full list of changes online</a>.",
                              Environment.getQuollWriterVersion ().getVersion (),
                              Environment.getQuollWriterVersion ().getVersion ().replace ('.', '_'));

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

        int ind = currStage.indexOf (":");
        
        Version v = new Version (currStage.substring (0,
                                                      ind));
        
        int lind = Integer.parseInt (currStage.substring (ind + 1));
        
        java.util.List<WhatsNewItem> its = this.items.get (v);
        
        if (its == null)
        {
            
            return null;
            
        }
        
        lind++;
        
        if (lind <= (its.size () - 1))
        {
            
            return v.getVersion () + ":" + lind;
            
        }
        
        Version n = this.items.higherKey (v);
        
        if (n == null)
        {
            
            return null;
            
        }
        
        java.util.List<WhatsNewItem> nits = this.items.get (n);
        
        if (nits != null)
        {
            
            return n.getVersion () + ":0";
            
        }
        
        return null;
                
    }
    
    public String getPreviousStage (String currStage)
    {

        if (currStage == null)
        {
            
            return null;
            
        }

        int ind = currStage.indexOf (":");
        
        Version v = new Version (currStage.substring (0,
                                                      ind));
        
        int lind = Integer.parseInt (currStage.substring (ind + 1));
        
        java.util.List<WhatsNewItem> its = this.items.get (v);
        
        if (its == null)
        {
            
            return null;
            
        }
        
        lind--;
        
        if (lind > -1)
        {
            
            return v.getVersion () + ":" + lind;
            
        }
        
        Version p = this.items.lowerKey (v);
        
        if (p == null)
        {
            
            return null;
            
        }
        
        java.util.List<WhatsNewItem> pits = this.items.get (p);
        
        if (pits != null)
        {
            
            return p.getVersion () + ":" + (pits.size () - 1);
            
        }
        
        return null;
        
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
    
            return this.items.firstKey ().getVersion () + ":0";
        
        } catch (Exception e) {
           
            return null;
            
        }

    }

    public WizardStep getStage (String stage)
    {

        final WhatsNew _this = this;

        WizardStep ws = new WizardStep ();

        int ind = stage.indexOf (":");

        Version v = new Version (stage.substring (0,
                                                  ind));

        int lind = Integer.parseInt (stage.substring (ind + 1));
        
        java.util.List<WhatsNewItem> its = this.items.get (v);

        if (its == null)
        {
            
            return null;
            
        }
        
        WhatsNewItem item = its.get (lind);

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
                hp.setSize (new Dimension (UIUtils.getPopupWidth () - 25, 500));
                
                Box hpb = new Box (BoxLayout.Y_AXIS);
                hpb.add (hp);
                hpb.setMaximumSize (hpb.getPreferredSize ());
                hpb.setBorder (UIUtils.createPadding (0, 5, 0, 0));
                b.add (hpb);

            }
                
            if (item.component != null)
            {
    
                if (item.description != null)
                {

                    b.add (Box.createVerticalStrut (5));
                    
                }
                
                item.component.setAlignmentY (Component.TOP_ALIGNMENT);
                item.component.setBorder (UIUtils.createPadding (5, 10, 0, 0));
                
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
                                                             Environment.getQuollWriterVersion ().getVersion ()));

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
