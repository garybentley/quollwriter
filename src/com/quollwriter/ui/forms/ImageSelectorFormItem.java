package com.quollwriter.ui.forms;

import java.io.*;

import java.awt.Dimension;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import com.quollwriter.ui.*;

public class ImageSelectorFormItem extends FormItem<File>
{
    
    private ImageSelector im = null;
    
    public ImageSelectorFormItem (String                  label,
                                  FileNameExtensionFilter filter,
                                  File                    defaultValue,
                                  Dimension               size,
                                  boolean                 fileRequired,
                                  String                  helpText)
    {

        super (label,
               fileRequired,
               helpText);

        this.im = new ImageSelector (defaultValue,
                                     filter,
                                     size);
        
        this.im.setBorder (UIUtils.createLineBorder ());

        if (size != null)
        {
            
            this.im.setPreferredSize (new Dimension (size.width + 2,
                                                     size.height + 2));
               
        }
               
    }
    
    public ImageSelectorFormItem (String                  label,
                                  FileNameExtensionFilter filter,
                                  BufferedImage           defaultValue,
                                  Dimension               size,
                                  boolean                 fileRequired,
                                  String                  helpText)
    {

        super (label,
               fileRequired,
               helpText);

        this.im = new ImageSelector (defaultValue,
                                     filter,
                                     size);
        
        this.im.setBorder (UIUtils.createLineBorder ());
        
        if (size != null)
        {
            
            this.im.setPreferredSize (new Dimension (size.width + 2,
                                                     size.height + 2));
               
        }
        
    }

    public ImageSelectorFormItem (String                 label,
                                  java.util.List<String> fileTypes,
                                  File                   defaultValue,
                                  Dimension              size,
                                  boolean                fileRequired,
                                  String                 helpText)
    {
        
        super (label,
               fileRequired,
               helpText);

        StringBuilder b = new StringBuilder ();
        
        for (String t : fileTypes)
        {
            
            if (b.length () > 0)
            {
                
                b.append (", ");
                
            }
            
            b.append (t);
            
        }
                           
        this.im = new ImageSelector (defaultValue,
                                     fileTypes,
                                     size);
        
        this.im.setBorder (UIUtils.createLineBorder ());

        if (size != null)
        {
            
            this.im.setPreferredSize (new Dimension (size.width + 2,
                                                     size.height + 2));
               
        }
        
    }
    
    public ImageSelector getImageSelector ()
    {
        
        return this.im;
        
    }
    
    public boolean hasError ()
    {
        
        if (this.requireValue)
        {
            
            return (this.getValue () == null);
            
        }
        
        return false;
        
    }
    
    public void addChangeListener (ChangeListener l)
    {
        
        this.im.addChangeListener (l);
        
    }
    
    @Override
    public JComponent getComponent ()
    {
        
        return this.im;
        
    }
    
    public BufferedImage getImage ()
    {
        
        return this.im.getImage ();
        
    }
    
    @Override
    public File getValue ()
    {
        
        return this.im.getFile ();
        
    }
        
}
