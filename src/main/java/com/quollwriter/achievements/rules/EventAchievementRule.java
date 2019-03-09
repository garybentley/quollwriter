package com.quollwriter.achievements.rules;

import java.util.*;

import org.jdom.*;

import com.gentlyweb.xml.*;

import com.quollwriter.ui.*;

public class EventAchievementRule extends AbstractAchievementRule
{
    
    public static final String RULE_TYPE = "event";
    
    public class XMLConstants
    {
        
        public static final String count = "count";
        public static final String match = "match";
        
    }
    
    private int count = -1;
    private ObjectMatch match = null;
    private int matchCount = 0;
        
    public EventAchievementRule (Element root)
                                 throws  JDOMException
    {
        
        super (root);

        this.count = JDOMUtils.getAttributeValueAsInt (root,
                                                       XMLConstants.count,
                                                       false);
        
        Element mEl = JDOMUtils.getChildElement (root,
                                                 XMLConstants.match,
                                                 false);
        
        if (mEl != null)
        {
            
            this.match = new ObjectMatch (mEl);
            
        }
        
        if (this.getEventIds ().size () == 0)
        {
            
            throw new JDOMException ("Expected at least one event/action to be defined, referenced by: " +
                                     JDOMUtils.getPath (root));            
            
        }
        
    }

    public String toString ()
    {
        
        return super.toString () + "(count: " + this.count + ", matched: " + this.matchCount + ", match: " + this.match + ")";
        
    }
        
    public boolean isEventTrigger ()
    {
        
        return true;        
        
    }
    
    public boolean shouldPersistState ()
    {
        
        return this.matchCount > 0;
        
    }
    
    public boolean achieved (AbstractProjectViewer viewer,
                             ProjectEvent          ev)
                             throws                Exception
    {
        
        return this.achieved (ev);
        
    }

    public boolean achieved (ProjectEvent ev)
                             throws       Exception
    {
        
        if (this.match != null)
        {
            
            if (!this.match.match (ev.getContextObject ()))
            {
                
                return false;
                
            }
            
        }
        
        this.matchCount++;
        
        return this.matchCount >= this.count;
        
    }

    public boolean achieved (AbstractProjectViewer viewer)
    {
        
        return false;
        
    }

    public void init (Element root)
    {
        
        // Init the count.
        try
        {

            if (root != null)
            {
                
                this.matchCount = JDOMUtils.getAttributeValueAsInt (root,
                                                                    XMLConstants.count,
                                                                    false);
                
                if (this.matchCount < 0)
                {
                    
                    this.matchCount = 0;
                    
                }
                
            }

        } catch (Exception e) {
            
            // Ignore it, for now.
            
        }
        
    }
    
    public void fillState (Element root)
    {
        
        root.setAttribute (XMLConstants.count,
                           String.valueOf (this.matchCount));
        
    }
     
}