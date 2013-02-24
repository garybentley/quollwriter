package com.quollwriter.ui.actionHandlers;

import java.awt.*;
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
import com.quollwriter.ui.renderers.*;


public class AssetActionHandler extends ProjectViewerActionHandler
{

    private static Map<String, Class> actionHandlers = new HashMap ();

    private JTextField       nameField = UIUtils.createTextField ();
    private JTextArea        descField = UIUtils.createTextArea (-1);
    private int              showAt = -1;
    private boolean          displayAfterSave = false;
    private DetailsEditPanel delegate = null;

    public AssetActionHandler(Asset         a,
                              ProjectViewer pv,
                              int           mode)
    {

        super (a,
               pv,
               mode,
               true);

        try
        {

            this.delegate = AssetViewPanel.getEditDetailsPanel (a,
                                                                this.projectViewer);

        } catch (Exception e)
        {

            Environment.logError ("Unable to create edit details panel delegate for: " +
                                  a,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Unable to show form.");

        }

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

    }

    public List<FormItem> getFormItems (int         mode,
                                        String      selectedText,
                                        NamedObject obj)
    {

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

            formFields.addAll (this.delegate.getExtraEditItems ()); // mode));

            /*
                                                        selectedText,
                                                        (Asset) obj));
             */
        }

        this.descField.setRows (5);
        this.descField.setLineWrap (true);
        this.descField.setWrapStyleWord (true);

        formFields.add (new FormItem ("Description",
                                      new JScrollPane (this.descField)));

        final AssetActionHandler _this = this;

        this.nameField.addKeyListener (new KeyAdapter ()
            {

                public void keyPressed (KeyEvent ev)
                {

                    if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                    {

                        // This is the same as save for the form.
                        _this.submitForm ();

                    }

                }

            });

        if (mode == AbstractActionHandler.EDIT)
        {

            Asset a = (Asset) obj;

            this.nameField.setText (a.getName ());
            this.descField.setText (a.getDescription ());

        } else
        {

            if (selectedText != null)
            {

                this.nameField.setText (selectedText);

            } else {                
                
                Asset a = (Asset) obj;
    
                this.nameField.setText (a.getName ());
                this.descField.setText (a.getDescription ());
                
            }

        }

        return formFields;

    }

    public void handleCancel (int mode)
    {

        // Nothing to do.

    }

    public boolean handleSave (int mode)
    {

        String n = this.nameField.getText ().trim ();

        if (n.equals (""))
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Please select a name.");

            return false;

        }

        if (mode == AbstractActionHandler.ADD)
        {

            Asset other = this.projectViewer.getProject ().getAssetByName (n.toLowerCase (),
                                                                           this.dataObject.getObjectType ());

            if (other != null)
            {

                UIUtils.showErrorMessage (this.projectViewer,
                                          "Already have a " + Environment.getObjectTypeName (this.dataObject).toLowerCase () + " called: " +
                                          other.getName ());

                return false;

            }

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
        asset.setDescription (this.descField.getText ().trim ());

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

            this.projectViewer.viewAsset (asset);

        }

        this.projectViewer.reloadAssetTree (asset);
        
        return true;
    
    }

}
