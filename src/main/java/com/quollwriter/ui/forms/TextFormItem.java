package com.quollwriter.ui.forms;

import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.text.*;

public class TextFormItem extends FormItem<String>
{
    
    private JTextField text = null;
    private int maxCount = -1;
    private int maxChars = -1;
    private String maxType = null;
    private String helpText = null;
        
    public TextFormItem (Object label,
                         String defaultValue)
    {
        
        this (label,
              defaultValue,
              -1,
              false,
              null);
        
    }
    
    public TextFormItem (Object  label,
                         String  defaultValue,
                         int     maxChars,
                         boolean requireValue,
                         String  helpText)
    {
        
        super (label,
               requireValue,
               null);

        this.maxChars = maxChars;
        this.maxType = maxType;
        this.helpText = helpText;
        
        this.text = UIUtils.createTextField ();
        
        if (defaultValue != null)
        {
            
            this.text.setText (defaultValue);
            
        } 
        
        if (this.helpText != null)
        {
            
            this.text.setToolTipText (this.helpText);
            
        }
        
    }
       
    public void setDoOnReturnPressed (ActionListener l)
    {
        
        UIUtils.addDoActionOnReturnPressed (this.text,
                                            l);                
       
    }
    
    @Override
    public void grabFocus ()
    {
        
        this.text.grabFocus ();   
        
    }
    
    @Override
    public JComponent getComponent ()
    {
        
        return this.text;
        
    }
    
    public JTextField getTextField ()
    {
        
        return this.text;
        
    }
    
    @Override
    public String getValue ()
    {
        
        String v = this.text.getText ().trim ();
        
        if (v.length () == 0)
        {
            
            return null;
            
        }
        
        return v;
    
    }
    
    public String getText ()
    {
        
        return this.getValue ();
        
    }
    
    public void setText (String v)
    {
        
        this.text.setText (v);
        
    }
    
    @Override
    public boolean hasError ()
    {
        
        boolean err = false;
        
        String v = this.getValue ();
        
        if ((this.helpText != null)
            &&
            (this.helpText.equals (v))
           )
        {
            
            v = null;
            
        }
        
        if ((v == null)
            &&
            (this.requireValue)
           )
        {
            
            err = true;
            
        }
        
        if (this.maxType == null)
        {
            
            return err;
            
        }
        
        if ((v != null)
            &&
            (this.maxType != null)
           )
        {
            
            if (this.maxType.equals ("chars"))
            {
                
                if (v.length () > this.maxCount)
                {
                    
                    err = true;
                    
                }
                
            }
            
            if (this.maxType.equals ("words"))
            {
                
                if (TextUtilities.getWordCount (v) > this.maxCount)
                {
                    
                    err = true;
                    
                }
                
            }
            
        }
        
        return err;
                    
    }
        
}
