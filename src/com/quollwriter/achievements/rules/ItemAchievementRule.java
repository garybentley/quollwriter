package com.quollwriter.achievements.rules;

import java.util.*;

import org.jdom.*;

import com.gentlyweb.utils.*;
import com.gentlyweb.xml.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.*;

public class ItemAchievementRule extends AbstractAchievementRule
{

    public static final String RULE_TYPE = "item";

    public static Map<String, Class> objTypeToClassMapping = new HashMap ();
    
    static
    {
        
        Map<String, Class> m = ItemAchievementRule.objTypeToClassMapping;
        
        m.put (Note.OBJECT_TYPE,
               Note.class);
        m.put (Chapter.OBJECT_TYPE,
               Chapter.class);
        m.put (QCharacter.OBJECT_TYPE,
               QCharacter.class);
        m.put (QObject.OBJECT_TYPE,
               QObject.class);
        m.put (Location.OBJECT_TYPE,
               Location.class);
        m.put (IdeaType.OBJECT_TYPE,
               IdeaType.class);
        m.put (Idea.OBJECT_TYPE,
               Idea.class);
        m.put (OutlineItem.OBJECT_TYPE,
               OutlineItem.class);
        m.put (Scene.OBJECT_TYPE,
               Scene.class);
        m.put (ResearchItem.OBJECT_TYPE,
               ResearchItem.class);
        
    }
    
    public class XMLConstants
    {
        
        public static final String action = "action";
        public static final String actions = "actions";
        public static final String objectType = "objectType";
        public static final String count = "count";
        public static final String match = "match";
        
    }
    
    private Set<String> actions = new HashSet ();
    private String objType = null;
    private ObjectMatch match = null;
    private int count = 0;
    private Class objClass = null;
    
    public ItemAchievementRule (Element root)
                                throws  JDOMException
    {
        
        super (root);
        
        this.objType = JDOMUtils.getAttributeValue (root,
                                                    XMLConstants.objectType,
                                                    true);
        
        this.objClass = ItemAchievementRule.objTypeToClassMapping.get (this.objType);
        
        if (this.objClass == null)
        {
            
            throw new JDOMException ("Object type: " +
                                     this.objType +
                                     ", referenced by: " +
                                     JDOMUtils.getAttribute (root,
                                                             XMLConstants.objectType,
                                                             true) +
                                     " is not supported.");
            
        }
        
        String act = JDOMUtils.getAttributeValue (root,
                                                  XMLConstants.action,
                                                  false);
        
        if (!act.equals (""))
        {
            
            this.eventIds.add (this.objType + act.toLowerCase ());
            
        }

        String acts = JDOMUtils.getAttributeValue (root,
                                                   XMLConstants.actions,
                                                   false);
        
        if (!acts.equals (""))
        {
            
            StringTokenizer t = new StringTokenizer (acts,
                                                     ",;");
            
            while (t.hasMoreTokens ())
            {
            
                this.eventIds.add (this.objType + t.nextToken ().toLowerCase ());
                
            }
            
        }
        
        Element mEl = JDOMUtils.getChildElement (root,
                                                 XMLConstants.match,
                                                 false);
        
        if (mEl != null)
        {
            
            this.match = new ObjectMatch (mEl);
            
        }
        
        this.count = JDOMUtils.getAttributeValueAsInt (root,
                                                       XMLConstants.count,
                                                       false);
        
    }
            
    public boolean shouldPersistState ()
    {
        
        return false;
        
    }
    
    public boolean achieved (AbstractProjectViewer viewer,
                             ProjectEvent          ev)
                             throws                Exception
    {
        
        return this.achieved (viewer);
        
    }

    public boolean achieved (ProjectEvent          ev)
    {
        
        return false;
        
    }

    public boolean achieved (AbstractProjectViewer viewer)
                             throws                Exception
    {
        
        // Get all the objects of the required type.
        Set<NamedObject> objs = viewer.getProject ().getAllNamedChildObjects (this.objClass);

        int c = objs.size ();

        if (this.match != null)
        {
        
            c = 0;
                
            for (NamedObject n : objs)
            {
                
                if (this.match.match (n))
                {
                    
                    c++;
                    
                }
                
            }
                        
        }   
        
        return c >= this.count;
                
    }

    public void init (Element root)
    {
        
        
        
    }
    
    public void fillState (Element root)
    {
        
    }
    
}