package com.quollwriter.ui;

import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.panels.*;

public abstract class DetailsEditPanel extends EditPanel
{

    private AbstractObjectViewPanel viewPanel = null;
    private JTextArea               descEdit = null;
    private JTextField              nameEdit = null;
    private JTextPane               desc = null;
    protected AbstractProjectViewer projectViewer = null;
    protected NamedObject           object = null;

    public DetailsEditPanel(NamedObject           n,
                            AbstractProjectViewer pv)
    {

        super (false);

        this.projectViewer = pv;
        this.object = n;

        this.descEdit = UIUtils.createTextArea (-1);
        this.descEdit.setWrapStyleWord (true);
        this.descEdit.setLineWrap (true);
        this.nameEdit = UIUtils.createTextField ();

    }

    public void init (AbstractObjectViewPanel avp)
    {

        this.desc = UIUtils.createObjectDescriptionViewPane (this.getViewDescription (),
                                                             this.object,
                                                             this.projectViewer,
                                                             avp);

        this.viewPanel = avp;

        this.init ();

        final DetailsEditPanel _this = this;

        this.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    if (ev.getID () == EditPanel.EDIT_VISIBLE)
                    {

                        _this.viewPanel.setHasUnsavedChanges (_this,
                                                              true);

                    }

                    if ((ev.getID () == EditPanel.CANCELLED) ||
                        (ev.getID () == EditPanel.VIEW_VISIBLE) ||
                        (ev.getID () == EditPanel.SAVED))
                    {

                        _this.viewPanel.setHasUnsavedChanges (_this,
                                                              false);

                    }

                }

            });

    }

    public abstract String getEditHelpText ();

    public abstract void fillForSave ();

    public abstract void fillForEdit ();

    public abstract boolean canSave ();

    public abstract List<FormItem> getExtraEditItems ();

    public abstract List<FormItem> getExtraViewItems ();

    public String getViewDescription ()
    {
        
        return this.object.getDescription ();
        
    }
    
    public void refreshViewPanel ()
    {

        this.desc.setText (UIUtils.getWithHTMLStyleSheet (this.desc,
                                                          UIUtils.markupStringForAssets (this.getViewDescription (),
                                                                                         this.projectViewer.getProject (),
                                                                                         this.object)));

    }

    public boolean handleSave ()
    {

        // Check name...
        String n = this.nameEdit.getText ().trim ();

        if (n.equals (""))
        {

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Please select a name.");

            this.nameEdit.setText (this.object.getName ());
            this.nameEdit.selectAll ();

            return false;

        }

        if (!this.canSave ())
        {

            return false;

        }

        Set<String> oldNames = this.object.getAllNames ();

        this.object.setName (this.nameEdit.getText ());

        this.object.setDescription (this.descEdit.getText ());

        this.fillForSave ();

        try
        {

            this.projectViewer.saveObject (this.object,
                                           true);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save: " +
                                  this.object,
                                  e);

            UIUtils.showErrorMessage (this.projectViewer,
                                      "Unable to save.");

            return false;

        }

        // Update the names in the project dictionary.
        if (this.object instanceof Asset)
        {
            
            this.projectViewer.updateProjectDictionaryForNames (oldNames,
                                                                this.object);
            
        }

        // May need to invoke this later since it might be labour intensive.
        if (this.viewPanel != null)
        {

            this.viewPanel.refresh (this.object);

        }

        return true;

    }

    public boolean handleCancel ()
    {

        return true;

    }

    public void handleEditStart ()
    {

        this.descEdit.setText (this.object.getDescription ());

        this.descEdit.grabFocus ();

        this.nameEdit.setText (this.object.getName ());

        this.fillForEdit ();

    }

    public IconProvider getIconProvider ()
    {

        DefaultIconProvider iconProv = new DefaultIconProvider ();
        iconProv.putIcon ("header",
                          "information");

        return iconProv;

    }

    public String getHelpText ()
    {

        String ht = this.getEditHelpText ();

        return "Press the Edit icon again to save the information." + ((ht != null) ? ("  " + ht) : "");

    }

    public String getTitle ()
    {

        return "About";

    }

    public List<FormItem> getEditItems ()
    {

        // this.initEditPanel ();

        List<FormItem> items = new ArrayList ();

        items.add (new FormItem ("Name",
                                 this.nameEdit));

        List<FormItem> extra = this.getExtraEditItems ();

        if (extra != null)
        {

            items.addAll (extra);

        }

        items.add (new FormItem ("Description",
                                 this.descEdit,
                                 "fill:50dlu:grow"));

        return items;

    }

    public JComponent getEditPanel ()
    {

        return null;

    }

    public JComponent getViewPanel ()
    {

        return null;

    }

    public List<FormItem> getViewItems ()
    {

        List<FormItem> items = new ArrayList ();

        List<FormItem> extra = this.getExtraViewItems ();

        if (extra != null)
        {

            items.addAll (extra);

        }

        items.add (new FormItem (null, //"Description",
                                 this.desc,
                                 "fill:50px:grow"));

        return items;

    }

}
