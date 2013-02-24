package com.quollwriter.achievements;

import java.util.*;

import com.quollwriter.achievements.rules.*;

public class AchievementReachedEvent extends EventObject
{
        
    private AchievementRule rule = null;
    
    public AchievementReachedEvent (AchievementRule rule)
    {
        
        super (rule);
        
        this.rule = rule;
                
    }

    public AchievementRule getRule ()
    {
        
        return this.rule;
        
    }
    
}