package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.io.File;

import java.net.*;

import java.text.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.tree.*;

import com.gentlyweb.xml.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.importer.*;

import com.quollwriter.ui.components.*;
import com.quollwriter.ui.renderers.*;


public class ImportProject extends Wizard implements ImportCallback
{

    private FileFinder      fileFind = null;
    private JLabel          fileFindError = null;
    private JTextField      urlField = null;
    private JRadioButton    addToProject = null;
    private JRadioButton    createNewProject = null;
    private JTree           itemsTree = null;
    private JScrollPane     itemsTreeScroll = null;
    private Box             nextB = null;
    private Project         proj = null;
    private NewProjectPanel newProjectPanel = null;
    private ProjectViewer   pv = null;

    public ImportProject (ProjectViewer pv)
    {

        super (pv);

        this.pv = pv;
        this.proj = pv.getProject ();

    }

    public String getFirstHelpText ()
    {

        return "It is recommended that you read the <a href='help://projects/importing-a-file'>guide to importing</a> prior to using this function.  The import expects the file(s) to be in a certain format.";

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

                    Asset a = (Asset) n;

                    this.pv.getProject ().addAsset (a);

                    try
                    {

                        this.pv.saveObject (a,
                                            true);

                        this.pv.createActionLogEntry (a,
                                                      "Imported asset from: " +
                                                      this.fileFind.getSelectedFile ());

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to save asset: " +
                                              a,
                                              e);

                        UIUtils.showErrorMessage (this,
                                                  "Unable to save: " +
                                                  a.getName ());

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
                                                      "Imported chapter from: " +
                                                      this.fileFind.getSelectedFile ());

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to save asset: " +
                                              c,
                                              e);

                        UIUtils.showErrorMessage (this,
                                                  "Unable to save: " +
                                                  c.getName ());

                        continue;

                    }

                    this.pv.addChapterToTreeAfter (c,
                                                   null);

                    this.pv.openObjectSection (c.getObjectType ());
                                                   
                }

            }

            UIUtils.showMessage ((PopupsSupported) this.pv,
                                 "Import complete",
                                 String.format ("The items have been imported into your {project}"));            
            
        } else
        {

            // Create a new project.
            ProjectViewer pj = new ProjectViewer ();

            this.proj.setName (this.newProjectPanel.getName ());

            String pwd = this.newProjectPanel.getPassword ();

            try
            {

                pj.newProject (this.newProjectPanel.getSaveDirectory (),
                               this.proj,
                               pwd);

                pj.createActionLogEntry (pj.getProject (),
                                         "Project imported from: " +
                                         this.fileFind.getSelectedFile ());

            } catch (Exception e)
            {

                Environment.logError ("Unable to create new project: " +
                                      this.proj,
                                      e);

                UIUtils.showErrorMessage (this,
                                          "Unable to create new project: " + this.proj.getName ());

                return false;

            }

        }

        this.pv.fireProjectEvent (ProjectEvent.IMPORT,
                                  ProjectEvent.ANY);

        return true;

    }

    public void handleCancel ()
    {

    }

    public String getNextStage (String currStage)
    {

        if (currStage == null)
        {

            return "decide";

        }

        if (currStage.equals ("select-file"))
        {

            return "select-items";

        }

        if (currStage.equals ("new-project"))
        {

            return "select-file";

        }

        if (currStage.equals ("select-items"))
        {

            return null;

        }

        if (currStage.equals ("decide"))
        {

            if (this.addToProject.isSelected ())
            {

                return "select-file";

            }

            return "new-project";

        }

        if (currStage.equals ("finish"))
        {

            if (this.addToProject.isSelected ())
            {

                return "finish-addto";

            }

        }

        return null;

    }

    public String getPreviousStage (String currStage)
    {

        if (currStage == null)
        {

            return null;

        }

        if (currStage.equals ("new-project"))
        {

            return "decide";

        }

        if (currStage.equals ("select-file"))
        {

            if (this.addToProject.isSelected ())
            {

                return "decide";

            } else
            {

                return "new-project";

            }

        }

        if (currStage.equals ("select-items"))
        {

            return "select-file";

        }

        return null;

    }

    private boolean checkForFileToImport ()
    {
        
        this.fileFindError.setVisible (false);
    
        if (this.fileFind.getSelectedFile () == null)
        {
            
            this.fileFindError.setText ("Please select a file to import.");
            this.fileFindError.setVisible (true);
            
            this.resize ();
            return false;
            
        }
    
        if (!this.fileFind.getSelectedFile ().exists ())
        {
            
            this.fileFindError.setText ("File does not exist, please select a valid file.");
            this.fileFindError.setVisible (true);
            
            this.resize ();
            return false;                    
            
        }
    
        if (this.fileFind.getSelectedFile ().isDirectory ())
        {
            
            this.fileFindError.setText ("Selection is a directory, please select a file instead.");
            this.fileFindError.setVisible (true);
            
            this.resize ();
            return false;
    
        }
    
        try
        {

            Importer.importProject (this.fileFind.getSelectedFile ().toURI (),
                                    this);

        } catch (Exception e)
        {

            Environment.logError ("Unable to convert: " +
                                  this.fileFind.getSelectedFile () +
                                  " to a uri",
                                  e);

            this.fileFindError.setText ("Unable to open selected file.");
            this.fileFindError.setVisible (true);
            
            this.resize ();
            return false;
            
        }
        
        this.resize ();
        
        return true;
        
    }
    
    public boolean handleStageChange (String oldStage,
                                      String newStage)
    {

        if (oldStage == null)
        {

            this.enableButton ("next",
                               false);

        }

        if (newStage != null)
        {
        
            if ((oldStage != null)
                &&
                ((oldStage.equals ("new-project"))
                 ||
                 (oldStage.equals ("select-items"))
                )
                &&
                (!newStage.equals ("decide"))
               )
            {

                return this.newProjectPanel.checkForm (this);

            }

            if (newStage.equals ("finish"))
            {

            }

            if (newStage.equals ("select-items"))
            {

                return this.checkForFileToImport ();
            
            }

        }

        return true;

    }

    public String getStartStage ()
    {

        return "decide";

    }

    public WizardStep getStage (String stage)
    {

        final ImportProject _this = this;

        WizardStep ws = new WizardStep ();

        if (stage.equals ("new-project"))
        {

            ws.title = "Enter the new {Project} details";

            ws.helpText = "To create a new {Project} enter the name below and select the directory where it should be saved.";

            this.newProjectPanel = new NewProjectPanel ();

            ws.panel = this.newProjectPanel.createPanel (this,
                                                         null,
                                                         false,
                                                         null,
                                                         false);

        }

        if (stage.equals ("decide"))
        {

            ws.title = "What would you like to do?";

            FormLayout fl = new FormLayout ("10px, p, 10px",
                                            "p, 6px, p, 6px, p, 6px");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            if (pv != null)
            {

                this.addToProject = new JRadioButton ("Add to " + Environment.getObjectTypeName (Project.OBJECT_TYPE) + ": " + pv.getProject ().getName ());

                this.addToProject.setOpaque (false);

                this.createNewProject = new JRadioButton ("Create a new " + Environment.getObjectTypeName (Project.OBJECT_TYPE));

                this.createNewProject.setOpaque (false);

                ButtonGroup bg = new ButtonGroup ();

                bg.add (this.addToProject);
                bg.add (this.createNewProject);

                this.addToProject.addActionListener (new ActionAdapter ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.showStage ("select-file");
                        
                            _this.enableButton ("next",
                                                true);

                        }

                    });

                this.createNewProject.addActionListener (new ActionAdapter ()
                    {

                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.showStage ("new-project");

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

        }

        if (stage.equals ("select-items"))
        {

            ws.title = "Select the items you wish to import";

            ws.helpText = "Check the items below to ensure that they match what is in your file.  The first and last sentences of the description (if present) are shown for each item." + (this.addToProject.isSelected () ? ("  Only items not already in the " + Environment.getObjectTypeName (Project.OBJECT_TYPE) + " will be listed.") : "");

            this.itemsTree = UIUtils.createTree ();
            this.itemsTree.setModel (null);

            this.itemsTree.addMouseListener (new MouseAdapter ()
                {

                    private void selectAllChildren (DefaultTreeModel       model,
                                                    DefaultMutableTreeNode n,
                                                    boolean                v)
                    {

                        Enumeration<DefaultMutableTreeNode> en = n.children ();

                        while (en.hasMoreElements ())
                        {

                            DefaultMutableTreeNode c = en.nextElement ();

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

            JScrollPane sp = new JScrollPane (this.itemsTree);

            sp.setOpaque (false);
            sp.getViewport ().setOpaque (false);
            sp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
            sp.setBorder (new LineBorder (new Color (127,
                                                     127,
                                                     127),
                                          1));

            ws.panel = sp;

        }

        if (stage.equals ("select-file"))
        {

            ws.title = "Select a file to import";
            ws.helpText = "Microsoft Word files (.doc and .docx) are supported.";

            Box b = new Box (BoxLayout.Y_AXIS);
            
            this.fileFindError = UIUtils.createErrorLabel ("Please select a file to import.");
            this.fileFindError.setVisible (false);
            this.fileFindError.setBorder (UIUtils.createPadding (0, 5, 5, 5));
            b.add (this.fileFindError);
            
            FormLayout fl = new FormLayout ("10px, right:p, 6px, fill:200px:grow, 10px",
                                            "p");

            PanelBuilder builder = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            this.fileFind = new FileFinder ();
        
            this.fileFind.setOnSelectHandler (new ActionListener ()
            {
                
                public void actionPerformed (ActionEvent ev)
                {
            
                    _this.checkForFileToImport ();
                    
                }
                
            });
            
            this.fileFind.setApproveButtonText ("Select");
            this.fileFind.setFinderSelectionMode (JFileChooser.FILES_ONLY);
            this.fileFind.setFinderTitle ("Select a file to import");
                        
            String def = this.proj.getProperty (Constants.EXPORT_DIRECTORY_PROPERTY_NAME);

            File defFile = FileSystemView.getFileSystemView ().getDefaultDirectory ();
            
            if (def != null)
            {

                defFile = new File (def);

            }

            this.fileFind.setFile (defFile);                            
                        
            this.fileFind.setFileFilter (new FileNameExtensionFilter ("Supported Files (docx, doc)",
                                                                      "docx",
                                                                      "doc"));
                                    
            this.fileFind.setFindButtonToolTip ("Click to find a file");
            this.fileFind.setClearOnCancel (true);
            this.fileFind.init ();
                        
            builder.addLabel ("File",
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
                                              "Unable to import file");

                }

            });

    }

    public void projectCreated (final Project p,
                                URI           uri)
    {

        final ImportProject _this = this;

        // Not on the swing event thread, need swingutilities.invokelater.
        UIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                if (_this.itemsTree != null)
                {
            
                    _this.itemsTree.setModel (new DefaultTreeModel (_this.createTree (p)));
    
                    UIUtils.expandAllNodesWithChildren (_this.itemsTree);
    
                    _this.proj = p;

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

        BreakIterator bi = BreakIterator.getSentenceInstance ();

        bi.setText (s);

        int start = bi.first ();
        int end = bi.next ();

        if ((end == BreakIterator.DONE) ||
            ((s.length () - 1) == end))
        {

            // Just return everything.
            return s;

        }

        StringBuilder b = new StringBuilder (s.substring (start,
                                                          end).trim ());

        start = bi.last ();

        // Go back one.
        int previous = bi.previous ();

        if (previous > end)
        {

            b.append (" ... ");

            b.append (s.substring (previous).trim ());

        }

        return b.toString ();

    }

    private void addAssetsToTree (DefaultMutableTreeNode          root,
                                  java.util.List<? extends Asset> assets)
    {

        if (assets.size () > 0)
        {

            TreeParentNode c = new TreeParentNode (assets.get (0).getObjectType (),
                                                   Environment.getObjectTypeNamePlural (assets.get (0).getObjectType ()));

            SelectableDataObject sd = new SelectableDataObject (c);

            sd.selected = true;

            DefaultMutableTreeNode tn = new DefaultMutableTreeNode (sd);

            root.add (tn);

            Collections.sort (assets,
                              new NamedObjectSorter ());

            for (Asset a : assets)
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

                String t = this.getFirstLastSentence (a.getDescription ());

                if (t.length () > 0)
                {

                    // Get the first and last sentence.
                    n.add (new DefaultMutableTreeNode (t));

                }

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

        Project p = new Project (projName);

        Book b = new Book (p,
                           null);

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

                        p.addAsset ((Asset) so.obj);

                    }

                    if (so.obj instanceof Chapter)
                    {

                        b.addChapter ((Chapter) so.obj);

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

                Collections.sort (b.getChapters (),
                                  new NamedObjectSorter ());

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

                    String t = this.getFirstLastSentence (ch.getText ());

                    if (t.length () > 0)
                    {

                        // Get the first and last sentence.
                        n.add (new DefaultMutableTreeNode (t));

                    }

                }

            }

        }

        this.addAssetsToTree (root,
                              p.getCharacters ());

        this.addAssetsToTree (root,
                              p.getLocations ());

        this.addAssetsToTree (root,
                              p.getQObjects ());

        this.addAssetsToTree (root,
                              p.getResearchItems ());

        return root;

    }

}
