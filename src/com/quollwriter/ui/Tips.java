package com.quollwriter.ui;

import java.util.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import com.quollwriter.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.data.*;

public class Tips
{
    
    public class XMLConstants
    {
        
        public static final String tip = "tip";
        
    }
    
    private List<Tip> baseTips = new ArrayList ();
    private List<Tip> tips = new ArrayList ();
    private Random ind = new Random ();
    private int lastInd = 0;
    private AbstractProjectViewer viewer = null;
    
    public Tips (AbstractProjectViewer viewer)
                 throws Exception
    {
        
        this.viewer = viewer;
        
        String tipsXML = Environment.getResourceFileAsString (Constants.TIPS_FILE);
        
        Element root = JDOMUtils.getStringAsElement (tipsXML);
        
        List tipEls = JDOMUtils.getChildElements (root,
                                                  XMLConstants.tip,
                                                  false);

        for (int i = 0; i < tipEls.size (); i++)
        {
            
            Element el = (Element) tipEls.get (i);
            
            this.baseTips.add (new Tip (el));
            
        }

        this.tips = new ArrayList (this.baseTips);

    }
        
    public String getNextTip ()
    {
        
        if (this.tips.size () == 0)
        {
            
            this.tips = new ArrayList (this.baseTips);
            
        }
        
        int n = this.ind.nextInt (this.tips.size ());
        
        if ((n == this.lastInd)
            &&
            (this.tips.size () > 1)
           )
        {
            
            return this.getNextTip ();
            
        }
        
        this.lastInd = n;
                
        Tip t = this.tips.remove (n);
        
        // See if there is a condition.
        String text = t.getText (this.viewer);
        
        if (text == null)
        {
                        
            return this.getNextTip ();
            
        }
                
        return text;
        
    }
    /*
    public JComponent getTipBox ()
    {
        
        String tipText = this.getNextTip ();

        final Box tipBox = new Box (BoxLayout.X_AXIS);

        final HTMLPanel htmlP = new HTMLPanel (tipText,
                                               this);

        htmlP.setBackground (null);
        htmlP.setOpaque (false);

        //final JScrollPane tip = HTMLPanel.createHelpPanel (htmlP);

        htmlP.setPreferredSize (new Dimension (Short.MAX_VALUE,
                                               htmlP.getPreferredSize ().height + 30));
                
        tipBox.add (htmlP);
        tipBox.add (Box.createHorizontalStrut (10));

        JButton nextBut = UIUtils.createButton ("next",
                                                Constants.ICON_MENU,
                                                "Click to view the next tip",
                                                null);

        ImageIcon cancel = Environment.getIcon ("cancel",
                                                Constants.ICON_MENU);
                
        cancel.setImage (cancel.getImage ().getScaledInstance (8, 8, Image.SCALE_SMOOTH));

        ImageIcon tipsOff = UIUtils.overlayImage (Environment.getIcon ("help",
                                                                       Constants.ICON_MENU),
                                                  cancel,
                                                  "br");

        java.util.List<JButton> buts = new ArrayList ();
        buts.add (nextBut);
        
        JButton offBut = UIUtils.createButton (tipsOff,
                                               "Click to stop showing tips when Quoll Writer starts",
                                               null);
        
        buts.add (offBut);
                                        
        // Show a tip.
        final Notification n = this.addNotification (tipBox,
                                                     "help",
                                                     90,
                                                     buts);

        nextBut.addActionListener (new ActionAdapter ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                String t = _this.tips.getNextTip ();
                
                if (t != null)
                {

                    htmlP.setText (t);
                                                                                
                    tipBox.getParent ().validate ();
                    tipBox.getParent ().repaint ();
                    
                    _this.repaint ();

                    n.restartTimer ();

                    _this.fireProjectEvent (ProjectEvent.TIPS,
                                            ProjectEvent.SHOW);

                }
                
            }
            
        });

        offBut.addActionListener (new ActionAdapter ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {
                
                if (JOptionPane.showConfirmDialog (_this,
                                                   "Stop showing tips when Quoll Writer starts?\n\nThey can enabled at any time in the Project Options.",
                                                   "Stop showing tips?",
                                                   JOptionPane.YES_NO_OPTION,
                                                   JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                {
                                                                            
                    _this.fireProjectEvent (ProjectEvent.TIPS,
                                            ProjectEvent.OFF);
                
                    try
                    {
                
                        Environment.setUserProperty (Constants.SHOW_TIPS_PROPERTY_NAME,
                                                     new BooleanProperty (Constants.SHOW_TIPS_PROPERTY_NAME,
                                                                          false));

                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to turn off tips",
                                              e);
                        
                    }

                    n.removeNotification ();

                
                }    
                
            }
            
        });

        tipBox.getParent ().validate ();
        tipBox.getParent ().repaint ();
                
    }
    */
    private class Tip
    {
        
