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
import com.quollwriter.ui.components.ItemAdapter;

public class version_2_5_9 implements WhatsNewComponentProvider
{
    
    private Map<String, JComponent> comps = new HashMap ();
    
    public version_2_5_9 ()
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
        
        final version_2_5_9 _this = this;
        
        JComponent c = this.comps.get (id);
        
        if (c != null)
        {
            
            return c;
            
        }
                
        if (id.equals ("editingassets"))
        {
        
            final JRadioButton rbAssetInPopup = UIUtils.createRadioButton ("Edit in a popup");
            final JRadioButton rbAssetInTab = UIUtils.createRadioButton ("Edit in their own tab");
    
            boolean editAssetInPopup = UserProperties.getAsBoolean (Constants.EDIT_ASSETS_IN_POPUP_PROPERTY_NAME);
    
            rbAssetInPopup.setSelected (editAssetInPopup);
            rbAssetInTab.setSelected (!editAssetInPopup);
    
            ButtonGroup g = new ButtonGroup ();
            g.add (rbAssetInPopup);
            g.add (rbAssetInTab);
            
            rbAssetInPopup.addItemListener (new ItemAdapter ()
            {
    
                public void itemStateChanged (ItemEvent ev)
                {
    
                    UserProperties.set (Constants.EDIT_ASSETS_IN_POPUP_PROPERTY_NAME,
                                        true);
    
                }
    
            });        
            
            rbAssetInTab.addItemListener (new ItemAdapter ()
            {
    
                public void itemStateChanged (ItemEvent ev)
                {
    
                    UserProperties.set (Constants.EDIT_ASSETS_IN_POPUP_PROPERTY_NAME,
                                        false);
    
                }
    
            });        
        
            Box b = new Box (BoxLayout.Y_AXIS);
                 
            b.add (rbAssetInPopup);
            b.add (rbAssetInTab);
                            
            c = b;
                                    
        }
        
        this.comps.put (id,
                        c);
        
        return c;
        
    }
    
}