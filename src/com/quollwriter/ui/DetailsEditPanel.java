package com.quollwriter.ui;

import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.ui.panels.*;

public abstract class DetailsEditPanel extends EditPanel
{

    private AbstractObjectViewPanel viewPanel = null;
    private TextArea                descEdit = null;
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

        this.descEdit = UIUtils.createTextArea (pv,
                                                String.format ("Describe the {%s} here.",
                                                               n.getObjectType ()),
                                                -1,
                                                -1);

        this.descEdit.setCanFormat (true);
        this.descEdit.setAutoGrabFocus (false);

        try
        {

            this.descEdit.setSynonymProvider (pv.getSynonymProvider ());

        } catch (Exception e) {

            Environment.logError ("Unable to set synonym provider for details edit panel for: " +
                                  n,
                                  e);

        }

        this.nameEdit = UIUtils.createTextField ();

        UIUtils.addDoActionOnReturnPressed (this.descEdit,
                                            this.getDoSaveAction ());
        UIUtils.addDoActionOnReturnPressed (this.nameEdit,
                                            this.getDoSaveAction ());

        InputMap im = this.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_S,
                                        InputEvent.CTRL_MASK),
                "save");

        final DetailsEditPanel _this = this;

        ActionMap am = this.getActionMap ();

        am.put ("save",
                new ActionAdapter ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                _this.getDoSaveAction ().actionPerformed (ev);

            }

        });

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

    public abstract List<FormItem> getExtraEditItems (ActionListener onSave);

    public abstract List<FormItem> getExtraViewItems ();

    /**
     * A property changed listener is added for the object for the specified types, when one
     * changes via the property the view description is updated.
     */
    public abstract Set<String> getObjectChangeEventTypes ();

    public String getViewDescription ()
    {

        return (this.object.getDescription () != null ? this.object.getDescription ().getMarkedUpText () : null);

    }

    public void refreshViewPanel ()
    {

        this.desc.setText (UIUtils.getWithHTMLStyleSheet (this.desc,
                                                          UIUtils.markupStringForAssets (this.getViewDescription (),
                                                                                         this.projectViewer.getProject (),
                                                                                         this.object)));

    }

    @Override
    public boolean handleSave ()
    {

        // Check name...
        String n = this.nameEdit.getText ().trim ();

        if (n.equals (""))
        {

            this.showEditError ("Please enter a name.");

            //this.nameEdit.setText (this.object.getName ());
            //this.nameEdit.selectAll ();

            return false;

        }

        if (this.object != null)
        {

            Asset a = this.projectViewer.getProject ().getAssetByName (n,
                                                                       this.object.getObjectType ());

            if ((a != null)
                &&
                (this.object.getKey () != a.getKey ())
               )
            {

                this.showEditError (Environment.replaceObjectNames (String.format ("Already have a {%s} called: <b>%s</b>",
                                                                                   a.getObjectType (),
                                                                                   a.getName ())));

                //this.nameEdit.setText (this.object.getName ());
                //this.nameEdit.selectAll ();

                return false;

            }

        }

        if (!this.canSave ())
        {

            return false;

        }

        Set<String> oldNames = this.object.getAllNames ();

        this.object.setName (this.nameEdit.getText ());

        this.object.setDescription (this.descEdit.getTextWithMarkup ());

        this.fillForSave ();

        try
        {

            this.projectViewer.saveObject (this.object,
                                           true);

            this.projectViewer.fireProjectEvent (this.object.getObjectType (),
                                                 ProjectEvent.EDIT,
                                                 this.object);

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

        if (this.object.getDescription () != null)
        {

            this.descEdit.setTextWithMarkup (this.object.getDescription ());

        }

        this.nameEdit.grabFocus ();

        this.nameEdit.setText (this.object.getName ());

        this.fillForEdit ();

    }

    public IconProvider getIconProvider ()
    {

        DefaultIconProvider iconProv = new DefaultIconProvider ()
        {

            @Override
            public ImageIcon getIcon (String name,
                                      int    type)
            {

                if (name.equals ("header"))
                {

                    name = Constants.INFO_ICON_NAME;

                }

                return super.getIcon (name,
                                      type);

            }

        };

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

        List<FormItem> extra = this.getExtraEditItems (this.getDoSaveAction ());

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
