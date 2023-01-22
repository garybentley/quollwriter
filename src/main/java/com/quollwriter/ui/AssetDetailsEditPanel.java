package com.quollwriter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.toedter.calendar.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.userobjects.*;
import com.quollwriter.ui.components.IconProvider;
import com.quollwriter.ui.components.QTextEditor;

public class AssetDetailsEditPanel<E extends Asset> extends EditPanel implements RefreshablePanel
{

    private ProjectViewer viewer = null;
    private Asset object = null;
    private boolean changeConfirm = false;
    private Set<UserConfigurableObjectFieldViewEditHandler> editHandlers = null;

    public AssetDetailsEditPanel (Asset                   a,
                                  ProjectViewer           viewer)
    {

        super ();

        this.object = a;
        this.viewer = viewer;

        InputMap im = this.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_S,
                                        InputEvent.CTRL_MASK),
                "save");

        final AssetDetailsEditPanel _this = this;

        ActionMap am = this.getActionMap ();

        am.put ("save",
                new AbstractAction ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                _this.getDoSaveAction ().actionPerformed (ev);

            }

        });

    }

    @Override
    public void refresh ()
    {

        this.showViewPanel ();

    }

    @Override
    public void refreshViewPanel ()
    {

        this.refresh ();

    }

    @Override
    public boolean handleSave ()
    {

        Set<String> errors = new LinkedHashSet ();

        // Do our error checks.
        for (UserConfigurableObjectFieldViewEditHandler h : this.editHandlers)
        {

            Set<String> herrs = h.getInputFormItemErrors ();

            if (herrs != null)
            {

                errors.addAll (herrs);

            }

        }

        if (errors.size () > 0)
        {

            this.showEditError (errors);

            return false;

        }

        Set<String> oldNames = this.object.getAllNames ();

        // Now setup the values.
        for (UserConfigurableObjectFieldViewEditHandler h : this.editHandlers)
        {

            try
            {

                h.updateFieldFromInput ();

            } catch (Exception e) {

                Environment.logError ("Unable to get input save value for: " +
                                      h.getTypeField (),
                                      e);

                UIUtils.showErrorMessage (this.viewer,
                                          Environment.getUIString (LanguageStrings.assets,
                                                                   LanguageStrings.save,
                                                                   LanguageStrings.actionerror));
                                          //"Unable to save.");

                return false;

            }

        }

        try
        {

            this.viewer.saveObject (this.object,
                                    true);

            this.viewer.fireProjectEvent (this.object.getObjectType (),
                                          ProjectEvent.EDIT,
                                          this.object);

        } catch (Exception e)
        {

            Environment.logError ("Unable to save: " +
                                  this.object,
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      Environment.getUIString (LanguageStrings.assets,
                                                               LanguageStrings.save,
                                                               LanguageStrings.actionerror));
                                      //"Unable to save.");

            return false;

        }

        this.viewer.updateProjectDictionaryForNames (oldNames,
                                                     this.object);

        this.showViewPanel ();

        return true;

    }

    @Override
    public boolean handleCancel ()
    {

        List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.assets);
        prefix.add (LanguageStrings.save);
        prefix.add (LanguageStrings.cancel);
        prefix.add (LanguageStrings.popup);

        final AssetDetailsEditPanel _this = this;

        boolean changed = false;

        // Check for any field having changes.

        if (!changed)
        {

            if ((changed)
                &&
                (!this.changeConfirm)
               )
            {

                UIUtils.createQuestionPopup (this.viewer,
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.title),
                                             //"Discard changes?",
                                             Constants.HELP_ICON_NAME,
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.text),
                                             //"You have made some changes, do you want to discard them?",
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.buttons,
                                                                      LanguageStrings.confirm),
                                             //"Yes, discard them",
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.buttons,
                                                                      LanguageStrings.cancel),
                                             //"No, keep them",
                                             // On confirm
                                             new ActionListener ()
                                             {

                                                 @Override
                                                 public void actionPerformed (ActionEvent ev)
                                                 {

                                                     _this.changeConfirm = true;

                                                     _this.doCancel ();

                                                 }

                                             },
                                             // On cancel
                                             null,
                                             null,
                                             null);

                return false;

            }

        }

        this.changeConfirm = false;

        return true;

    }

    @Override
    public void handleEditStart ()
    {

        // Give the focus to the primary name field.

    }

    @Override
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

    @Override
    public String getEditTitle ()
    {

        return Environment.getUIString (LanguageStrings.assets,
                                        LanguageStrings.edit,
                                        LanguageStrings.aboutpanel,
                                        LanguageStrings.title);

    }

    @Override
    public String getTitle ()
    {

        return Environment.getUIString (LanguageStrings.assets,
                                        LanguageStrings.view,
                                        LanguageStrings.aboutpanel,
                                        LanguageStrings.title);
        //"About";

    }

    @Override
    public JComponent getSaveButton ()
    {

        final AssetDetailsEditPanel _this = this;

        IconProvider ip = this.getIconProvider ();

        JButton save = UIUtils.createButton (ip.getIcon (Constants.SAVE_ICON_NAME,
                                                      Constants.ICON_PANEL_SECTION_ACTION),
                                             String.format (Environment.getUIString (LanguageStrings.assets,
                                                                                     LanguageStrings.edit,
                                                                                     LanguageStrings.aboutpanel,
                                                                                     LanguageStrings.buttons,
                                                                                     LanguageStrings.save,
                                                                                     LanguageStrings.tooltip),
                                                            this.object.getObjectTypeName ()),
                                             //"Click to save the details",
                                             new ActionListener ()
                                             {

                                              @Override
                                              public void actionPerformed (ActionEvent ev)
                                              {

                                                  _this.doSave ();

                                              }

                                          });

        return save;

    }

    @Override
    public JComponent getCancelButton ()
    {

        final AssetDetailsEditPanel _this = this;

        IconProvider ip = this.getIconProvider ();

        JButton cancel = UIUtils.createButton (ip.getIcon (Constants.CANCEL_ICON_NAME,
                                                           Constants.ICON_PANEL_SECTION_ACTION),
                                               Environment.getUIString (LanguageStrings.assets,
                                                                        LanguageStrings.edit,
                                                                        LanguageStrings.aboutpanel,
                                                                        LanguageStrings.buttons,
                                                                        LanguageStrings.cancel,
                                                                        LanguageStrings.tooltip),
                                               //"Click to cancel the editing",
                                               new ActionListener ()
                                               {

                                                  @Override
                                                  public void actionPerformed (ActionEvent ev)
                                                  {

                                                      _this.doCancel ();

                                                  }

                                               });

        return cancel;

    }

    @Override
    public JComponent getEditButton ()
    {

        final AssetDetailsEditPanel _this = this;

        IconProvider ip = this.getIconProvider ();

        return UIUtils.createButton (ip.getIcon (Constants.EDIT_ICON_NAME,
                                                 Constants.ICON_PANEL_SECTION_ACTION),
                                     String.format (Environment.getUIString (LanguageStrings.assets,
                                                                             LanguageStrings.view,
                                                                             LanguageStrings.aboutpanel,
                                                                             LanguageStrings.buttons,
                                                                             LanguageStrings.edit,
                                                                             LanguageStrings.tooltip),
                                                    this.object.getObjectTypeName ()),
                                     //"Click to edit this section",
                                     new ActionListener ()
                                     {

                                        @Override
                                        public void actionPerformed (ActionEvent ev)
                                        {

                                            _this.doEdit ();

                                        }

                                     });

    }

    public JComponent getAddPanel ()
    {

        final AssetDetailsEditPanel _this = this;

        // TODO Remove this.editHandlers = this.object.getViewEditHandlers (this.viewer);

        // Build the edit panel.
        // Get the layout.
        AssetViewAddEditLayout l = this.getLayout (this.object);

        return l.createEdit (this.editHandlers,
                            new ActionListener ()
                            {

                                @Override
                                public void actionPerformed (ActionEvent ev)
                                {

                                    _this.doSave ();

                                }

                            });

    }

    @Override
    public JComponent getEditPanel ()
    {

        final AssetDetailsEditPanel _this = this;

        // TODO Remove this.editHandlers = this.object.getViewEditHandlers (this.viewer);

        // Build the edit panel.
        // Get the layout.
        AssetViewAddEditLayout l = this.getLayout (this.object);

        return l.createEdit (this.editHandlers,
                             new ActionListener ()
                             {

                                @Override
                                public void actionPerformed (ActionEvent ev)
                                {

                                    _this.doSave ();

                                }

                             });

    }

    @Override
    public JComponent getViewPanel ()
    {

        // Build the view panel.

        // Get the layout.
        AssetViewAddEditLayout l = this.getLayout (this.object);

        return null;

        // TODO Remove? l.createView (this.object.getViewEditHandlers (this.viewer));

    }

    @Override
    public Set<FormItem> getEditItems ()
    {

        return null;

    }

    @Override
    public Set<FormItem> getViewItems ()
    {

        return null;

    }

    @Override
    public void doEdit ()
    {

        ActionListener a = AssetViewPanel.getEditAssetAction (this.viewer,
                                                              this.object);

        if (a == null)
        {

            Environment.logError ("Unable to get edit asset action for: " +
                                  this.object,
                                  null);

            UIUtils.showErrorMessage (this.viewer,
                                      String.format (Environment.getUIString (LanguageStrings.assets,
                                                                              LanguageStrings.edit,
                                                                              LanguageStrings.actionerror),
                                                     //"Unable to edit the {%s}",
                                                     this.object.getObjectTypeName ()));

            return;

        }

        UIUtils.doLater (a);

    }

    public AssetViewAddEditLayout getLayout (UserConfigurableObject obj)
    {

        return new AssetViewAddEditLayout (obj.getUserConfigurableObjectType ().getLayout (),
                                           this.viewer);

    }

}
