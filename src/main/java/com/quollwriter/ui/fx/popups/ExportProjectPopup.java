package com.quollwriter.ui.fx.popups;

import java.util.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

import org.jdom.*;

import javafx.event.*;
import javafx.scene.*;
import javafx.stage.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.embed.swing.*;
import javafx.scene.image.*;

import com.gentlyweb.xml.*;

import com.quollwriter.importer.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

// TEST ADD
import javafx.beans.value.*;
import javafx.collections.*;

public class ExportProjectPopup extends PopupContent
{

    public static final String POPUP_ID = "exportproject";

    public static final String SELECT_ITEMS_STAGE = "select-items";
    public static final String SELECT_FILE_STAGE = "select-file";
    public static final String SELECT_PROJECT_STAGE = "select-project";
    public static final String CHOOSE_STAGE = "choose";
    public static final String DECIDE_STAGE = "decide";
    public static final String NEW_PROJECT_STAGE = "new-project";

    private boolean         addToProjectOnly = false;
    private boolean         newProjectOnly = false;
    private Path filePathToImport = null;
    private Project         proj = null;
    private Project importFromProj = null;
    private NewProjectPanel newProjectPanel = null;
    private Wizard wizard = null;
    private RadioButton addToProject = null;
    private QuollFileField fileFind = null;
    private Form selectFileForm = null;
    private RadioButton importFromFile = null;
    private RadioButton importFromProject = null;
    private RadioButton createNewProject = null;
    private ProjectViewer pv = null;
    private FileChooser.ExtensionFilter fileFilter = null;
    private QuollTreeView<NamedObject> itemsTree = null;
    private VBox projectList = null;
    private Set<String> filesToCopy = new HashSet<> ();

    private Map<String, Wizard.Step> steps = new HashMap<> ();

