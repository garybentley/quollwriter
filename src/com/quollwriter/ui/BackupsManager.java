package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.net.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Date;
import java.util.Arrays;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import org.josql.*;

import com.gentlyweb.utils.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.exporter.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;

public class BackupsManager extends Box implements ProjectEventListener
{

    private AbstractViewer viewer = null;
    private ProjectInfo proj = null;
    private Box backupsBox = null;
    private JLabel noBackups = null;
    private JLabel backupsDir = null;
    private JScrollPane scrollPane = null;

    public BackupsManager (AbstractViewer pv,
                           ProjectInfo    pi)
    {

        super (BoxLayout.Y_AXIS);

        this.viewer = pv;
        this.proj = pi;

        Environment.addUserProjectEventListener (this);

    }

    public void eventOccurred (ProjectEvent ev)
    {

        if (!ev.getType ().equals (ProjectEvent.BACKUPS))
        {

            return;

        }

        Object c = ev.getContextObject ();

        if ((c != null)
            &&
            (c instanceof Project)
           )
        {

            Project p = (Project) c;

            if (Environment.getProjectInfo (p) != this.proj)
            {

                // Not interested in this project.
                return;

            }

        }

        this.update ();

    }

    private void update ()
    {

        try
        {

            final BackupsManager _this = this;

            this.noBackups.setVisible (true);

            this.backupsDir.setVisible (this.proj.getBackupDirectory ().exists ());

            this.backupsBox.removeAll ();

            File[] _files = this.proj.getBackupDirectory ().listFiles ();

            if ((_files != null)
                &&
                (_files.length > 0)
               )
            {

                java.util.List<File> files = (java.util.List<File>) Arrays.asList (_files);

                Query q = new Query ();

                q.parse (String.format ("SELECT * FROM %s WHERE fileExtension(:_currobj) = 'zip' ORDER BY lastModified DESC",
                                        File.class.getName ()));

                QueryResults qr = q.execute (files);

                files = (java.util.List<File>) qr.getResults ();

                for (File f : files)
                {

                    Backup b = new Backup (f,
                                           this.proj);

                    b.init ();

                    this.backupsBox.add (b);

                }

                this.noBackups.setVisible (files.size () == 0);

                UIUtils.doLater (new ActionListener ()
                {

                    @Override
                    public void actionPerformed (ActionEvent ev)
                    {

                        _this.scrollPane.getVerticalScrollBar ().setValue (0);

                    }

                });

            }

        } catch (Exception e) {

            Environment.logError ("Unable to build list of backups",
                                  e);

            UIUtils.showErrorMessage (this.viewer,
                                      Environment.getUIString (LanguageStrings.backups,
                                                               LanguageStrings.show,
                                                               LanguageStrings.actionerror));
                                      //"Unable to show list of backups, please contact Quoll Writer support for assistance.");

        }

    }

    public void init ()
               throws Exception
    {

        final BackupsManager _this = this;

        JTextPane tp = UIUtils.createHelpTextPane (String.format (Environment.getUIString (LanguageStrings.backups,
                                                                                           LanguageStrings.text),
                                                                  //"Listed below are the backups you have for {project} <b>%s</b>.",
                                                                  this.proj.getName ()),
                                                   this.viewer);

        tp.setBorder (UIUtils.createPadding (0, 0, 10, 0));

        this.add (tp);

        this.backupsDir = UIUtils.createClickableLabel (Environment.getUIString (LanguageStrings.backups,
                                                                                 LanguageStrings.viewbackupsdir),
                                                        //"Click to view the backups directory",
                                                        Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                             Constants.ICON_MENU));

        UIUtils.makeClickable (this.backupsDir,
                               new ActionListener ()
                               {
                                
                                    @Override
                                    public void actionPerformed (ActionEvent ev)
                                    {
                                        
                                        try
                                        {
                                        
                                            UIUtils.openURL (_this.viewer,
                                                             _this.proj.getBackupDirectory ().toURI ().toURL ());
                                            
                                        } catch (Exception e) {
                                            
                                            // Can ignore.
                                            
                                        }
                                        
                                    }
                                
                               });

