package com.quollwriter.ui.forms;

import java.io.*;

import java.awt.Dimension;

import javax.swing.*;

import com.quollwriter.ui.*;

public class ImageSelectorFormItem extends FormItem<File>
{
    
    private ImageSelector im = null;
    private String fileTypes = null;
    
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
        
        this.fileTypes = b.toString ();
                   
        this.im = new ImageSelector (defaultValue,
                                     fileTypes,
                                     size);
        
        this.im.setBorder (UIUtils.createLineBorder ());
        
    }
    
    public ImageSelector getImageSelector ()
    {
        
        return this.im;
        
    }
    
    public boolean hasError ()
    {
        
        if (this.notNull)
        {
            
            return (this.getValue () == null);
            
        }
        
        return false;
        
    }
    
    public JComponent getComponent ()
    {
        
        return this.im;
        
    }
    
    public File getValue ()
    {
        
        return this.im.getFile ();
        
    }
    
    public void updateRequireLabel (JLabel l)
    {
                    
        l.setText (String.format ("(%s)",
                                  this.fileTypes));
        
    }
    
}