    public ExportProjectPopup (AbstractProjectViewer viewer)
    {

        super (viewer);

        if (viewer instanceof ProjectViewer)
        {

            this.pv = (ProjectViewer) viewer;

            this.addToProject.textProperty ().bind (getUILanguageStringProperty (Arrays.asList (importproject,stages,decide,options,addtoproject),
                                                                                 pv.getProject ().nameProperty ()));

        } else {

            this.newProjectOnly = true;

        }

        this.fileFind = QuollFileField.builder ()
            .chooserTitle (getUILanguageStringProperty (importproject,stages,selectfile,finder,title))
            .limitTo (QuollFileField.Type.file)
            .withViewer (viewer)
            .fileExtensionFilter (getUILanguageStringProperty (importproject,supportedfiletypesdescription),
                                  "docx",
                                  "doc")
            .build ();

        this.fileFind.fileProperty ().addListener ((p, oldv, newv) ->
        {

            this.filePathToImport = (newv != null ? newv.toPath () : null);

        });

        this.itemsTree = new QuollTreeView<> ();

        this.itemsTree.setCellProvider (treeItem ->
        {

            NamedObject n = treeItem.getValue ();

            if (n instanceof UserConfigurableObjectType)
            {

                UserConfigurableObjectType uc = (UserConfigurableObjectType) n;

                QuollLabel l = QuollLabel.builder ()
                    .label (n.nameProperty ())
                    .build ();

                l.setGraphic (new ImageView (uc.getIcon16x16 ()));

                return l;

            }

            if (n instanceof TreeParentNode)
            {

                Node l = null;

                TreeParentNode tn = (TreeParentNode) treeItem.getValue ();

                if (tn.getForObjectType ().equals ("_string"))
                {

                    l = BasicHtmlTextFlow.builder ()
                        .text (n.nameProperty ())
                        .styleClassName (StyleClassNames.TEXT)
                        .build ();

                } else {

                    l = QuollLabel.builder ()
                        .styleClassName (tn.getForObjectType ())
                        .label (n.nameProperty ())
                        .build ();

                }

                return l;

            }

            return QuollLabel.builder ()
                .label (n.nameProperty ())
                .build ();

        });
        /*
        .builder ()
            .cellFactory (treeView ->
            {

                return new NamedObjectCheckBoxTreeCell (treeItem ->
                {

                    return new SimpleBooleanProperty (treeItem.getValue ().getObjectType ().equals ("_string"));

                });

            })
            .build ();
*/
        this.itemsTree.setShowRoot (false);

        EventHandler<ActionEvent> hand = ev ->
        {

            this.enableButtons (this.wizard,
                                this.wizard.getCurrentStepId ());

            this.proj = null;

            if (this.itemsTree != null)
            {

                this.itemsTree.setRoot (null);

            }

            if (this.projectList != null)
            {

                UIUtils.unselectChildren (this.projectList);

            }

        };

        this.importFromProject = QuollRadioButton.builder ()
            .label (getUILanguageStringProperty (importproject,stages,choose,options,importfromproject))
            .onAction (hand)
            .build ();

        this.importFromFile = QuollRadioButton.builder ()
            .label (getUILanguageStringProperty (importproject,stages,choose,options,importfromfile))
            .onAction (hand)
            .build ();

        this.addToProject = QuollRadioButton.builder ()
            .onAction (ev -> this.wizard.enableButton (Wizard.NEXT_BUTTON_ID, true))
            .build ();

        this.createNewProject = QuollRadioButton.builder ()
            .label (getUILanguageStringProperty (importproject,stages,decide,options,newproject))
            .onAction (ev -> this.wizard.enableButton (Wizard.NEXT_BUTTON_ID, true))
            .build ();

        // Maybe move to UIUtils?
        this.wizard = Wizard.builder ()
            .startStepId (this.getStartStepId ())
            .nextStepIdProvider (currId ->
            {

                return getNextStepId (currId);

            })
            .previousStepIdProvider (currId ->
            {

                return getPreviousStepId (currId);

            })
            .stepProvider (currId ->
            {

                return getStep (currId);

            })
            .onCancel (ev ->
            {

                this.close ();

            })
            .onFinish (ev ->
            {

                this.handleFinish ();

                this.close ();

            })
            .nextStepCheck ((currId, nextId) ->
            {

                return this.handleStepChange (currId,
                                              nextId);

            })
            .previousStepCheck ((currId, prevId) ->
            {

                return this.handleStepChange (currId, prevId);

            })
            .onStepShow (ev ->
            {

                this.enableButtons (ev.getWizard (),
                                    ev.getCurrentStepId ());

            })
            .build ();

        VBox b = new VBox ();
        VBox.setVgrow (this.wizard, Priority.ALWAYS);
        b.getChildren ().addAll (this.wizard);

        this.getChildren ().addAll (b);

    }

    public String getStartStepId ()
    {

        if ((this.newProjectOnly)
            &&
            (this.filePathToImport != null)
           )
        {

            return SELECT_ITEMS_STAGE;

        }

        if ((this.addToProjectOnly)
            &&
            (this.filePathToImport != null)
           )
        {

            return SELECT_ITEMS_STAGE;

        }

        return CHOOSE_STAGE;

    }

    public boolean handleStepChange (String oldStepId,
                                     String newStepId)
    {

        final ExportProjectPopup _this = this;

        if (SELECT_PROJECT_STAGE.equals (oldStepId)
            &&
            (SELECT_ITEMS_STAGE.equals (newStepId))
           )
        {

            List<Node> sel = UIUtils.getSelected (this.projectList);

            if (sel.size () == 0)
            {

                return false;

            }

            final ProjectInfo pi = (ProjectInfo) sel.get (0).getUserData (); // TODO Remove this.projectList.getSelectedValue ();

            Runnable r = () ->
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

                    ComponentUtils.showErrorMessage (_this.viewer,
                                                     getUILanguageStringProperty (Arrays.asList (importproject,errors,cantopenproject)));
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

                // Need a selectable tree here.
                _this.itemsTree.setRoot (_this.createTree (p));

                _this.enableButtons (_this.wizard,
                                     newStepId);

                _this.importFromProj = p;

                _this.proj = p;

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
                                                  pwd ->
                                                  {

                                                      pi.setFilePassword (pwd);

                                                      this.wizard.showStep (SELECT_ITEMS_STAGE);

                                                      UIUtils.runLater (r);

                                                  },
                                                  null,
                                                  this.viewer);

                return false;

            } else {

                UIUtils.runLater (r);

            }