        this.backupsDir.setBorder (UIUtils.createPadding (0, 0, 10, 0));

        this.add (this.backupsDir);

        this.noBackups = UIUtils.createLabel (Environment.getUIString (LanguageStrings.backups,
                                                                       LanguageStrings.nobackups));
        //"You currently have no backups for this {project}.");
        this.noBackups.setIcon (Environment.getIcon (Constants.INFO_ICON_NAME,
                                                     Constants.ICON_MENU));

        this.noBackups.setBorder (UIUtils.createPadding (0, 0, 10, 0));

        this.add (this.noBackups);

        this.backupsBox = new ScrollableBox (BoxLayout.Y_AXIS);
        this.backupsBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.backupsBox.setOpaque (false);

        this.scrollPane = UIUtils.createScrollPane (backupsBox);
        this.scrollPane.getVerticalScrollBar ().setUnitIncrement (20);
        this.scrollPane.getViewport ().setPreferredSize (new Dimension (450,
                                                                        300));

        Box wspsp = new Box (BoxLayout.Y_AXIS);
        wspsp.add (this.scrollPane);
        wspsp.setBorder (UIUtils.createPadding (0, 0, 0, 0));
        this.add (wspsp);

        this.update ();

        this.add (Box.createVerticalStrut (10));

        JButton create = UIUtils.createButton (Constants.CREATE_BACKUP_BUTTON_LABEL_ID,
                                               new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.showCreateBackup (proj,
                                          null,
                                          _this.viewer);

            }

        });

        JButton finish = UIUtils.createButton (Constants.FINISH_BUTTON_LABEL_ID,
                                               new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                UIUtils.closePopupParent (_this.getParent ());

            }

        });

        JButton[] buts = new JButton[] { create, finish };

        JPanel bp = UIUtils.createButtonBar2 (buts,
                                              Component.CENTER_ALIGNMENT);
        bp.setOpaque (false);

        this.add (bp);

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                _this.scrollPane.getVerticalScrollBar ().setValue (0);

            }

        });

    }

    private class Backup extends Box
    {

        private File file = null;
        private ProjectInfo proj = null;

        public Backup (File        b,
                       ProjectInfo proj)
        {

            super (BoxLayout.Y_AXIS);

            this.file = b;
            this.proj = proj;

            this.setBorder (new CompoundBorder (new MatteBorder (0,
                                                                 0,
                                                                 1,
                                                                 0,
                                                                 UIUtils.getInnerBorderColor ()),
                                                new EmptyBorder (5,
                                                                 5,
                                                                 5,
                                                                 5)));

        }

        public void restore ()
        {

            Set<String> prefix = new LinkedHashSet ();
            prefix.add (LanguageStrings.backups);
            prefix.add (LanguageStrings.restore);
            prefix.add (LanguageStrings.popup);
        
            final Backup _this = this;

            UIUtils.createQuestionPopup ((AbstractViewer) BackupsManager.this.viewer,
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.title),
                                         //"Confirm restore",
                                         Constants.RESTORE_ICON_NAME,
                                         String.format (Environment.getUIString (prefix,
                                                                                 LanguageStrings.text),
                                                        //"Please confirm you wish to restore {project} <b>%s</b> using the backup file <b>%s</b>.<br /><br />A backup of the {project} will be created before the restore occurs.",
                                                        this.proj.getName (),
                                                        this.file.getName ()),
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.buttons,
                                                                  LanguageStrings.confirm),
                                         //"Yes, restore it",
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.buttons,
                                                                  LanguageStrings.cancel),
                                         //Constants.CANCEL_BUTTON_LABEL_ID,
                                         new ActionListener ()
                                         {

                                            @Override
                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                final AbstractProjectViewer pv = Environment.getProjectViewer (_this.proj);

                                                ActionListener doRestore = new ActionListener ()
                                                {

                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        UIUtils.closePopupParent (BackupsManager.this);

                                                        ActionListener doRestore = new ActionListener ()
                                                        {

                                                            @Override
                                                            public void actionPerformed (ActionEvent ev)
                                                            {

                                                                try
                                                                {

                                                                    // Create a backup.
                                                                    File f = Environment.createBackupForProject (_this.proj,
                                                                                                                 true);

                                                                } catch (Exception e) {

                                                                    Environment.logError ("Unable to create backup for project: " +
                                                                                          _this.proj,
                                                                                          e);

                                                                    UIUtils.showErrorMessage (BackupsManager.this.viewer,
                                                                                              Environment.getUIString (LanguageStrings.backups,
                                                                                                                       LanguageStrings._new,
                                                                                                                       LanguageStrings.actionerror));
                                                                                              //"Unable to create a backup of the {project} in its current state.");

                                                                }

                                                                // Restore using our file.
                                                                try
                                                                {

                                                                    Environment.restoreBackupForProject (_this.proj,
                                                                                                         _this.file);

                                                                } catch (Exception e) {

                                                                    Environment.logError ("Unable to restore project with file: " +
                                                                                          _this.file +
                                                                                          ", project: " +
                                                                                          _this.proj,
                                                                                          e);

                                                                    UIUtils.showErrorMessage (BackupsManager.this.viewer,
                                                                                              Environment.getUIString (LanguageStrings.backups,
                                                                                                                       LanguageStrings.restore,
                                                                                                                       LanguageStrings.actionerror));
                                                                                              //"Unable to restore backup");

                                                                    return;

                                                                }

                                                                Set<String> prefix = new LinkedHashSet ();
                                                                prefix.add (LanguageStrings.backups);
                                                                prefix.add (LanguageStrings.restore);
                                                                prefix.add (LanguageStrings.confirmpopup);

                                                                if (pv != null)
                                                                {
                                                                
                                                                    try
                                                                    {

                                                                        // Reopen the project.
                                                                        Environment.openProject (_this.proj);

                                                                        AbstractProjectViewer p = Environment.getProjectViewer (_this.proj);

                                                                        // Show confirmation.
                                                                        UIUtils.showMessage ((PopupsSupported) p,
                                                                                             Environment.getUIString (prefix,
                                                                                                                      LanguageStrings.title),
                                                                                             //"{Project} restored",
                                                                                             String.format (Environment.getUIString (prefix,
                                                                                                                                     LanguageStrings.text),
                                                                                                            //"The {project} has been restored from file <b>%s</b>.",
                                                                                                            _this.file.getName ()));

                                                                        p.fireProjectEventLater (ProjectEvent.BACKUPS,
                                                                                                 ProjectEvent.RESTORE);

                                                                    } catch (Exception e) {

                                                                        Environment.logError ("Unable to reopen project: " +
                                                                                              _this.proj,
                                                                                              e);

                                                                        UIUtils.showErrorMessage (BackupsManager.this.viewer,
                                                                                                  Environment.getUIString (prefix,
                                                                                                                           LanguageStrings.actionerror));
                                                                                                  //"Unable to re-open backup");

                                                                        return;

                                                                    }

                                                                    return;

                                                                }

                                                                // Show confirmation.
                                                                UIUtils.showMessage ((PopupsSupported) BackupsManager.this.viewer,
                                                                                     Environment.getUIString (prefix,
                                                                                                              LanguageStrings.title),
                                                                                     //"{Project} restored",
                                                                                     String.format (Environment.getUIString (prefix,
                                                                                                                             LanguageStrings.text),
                                                                                                    //"{Project} <b>%s</b> has been restored using file <b>%s</b>.",
                                                                                                    _this.proj.getName (),
                                                                                                    _this.file.getName ()));

                                                            }

                                                        };

                                                        UIUtils.askForPasswordForProject (proj,
                                                                                          null,
                                                                                          doRestore,
                                                                                          BackupsManager.this.viewer);

                                                    }

                                                };

                                                if (pv != null)
                                                {

                                                    // Close the project.
                                                    pv.close (false,
                                                              doRestore);

                                                } else {

                                                    doRestore.actionPerformed (new ActionEvent ("restore", 1, "restore"));

                                                }

            // Create a backup of the project first.

            // Close the project viewer, if open.

            // Unzip to the current dir.

            // Open the project, if already open.

                                            }

                                         },
                                         null,
                                         null,
                                         null);

        }

     public void delete ()
     {

          Set<String> prefix = new LinkedHashSet ();
          prefix.add (LanguageStrings.backups);
          prefix.add (LanguageStrings.delete);
          prefix.add (LanguageStrings.confirmpopup);
        
            final Backup _this = this;

            UIUtils.createQuestionPopup ((AbstractViewer) BackupsManager.this.viewer,
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.title),
                                         //"Confirm delete",
                                         Constants.DELETE_ICON_NAME,
                                         String.format (Environment.getUIString (prefix,
                                                                                 LanguageStrings.text),
                                                        //"Please confirm you wish to delete backup file <b>%s</b>.",
                                                        this.file.getName ()),
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.buttons,
                                                                  LanguageStrings.confirm),
                                         //"Yes, delete it",
                                         Environment.getUIString (prefix,
                                                                  LanguageStrings.buttons,
                                                                  LanguageStrings.cancel),
                                         //"No, keep it",
                                         new ActionListener ()
                                         {

                                            @Override
                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                _this.file.delete ();

                                                Container p = _this.getParent ();

                                                p.remove (_this);

                                                p.validate ();
                                                p.repaint ();

                                                BackupsManager.this.viewer.fireProjectEventLater (ProjectEvent.BACKUPS,
                                                                                                  ProjectEvent.DELETE);

                                                BackupsManager.this.update ();

                                            }

                                         },
                                         null,
                                         null,
                                         null);

        }

        public void init ()
        {

            final Backup _this = this;

            this.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));

            final Box main = new Box (BoxLayout.X_AXIS);

            this.add (main);

            this.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.setOpaque (false);
            main.setAlignmentX (Component.LEFT_ALIGNMENT);
            main.setOpaque (false);

            JComponent name = UIUtils.createHelpTextPane (String.format ("%s (%s)",
                                                                         Environment.formatDateTime (new Date (this.file.lastModified ())),
                                                                         this.file.getName ()),
                                                          BackupsManager.this.viewer);

            main.add (name);
            main.add (Box.createHorizontalGlue ());

            java.util.List<JButton> buttons = new ArrayList ();

            buttons.add (UIUtils.createButton (Constants.RESTORE_ICON_NAME,
                                               Constants.ICON_MENU,
                                               Environment.getUIString (LanguageStrings.backups,
                                                                        LanguageStrings.restore,
                                                                        LanguageStrings.tooltip),
                                               //"Restore the {project} using this backup",
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.restore ();

                                                    }

                                               }));

            buttons.add (UIUtils.createButton (Constants.DELETE_ICON_NAME,
                                               Constants.ICON_MENU,
                                               Environment.getUIString (LanguageStrings.backups,
                                                                        LanguageStrings.delete,
                                                                        LanguageStrings.tooltip),
                                               //"Click to delete this backup",
                                               new ActionAdapter ()
                                               {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        _this.delete ();

                                                    }

                                               }));

            JComponent bbar = UIUtils.createButtonBar (buttons);

            //this.buttons.setVisible (false);
            bbar.setAlignmentX (Component.LEFT_ALIGNMENT);

            main.add (bbar);

        }

    }

}
