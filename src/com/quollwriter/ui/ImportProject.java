package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.net.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.text.*;
import com.quollwriter.db.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.importer.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class ImportProject extends Wizard implements ImportCallback
{

    public static FileNameExtensionFilter fileFilter = new FileNameExtensionFilter (Environment.getUIString (LanguageStrings.importproject,
                                                                                                             LanguageStrings.supportedfiletypesdescription) + " (docx, doc)",
                                                                                    "docx",
                                                                                    "doc");

    public static final String SELECT_ITEMS_STAGE = "select-items";
    public static final String SELECT_FILE_STAGE = "select-file";
    public static final String SELECT_PROJECT_STAGE = "select-project";
    public static final String CHOOSE_STAGE = "choose";
    public static final String DECIDE_STAGE = "decide";
    public static final String NEW_PROJECT_STAGE = "new-project";

    private FileFinder      fileFind = null;
    private JLabel          fileFindError = null;
    private JTextField      urlField = null;
    private JRadioButton    addToProject = null;
    private JRadioButton    createNewProject = null;
    private JRadioButton    importFromProject = null;
    private JRadioButton    importFromFile = null;
    private JTree           itemsTree = null;
    private JScrollPane     itemsTreeScroll = null;
    private Box             nextB = null;
    private Project         proj = null;
    private NewProjectPanel newProjectPanel = null;
    private ProjectViewer   pv = null;
    private File            file = null;
    private boolean         addToProjectOnly = false;
    private boolean         newProjectOnly = false;
    private JList           projectList = null;
    private Project         fromProject = null;
    private Set<String>     filesToCopy = new HashSet ();


    public ImportProject (ProjectViewer pv)
    {

        super (pv);

        if (pv != null)
        {

            this.pv = pv;
            //this.proj = pv.getProject ();

        }

        this.addToProject = new JRadioButton ();
        this.fileFindError = UIUtils.createErrorLabel (Environment.getUIString (LanguageStrings.importproject,
                                                                                LanguageStrings.errors,
                                                                                LanguageStrings.nofileselected));
                                                       //"Please select a file to import.");
        this.fileFind = new FileFinder ();

        this.itemsTree = UIUtils.createTree ();
        this.itemsTree.setModel (null);

        this.importFromProject = UIUtils.createRadioButton ("");
        this.importFromFile = UIUtils.createRadioButton ("");

    }

    public ImportProject ()
    {

        this (null);

    }

    public void setAddToProjectOnly (boolean v)
    {

        this.addToProjectOnly = v;

    }

    public void setNewProjectOnly (boolean v)
    {

        this.newProjectOnly = v;

    }

    public void setFile (File f)
    {

        if (f == null)
        {

            this.file = null;

            return;

        }

        if (!fileFilter.accept (f))
        {

            throw new IllegalArgumentException ("File type not supported for file: " + f);

        }

        if (this.fileFind != null)
        {

            this.fileFind.setFile (f);

        }

        this.file = f;

    }

    public String getFirstHelpText ()
    {

        return Environment.getUIString (LanguageStrings.importproject,
                                        LanguageStrings.help);
        //return "It is recommended that you read the <a href='help://projects/importing-a-file'>guide to importing</a> prior to using this function.  The import expects the file(s) to be in a certain format.";

    }

    public boolean handleFinish ()
    {

        this.proj = this.getSelectedItems ();

        if (this.addToProject.isSelected ())
        {

            // Add all the items.
            Set<NamedObject> objs = this.proj.getAllNamedChildObjects ();

            Book b = this.pv.getProject ().getBooks ().get (0);

            for (NamedObject n : objs)
            {

                if (n instanceof Asset)
                {

                    String prefix = "Imported";

                    Asset a = (Asset) n;

                    // See if we should merge.
                    Asset oa = this.pv.getProject ().getAssetByName (a.getName (),
                                                                     a.getUserConfigurableObjectType ());

                    if (oa != null)
                    {

                        // Don't merge but don't add either, merging is no longer viable.
                        /*
                        // Merge.
                        oa.merge (a);

                        a = oa;

                        prefix = "Merged";
                        */

                    } else {

                        this.pv.getProject ().addAsset (a);

                    }

                    try
                    {

                        this.pv.saveObject (a,
                                            true);

                        this.pv.createActionLogEntry (a,
                                                      prefix + " asset from: " +
                                                      this.fileFind.getSelectedFile ());

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to save asset: " +
                                              a,
                                              e);

                        UIUtils.showErrorMessage (this,
                                                  String.format (Environment.getUIString (LanguageStrings.importproject,
                                                                                          LanguageStrings.errors,
                                                                                          LanguageStrings.unabletosave),
                                                                 a.getName ()));

                    }

                    this.pv.reloadAssetTree (a);

                    this.pv.openObjectSection (a.getObjectType ());

                }

                if (n instanceof Chapter)
                {

                    Chapter c = (Chapter) n;

                    b.addChapter (c);

                    try
                    {

                        this.pv.saveObject (c,
                                            true);

                        this.pv.createActionLogEntry (c,
                                                      "Imported chapter from project: " +
                                                      this.proj.getId ());

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to save chapter: " +
                                              c,
                                              e);

                        UIUtils.showErrorMessage (this,
                                                  String.format (Environment.getUIString (LanguageStrings.importproject,
                                                                                          LanguageStrings.errors,
                                                                                          LanguageStrings.unabletosave),
                                                                 //"Unable to save: " +
                                                                 c.getName ()));

                        continue;

                    }

                    this.pv.addChapterToTreeAfter (c,
                                                   null);

                    this.pv.openObjectSection (c.getObjectType ());

                }

            }

            if (this.fromProject != null)
            {

                for (String fn : this.filesToCopy)
                {

                    File f = new File (this.fromProject.getFilesDirectory (),
                                       fn);

                    try
                    {

                        this.pv.getProject ().saveToFilesDirectory (f,
                                                                    fn);

                    } catch (Exception e) {

                        Environment.logError ("Unable to copy file: " +
                                              f,
                                              e);

                    }

                }

            }

            this.pv.fireProjectEvent (ProjectEvent.IMPORT,
                                      ProjectEvent.ANY);

            UIUtils.showMessage ((PopupsSupported) this.pv,
                                 Environment.getUIString (LanguageStrings.importproject,
                                                          LanguageStrings.importcompletepopup,
                                                          LanguageStrings.title),
                                 //"Import complete",
                                 Environment.getUIString (LanguageStrings.importproject,
                                                          LanguageStrings.importcompletepopup,
                                                          LanguageStrings.text));
                                 //String.format ("The items have been imported into your {project}"));

        } else
        {

            if (!this.newProjectPanel.checkForm (this))
            {

                return false;

            }

            // Create a new project.
            ProjectViewer pj = new ProjectViewer ();

            String pwd = this.newProjectPanel.getPassword ();

            try
            {

                pj.init ();

                // Get the "old" files dir.

                this.proj.setName (this.newProjectPanel.getName ());

                pj.newProject (this.newProjectPanel.getSaveDirectory (),
                               this.proj,
                               pwd);

                if (this.filesToCopy != null)
                {

                    for (String fn : this.filesToCopy)
                    {

                        pj.getProject ().saveToFilesDirectory (new File (this.fromProject.getFilesDirectory (),
                                                                         fn),
                                                               fn);

                    }

                }

                pj.createActionLogEntry (pj.getProject (),
                                         "Project imported from: " +
                                         this.fileFind.getSelectedFile ());

            } catch (Exception e)
            {

                Environment.logError ("Unable to create new project: " +
                                      this.proj,
                                      e);

                UIUtils.showErrorMessage (this,
                                          String.format (Environment.getUIString (LanguageStrings.importproject,
                                                                                  LanguageStrings.errors,
                                                                                  LanguageStrings.unabletocreateproject),
                                                         this.proj.getName ()));
                                          //"Unable to create new project: " + this.proj.getName ());

                return false;

            }

            pj.fireProjectEvent (ProjectEvent.IMPORT,
                                 ProjectEvent.ANY);

        }

        return true;

    }

    public void handleCancel ()
    {

    }

    public String getNextStage (String currStage)
    {

        if (currStage == null)
        {

            return CHOOSE_STAGE;

        }

        if (currStage.equals (CHOOSE_STAGE))
        {

            if (this.importFromFile.isSelected ())
            {

                return SELECT_FILE_STAGE;

            }

            if (this.importFromProject.isSelected ())
            {

                return SELECT_PROJECT_STAGE;

            }

            return CHOOSE_STAGE;

        }

        if (currStage.equals (SELECT_FILE_STAGE))
        {

            return SELECT_ITEMS_STAGE;

        }

        if (SELECT_PROJECT_STAGE.equals (currStage))
        {

            return SELECT_ITEMS_STAGE;

        }

        if (currStage.equals (SELECT_ITEMS_STAGE))
        {

            if (this.newProjectOnly)
            {

                return NEW_PROJECT_STAGE;

            }

            return DECIDE_STAGE;

            //return null;

        }

        if (currStage.equals (DECIDE_STAGE))
        {

            if (this.addToProject.isSelected ())
            {

                return null;

            }

            return NEW_PROJECT_STAGE;

        }

        if (NEW_PROJECT_STAGE.equals (currStage))
        {

            return null;

        }

        return null;

    }

    public String getPreviousStage (String currStage)
    {

        if (currStage == null)
        {

            return null;

        }


        if (currStage.equals (NEW_PROJECT_STAGE))
        {

            if (this.newProjectOnly)
            {

                return SELECT_ITEMS_STAGE;

            }

            return DECIDE_STAGE;

        }

        if (currStage.equals (SELECT_FILE_STAGE))
        {

            return CHOOSE_STAGE;

        }

        if (currStage.equals (SELECT_PROJECT_STAGE))
        {

            return CHOOSE_STAGE;

        }

        if (currStage.equals (DECIDE_STAGE))
        {

            return SELECT_ITEMS_STAGE;

        }

        if (currStage.equals (SELECT_ITEMS_STAGE))
        {

            if (this.importFromProject.isSelected ())
            {

                return SELECT_PROJECT_STAGE;

            }

            return SELECT_FILE_STAGE;

        }

        return null;

    }

    private boolean checkForFileToImport ()
    {

        this.fileFindError.setVisible (false);

        if (this.file == null)
        {

            this.fileFindError.setText (Environment.getUIString (LanguageStrings.importproject,
                                                                 LanguageStrings.errors,
                                                                 LanguageStrings.nofileselected));
                                        //"Please select a file to import.");
            this.fileFindError.setVisible (true);

            this.resize ();
            return false;

        }

        if (!this.file.exists ())
        {

            this.fileFindError.setText (Environment.getUIString (LanguageStrings.importproject,
                                                                 LanguageStrings.errors,
                                                                 LanguageStrings.filenotexist));
            //"File does not exist, please select a valid file.");
            this.fileFindError.setVisible (true);

            this.resize ();
            return false;

        }

        if (this.file.isDirectory ())
        {

            this.fileFindError.setText (Environment.getUIString (LanguageStrings.importproject,
                                                                 LanguageStrings.errors,
                                                                 LanguageStrings.dirselected));
            //"Selection is a directory, please select a file instead.");
            this.fileFindError.setVisible (true);

            this.resize ();
            return false;

        }

        try
        {

            Importer.importProject (this.file.toURI (),
                                    this);

        } catch (Exception e)
        {

            Environment.logError ("Unable to convert: " +
                                  this.file +
                                  " to a uri",
                                  e);

            this.fileFindError.setText (Environment.getUIString (LanguageStrings.importproject,
                                                                 LanguageStrings.errors,
                                                                 LanguageStrings.openfile));
            //"Unable to open selected file.");
            this.fileFindError.setVisible (true);

            this.resize ();
            return false;

        }

        this.resize ();

        return true;

    }

    public boolean handleStageChange (final String oldStage,
                                      final String newStage)
    {
/*
        if ((oldStage == null)
            &&
            (this.startStage.equals ("decide"))
           )
        {

            this.enableButton ("next",
                               false);

        }
*/

        final ImportProject _this = this;

        if (SELECT_PROJECT_STAGE.equals (oldStage)
            &&
            (SELECT_ITEMS_STAGE.equals (newStage))
           )
        {

            final ProjectInfo pi = (ProjectInfo) this.projectList.getSelectedValue ();

            final ActionListener open = new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    Project p = null;

                    try
                    {

                        p = Environment.getProjectObjectManager (pi,
                                                                 pi.getFilePassword ()).getProject ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to get project for: " +
                                              p,
                                              e);

                        UIUtils.showErrorMessage (_this.viewer,
                                                  Environment.getUIString (LanguageStrings.importproject,
                                                                           LanguageStrings.errors,
                                                                           LanguageStrings.cantopenproject));
                                                  //"Unable to open {project}");

                        return;

                    }

                    for (NamedObject n : p.getAllNamedChildObjects ())
                    {

                        if (n instanceof Chapter)
                        {

                            Chapter c = (Chapter) n;

                            for (NamedObject cn : c.getAllNamedChildObjects ())
                            {

                                if (cn instanceof ChapterItem)
                                {

                                    c.removeChapterItem ((ChapterItem) cn);

                                }

                            }

                        }

                        if (n instanceof IdeaType)
                        {

                            p.getIdeaTypes ().remove ((IdeaType) n);

                        }

                    }

                    // Need to null the key/id for all items in the project.
                    for (NamedObject n : p.getAllNamedChildObjects ())
                    {

                        n.setKey (null);
                        n.setId (null);
                        n.setDateCreated (new java.util.Date ());

                        // For all the object fields, null the id/key.

                        if (n instanceof UserConfigurableObject)
                        {

                            UserConfigurableObject cn = (UserConfigurableObject) n;

                            for (UserConfigurableObjectField f : cn.getFields ())
                            {

                                f.setKey (null);
                                f.setId (null);
                                f.setDateCreated (new java.util.Date ());

                            }

                        }

                    }

                    _this.itemsTree.setModel (new DefaultTreeModel (_this.createTree (p)));

                    UIUtils.expandAllNodesWithChildren (_this.itemsTree);

                    _this.fromProject = p;

                    _this.proj = p;

                }

            };

            if ((pi != null)
                &&
                (pi.isEncrypted ())
                &&
                (Environment.getProjectViewer (pi) == null)
                &&
                (pi.getFilePassword () == null)
               )
            {

                UIUtils.askForPasswordForProject (pi,
                                                  null,
                                                  new ActionListener ()
                                                  {

                                                      @Override
                                                      public void actionPerformed (ActionEvent ev)
                                                      {

                                                          _this.showStage (SELECT_ITEMS_STAGE);

                                                          open.actionPerformed (ev);

                                                      }

                                                  },
                                                  this.viewer);

                return false;

            } else {

                open.actionPerformed (new ActionEvent (pi, 1, "open"));

            }

            return true;

        }

        if (SELECT_FILE_STAGE.equals (oldStage))
        {

            if (CHOOSE_STAGE.equals (newStage))
            {

                return true;

            }

            return this.checkForFileToImport ();

        }

        if (SELECT_ITEMS_STAGE.equals (newStage))
        {

            if (this.importFromProject.isSelected ())
            {

                return true;

            }

            if (!SELECT_PROJECT_STAGE.equals (oldStage))
            {

                this.fromProject = null;

                return this.checkForFileToImport ();

            }

        }

        return true;

    }

    public String getStartStage ()
    {

        if ((this.newProjectOnly)
            &&
            (this.file != null)
           )
        {

            return SELECT_ITEMS_STAGE;

        }

        if ((this.addToProjectOnly)
            &&
            (this.file != null)
           )
        {

            return SELECT_ITEMS_STAGE;

        }

        return CHOOSE_STAGE;

    }

    public WizardStep getStage (String stage)
    {

        final ImportProject _this = this;

        WizardStep ws = new WizardStep ();

        if (stage.equals (NEW_PROJECT_STAGE))
        {

            ws.title = Environment.getUIString (LanguageStrings.importproject,
                                                LanguageStrings.stages,
                                                LanguageStrings.newproject,
                                                LanguageStrings.title);
            //"Enter the new {Project} details";

            ws.helpText = Environment.getUIString (LanguageStrings.importproject,
                                                   LanguageStrings.stages,
                                                   LanguageStrings.newproject,
                                                   LanguageStrings.text);
            //"To create a new {Project} enter the name below and select the directory where it should be saved.";

            this.newProjectPanel = new NewProjectPanel ();

            ws.panel = this.newProjectPanel.createPanel (this,
                                                         null,
                                                         false,
                                                         null,
                                                         false);

            return ws;

        }

        if (stage.equals (DECIDE_STAGE))
        {

            ws.title = Environment.getUIString (LanguageStrings.importproject,
                                                LanguageStrings.stages,
                                                LanguageStrings.decide,
                                                LanguageStrings.title);
            //"What would you like to do?";

            FormLayout fl = new FormLayout ("10px, p, 10px",
                                            "p, 6px, p, 6px, p, 6px");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            if (pv != null)
            {

                this.addToProject.setText (String.format (Environment.getUIString (LanguageStrings.importproject,
                                                                                   LanguageStrings.stages,
                                                                                   LanguageStrings.decide,
                                                                                   LanguageStrings.options,
                                                                                   LanguageStrings.addtoproject),
                                                          pv.getProject ().getName ()));
                                           //"Add to " + Environment.getObjectTypeName (Project.OBJECT_TYPE) + ": " + pv.getProject ().getName ());

                this.addToProject.setOpaque (false);

                this.createNewProject = new JRadioButton (Environment.getUIString (LanguageStrings.importproject,
                                                                                   LanguageStrings.stages,
                                                                                   LanguageStrings.decide,
                                                                                   LanguageStrings.options,
                                                                                   LanguageStrings.newproject));
                                                          //"Create a new " + Environment.getObjectTypeName (Project.OBJECT_TYPE));

                this.createNewProject.setOpaque (false);

                ButtonGroup bg = new ButtonGroup ();

                bg.add (this.addToProject);
                bg.add (this.createNewProject);

                this.addToProject.addActionListener (new ActionAdapter ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.enableButton ("next",
                                                true);

                        }

                    });

                this.createNewProject.addActionListener (new ActionAdapter ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.enableButton ("next",
                                                true);

                        }

                    });

                builder.add (this.createNewProject,
                             cc.xy (2,
                                    1));

                builder.add (this.addToProject,
                             cc.xy (2,
                                    3));

            }

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            ws.panel = p;

            return ws;

        }

        if (stage.equals (SELECT_ITEMS_STAGE))
        {

            ws.title = Environment.getUIString (LanguageStrings.importproject,
                                                LanguageStrings.stages,
                                                LanguageStrings.selectitems,
                                                LanguageStrings.title);
            //"Select the items you wish to import";

            ws.helpText = String.format (Environment.getUIString (LanguageStrings.importproject,
                                                                  LanguageStrings.stages,
                                                                  LanguageStrings.selectitems,
                                                                  LanguageStrings.text),
                                         (this.addToProject.isSelected () ? Environment.getUIString (LanguageStrings.importproject,
                                                                                                     LanguageStrings.stages,
                                                                                                     LanguageStrings.selectitems,
                                                                                                     LanguageStrings.textextra) : ""));
                                         //"Check the items below to ensure that they match what is in your file.  The first and last sentences of the description (if present) are shown for each item." + (this.addToProject.isSelected () ? ("  Only items not already in the " + Environment.getObjectTypeName (Project.OBJECT_TYPE) + " will be listed.") : "");

            this.itemsTree.addMouseListener (new MouseAdapter ()
                {

                    private void selectAllChildren (DefaultTreeModel       model,
                                                    DefaultMutableTreeNode n,
                                                    boolean                v)
                    {

                        Enumeration<TreeNode> en = n.children ();

                        while (en.hasMoreElements ())
                        {

                            DefaultMutableTreeNode c = (DefaultMutableTreeNode) en.nextElement ();

                            Object uo = c.getUserObject ();

                            if (uo instanceof SelectableDataObject)
                            {

                                SelectableDataObject s = (SelectableDataObject) uo;

                                s.selected = v;

                                // Tell the model that something has changed.
                                model.nodeChanged (c);

                                // Iterate.
                                this.selectAllChildren (model,
                                                        c,
                                                        v);

                            }

                        }

                    }

                    public void mousePressed (MouseEvent ev)
                    {

                        TreePath tp = _this.itemsTree.getPathForLocation (ev.getX (),
                                                                          ev.getY ());

                        if (tp != null)
                        {

                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                            // Tell the model that something has changed.
                            DefaultTreeModel model = (DefaultTreeModel) _this.itemsTree.getModel ();

                            SelectableDataObject s = (SelectableDataObject) n.getUserObject ();

                            s.selected = !s.selected;

                            model.nodeChanged (n);

                            this.selectAllChildren (model,
                                                    n,
                                                    s.selected);

                        }

                    }

                });

            this.itemsTree.setCellRenderer (new SelectableProjectTreeCellRenderer ());

            this.itemsTree.setOpaque (false);
            this.itemsTree.setBorder (new EmptyBorder (5,
                                                       5,
                                                       5,
                                                       5));

            JScrollPane sp = UIUtils.createScrollPane (this.itemsTree);

            ws.panel = sp;

            return ws;

        }

        if (stage.equals (SELECT_FILE_STAGE))
        {

            ws.title = Environment.getUIString (LanguageStrings.importproject,
                                                LanguageStrings.stages,
                                                LanguageStrings.selectfile,
                                                LanguageStrings.title);
            //"Select a file to import";
            ws.helpText = Environment.getUIString (LanguageStrings.importproject,
                                                   LanguageStrings.stages,
                                                   LanguageStrings.selectfile,
                                                   LanguageStrings.text);
            //"Microsoft Word files (.doc and .docx) are supported.  Please check <a href='help://projects/importing-a-file'>the import guide</a> to ensure your file has the correct format.";

            Box b = new Box (BoxLayout.Y_AXIS);

            this.fileFindError.setVisible (false);
            this.fileFindError.setBorder (UIUtils.createPadding (0, 5, 5, 5));
            b.add (this.fileFindError);

            FormLayout fl = new FormLayout ("10px, right:p, 6px, fill:200px:grow, 10px",
                                            "p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            this.fileFind.setOnSelectHandler (new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.file = _this.fileFind.getSelectedFile ();

                    //_this.checkForFileToImport ();

                }

            });

            this.fileFind.setApproveButtonText (Environment.getUIString (LanguageStrings.importproject,
                                                                         LanguageStrings.stages,
                                                                         LanguageStrings.selectfile,
                                                                         LanguageStrings.finder,
                                                                         LanguageStrings.button));
                                                //"Select");
            this.fileFind.setFinderSelectionMode (JFileChooser.FILES_ONLY);
            this.fileFind.setFinderTitle (Environment.getUIString (LanguageStrings.importproject,
                                                                   LanguageStrings.stages,
                                                                   LanguageStrings.selectfile,
                                                                   LanguageStrings.finder,
                                                                   LanguageStrings.title));

            String def = null;

            if (this.pv != null)
            {

                def = this.pv.getProject ().getProperty (Constants.EXPORT_DIRECTORY_PROPERTY_NAME);

            }

            File f = this.file;

            if (f == null)
            {

                if (def != null)
                {

                    f = new File (def);

                }
            }

            if (f == null)
            {

                f = FileSystemView.getFileSystemView ().getDefaultDirectory ();

            }

            this.fileFind.setFile (f);

            this.fileFind.setFileFilter (ImportProject.fileFilter);

            this.fileFind.setFindButtonToolTip (Environment.getUIString (LanguageStrings.importproject,
                                                                         LanguageStrings.stages,
                                                                         LanguageStrings.selectfile,
                                                                         LanguageStrings.finder,
                                                                         LanguageStrings.title));
            //"Click to find a file");
            this.fileFind.setClearOnCancel (true);
            this.fileFind.init ();

            builder.addLabel (Environment.getUIString (LanguageStrings.importproject,
                                                       LanguageStrings.stages,
                                                       LanguageStrings.selectfile,
                                                       LanguageStrings.finder,
                                                       LanguageStrings.label),
                              //"File",
                              cc.xy (2,
                                     1));
            builder.add (this.fileFind,
                         cc.xy (4,
                                1));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);

            b.add (p);

            ws.panel = b;

            return ws;

        }

        if (stage.equals (CHOOSE_STAGE))
        {

            ActionListener onSelect = new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    _this.enableButtons (_this.getCurrentStage ());

                }

            };

            this.importFromProject = UIUtils.createRadioButton (Environment.getUIString (LanguageStrings.importproject,
                                                                                         LanguageStrings.stages,
                                                                                         LanguageStrings.choose,
                                                                                         LanguageStrings.options,
                                                                                         LanguageStrings.importfromproject),
                                                                //"Import from a {Project}",
                                                                onSelect);
            this.importFromFile = UIUtils.createRadioButton (Environment.getUIString (LanguageStrings.importproject,
                                                                                         LanguageStrings.stages,
                                                                                         LanguageStrings.choose,
                                                                                         LanguageStrings.options,
                                                                                         LanguageStrings.importfromfile),
                                                             //"Import from a File",
                                                             onSelect);

            this.importFromFile.setSelected (false);
            this.importFromProject.setSelected (false);

            this.importFromFile.addActionListener (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    _this.proj = null;

                    if (_this.itemsTree != null)
                    {

                        _this.itemsTree.setModel (null);

                    }

                    if (_this.projectList != null)
                    {

                        _this.projectList.clearSelection ();

                    }

                }

            });

            this.importFromProject.addActionListener (new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    _this.proj = null;

                    if (_this.itemsTree != null)
                    {

                        _this.itemsTree.setModel (null);

                    }

                    if (_this.projectList != null)
                    {

                        _this.projectList.clearSelection ();

                    }

                }

            });

            ButtonGroup g = new ButtonGroup ();
            g.add (this.importFromFile);
            g.add (this.importFromProject);

            ws.title = Environment.getUIString (LanguageStrings.importproject,
                                                LanguageStrings.stages,
                                                LanguageStrings.choose,
                                                LanguageStrings.title);
            //"What would you like to import?";
            ws.helpText = Environment.getUIString (LanguageStrings.importproject,
                                                LanguageStrings.stages,
                                                LanguageStrings.choose,
                                                LanguageStrings.text);
            //"Select whether you wish to import from a file or from one of your {projects}.";

            FormLayout fl = new FormLayout ("p",
                                            "p, 6px, p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            builder.add (this.importFromFile,
                         cc.xy (1,
                                1));
            builder.add (this.importFromProject,
                         cc.xy (1,
                                3));

            JPanel p = builder.getPanel ();
            p.setOpaque (false);
            p.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            p.setBorder (UIUtils.createPadding (5, 10, 0, 10));

            ws.panel = p;

            return ws;

        }

        if (stage.equals (SELECT_PROJECT_STAGE))
        {

            ws.title = Environment.getUIString (LanguageStrings.importproject,
                                                LanguageStrings.stages,
                                                LanguageStrings.selectproject,
                                                LanguageStrings.title);
            //"Select a {Project} to import from";
            // TODO: Fix this, we need some text otherwise the textbox collapses.
            ws.helpText = "&nbsp;";

            java.util.List<ProjectInfo> projs = null;

            try
            {

                projs = new ArrayList (Environment.getAllProjectInfos ());

            } catch (Exception e) {

                Environment.logError ("Unable to get all projects",
                                      e);

                UIUtils.showErrorMessage (this.viewer,
                                          Environment.getUIString (LanguageStrings.importproject,
                                                                   LanguageStrings.stages,
                                                                   LanguageStrings.selectproject,
                                                                   LanguageStrings.getallprojectserror));
                                          //"Unable to get all the {projects}.");

                return null;

            }

            if (this.pv != null)
            {

                projs.remove (Environment.getProjectInfo (this.pv.getProject ()));

            }

            Collections.sort (projs,
                              new ProjectInfoSorter ());

            this.projectList = new JList (new Vector (projs));
            this.projectList.setLayoutOrientation (JList.VERTICAL);
            this.projectList.setOpaque (true);
            this.projectList.setBackground (UIUtils.getComponentColor ());
            this.projectList.setToolTipText (Environment.getUIString (LanguageStrings.importproject,
                                                                      LanguageStrings.stages,
                                                                      LanguageStrings.selectproject,
                                                                      LanguageStrings.projectslist,
                                                                      LanguageStrings.tooltip));
                                             //Environment.replaceObjectNames ("Click to select this {Project}."));
            UIUtils.setAsButton (this.projectList);

            this.projectList.addListSelectionListener (new ListSelectionListener ()
            {

                @Override
                public void valueChanged (ListSelectionEvent ev)
                {

                    _this.enableButton (NEXT_BUTTON_ID,
                                        true);

                }

            });

            this.projectList.setCellRenderer (new DefaultListCellRenderer ()
            {

                public Component getListCellRendererComponent (JList   list,
                                                               Object  value,
                                                               int     index,
                                                               boolean isSelected,
                                                               boolean cellHasFocus)
                {

                    JLabel c = (JLabel) super.getListCellRendererComponent (list,
                                                                            value,
                                                                            index,
                                                                            isSelected,
                                                                            cellHasFocus);

                    ProjectInfo p = (ProjectInfo) value;
                    c.setFont (c.getFont ().deriveFont (UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));
                    c.setText (p.getName ());
                    c.setBorder (UIUtils.createBottomLineWithPadding (5, 5, 5, 5));

                    if (isSelected)
                    {

                        c.setForeground (UIUtils.getComponentColor ());

                    } else {

                        c.setForeground (UIUtils.getTitleColor ());

                    }

                    if (cellHasFocus)
                    {

                        c.setBackground (list.getSelectionBackground ());

                    }

                    return c;

                }

            });

            JScrollPane sp = UIUtils.createScrollPane (this.projectList);

            ws.panel = sp;

            return ws;

        }

        return ws;

    }

    public void exceptionOccurred (Exception e,
                                   URI       u)
    {

        Environment.logError ("Unable to import file/directory: " +
                              u,
                              e);

        final ImportProject _this = this;

        SwingUtilities.invokeLater (new Runner ()
            {

                public void run ()
                {

                    UIUtils.showErrorMessage (_this,
                                              getUIString (importproject,errors,general));
                                              //"Unable to import file");

                }

            });

    }

    public void projectCreated (final Project p,
                                URI           uri)
    {

        final ImportProject _this = this;

        UIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (_this.itemsTree != null)
                {

                    _this.itemsTree.setModel (new DefaultTreeModel (_this.createTree (p)));

                    UIUtils.expandAllNodesWithChildren (_this.itemsTree);

                    _this.proj = p;

                    _this.enableButton (NEXT_BUTTON_ID,
                                        true);

                }

            }

        });

    }

    private String getFirstLastSentence (String s)
    {

        if (s == null)
        {

            return "";

        }

        Paragraph p = new Paragraph (s,
                                     0);

        if (p.getSentenceCount () == 0)
        {

            return "";

        }

        StringBuilder b = new StringBuilder (p.getFirstSentence ().getText ());

        if (p.getSentenceCount () > 1)
        {

            b.append (Environment.getUIString (LanguageStrings.importproject,
                                               LanguageStrings.moretextindicator));

            b.append (p.getFirstSentence ().getNext ().getText ());

        }

        return b.toString ();

    }

    private void addAssetsToTree (DefaultMutableTreeNode     root,
                                  UserConfigurableObjectType type,
                                  Set<Asset>                 assets)
    {

        if ((assets == null)
            ||
            (assets.size () == 0)
           )
        {

            return;

        }

        TreeParentNode c = new TreeParentNode (type.getObjectTypeId (),
                                               type.getObjectTypeNamePlural ());

        SelectableDataObject sd = new SelectableDataObject (c);

        sd.selected = true;

        DefaultMutableTreeNode tn = new DefaultMutableTreeNode (sd);

        root.add (tn);

        java.util.List<Asset> lassets = new ArrayList (assets);

        Collections.sort (lassets,
                          NamedObjectSorter.getInstance ());

        for (Asset a : lassets)
        {

            if ((this.pv != null) &&
                (this.addToProject.isSelected ()))
            {

                if (this.pv.getProject ().hasAsset (a))
                {

                    continue;

                }

            }

            sd = new SelectableDataObject (a);

            sd.selected = true;

            DefaultMutableTreeNode n = new DefaultMutableTreeNode (sd);

            tn.add (n);

            String t = this.getFirstLastSentence (a.getDescriptionText ());

            if (t.length () > 0)
            {

                // Get the first and last sentence.
                n.add (new DefaultMutableTreeNode (t));

            }

        }

    }

    private Project getSelectedItems ()
    {

        String projName = null;

        if (this.newProjectPanel == null)
        {

            projName = this.pv.getProject ().getName ();

        } else {

            projName = this.newProjectPanel.getName ();

        }

        this.filesToCopy = new HashSet ();

        Project p = new Project (projName);

        Book b = new Book (p,
                           null);

        if (this.proj != null)
        {

            p.setProjectDirectory (this.proj.getProjectDirectory ());

        }

        p.addBook (b);
        b.setName (projName);

        DefaultTreeModel dtm = (DefaultTreeModel) this.itemsTree.getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        Enumeration en = root.depthFirstEnumeration ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) en.nextElement ();

            Object o = node.getUserObject ();

            if (o instanceof SelectableDataObject)
            {

                SelectableDataObject so = (SelectableDataObject) o;

                if (so.selected)
                {

                    if (so.obj instanceof Asset)
                    {

                        Asset a = (Asset) so.obj;

                        p.addAsset (a);

                    }

                }

                if (so.obj instanceof Chapter)
                {

                    b.addChapter ((Chapter) so.obj);

                }

                if (this.fromProject != null)
                {

                    if (so.obj instanceof UserConfigurableObject)
                    {

                        UserConfigurableObject uo = (UserConfigurableObject) so.obj;

                        // Get the files that need to be transfered/copied.
                        for (UserConfigurableObjectField f : uo.getFields ())
                        {

                            for (String fn : f.getProjectFileNames ())
                            {

                                if (fn == null)
                                {

                                    continue;

                                }

                                this.filesToCopy.add (fn);

                            }

                        }

                    }

                }

            }

        }

        if ((b.getChapters () != null)
            &&
            (b.getChapters ().size () == 0)
           )
        {

            p.getBooks ().remove (b);

        }

        return p;

    }

    private DefaultMutableTreeNode createTree (Project p)
    {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode (new SelectableDataObject (p));

        DefaultMutableTreeNode n = null;
        DefaultMutableTreeNode tn = null;

        if (p.getBooks ().size () > 0)
        {

            Book b = p.getBooks ().get (0);

            if (b.getChapters ().size () > 0)
            {

                TreeParentNode c = new TreeParentNode (Chapter.OBJECT_TYPE,
                                                       Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE));

                SelectableDataObject sd = new SelectableDataObject (c);

                sd.selected = true;

                tn = new DefaultMutableTreeNode (sd);

                root.add (tn);
/*
                Collections.sort (b.getChapters (),
                                  NamedObjectSorter.getInstance ());
*/
                for (Chapter ch : b.getChapters ())
                {

                    if ((this.pv != null) &&
                        (this.addToProject.isSelected ()))
                    {

                        if (this.pv.getProject ().getBooks ().get (0).getChapterByName (ch.getName ()) != null)
                        {

                            continue;

                        }

                    }

                    sd = new SelectableDataObject (ch);

                    sd.selected = true;

                    n = new DefaultMutableTreeNode (sd);

                    tn.add (n);

                    String t = this.getFirstLastSentence (ch.getChapterText ());

                    if (t.length () > 0)
                    {

                        // Get the first and last sentence.
                        n.add (new DefaultMutableTreeNode (t));

                    }

                }

            }

        }

        Set<UserConfigurableObjectType> assetTypes = Environment.getAssetUserConfigurableObjectTypes (true);

        for (UserConfigurableObjectType t : assetTypes)
        {

            Set<Asset> as = p.getAssets (t);

            this.addAssetsToTree (root,
                                  t,
                                  as);

        }

        return root;

    }

    public static boolean isSupportedFileType (File f)
    {

        return ImportProject.fileFilter.accept (f);

    }

    @Override
    protected void enableButtons (String currentStage)
    {

        super.enableButtons (currentStage);

        if (currentStage.equals (CHOOSE_STAGE))
        {

            if ((!this.importFromFile.isSelected ())
                &&
                (!this.importFromProject.isSelected ())
               )
            {

                this.enableButton (NEXT_BUTTON_ID,
                                   false);

            }

        }

        if ((SELECT_ITEMS_STAGE.equals (currentStage))
            &&
            (this.proj == null)
           )
        {

            this.enableButton (NEXT_BUTTON_ID,
                               false);

        }

        if (SELECT_PROJECT_STAGE.equals (currentStage))
        {

            if (this.projectList != null)
            {

                if (this.projectList.getSelectedValue () != null)
                {

                    return;

                }

            }

            this.enableButton (NEXT_BUTTON_ID,
                               false);

        }

    }

}