            return true;

        }

        if (SELECT_FILE_STAGE.equals (oldStepId))
        {

            if (CHOOSE_STAGE.equals (newStepId))
            {

                return true;

            }

            return this.checkForFileToImport ();

        }

        if (SELECT_ITEMS_STAGE.equals (newStepId))
        {

            if (this.importFromProject.isSelected ())
            {

                return true;

            }

            if (!SELECT_PROJECT_STAGE.equals (oldStepId))
            {

                this.importFromProj = null;

                return this.checkForFileToImport ();

            }

        }

        if ((NEW_PROJECT_STAGE.equals (oldStepId))
            &&
            (newStepId == null)
           )
        {

            if (!this.newProjectPanel.checkForm ())
            {

                return false;

            }

        }

        return true;

    }

    private void handleFinish ()
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
                                                      this.fileFind.getFile ());

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to save asset: " +
                                              a,
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (Arrays.asList (importproject,errors,unabletosave),
                                                                                      a.getName ()));

                    }

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

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         getUILanguageStringProperty (Arrays.asList (importproject,errors,unabletosave),
                                                                                      //"Unable to save: " +
                                                                                      c.getName ()));

                        continue;

                    }

                    this.pv.addChapterToTreeAfter (c,
                                                   null);

                    this.pv.openObjectSection (c.getObjectType ());

                }

            }

            if (this.importFromProj != null)
            {

                for (String fn : this.filesToCopy)
                {

                    File f = new File (this.importFromProj.getFilesDirectory (),
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

            this.pv.fireProjectEvent (ProjectEvent.Type._import,
                                      ProjectEvent.Action.any);

            QuollPopup.messageBuilder ()
                .withViewer (this.pv)
                .title (importproject,importcompletepopup,title)
                .message (importproject,importcompletepopup,text)
                .build ();

        } else {

            // Create a new project.
            ProjectViewer pj = new ProjectViewer ();

            String pwd = this.newProjectPanel.getPassword ();

            try
            {

                // Get the "old" files dir.

                this.proj.setName (this.newProjectPanel.getName ());

                pj.newProject (this.newProjectPanel.getSaveDirectory ().toPath (),
                               this.proj,
                               pwd);

                if (this.filesToCopy != null)
                {

                    for (String fn : this.filesToCopy)
                    {

                        pj.getProject ().saveToFilesDirectory (new File (this.importFromProj.getFilesDirectory (),
                                                                         fn),
                                                               fn);

                    }

                }

                pj.init (null);

                pj.createActionLogEntry (pj.getProject (),
                                         "Project imported from: " +
                                         this.fileFind.getFile ());

            } catch (Exception e)
            {

                Environment.logError ("Unable to create new project: " +
                                      this.proj,
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (Arrays.asList (importproject,errors,unabletocreateproject),
                                                                              this.proj.getName ()));
                                          //"Unable to create new project: " + this.proj.getName ());

                return;

            }

            pj.fireProjectEvent (ProjectEvent.Type._import,
                                 ProjectEvent.Action.any);

        }

    }

    public void setFilePathToImport (Path p)
    {

        if (p == null)
        {

            this.filePathToImport = null;

            return;

        }

        String fn = p.getFileName ().toString ().toLowerCase ();

        if ((!fn.endsWith (".docx"))
            &&
            (!fn.endsWith (".doc"))
           )
        {

            throw new IllegalArgumentException ("File type not supported for path: " + p);

        }

        if (this.fileFind != null)
        {

            this.fileFind.setFile (p.toFile ());

        }

        this.filePathToImport = p;

    }

    public Wizard.Step getStep (String stepId)
    {

        final ExportProjectPopup _this = this;

        Wizard.Step ws = this.steps.get (stepId);

        if (ws != null)
        {

            return ws;

        }

        ws = new Wizard.Step ();

        if (stepId.equals (NEW_PROJECT_STAGE))
        {

            ws = this.createNewProjectStep ();

        }

        if (stepId.equals (DECIDE_STAGE))
        {

            ws = this.createDecideStep ();

        }

        if (stepId.equals (SELECT_ITEMS_STAGE))
        {

            ws = this.createSelectItemsStep ();

        }

        if (stepId.equals (SELECT_FILE_STAGE))
        {

            ws = this.createSelectFileStep ();

        }

        if (stepId.equals (CHOOSE_STAGE))
        {

            ws = this.createChooseStep ();

        }

        if (stepId.equals (SELECT_PROJECT_STAGE))
        {

            ws = this.createSelectProjectStep ();

        }

        ws.content.getStyleClass ().add (stepId);

        this.steps.put (stepId,
                        ws);

        return ws;

    }

    private Wizard.Step createNewProjectStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (importproject,stages,newproject,title);
        //"Enter the new {Project} details";

        VBox b = new VBox ();

        //"To create a new {Project} enter the name below and select the directory where it should be saved.";

        this.newProjectPanel = new NewProjectPanel (this.viewer,
                                                    getUILanguageStringProperty (importproject,stages,newproject,LanguageStrings.text),
                                                    false);

        b.getChildren ().addAll (this.newProjectPanel);

        // TODO Need to handle the buttons.

        ws.content = b;
/*
        ws.panel = this.newProjectPanel.createPanel (this,
                                                     null,
                                                     false,
                                                     null,
                                                     false);
*/
        return ws;

    }

    private Wizard.Step createDecideStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (importproject,stages,decide,title);
        //"What would you like to do?";

                                   //"Add to " + Environment.getObjectTypeName (Project.OBJECT_TYPE) + ": " + pv.getProject ().getName ());
