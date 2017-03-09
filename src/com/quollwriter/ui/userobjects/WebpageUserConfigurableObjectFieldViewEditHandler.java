package com.quollwriter.ui.userobjects;

import java.awt.event.*;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class WebpageUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<WebpageUserConfigurableObjectTypeField, String>
{
    
    private TextFormItem editItem = null;
    
    public WebpageUserConfigurableObjectFieldViewEditHandler (WebpageUserConfigurableObjectTypeField typeField,
                                                              UserConfigurableObject                 obj,
                                                              UserConfigurableObjectField            field,
                                                              AbstractProjectViewer                  viewer)
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
    
        this.editItem = new TextFormItem (this.typeField.getFormName (),
                                          (this.field != null ? (String) this.field.getValue () : initValue));
    
        UIUtils.addDoActionOnReturnPressed (this.editItem.getTextField (),
                                            formSave);
    
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
    {
        
        return this.editItem.getValue ();
        
    }
    
    @Override
    public String valueToString (String val)
    {
        
        return val;
        
    }
    
    @Override
    public String stringToValue (String s)
    {
        
        return s;
        
    }
    
    @Override
    public Set<FormItem> getViewFormItems ()
    {
        
        Set<FormItem> items = new LinkedHashSet ();
                
        String value = (this.field != null ? (String) this.field.getValue () : null);
                
        if (value != null)
        {                
        
            if ((!value.startsWith ("http://"))
                &&
                (!value.startsWith ("https://"))
               )
            {
                
                value = "http://" + value;
                
            }
        
            items.add (new AnyFormItem (this.typeField.getFormName (),
                                        UIUtils.createClickableLabel (value,
                                                                      null,
                                                                      value)));
        
        } else {
            
            items.add (this.createNoValueItem ());
            
        }
        
        return items;
        
    }

}
