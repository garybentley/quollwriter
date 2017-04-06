package com.quollwriter.ui.userobjects;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public abstract class AbstractUserConfigurableObjectFieldViewEditHandler<E extends UserConfigurableObjectTypeField, T> implements UserConfigurableObjectFieldViewEditHandler<E, T>
{

    protected UserConfigurableObjectField field = null;
    protected E typeField = null;
    protected AbstractProjectViewer viewer = null;
    protected UserConfigurableObject obj = null;
         
    public AbstractUserConfigurableObjectFieldViewEditHandler (E                           typeField,
                                                               UserConfigurableObject      obj,
                                                               UserConfigurableObjectField field,
                                                               AbstractProjectViewer       viewer)
    {
        
        this.field = field;
        this.viewer = viewer;
        
        this.typeField = typeField;
        
        if (obj == null)
        {
            
            throw new NullPointerException ("User object cannot be null.");
            
        }
        
        this.obj = obj;
        
    }

    @Override
    public Set<String> getNamesFromFieldValue ()
    {
        
        return new LinkedHashSet ();
        
    }

    @Override
    public void updateFieldFromInput ()
                               throws GeneralException
    {
        
        if (this.field == null)
        {
            
            this.field = new UserConfigurableObjectField (this.typeField);
            
            this.obj.addField (this.field);
            
        }
        
        this.field.setValue (this.getInputSaveValue ());
        
    }    
    
    @Override
    public T getFieldValue ()
    {
        
        return (this.field != null ? (T) this.field.getValue () : null);
        
    }
    
    @Override
    public UserConfigurableObjectField getField ()
    {
        
        return this.field;
        
    }
    
    @Override
    public E getTypeField ()
    {
        
        return this.typeField;
        
    }
    
    public FormItem createNoValueItem ()
    {
    
        return new AnyFormItem (this.typeField.getFormName (),
                                UIUtils.createInformationLabel ("Not provided."));
        
    }
    
}