/*
TODO Remove
            this.createNewProject = new JRadioButton (Environment.getUIString (LanguageStrings.importproject,
                                                                               LanguageStrings.stages,
                                                                               LanguageStrings.decide,
                                                                               LanguageStrings.options,
                                                                               LanguageStrings.newproject));
                                                      //"Create a new " + Environment.getObjectTypeName (Project.OBJECT_TYPE));

            this.createNewProject.setOpaque (false);
*/

        QuollRadioButtons buts = QuollRadioButtons.builder ()
            .button (this.addToProject)
            .button (this.createNewProject)
            .build ();

        ws.content = buts;

        return ws;

    }

    private Wizard.Step createSelectItemsStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (Arrays.asList (importproject,stages,selectitems,title));
        //"Select the items you wish to import";

        BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        desc.textProperty ().bind (Bindings.createStringBinding (() ->
        {

            return String.format (getUILanguageStringProperty (importproject,stages,selectitems,text).getValue (),
                                  (this.addToProject.isSelected () ? getUILanguageStringProperty (importproject,stages,selectitems,textextra).getValue () : ""));

        },
        this.addToProject.selectedProperty ()));
        //"Check the items below to ensure that they match what is in your file.  The first and last sentences of the description (if present) are shown for each item." + (this.addToProject.isSelected () ? ("  Only items not already in the " + Environment.getObjectTypeName (Project.OBJECT_TYPE) + " will be listed.") : "");

/*
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
*/

        ScrollPane sp = new ScrollPane (this.itemsTree);
        //this.createFileSystemTree ());

        //this.itemsTree);
        VBox b = new VBox ();

        b.getChildren ().addAll (desc, sp); //this.itemsTree);

        ws.content = b;

        return ws;

    }

    private Wizard.Step createSelectFileStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (importproject,stages,selectfile,title);
        //"Select a file to import";

        VBox b = new VBox ();
        b.getChildren ().add (BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .text (getUILanguageStringProperty (importproject,stages,selectfile,text))
            .build ());
        //"Microsoft Word files (.doc and .docx) are supported.  Please check <a href='help://projects/importing-a-file'>the import guide</a> to ensure your file has the correct format.";

        // TODO Add the error.
        this.selectFileForm = Form.builder ()
            .item (getUILanguageStringProperty (importproject,stages,selectfile,finder,label),
                   this.fileFind)
            .build ();

        b.getChildren ().add (this.selectFileForm);
