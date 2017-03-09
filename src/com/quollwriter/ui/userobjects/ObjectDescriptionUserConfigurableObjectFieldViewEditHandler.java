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
import com.quollwriter.ui.components.QTextEditor;

public class ObjectDescriptionUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<ObjectDescriptionUserConfigurableObjectTypeField, StringWithMarkup>
{
    
    private MultiLineTextFormItem editItem = null;
    
    public ObjectDescriptionUserConfigurableObjectFieldViewEditHandler (ObjectDescriptionUserConfigurableObjectTypeField typeField,
                                                                        UserConfigurableObject                           obj,
                                                                        UserConfigurableObjectField                      field,
                                                                        AbstractProjectViewer                            viewer)
    {
        
        super (typeField,
               obj,
               field,
               viewer);
                                
    }
             
    @Override
    public void updateFieldFromInput ()
    {
       
        this.obj.setDescription (this.getInputSaveValue ());
     
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
                                                   10);
            
        this.editItem.setCanFormat (true);
        this.editItem.setAutoGrabFocus (false);
            
        UIUtils.addDoActionOnReturnPressed (this.editItem.getTextArea (),
                                            formSave);
        
        if (this.typeField.isNameField ())
        {
            
            this.editItem.setSpellCheckEnabled (false);
                                                   
        }
        
        this.editItem.setText (this.obj.getDescription ());
        
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

        StringWithMarkup text = this.obj.getDescription ();
            
        Set<FormItem> items = new LinkedHashSet ();

        FormItem item = null;
        
        if (text != null)
        {
        
            JComponent t = UIUtils.createObjectDescriptionViewPane (text,
                                                                    this.obj,
                                                                    this.viewer,
                                                                    null);
    
            t.setSize (new java.awt.Dimension (250, 250));
            //t.setPreferredSize (new java.awt.Dimension (250, 250));
            //t.setPreferredSize (null);
            t.setBorder (null);
                                                                    
            item = new AnyFormItem (this.typeField.getFormName (),
                                    t);

        } else {
            
            item = this.createNoValueItem ();
            
        }

        items.add (item);
        
        return items;
        
    }
    
    public QTextEditor getInputTextComponent (ActionListener formSave)
    {

        Set<FormItem> its = this.getInputFormItems (null,
                                                    formSave);
                
        QTextEditor ed = this.editItem.getTextArea ().getEditor ();
                            
        ed.setBorder (new JScrollPane ().getBorder ());
        ed.setMargin (new java.awt.Insets (3, 3, 3, 3));
        ed.setMaximumSize (new java.awt.Dimension (Short.MAX_VALUE, Short.MAX_VALUE));
        
        return ed;
    
    }
    
    public JComponent getViewTextComponent ()
    {
        
        return this.getViewFormItems ().iterator ().next ().getComponent ();
        
    }
    
}
