package com.quollwriter.ui.whatsnewcomps;

import java.awt.Dimension;

import java.util.Set;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.achievements.*;
import com.quollwriter.achievements.rules.*;
import com.quollwriter.ui.components.ActionAdapter;

public class version_2_2 implements WhatsNewComponentProvider
{
    
    private Map<String, JComponent> comps = new HashMap ();
    
    public version_2_2 ()
    {
        
    }
    
    private void setUserProperty (String  name,
                                  boolean value)
    {
        
        UserProperties.set (name,
                            value);
            
    }
    
    public JComponent getComponent (final AbstractViewer pv,
                                    final String         id)
    {
        
        final version_2_2 _this = this;
        
        JComponent c = this.comps.get (id);
        
        if (c != null)
        {
            
            return c;
            
        }
                
        if (id.equals ("errors"))
        {

            final JCheckBox errs = UIUtils.createCheckBox ("Send errors to Quoll Writer support");
            
            errs.addActionListener (new ActionAdapter ()
            {
               
                public void actionPerformed (ActionEvent ev)
                {
    
                    _this.setUserProperty (Constants.AUTO_SEND_ERRORS_TO_SUPPORT_PROPERTY_NAME,
                                           errs.isSelected ());
                                    
                }
                
            });
                    
            c = errs;
                                    
        }

        if (id.equals ("distractionfreemode"))
        {

            final JCheckBox distMode = UIUtils.createCheckBox ("Enable distraction free mode when in full screen mode");
    
            distMode.addActionListener (new ActionAdapter ()
            {
               
                public void actionPerformed (ActionEvent ev)
                {
                                        
                    _this.setUserProperty (Constants.FULL_SCREEN_ENABLE_DISTRACTION_FREE_MODE_WHEN_EDITING_PROPERTY_NAME,
                                           distMode.isSelected ());
                    _this.setUserProperty (Constants.FULL_SCREEN_EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME,
                                           distMode.isSelected ());
                                    
                }
                
            });
            
            c = distMode;
            
        }
        
        if (id.equals ("highlightwritingline"))
        {

            final JCheckBox line = UIUtils.createCheckBox ("Highlight the writing line");
            
            line.addActionListener (new ActionAdapter ()
            {
               
                public void actionPerformed (ActionEvent ev)
                {
    
                    if (pv instanceof AbstractProjectViewer)
                    {
    
                        ((AbstractProjectViewer) pv).setHighlightWritingLine (line.isSelected ());
                        
                    }

                    if (line.isSelected ())
                    {
                        
                        _this.setUserProperty (Constants.FULL_SCREEN_EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME,
                                               true);
                        _this.setUserProperty (Constants.EDITOR_HIGHLIGHT_WRITING_LINE_PROPERTY_NAME,
                                               true);
                        
                    }
                    
                }
                
            });
                    
            c = line;
            
        }
        
        this.comps.put (id,
                        c);
        
        return c;
        
    }
    
}