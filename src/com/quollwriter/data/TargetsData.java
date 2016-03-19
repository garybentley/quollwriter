package com.quollwriter.data;

import java.util.Map;
import java.util.HashMap;

import com.gentlyweb.properties.*;

import com.quollwriter.*;

public class TargetsData
{
    
    public enum Target
    {
        
        myDailyWriting ("myDailyWriting"),
        mySessionWriting ("mySessionWriting"),
        myWeeklyWriting ("myWeeklyWriting"),
        myMonthlyWriting ("myMonthlyWriting"),
        maxChapterCount ("maxChapterCount"),
        readabilityGF ("readabilityGF"),
        readabilityFK ("readabilityFK");
        
        private String type = null;
        
        Target (String type)
        {
            
            this.type = type;
            
        }
        
        public String getType ()
        {
            
            return this.type;
            
        }
                
    }
    
    private Map<Target, Number> targets = new HashMap ();
    private Properties props = null;
    private boolean targetShowMessageWhenMaxChapterCountExceeded = false;
    private boolean targetShowMessageWhenSessionTargetReached = false;
    
    public TargetsData (Properties props)
    {
        
        this.props = props;
        
        this.init ();
        
    }
        
    private void init ()
    {
        
        int val = this.props.getPropertyAsInt (Constants.TARGET_DAILY_WRITING_PROPERTY_NAME);
        
        if (val < 0)
        {
            
            // Get the default.
            val = Environment.getDefaultUserTargets ().getMyDailyWriting ();
            
        }
        
        this.targets.put (Target.myDailyWriting,
                          val);

        val = this.props.getPropertyAsInt (Constants.TARGET_SESSION_WRITING_PROPERTY_NAME);
        
        if (val < 0)
        {
            
            // Get the default.
            val = Environment.getDefaultUserTargets ().getMySessionWriting ();
            
        }
        
        this.targets.put (Target.mySessionWriting,
                          val);

        val = this.props.getPropertyAsInt (Constants.TARGET_WEEKLY_WRITING_PROPERTY_NAME);
        
        if (val < 0)
        {
            
            // Get the default.
            val = Environment.getDefaultUserTargets ().getMyWeeklyWriting ();
            
        }
        
        this.targets.put (Target.myWeeklyWriting,
                          val);

        val = this.props.getPropertyAsInt (Constants.TARGET_MONTHLY_WRITING_PROPERTY_NAME);
        
        if (val < 0)
        {
            
            // Get the default.
            val = Environment.getDefaultUserTargets ().getMyMonthlyWriting ();
            
        }

        this.targets.put (Target.myMonthlyWriting,
                          val);

        val = this.props.getPropertyAsInt (Constants.TARGET_READABILITY_FK_PROPERTY_NAME);
        
        if (val < 0)
        {
            
            // Get the default.
            val = Environment.getDefaultUserTargets ().getReadabilityFK ();
            
        }
        
        this.targets.put (Target.readabilityFK,
                          val);
        
        val = this.props.getPropertyAsInt (Constants.TARGET_READABILITY_GF_PROPERTY_NAME);
        
        if (val < 0)
        {
            
            // Get the default.
            val = Environment.getDefaultUserTargets ().getReadabilityGF ();
            
        }
        
        this.targets.put (Target.readabilityGF,
                          val);

        val = this.props.getPropertyAsInt (Constants.TARGET_MAX_CHAPTER_COUNT_PROPERTY_NAME);
        
        if (val < 0)
        {
            
            // Get the default.
            val = Environment.getDefaultUserTargets ().getMaxChapterCount ();
            
        }
        
        this.targets.put (Target.maxChapterCount,
                          val);

        this.targetShowMessageWhenMaxChapterCountExceeded = this.props.getPropertyAsBoolean (Constants.TARGET_SHOW_MESSAGE_WHEN_MAX_CHAPTER_COUNT_EXCEEDED_PROPERTY_NAME);                          
                          
        this.targetShowMessageWhenSessionTargetReached = this.props.getPropertyAsBoolean (Constants.TARGET_SHOW_MESSAGE_WHEN_SESSION_TARGET_REACHED_PROPERTY_NAME);                          

    }
    
