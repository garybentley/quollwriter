package com.quollwriter.ui;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.ImagePanel;

public class ObjectTypeNameChanger extends Box
{

    private Map<String, JTextField> singular = new HashMap ();
    private Map<String, JTextField> plural = new HashMap ();
    private AbstractViewer viewer = null;

    public ObjectTypeNameChanger (AbstractViewer viewer)
    {

        super (BoxLayout.Y_AXIS);

        this.viewer = viewer;

    }

    private void save ()
    {

        // See if any of the values are changing, if so we need to reopen the project.

        boolean changing = false;

        final Map<String, String> sing = new HashMap ();
        final Map<String, String> plur = new HashMap ();

        Set<UserConfigurableObjectType> updateTypes = new HashSet ();
        for (String ot : this.singular.keySet ())
        {

            JTextField f = this.singular.get (ot);

            String s = f.getText ().trim ();

            if (!s.equals (Environment.getObjectTypeName (ot)))
            {

                sing.put (ot,
                          s);

            }

        }

        for (String ot : this.plural.keySet ())
        {

            JTextField f = this.plural.get (ot);

            String p = f.getText ().trim ();

            if (!p.equals (Environment.getObjectTypeName (ot)))
            {

                plur.put (ot,
                          p);

            }

        }

        if ((sing.size () > 0)
            ||
            (plur.size () > 0)
           )
        {

            changing = true;

        }

        if (changing)
        {

            final java.util.List<String> prefix = new ArrayList ();
            prefix.add (LanguageStrings.objectnames);
            prefix.add (LanguageStrings.changer);
            prefix.add (LanguageStrings.confirmchange);

            if (this.viewer instanceof AbstractProjectViewer)
            {

                AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

                final Project proj = pv.getProject ();
                final ObjectTypeNameChanger _this = this;

            //XXX xxx handle ALL open projects
            //xxx recreate the landing

                // Offer to reopen project.
                UIUtils.createQuestionPopup (this.viewer,
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.popup,
                                                                      LanguageStrings.title),
                                             //"Confirm name changes?",
                                             Constants.EDIT_ICON_NAME,
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.popup,
                                                                      LanguageStrings.text),
                                             //"Warning!  To change the object names the {project} must first be saved then reopened.<br /><br />Do you wish to continue?",
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.popup,
                                                                      LanguageStrings.buttons,
                                                                      LanguageStrings.confirm),
                                             //"Yes, change the names",
                                             Environment.getUIString (prefix,
                                                                      LanguageStrings.popup,
                                                                      LanguageStrings.buttons,
                                                                      LanguageStrings.cancel),
                                             //null,
                                             new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    try
                                                    {
/*
TODO
                                                        Environment.updateUserObjectTypeNames (sing,
                                                                                               plur);
*/
                                                    } catch (Exception e) {

                                                        UIUtils.showErrorMessage (_this,
                                                                                  Environment.getUIString (prefix,
                                                                                                           LanguageStrings.actionerror));
                                                                                  //"Unable to modify names");

                                                        Environment.logError ("Unable to modify names",
                                                                              e);

                                                        return;

                                                    }

                                                    UIUtils.closePopupParent (_this.getParent ());

                                                    _this.viewer.close (true,
                                                                        new ActionListener ()
                                                                        {

                                                                            public void actionPerformed (ActionEvent ev)
                                                                            {

                                                                                  // Open the project again.
                                                                                  try
                                                                                  {

                                                                                        Environment.openProject (proj);

                                                                                  } catch (Exception e)
                                                                                  {

                                                                                        Environment.logError ("Unable to reopen project: " +
                                                                                                              proj,
                                                                                                              e);

                                                                                        try
                                                                                        {

                                                                                            // TODO Probably not needed Environment.relaunchLanding ();

                                                                                        } catch (Exception ee) {

                                                                                            Environment.logError ("Unable to open landing.",
                                                                                                                  e);

                                                                                            UIUtils.showErrorMessage (this,
                                                                                                                      Environment.getUIString (LanguageStrings.allprojects,
                                                                                                                                               LanguageStrings.actionerror));

                                                                                        }

                                                                                  }

                                                                            }

                                                                        });

                                                }

                                             },
                                             null,
                                             null,
                                             null);

                return;

            } else {

                try
                {
/*
TODO
                    Environment.updateUserObjectTypeNames (sing,
                                                           plur);
*/
                } catch (Exception e) {

                    UIUtils.showErrorMessage (this,
                                              Environment.getUIString (prefix,
                                                                       LanguageStrings.actionerror));
                                              //"Unable to modify names");

                    Environment.logError ("Unable to modify names",
                                          e);

                    return;

                }

                try
                {

                    // TODO Not needed? Environment.relaunchLanding ();

                } catch (Exception e) {

                    UIUtils.showErrorMessage (this,
                                              Environment.getUIString (LanguageStrings.allprojects,
                                                                       LanguageStrings.actionerror));
                                              //"Unable to show start window, please contact Quoll Writer support for assistance.");

                    Environment.logError ("Unable to show start window",
                                          e);

                    return;

                }

            }

        }

        UIUtils.closePopupParent (this.getParent ());
        //this.close ();

    }

    private void reset ()
    {

        final ObjectTypeNameChanger _this = this;

        final java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.objectnames);
        prefix.add (LanguageStrings.changer);
        prefix.add (LanguageStrings.resetchange);

        if (this.viewer instanceof AbstractProjectViewer)
        {

            AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

            final Project proj = pv.getProject ();

            // Offer to reopen project.
            UIUtils.createQuestionPopup (this.viewer,
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.popup,
                                                                  LanguageStrings.title),
                                         //"Confirm name changes?",
                                         Constants.EDIT_ICON_NAME,
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.popup,
                                                                  LanguageStrings.text),
                                         //"Warning!  To reset the object names back to the defaults the {project} must first be saved then reopened.<br /><br />Do you wish to continue?",
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.popup,
                                                                  LanguageStrings.buttons,
                                                                  LanguageStrings.confirm),
                                         //"Yes, reset the names",
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.popup,
                                                                  LanguageStrings.buttons,
                                                                  LanguageStrings.cancel),
                                         //null,
                                         new ActionListener ()
                                         {

                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                try
                                                {

                                                    Environment.resetObjectTypeNamesToDefaults ();

                                                } catch (Exception e) {

                                                    UIUtils.showErrorMessage (_this,
                                                                              Environment.getUIString (prefix,
                                                                                                       LanguageStrings.actionerror));
                                                                              //"Unable to modify names");

                                                    Environment.logError ("Unable to modify names",
                                                                          e);

                                                    return;

                                                }

                                                //_this.close ();
                                                UIUtils.closePopupParent (_this.getParent ());

                                                _this.viewer.close (true,
                                                                    new ActionListener ()
                                                                    {

                                                                       public void actionPerformed (ActionEvent ev)
                                                                       {

                                                                             // Open the project again.
                                                                             try
                                                                             {

                                                                                 Environment.openProject (proj);

                                                                             } catch (Exception e)
                                                                             {

                                                                                Environment.logError ("Unable to reopen project: " +
                                                                                                      proj,
                                                                                                      e);
                                                                                try
                                                                                {

                                                                                    // TODO Not needed? Environment.relaunchLanding ();

                                                                                } catch (Exception ee) {

                                                                                    Environment.logError ("Unable to open landing.",
                                                                                                          e);

                                                                                    UIUtils.showErrorMessage (this,
                                                                                                              Environment.getUIString (LanguageStrings.allprojects,
                                                                                                                                       LanguageStrings.actionerror));

                                                                                }

                                                                             }

                                                                       }

                                                                    });

                                            }
                                         },
                                         null,
                                         null,
                                         null);

        } else {

            try
            {

                Environment.resetObjectTypeNamesToDefaults ();

            } catch (Exception e) {

                UIUtils.showErrorMessage (this,
                                          Environment.getUIString (prefix,
                                                                   LanguageStrings.actionerror));
                                          //"Unable to modify names");

                Environment.logError ("Unable to modify names",
                                      e);

                return;

            }

            try
            {

                // TODO Not needed? Environment.relaunchLanding ();

            } catch (Exception e) {

                UIUtils.showErrorMessage (this,
                                          Environment.getUIString (LanguageStrings.allprojects,
                                                                   LanguageStrings.actionerror));
                                          //"Unable to show start window, please contact Quoll Writer support for assistance.");

                Environment.logError ("Unable to show landing.",
                                      e);

                return;

            }

        }
    }

    private boolean addRow (final String objType,
                            PanelBuilder builder,
                            int          row)
    {

        final ObjectTypeNameChanger _this = this;

        CellConstraints cc = new CellConstraints ();

        ImageIcon ii = Environment.getIcon (objType,
                                            Constants.ICON_MENU);

        if (ii == null)
        {

            return false;

        }

        builder.add (new ImagePanel (ii,
                                     null),
                     cc.xy (1,
                            row));

        final JTextField s = new JTextField (Environment.getObjectTypeName (objType).getValue ());
        final JTextField p = new JTextField (Environment.getObjectTypeNamePlural (objType).getValue ());

        this.singular.put (objType,
                           s);

        this.plural.put (objType,
                         p);

        builder.add (s,
                     cc.xy (3,
                            row));
        builder.add (p,
                     cc.xy (5,
                            row));

        return true;

    }

    public void init ()
    {

        final ObjectTypeNameChanger _this = this;

        java.util.List<String> prefix = new ArrayList ();
        prefix.add (LanguageStrings.objectnames);
        prefix.add (LanguageStrings.changer);
        prefix.add (LanguageStrings.popup);

        JTextPane help = UIUtils.createHelpTextPane (Environment.getUIString (prefix,
                                                                              LanguageStrings.text),
                                                     //"After saving any changes to names will appear when you next open the {project}.",
                                                     this.viewer);

        help.setBorder (null);
        help.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.add (help);
        this.add (Box.createVerticalStrut (10));

        List<String> objTypes = new ArrayList ();
        objTypes.add (Chapter.OBJECT_TYPE);
        /*
        objTypes.add (QCharacter.OBJECT_TYPE);
        objTypes.add (Location.OBJECT_TYPE);
        objTypes.add (QObject.OBJECT_TYPE);
        objTypes.add (ResearchItem.OBJECT_TYPE);
        */
        objTypes.add (OutlineItem.OBJECT_TYPE);
        objTypes.add (Scene.OBJECT_TYPE);
        objTypes.add (Note.OBJECT_TYPE);
        objTypes.add (Project.OBJECT_TYPE);
        objTypes.add (Warmup.OBJECT_TYPE);
        objTypes.add (EditorEditor.OBJECT_TYPE);

        StringBuilder rows = new StringBuilder ("p, 5px");

        for (String ot : objTypes)
        {

            if (Environment.getIcon (ot,
                                     Constants.ICON_MENU) != null)
            {

                rows.append (", center:p, 10px");

            }

        }

        rows.append (",p");

        FormLayout fl = new FormLayout ("p, 6px, 180px:grow, 20px, 180px:grow",
                                        rows.toString ());

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int row = 1;

        builder.addLabel (Environment.getUIString (prefix,
                                                   LanguageStrings.labels,
                                                   LanguageStrings.singular),
                          //"Singular",
                     cc.xy (3,
                            row));
        builder.addLabel (Environment.getUIString (prefix,
                                                   LanguageStrings.labels,
                                                   LanguageStrings.plural),
                          //"Plural",
                     cc.xy (5,
                            row));

        row += 2;

        for (String ot : objTypes)
        {

            this.addRow (ot,
                         builder,
                         row);

            row += 2;

        }

        JButton save = UIUtils.createButton (Environment.getUIString (prefix,
                                                                      LanguageStrings.buttons,
                                                                      LanguageStrings.save));
                                    //"Save");

        save.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.save ();

            }

        });

        JButton cancel = UIUtils.createButton (Environment.getUIString (prefix,
                                                                        LanguageStrings.buttons,
                                                                        LanguageStrings.cancel));
        //"Cancel");

        cancel.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.closePopupParent (_this.getParent ());

            }

        });

        JButton reset = UIUtils.createButton (Environment.getUIString (prefix,
                                                                       LanguageStrings.buttons,
                                                                       LanguageStrings.reset));
        //"Reset to defaults");

        reset.addActionListener (new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.reset ();

            }

        });

        JButton[] buts = { save, reset, cancel };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.CENTER_ALIGNMENT);
        bp.setOpaque (false);

        builder.add (bp,
                     cc.xywh (1, row, 5, 1));

        JPanel p = builder.getPanel ();
        p.setBorder (null);
        p.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.add (p);

    }

}
