package com.quollwriter.ui.actionHandlers;

import java.awt.event.*;
import java.awt.font.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.renderers.*;

public class AssetActionHandler extends ProjectViewerActionHandler<ProjectViewer>
{

    private static Map<String, Class> actionHandlers = new HashMap ();

    private JTextField       nameField = null;
    private TextArea descField = null;    
    private int              showAt = -1;
    private boolean          displayAfterSave = false;
    private DetailsEditPanel delegate = null;

    public AssetActionHandler (Asset         a,
                               ProjectViewer pv,
                               int           mode)
    {

        super (a,
               pv,
               mode,
               true);

        this.initFormItems ();
               
        try
        {

            this.delegate = AssetViewPanel.getEditDetailsPanel (a,
                                                                pv);

        } catch (Exception e)
        {

            Environment.logError ("Unable to create edit details panel delegate for: " +
                                  a,
                                  e);

            UIUtils.showErrorMessage (pv,
                                      "Unable to show form.");

        }

        this.setPopupOver (pv);
        
    }

    public void setDisplayAfterSave (boolean v)
    {

        this.displayAfterSave = v;

    }

    public boolean isDisplayAfterSave ()
    {

        return this.displayAfterSave;

    }

    public int getShowAtPosition ()
    {

        return -1;

    }

    public JTextField getFocussedField ()
    {

        return this.nameField;

    }

    public String getIcon (int mode)
    {

        return this.dataObject.getObjectType ();

    }

    public String getTitle (int mode)
    {

        if (mode == AbstractActionHandler.EDIT)
        {

            return "Edit " + Environment.getObjectTypeName (this.dataObject);

        }

        return "Add New " + Environment.getObjectTypeName (this.dataObject);

    }

    private void initFormItems ()
    {

        this.nameField = UIUtils.createTextField ();
        this.descField = UIUtils.createTextArea (this.projectViewer,
                                                 null,
                                                 //String.format ("Describe the {%s} here.",
                                                 //               this.dataObject.getObjectType ()),
                                                 5,
                                                 -1);        
        this.descField.setCanFormat (true);
        
        this.descField.setAutoGrabFocus (false);        
            
    }

    public List<FormItem> getFormItems (int         mode,
                                        String      selectedText,
                                        NamedObject obj)
    {

        final AssetActionHandler _this = this;    
    
        ActionListener doSave = new ActionListener ()
        {
          
            @Override
            public void actionPerformed (ActionEvent ev)
            {
                
                _this.submitForm ();
                
            }
            
        };
        
        UIUtils.addDoActionOnReturnPressed (this.nameField,
                                            doSave);
        UIUtils.addDoActionOnReturnPressed (this.descField,
                                            doSave);
    
        List<FormItem> formFields = new ArrayList ();

        formFields.add (new FormItem ("Name",
                                      this.nameField));

        if (selectedText != null)
        {

            this.nameField.setText (selectedText.trim ());

        }

        if (this.delegate != null)
        {

            this.delegate.fillForEdit ();

            formFields.addAll (this.delegate.getExtraEditItems (doSave)); // mode));

        }

        formFields.add (new FormItem ("Description",
                                      this.descField));

        if (mode == AbstractActionHandler.EDIT)
        {

            Asset a = (Asset) obj;

            this.nameField.setText (a.getName ());
                        
            this.descField.setTextWithMarkup (a.getDescription ());

        } else
        {

            if (selectedText != null)
            {

                this.nameField.setText (selectedText);

            } else {                
                
                Asset a = (Asset) obj;
    
                if (a.getName () != null)
                {
    
                    this.nameField.setText (a.getName ());
                    
                }
                
                if (a.getDescription () != null)
                {
                
                    this.descField.setTextWithMarkup (a.getDescription ());
                    
                }
                
            }

        }

        return formFields;

    }

    public void handleCancel (int mode)
    {

        // Nothing to do.

    }

    public boolean handleSave (Form f,
                               int  mode)
    {

        String n = this.nameField.getText ().trim ();

        if (n.equals (""))
        {

            f.showError ("Please select a name.");

            return false;

        }

        Set<Asset> matches = this.projectViewer.getProject ().getAllAssetsByName (n.toLowerCase (),
                                                                                  this.dataObject.getObjectType ());
                
        Asset match = null;
        
        if (mode == AbstractActionHandler.ADD)
        {

            if (matches.size () > 0)
            {
        
                match = matches.iterator ().next ();

            }

        } else {
            
            if (matches.size () > 0)
            {
                                
                for (Asset a : matches)
                {
                    
                    if (!a.getKey ().equals (this.dataObject.getKey ()))
                    {
                        
                        match = a;
                        
                    }
                    
                }
                
            }
            
        }

        if (match != null)
        {
            
            f.showError (Environment.replaceObjectNames (String.format ("Already have a {%s} called: <b>%s</b>",
                                                                        this.dataObject.getObjectType (),
                                                                        match.getName ())));

            return false;            
            
        }
        
        if (this.delegate != null)
        {

            if (!this.delegate.canSave ())
            {

                return false;

            }
/*
            if (!this.delegate.handleErrors (mode,
                                             this.projectViewer))
            {

                return false;

            }
*/
        }

        Asset asset = (Asset) this.dataObject;

        Set<String> oldNames = asset.getAllNames ();

        // Fill up the object.
        asset.setName (n);
        asset.setDescription (this.descField.getTextWithMarkup ());

        if (this.delegate != null)
        {

            this.delegate.fillForSave ();

        }

/*
        if (this.delegate != null)
        {

            this.delegate.fillAsset ((Asset) this.dataObject);

        }
*/
        asset.setProject (this.projectViewer.getProject ());

        if (mode == AbstractActionHandler.ADD)
        {

            try
            {

                this.projectViewer.getProject ().addAsset (asset);

                this.projectViewer.saveObject (asset,
                                               true);

                this.projectViewer.openObjectSection (asset.getObjectType ());                                               
                                               
                this.projectViewer.fireProjectEvent (asset.getObjectType (),
                                                     ProjectEvent.NEW,
                                                     asset);

            } catch (Exception e)
            {

                Environment.logError ("Unable to add new: " +
                                      asset +
                                      ", with name: " +
                                      this.nameField.getText (),
                                      e);

                UIUtils.showErrorMessage (this.projectViewer,
                                          "An internal error has occurred.\n\nUnable to add new " + Environment.getObjectTypeName (asset).toLowerCase () + ".");

                return false;

            }

        } else {

            try
            {

                this.projectViewer.saveObject (asset,
                                               true);

                this.projectViewer.fireProjectEvent (asset.getObjectType (),
                                                     ProjectEvent.EDIT,
                                                     asset);
    
            } catch (Exception e)
            {
    
                Environment.logError ("Unable to save asset: " +
                                      asset,
                                      e);
    
                UIUtils.showErrorMessage (this.projectViewer,
                                          "An internal error has occurred.\n\nUnable to save " + Environment.getObjectTypeName (asset).toLowerCase () + ".");
    
                return false;
    
            }

        }
            
        this.projectViewer.updateProjectDictionaryForNames (oldNames,
                                                            asset);

        if (this.displayAfterSave)
        {

            this.projectViewer.viewObject (asset);

        }

        this.projectViewer.reloadTreeForObjectType (asset);
        
        return true;
    
    }

}
