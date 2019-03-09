package com.quollwriter.ui.userobjects;

import java.awt.event.*;

import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.UUID;

import java.io.*;

import javax.swing.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.data.*;

public class FileUserConfigurableObjectFieldViewEditHandler extends AbstractUserConfigurableObjectFieldViewEditHandler<FileUserConfigurableObjectTypeField, String>
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
                                                  this.viewer.getProjectFile ((this.getFieldValue () != null ? this.getFieldValue () : initValue)),                                                  
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
    public String getInputSaveValue ()
                              throws GeneralException
    {
        
        File f = this.editItem.getValue ();

        if (f == null)
        {
            
            if (this.getFieldValue () != null)
            {
                
                this.viewer.getProjectFile (this.getFieldValue ()).delete ();
                
            }
            
            return null;
            
        }
        
        File currF = this.viewer.getProjectFile (this.getFieldValue ());
        
        boolean save = false;
        
        // Do we have a selected file and don't have a saved one?
        // Or, do we have both and have they changed?
        if ((f != null)
            &&
            ((currF == null)
             ||
             (f.compareTo (currF) != 0)
            )
           )
        {
                    
            String v = this.getFieldValue ();
            
            String fn = null;
            
            if (currF == null)
            {
                
                fn = UUID.randomUUID ().toString () + "." + Utils.getFileType (f);
                
            } else {
                
                String t = Utils.getFileType (currF);
                
                fn = v.substring (0,
                                  v.length () - t.length ()) + Utils.getFileType (f);
                
            }
                            
            try
            {
            
                // Copy the new file into place.            
                this.viewer.saveToProjectFilesDirectory (f,
                                                         fn);

            } catch (Exception e) {
                
                throw new GeneralException ("Unable to copy file: " +
                                            f +
                                            " to project files dir with filename: " +
                                            fn,
                                            e);
                
            }
            
            f = this.viewer.getProjectFile (fn);
            
        }
        
        return f.getName ();
        
    }
    
    @Override
    public String stringToValue (String s)
    {

        // It's just the file name.
        return s;
                
    }
    
    @Override
    public String valueToString (String f)
                          throws GeneralException
    {
        
        return f;
        
    }
    
    @Override
    public Set<FormItem> getViewFormItems ()
    {
        
        Set<FormItem> items = new LinkedHashSet ();
        
        File f = null;
        
        if (this.getFieldValue () != null)
        {
            
            f = this.viewer.getProjectFile (this.getFieldValue ());
            
        }
                        
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
