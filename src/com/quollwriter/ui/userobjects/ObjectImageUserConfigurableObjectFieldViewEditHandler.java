package com.quollwriter.ui.userobjects;

import java.io.*;
import java.awt.image.*;
import java.awt.event.*;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class ObjectImageUserConfigurableObjectFieldViewEditHandler extends ImageUserConfigurableObjectFieldViewEditHandler
{
    
    public ObjectImageUserConfigurableObjectFieldViewEditHandler (ImageUserConfigurableObjectTypeField typeField,
                                                                  UserConfigurableObject               obj,
                                                                  UserConfigurableObjectField          field,
                                                                  AbstractProjectViewer                viewer)
    {
        
        super (typeField,
               obj,
               field,
               viewer);
                                
    }
             
    @Override
    public Set<FormItem> getViewFormItems ()
    {
        
        final java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.form);
        prefix.add (LanguageStrings.view);
        prefix.add (LanguageStrings.types);
        prefix.add (UserConfigurableObjectTypeField.Type.objectimage.getType ());
        
        final ImageUserConfigurableObjectFieldViewEditHandler _this = this;
        
        Set<FormItem> items = new LinkedHashSet ();
                
        final File pf = this.viewer.getProjectFile (this.getFieldValue ());
                
        BufferedImage v = UIUtils.getImage (pf);
                
        ImageIcon icon = null;
                
        if (v != null)
        {
            
            icon = new ImageIcon (v);
                
            if (v.getWidth () > 250)
            {
                
                v = UIUtils.getScaledImage (v,
                                            250);
                
                icon = new ImageIcon (v);
                    
            }

        }
                                
        if (icon != null)
        {
          
            JLabel l = UIUtils.createLabel (null,
                                            icon,
                                            new ActionListener ()
                                            {
                                            
                                                @Override
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    try
                                                    {
                                                                                                                                                            
                                                        UIUtils.showFile (_this.viewer,
                                                                          pf);
                                                        
                                                    } catch (Exception e) {
                                                        
                                                        UIUtils.showErrorMessage (_this.viewer,
                                                                                  Environment.getUIString (prefix,
                                                                                                           LanguageStrings.actionerror));
                                                                                  //"Unable to show image.");
                                                        
                                                        Environment.logError ("Unable to show image: " +
                                                                              pf +
                                                                              ", for: " +
                                                                              _this.field,
                                                                              e);
                                                        
                                                    }
                                                    
                                                }
                                            
                                            });

            l.setToolTipText (Environment.getUIString (prefix,
                                                       LanguageStrings.tooltip));
            //"Click to view the full image");
            items.add (new AnyFormItem (this.typeField.getFormName (),
                                        l));

        } else {
            
            items.add (this.createNoValueItem (Environment.getUIString (prefix,
                                                                        LanguageStrings.novalue)));
            
        } 
        
        return items;
        
    }             
             
    public JComponent getViewImageComponent ()
    {
        
        return this.getViewFormItems ().iterator ().next ().getComponent ();
                 
    }

    public JComponent getInputImageComponent ()
    {
        
        return this.getInputFormItems (null,
                                       null).iterator ().next ().getComponent ();
                 
    }

}
