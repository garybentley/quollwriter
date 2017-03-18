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

public class MultiTextUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<MultiTextUserConfigurableObjectTypeField, StringWithMarkup>
{

    private MultiLineTextFormItem editItem = null;
    
    public MultiTextUserConfigurableObjectFieldViewEditHandler (MultiTextUserConfigurableObjectTypeField typeField,
                                                                UserConfigurableObject                   obj,
                                                                UserConfigurableObjectField              field,
                                                                AbstractProjectViewer                    viewer)
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
            
            this.editItem.getTextArea ().grabFocus ();
                  
        }
    }
    
    @Override
    public Set<FormItem> getInputFormItems (String         initValue,
                                            ActionListener formSave)
    {
        
        this.editItem = new MultiLineTextFormItem (this.typeField.getFormName (),
                                                   this.viewer,
                                                   (this.typeField.isNameField () ? 3 : 10));
            
        this.editItem.setCanFormat (true);
        this.editItem.setAutoGrabFocus (false);
            
        UIUtils.addDoActionOnReturnPressed (this.editItem.getTextArea (),
                                            formSave);
        
        if (this.typeField.isNameField ())
        {
            
            this.editItem.setSpellCheckEnabled (false);
                                                   
        }

        if (this.getFieldValue () != null)
        {
        
            this.editItem.setText (this.getFieldValue ()); 
        
        } else {
            
            if (initValue != null)
            {
                
                this.editItem.setText (initValue);
                
            }
            
        }
        
        Set<FormItem> items = new LinkedHashSet ();

        items.add (this.editItem);
        
        return items;
        
    }
    
    @Override
    public Set<String> getInputFormItemErrors ()
    {
        
        return null;
        
    }
    
    @Override
    public StringWithMarkup getInputSaveValue ()
    {
        
        return this.editItem.getValue ();
        
    }
    
    @Override
    public String valueToString (StringWithMarkup val)
                          throws GeneralException
    {
        
        try
        {
            
            return JSONEncoder.encode (val);
        
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to encode to string",
                                        e);
            
        }
        
    }
    
    @Override
    public StringWithMarkup stringToValue (String s)
    {

        return JSONDecoder.decodeToStringWithMarkup (s);
        
    }
    
    @Override
    public Set<FormItem> getViewFormItems ()
    {

        StringWithMarkup text = (this.field != null ? (StringWithMarkup) this.field.getValue () : null);
            
        Set<FormItem> items = new LinkedHashSet ();

        FormItem item = null;
        
        if (text != null)
        {
        
            JComponent t = UIUtils.createObjectDescriptionViewPane (text,
                                                                    this.field.getParentObject (),
                                                                    this.viewer,
                                                                    null);
    
            t.setBorder (null);
                                                                    
            item = new AnyFormItem (this.typeField.getFormName (),
                                    t);

        } else {
            
            item = this.createNoValueItem ();
            
        }

        items.add (item);
        
        return items;
        
    }

}