/*
        Box b = new Box (BoxLayout.Y_AXIS);

        this.fileFindError.setVisible (false);
        this.fileFindError.setBorder (UIUtils.createPadding (0, 5, 5, 5));
        b.add (this.fileFindError);
        */
/*
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
*/
/*
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
*/
        String def = null;

        if (this.pv != null)
        {

            def = this.pv.getProject ().getProperty (Constants.EXPORT_DIRECTORY_PROPERTY_NAME);

        }

        File f = null;

        if (this.filePathToImport != null)
        {

            f = this.filePathToImport.toFile ();

        }

        if (f == null)
        {

            if (def != null)
            {

                f = new File (def);

            }
        }

        if (f == null)
        {

            f = new File (System.getProperty ("user.home"));
            // TODO Remove? FileSystemView.getFileSystemView ().getDefaultDirectory ();

        }

        this.fileFind.setFile (f);
/*
TODO Add tool tip?
        this.fileFind.setFileFilter (ImportProject.fileFilter);

        this.fileFind.setFindButtonToolTip (Environment.getUIString (LanguageStrings.importproject,
                                                                     LanguageStrings.stages,
                                                                     LanguageStrings.selectfile,
                                                                     LanguageStrings.finder,
                                                                     LanguageStrings.title));
*/
/*
        //"Click to find a file");
        this.fileFind.setClearOnCancel (true);
        this.fileFind.init ();
*/
/*
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
*/
        ws.content = b;

        return ws;

    }

    private Wizard.Step createChooseStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        VBox b = new VBox ();

        b.getStyleClass ().add (CHOOSE_STAGE);

        b.getChildren ().add (BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.DESCRIPTION)
            .text (getUILanguageStringProperty (importproject,stages,choose,text))
            .build ());

        this.importFromFile.setSelected (false);
        this.importFromProject.setSelected (false);

        QuollRadioButtons rbs = QuollRadioButtons.builder ()
            .button (this.importFromFile)
            .button (this.importFromProject)
            .build ();

        b.getChildren ().add (rbs);

        ws.content = b;
        ws.title = getUILanguageStringProperty (importproject,stages,choose,title);
        //"What would you like to import?";
        //"Select whether you wish to import from a file or from one of your {projects}.";

        return ws;

    }

    private Wizard.Step createSelectProjectStep ()
    {

        Wizard.Step ws = new Wizard.Step ();

        ws.title = getUILanguageStringProperty (importproject,stages,selectproject,title);
        //"Select a {Project} to import from";
        // TODO: Fix this, we need some text otherwise the textbox collapses.

        List<ProjectInfo> projs = null;

        try
        {

            projs = new ArrayList<> (Environment.getAllProjectInfos ());

        } catch (Exception e) {

            Environment.logError ("Unable to get all projects",
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (importproject,stages,selectproject,getallprojectserror));
                                      //"Unable to get all the {projects}.");

            return null;

        }

        if (this.pv != null)
        {

            projs.remove (Environment.getProjectInfo (this.pv.getProject ()));

        }

        Collections.sort (projs,
                          new ProjectInfoSorter ());

        this.projectList = new VBox ();
        this.projectList.getStyleClass ().add (StyleClassNames.PROJECTS);

        projs.stream ()
            .forEach (pr ->
            {

                QuollLabel l = QuollLabel.builder ()
                    .label (pr.nameProperty ())
                    .tooltip (importproject,stages,selectproject,projectslist,tooltip)
                    .build ();

                l.setUserData (pr);

                l.setOnMouseClicked (ev ->
                {

                    UIUtils.toggleSelected (this.projectList,
                                            l);

                    if (UIUtils.isSelected (l))
                    {

                        this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                                  true);

                    }

                });

                this.projectList.getChildren ().add (l);

            });
