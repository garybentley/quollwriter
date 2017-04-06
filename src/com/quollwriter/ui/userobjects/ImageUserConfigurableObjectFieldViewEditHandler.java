package com.quollwriter.ui.userobjects;

import java.awt.event.*;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.UUID;

import java.io.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

import javax.imageio.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class ImageUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<ImageUserConfigurableObjectTypeField, String>
{
    
    private ImageSelectorFormItem editItem = null;
    
    public ImageUserConfigurableObjectFieldViewEditHandler (ImageUserConfigurableObjectTypeField typeField,
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
    public void grabInputFocus ()
    {
        
        if (this.editItem != null)
        {
            
            this.editItem.grabFocus ();
            
        }
          
    }
              
    @Override
    public Set<FormItem> getInputFormItems (String         initValue,
                                            ActionListener formSave)
    {
        
        Set<FormItem> items = new LinkedHashSet ();
                
        this.editItem = new ImageSelectorFormItem (this.typeField.getFormName (),
                                                   UIUtils.imageFileFilter,
                                                   this.viewer.getProjectFile ((this.getFieldValue () != null ? this.getFieldValue () : initValue)),
                                                   new Dimension (100, 100),
                                                   false,
                                                   null);
        
        items.add (this.editItem);
        
        return items;
        
    }
    
    @Override
    public Set<String> getInputFormItemErrors ()
    {
        
        return null;
        
    }
    
    @Override
    public String getInputSaveValue ()
                              throws GeneralException
    {
        
        File f = this.editItem.getValue ();

        if (f == null)
        {
            
            if (this.getFieldValue () != null)
            {
                
                this.viewer.getProjectFile (this.getFieldValue ()).delete ();
                
            }
            
            return null;
            
        }
        
        File currF = this.viewer.getProjectFile (this.getFieldValue ());
        
        boolean save = false;
        
        // Do we have a selected file and don't have a saved one?
        // Or, do we have both and have they changed?
        if ((f != null)
            &&
            ((currF == null)
             ||
             (f.compareTo (currF) != 0)
            )
           )
        {
                    
            String v = this.getFieldValue ();
            
            String fn = null;
            
            if (currF == null)
            {
                
                fn = UUID.randomUUID ().toString () + "." + Utils.getFileType (f);
                
            } else {
                
                String t = Utils.getFileType (currF);
                
                fn = v.substring (0,
                                  v.length () - t.length ()) + Utils.getFileType (f);
                
            }
                            
            try
            {
            
                // Copy the new file into place.            
                this.viewer.saveToProjectFilesDirectory (f,
                                                         fn);

            } catch (Exception e) {
                
                throw new GeneralException ("Unable to copy file: " +
                                            f +
                                            " to project files dir with filename: " +
                                            fn,
                                            e);
                
            }
            
            f = this.viewer.getProjectFile (fn);
            
        }
        
        return f.getName ();
        
    }
    
    @Override
    public String stringToValue (String s)
    {

        // It's just the file name.
        return s;
                
    }
    
    @Override
    public String valueToString (String f)
                          throws GeneralException
    {
        
        return f;
        
    }
        
    public boolean hasImage ()
    {
        
        final File pf = this.viewer.getProjectFile (this.getFieldValue ());
        
        if (pf == null)
        {
            
            return false;
            
        }
                     
        BufferedImage v = UIUtils.getImage (pf);

        return v != null;
        
    }
    
    @Override
    public Set<FormItem> getViewFormItems ()
    {
        
        final ImageUserConfigurableObjectFieldViewEditHandler _this = this;
        
        Set<FormItem> items = new LinkedHashSet ();
                
        final File pf = this.viewer.getProjectFile (this.getFieldValue ());
                
        BufferedImage v = UIUtils.getImage (pf);
                
        ImageIcon icon = null;
                
        if (v != null)
        {
            
            icon = new ImageIcon (v);
                
            if (v.getWidth () > 150)
            {
                
                v = UIUtils.getScaledImage (v,
                                            150);
                
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
                                                                                  "Unable to show image.");
                                                        
                                                        Environment.logError ("Unable to show image: " +
                                                                              pf +
                                                                              ", for: " +
                                                                              _this.field,
                                                                              e);
                                                        
                                                    }
                                                    
                                                }
                                            
                                            });

            l.setToolTipText ("Click to view the full image");
            items.add (new AnyFormItem (this.typeField.getFormName (),
                                        l));

        } else {
            
            items.add (this.createNoValueItem ());
            
        } 
        
        return items;
        
    }

}
