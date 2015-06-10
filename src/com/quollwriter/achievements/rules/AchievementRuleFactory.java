package com.quollwriter.achievements.rules;

import org.jdom.*;

import com.gentlyweb.xml.*;

public class AchievementRuleFactory
{
    
    public class XMLConstants
    {
        
        public static final String type = "type";
        
    }
    
    public static AchievementRule createRule (Element root)
                                              throws  JDOMException
    {
        
        String type = JDOMUtils.getAttributeValue (root,
                                                   XMLConstants.type,
                                                   true);
        
        // TODO: Make this nicer.
        if (type.equals (ItemAchievementRule.RULE_TYPE))
        {
            
            return new ItemAchievementRule (root);
            
        }
        
        if (type.equals (EventAchievementRule.RULE_TYPE))
        {
            
            return new EventAchievementRule (root);
            
        }

        if (type.equals (SessionAchievementRule.RULE_TYPE))
        {
            
            return new SessionAchievementRule (root);
            
        }

        if (type.equals (WordCountAchievementRule.RULE_TYPE))
        {
            
            return new WordCountAchievementRule (root);
            
        }

        if (type.equals (WordAchievementRule.RULE_TYPE))
        {
            
            return new WordAchievementRule (root);
            
        }

        if (type.equals (SentenceAchievementRule.RULE_TYPE))
        {
            
            return new SentenceAchievementRule (root);
            
        }
        
        if (type.equals (EditorMessageAchievementRule.RULE_TYPE))
        {
            
            return new EditorMessageAchievementRule (root);
            
        }

        return null;
        
    }
    
}