        public class XMLConstants
        {
            
            public static final String item = "item";
            
        }
        
        private List<Item> items = new ArrayList ();
        
        public Tip (Element root)
                    throws  JDOMException
        {
            
            List els = JDOMUtils.getChildElements (root,
                                                   XMLConstants.item,
                                                   false);
            
            for (int i = 0; i < els.size (); i++)
            {
                
                Element el = (Element) els.get (i);
                
                this.items.add (new Item (el));
                
            }
            
        }
        
        public String getText (AbstractProjectViewer viewer)
        {
            
            for (Item it : this.items)
            {
                
                if (it.shouldShow (viewer))
                {
                    
                    return it.getText ();
                    
                }
                
            }
            
            return null;
            
        }
        
        private class Item
        {
            
            public class XMLConstants
            {
                
                public static final String condition = "condition";
                
            }
            
            private List<String> conds = new ArrayList ();
            private String text = null;
            
            public Item (Element root)
                         throws  JDOMException
            {
                
                String cs = JDOMUtils.getAttributeValue (root,
                                                         XMLConstants.condition,
                                                         false);
                
                if (!cs.equals (""))
                {
                    
                    StringTokenizer t = new StringTokenizer (cs,
                                                             ",;");
                    
                    while (t.hasMoreTokens ())
                    {
                        
                        this.conds.add (t.nextToken ().trim ().toLowerCase ());
                        
                    }
                    
                }
                
                this.text = JDOMUtils.getChildContent (root);
                
            }
            
            public String getText ()
            {
                
                return this.text;
                
            }
            
            public boolean shouldShow (AbstractProjectViewer viewer)
            {
                
                for (String c : this.conds)
                {
                    
                    boolean not = c.startsWith ("!");
                    
                    if (not)
                    {
                        
                        c = c.substring (1);
                        
                    }
                    
                    boolean v = false;
                    
                    if (c.equals ("chaptertab"))
                    {
                        
                        v = (viewer.getCurrentlyVisibleTab ().getForObject () instanceof Chapter);
                        
                    }

                    if (c.equals ("ideaboard"))
                    {
                        
                        v = viewer.getCurrentlyVisibleTab ().getPanelId ().equals (IdeaBoard.PANEL_ID);
                        
                    }

                    if (c.equals ("projectviewer"))
                    {
                        
                        v = (viewer instanceof ProjectViewer);
                        
                    }

                    if (c.equals ("warmupsviewer"))
                    {
                        
                        v = (viewer instanceof WarmupsViewer);
                        
                    }

                    if (c.equals ("spellcheckon"))
                    {
                        
                        v = viewer.isSpellCheckingEnabled ();

                    }

                    if (c.equals ("spellcheckoff"))
                    {
                        
                        v = !viewer.isSpellCheckingEnabled ();

                    }
                        
                    if (v)
                    {
                        
                        if (not)
                        {
                            
                            return false;
                            
                        }
                        
                    } else {
                        
                        if (!not)
                        {
                            
                            return false;
                            
                        }
                        
                        
                    }
                                                                
                }
                
                return true;
                
            }
            
        }
        
    }
    
}