package com.quollwriter.ui.forms;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;

import java.io.*;

import javax.swing.*;
import javax.swing.text.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.text.*;

public class FileSelectorFormItem extends FormItem<File>
{
    
    private FileFinder find = null;
        
    public FileSelectorFormItem (Object  label,
                                 File    defaultValue,
                                 boolean requireValue,
                                 int     selectionMode)
    {
        
        super (label,
               requireValue,
               null);

        this.find = new FileFinder ();
        
        this.find.setFinderSelectionMode ((selectionMode > -1 ? selectionMode : JFileChooser.FILES_ONLY));
        
        this.find.init ();
                
    }
        
    @Override
    public JComponent getComponent ()
    {
        
        return this.find;
        
    }
    
    @Override
    public File getValue ()
    {
        
        return this.find.getSelectedFile ();
            
    }
        
    @Override
    public boolean hasError ()
    {
        
        boolean err = false;
                
        return err;
                    
    }
        
}
