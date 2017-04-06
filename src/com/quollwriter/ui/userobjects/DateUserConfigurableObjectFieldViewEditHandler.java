package com.quollwriter.ui.userobjects;

import java.awt.event.*;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Date;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class DateUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<DateUserConfigurableObjectTypeField, Date>
{
    
    private DateFormItem editItem = null;
    
    public DateUserConfigurableObjectFieldViewEditHandler (DateUserConfigurableObjectTypeField typeField,
                                                           UserConfigurableObject              obj,
                                                           UserConfigurableObjectField         field,
                                                           AbstractProjectViewer               viewer)
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
                
        this.editItem = new DateFormItem  (this.typeField.getFormName (),
                                           this.typeField.getMinimum (),
                                           this.typeField.getMaximum (),
                                           this.typeField.getDefault ());
        
        if (this.getFieldValue () != null)
        {
        
            this.editItem.setDate (this.getFieldValue ());
            
        }
        
        if (initValue != null)
        {
            
            this.editItem.setDate (this.stringToValue (initValue));
        
        }
        
        items.add (this.editItem);
        
        return items;
        
    }
    
    @Override    
    public Set<String> getInputFormItemErrors ()
    {
        
        return null;
        
    }
    
    @Override
    public Date getInputSaveValue ()
    {
        
        return this.editItem.getValue ();
        
    }
    
    @Override
    public Date stringToValue (String s)
    {
    
        if (s == null)
        {
            
            return null;
            
        }
    
        return Environment.parseDate (s);
            
    }
    
    @Override
    public String valueToString (Date val)
    {
        
        if (val == null)
        {
            
            return null;
            
        }
        
        return Environment.formatDate (val);
        
    }
    
    @Override
    public Set<FormItem> getViewFormItems ()
    {
        
        Set<FormItem> items = new LinkedHashSet ();
                
        Date d = this.getFieldValue ();
        
        if (d != null)
        {
          
            items.add (new AnyFormItem (this.typeField.getFormName (),
                                        UIUtils.createLabel (Environment.formatDate (d))));

        } else {
            
            items.add (this.createNoValueItem ());
            
        } 
        
        return items;
        
    }

}
