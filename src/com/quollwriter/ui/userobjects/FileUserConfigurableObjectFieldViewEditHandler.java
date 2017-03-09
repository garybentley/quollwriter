package com.quollwriter.ui.userobjects;

import java.awt.event.*;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import java.io.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class FileUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<FileUserConfigurableObjectTypeField, File>
{
    
    private FileSelectorFormItem editItem = null;
    
    public FileUserConfigurableObjectFieldViewEditHandler (FileUserConfigurableObjectTypeField typeField,
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
        
        this.editItem = new FileSelectorFormItem (this.typeField.getFormName (),
                                                  (this.getFieldValue () != null ? this.getFieldValue () : new File (initValue)),
                                                  false,
                                                  JFileChooser.FILES_ONLY);
                                                  
        items.add (this.editItem);
        
        return items;
        
    }
    
    @Override
    public Set<String> getInputFormItemErrors ()
    {
        
        return null;
        
    }
    
    @Override
    public File getInputSaveValue ()
    {
        
        return this.editItem.getValue ();
        
    }
    
    @Override
    public File stringToValue (String s)
    {
    
        if (s == null)
        {
            
            return null;
            
        }
    
        return new File (s);
            
    }
    
    @Override
    public String valueToString (File val)
    {
        
        if (val == null)
        {
            
            return null;
            
        }
        
        return val.getPath ();
        
    }
    
    @Override
    public Set<FormItem> getViewFormItems ()
    {
        
        Set<FormItem> items = new LinkedHashSet ();
        
        File f = (this.field != null ? (File) this.field.getValue () : null);
                
        String url = null;
        
        if (f != null)
        {
        
            try
            {
                
                url = f.toURI ().toURL ().toExternalForm ();
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to convert file to a url string: " +
                                      f,
                                      e);
                
                url = "file://" + f.getPath ();
                
            }

        }
        
        if (url != null)
        {
                    
            items.add (new AnyFormItem (this.typeField.getFormName (),
                                        UIUtils.createClickableLabel (f.getName (),
                                                                      null,
                                                                      url)));

        } else {
            
            items.add (this.createNoValueItem ());
            
        } 
        
        return items;
        
    }

}