/*
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

                this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
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
*/
        ScrollPane sp = new ScrollPane (this.projectList);

        VBox b = new VBox ();

        b.getStyleClass ().add (SELECT_PROJECT_STAGE);
        b.getChildren ().add (sp);

        ws.content = b;

        return ws;

    }

    public String getNextStepId (String currStage)
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

        if (SELECT_ITEMS_STAGE.equals (currStage))
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

    public String getPreviousStepId (String currStage)
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

    private void getSelectedObjects (TreeItem<NamedObject> item,
                                     Set<NamedObject>      selected)
    {

        if (item instanceof CheckBoxTreeItem)
        {

            CheckBoxTreeItem<NamedObject> cti = (CheckBoxTreeItem<NamedObject>) item;

            if (cti.isSelected ())
            {

                selected.add (cti.getValue ());

            }

        }

        item.getChildren ().stream ()
            .forEach (it -> getSelectedObjects (it, selected));

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

        this.filesToCopy = new HashSet<> ();

        Project p = new Project (projName);

        Book b = new Book (p,
                           null);

        if (this.proj != null)
        {

            p.setProjectDirectory (this.proj.getProjectDirectory ());

        }

        p.addBook (b);
        b.setName (projName);

        Set<NamedObject> selected = new HashSet<> ();
        this.getSelectedObjects (this.itemsTree.getRoot (),
                                 selected);

        for (NamedObject n : selected)
        {

            if (n instanceof Chapter)
            {

                b.addChapter ((Chapter) n);

            }

            if (n instanceof Asset)
            {

                Asset a = (Asset) n;

                p.addAsset (a);

            }

            if (this.importFromProj != null)
            {

                if (n instanceof UserConfigurableObject)
                {

                    UserConfigurableObject uo = (UserConfigurableObject) n;

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

        if ((b.getChapters () != null)
            &&
            (b.getChapters ().size () == 0)
           )
        {

            p.getBooks ().remove (b);

        }

        return p;

    }

    private boolean checkForFileToImport ()
    {

        final ExportProjectPopup _this = this;

        this.selectFileForm.hideError ();

        File f = this.fileFind.getFile ();

        Path file = null;

        if (f != null)
        {

            file = f.toPath ();

        }

        if (file == null)
        {

            this.selectFileForm.showError (getUILanguageStringProperty (importproject,errors,nofileselected));
                                        //"Please select a file to import.");
            return false;

        }

        if (Files.notExists (file))
        {

            this.selectFileForm.showError (getUILanguageStringProperty (importproject,errors,filenotexist));
            //"File does not exist, please select a valid file.");
            return false;

        }

        // TODO, remove this can't happen.
        if (Files.isDirectory (file))
        {

            this.selectFileForm.showError (getUILanguageStringProperty (importproject,errors,dirselected));
            return false;

        }

        try
        {

            Importer.importProject (file.toUri (),
                                    new ImportCallback ()
            {

                @Override
                public void projectCreated (Project p,
                                            URI     u)
                {

                    UIUtils.runLater (() ->
                    {

                        if (_this.itemsTree != null)
                        {

                            _this.itemsTree.setRoot (_this.createTree (p));

                            // TODO Needed? UIUtils.expandAllNodesWithChildren (_this.itemsTree.getRoot ());

                            _this.proj = p;

                            _this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                                       true);

                        }

                    });

                }

                public void exceptionOccurred (Exception e,
                                               URI       u)
                {

                    Environment.logError ("Unable to import file/directory: " +
                                          u,
                                          e);

                    UIUtils.runLater (() ->
                    {

                        ComponentUtils.showErrorMessage (_this.getViewer (),
                                                         getUILanguageStringProperty (importproject,errors,general));
                                                      //"Unable to import file");

                    });

                }

            });

        } catch (Exception e)
        {

            Environment.logError ("Unable to convert: " +
                                  file +
                                  " to a uri",
                                  e);

            this.selectFileForm.showError (getUILanguageStringProperty (importproject,errors,openfile));

            return false;

        }

        return true;

    }

    private CheckBoxTreeItem<NamedObject> createTree (Project p)
    {

        CheckBoxTreeItem<NamedObject> root = new CheckBoxTreeItem<> (p);

        if (p.getBooks ().size () > 0)
        {

            Book b = p.getBooks ().get (0);

            if (b.getChapters ().size () > 0)
            {

                TreeParentNode c = new TreeParentNode (Chapter.OBJECT_TYPE,
                                                       // Should use the property instead?
                                                       Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE).getValue ());

                CheckBoxTreeItem<NamedObject> ci = new CheckBoxTreeItem<> (c);
                root.getChildren ().add (ci);

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

                    CheckBoxTreeItem<NamedObject> chi = new CheckBoxTreeItem<> (ch);
                    ci.getChildren ().add (chi);
                    chi.setExpanded (true);

                    String t = this.getFirstLastSentence (ch.getChapterText ());

                    if (t.length () > 0)
                    {

                        TreeParentNode n = new TreeParentNode ("_string",
                                                               t);

                        // Get the first and last sentence.
                        chi.getChildren ().add (new TreeItem<NamedObject> (n));

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

        root.addEventHandler (CheckBoxTreeItem.checkBoxSelectionChangedEvent (),
                              ev ->
        {

            this.wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                      this.getSelectedCount (root) > 0);

        });

        return root;

    }

    private int getSelectedCount (TreeItem<NamedObject> item)
    {

        int c = 0;

        if (item instanceof CheckBoxTreeItem)
        {

            c += ((CheckBoxTreeItem<NamedObject>) item).isSelected () ? 1 : 0;

        }

        c += item.getChildren ().stream ()
            .mapToInt (it ->
            {

                int _c = 0;

                if (it instanceof CheckBoxTreeItem)
                {

                    CheckBoxTreeItem<NamedObject> cit = (CheckBoxTreeItem<NamedObject>) it;

                    _c = cit.isSelected () ? 1 : 0 + this.getSelectedCount (cit);

                }

                return _c;

            })
            .sum ();

        return c;

    }

    private void addAssetsToTree (TreeItem<NamedObject>      root,
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

        CheckBoxTreeItem<NamedObject> ci = new CheckBoxTreeItem<> (type);

        root.getChildren ().add (ci);

        List<Asset> lassets = new ArrayList<> (assets);

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

            CheckBoxTreeItem<NamedObject> cai = new CheckBoxTreeItem<> (a);

            ci.getChildren ().add (cai);

            String t = this.getFirstLastSentence (a.getDescriptionText ());

            if (t.length () > 0)
            {

                // Get the first and last sentence.
                cai.getChildren ().add (new TreeItem<NamedObject> (new TreeParentNode ("_string", t)));

            }

        }

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

            b.append (getUILanguageStringProperty (importproject,moretextindicator).getValue ());

            b.append (p.getFirstSentence ().getNext ().getText ());

        }

        return b.toString ();

    }

    public void setAddToProjectOnly (boolean v)
    {

        this.addToProjectOnly = v;

    }

    public void setNewProjectOnly (boolean v)
    {

        this.newProjectOnly = v;

    }

    private void enableButtons (Wizard wizard,
                                String currentStepId)
    {

        wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                             true);

        if (CHOOSE_STAGE.equals (currentStepId))
        {

            if ((!this.importFromFile.isSelected ())
                &&
                (!this.importFromProject.isSelected ())
               )
            {

                wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                     false);

            }

        }

        if ((SELECT_ITEMS_STAGE.equals (currentStepId))
            &&
            (this.proj == null)
           )
        {

            wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                 false);

        }

        if ((SELECT_ITEMS_STAGE.equals (currentStepId))
            &&
            (this.itemsTree.getRoot () != null)
           )
        {

            wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                 this.getSelectedCount (this.itemsTree.getRoot ()) > 0);

        }

        if (SELECT_PROJECT_STAGE.equals (currentStepId))
        {

            if (UIUtils.getSelected (this.projectList).size () == 0)
            {

                wizard.enableButton (Wizard.NEXT_BUTTON_ID,
                                     false);

            }

        }

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (getUILanguageStringProperty (importproject,title))
            .styleClassName (StyleClassNames.IMPORT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .removeOnClose (true)
            .build ();
        p.requestFocus ();

        Environment.fireUserProjectEvent (this.viewer,
                                          ProjectEvent.Type._import,
                                          ProjectEvent.Action.show);

        return p;

    }

}