    public void setMaxChapterCount (int v)
    {
        
        this.updateIntTarget (Target.maxChapterCount,
                              Constants.TARGET_MAX_CHAPTER_COUNT_PROPERTY_NAME,
                              v);
        
    }
    
    public int getMaxChapterCount ()
    {
        
        return this.getTargetAsInt (Target.maxChapterCount);

    }

    public void setReadabilityGF (int v)
    {
        
        this.updateIntTarget (Target.readabilityGF,
                              Constants.TARGET_READABILITY_GF_PROPERTY_NAME,
                              v);
        
    }
    
    public boolean isShowMessageWhenSessionTargetReached ()
    {
        
        return this.targetShowMessageWhenSessionTargetReached;
        
    }
    
    public void setShowMessageWhenSessionTargetReached (boolean v)
    {
        
        this.targetShowMessageWhenSessionTargetReached = v;
        
        this.props.setProperty (Constants.TARGET_SHOW_MESSAGE_WHEN_SESSION_TARGET_REACHED_PROPERTY_NAME,
                                new BooleanProperty (Constants.TARGET_SHOW_MESSAGE_WHEN_SESSION_TARGET_REACHED_PROPERTY_NAME,
                                                     v));                        
        
    }

    public void setShowMessageWhenMaxChapterCountExceeded (boolean v)
    {
        
        this.targetShowMessageWhenMaxChapterCountExceeded = v;
        
        this.props.setProperty (Constants.TARGET_SHOW_MESSAGE_WHEN_MAX_CHAPTER_COUNT_EXCEEDED_PROPERTY_NAME,
                                new BooleanProperty (Constants.TARGET_SHOW_MESSAGE_WHEN_MAX_CHAPTER_COUNT_EXCEEDED_PROPERTY_NAME,
                                                     v));                        
        
    }
    
    public boolean isShowMessageWhenMaxChapterCountExceeded ()
    {
        
        return this.targetShowMessageWhenMaxChapterCountExceeded;
        
    }
    
    public int getReadabilityGF ()
    {
        
        return this.getTargetAsInt (Target.readabilityGF);

    }
    
    public void setReadabilityFK (int v)
    {
        
        this.updateIntTarget (Target.readabilityFK,
                              Constants.TARGET_READABILITY_FK_PROPERTY_NAME,
                              v);
                
    }
    
    public int getReadabilityFK ()
    {
        
        return this.getTargetAsInt (Target.readabilityFK);

    }
    
    public void setMyDailyWriting (int c)
    {
        
        this.updateIntTarget (Target.myDailyWriting,
                              Constants.TARGET_DAILY_WRITING_PROPERTY_NAME,
                              c);
                
    }
    
    public int getMyDailyWriting ()
    {
        
        return this.getTargetAsInt (Target.myDailyWriting);
        
    }
    
    public int getMySessionWriting ()
    {
        
        return this.getTargetAsInt (Target.mySessionWriting);
        
    }

    public void setMySessionWriting (int c)
    {
        
        this.updateIntTarget (Target.mySessionWriting,
                              Constants.TARGET_SESSION_WRITING_PROPERTY_NAME,
                              c);
                
    }

    public int getMyWeeklyWriting ()
    {
        
        return this.getTargetAsInt (Target.myWeeklyWriting);
        
    }

    public void setMyWeeklyWriting (int c)
    {
        
        this.updateIntTarget (Target.myWeeklyWriting,
                              Constants.TARGET_WEEKLY_WRITING_PROPERTY_NAME,
                              c);
                
    }

    public int getMyMonthlyWriting ()
    {
        
        return this.getTargetAsInt (Target.myMonthlyWriting);
        
    }
    
    public void setMyMonthlyWriting (int c)
    {
        
        this.updateIntTarget (Target.myMonthlyWriting,
                              Constants.TARGET_MONTHLY_WRITING_PROPERTY_NAME,
                              c);
        
    }
    
    public void updateIntTarget (Target t,
                                 String propName,
                                 int    value)
    {

        this.targets.put (t,
                          value);

        this.props.setProperty (propName,
                                new IntegerProperty (propName,
                                                     this.targets.get (t).intValue ()));                        
        
    }
    
    public int getTargetAsInt (Target t)
    {
        
        Number n = this.targets.get (t);
        
        if (n == null)
        {
            
            return 0;
            
        }
        
        return n.intValue ();
        
    }
    
}