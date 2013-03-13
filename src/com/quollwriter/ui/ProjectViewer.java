package com.quollwriter.ui;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;

import java.net.*;

import java.security.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.db.*;

import com.quollwriter.events.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.text.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.panels.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.renderers.*;


public class ProjectViewer extends AbstractProjectViewer
{

    public static final String TAB_OBJECT_TYPE = "tab";
    public static final int    NEW_CHAPTER_ACTION = 102; // "newChapter";
    public static final int    NEW_NOTE_ACTION = 103; // "newNote";
    public static final int    VIEW_CHAPTER_INFO_ACTION = 105; // "viewChapterInfo";

    // public static final int DELETE_CHAPTER_ACTION = 106;
    public static final int NEW_BOOK_ACTION = 108; // "newBook";
    public static final int NEW_PLOT_OUTLINE_ITEM_ACTION = 116; // "newPlotOutlineItem";
    public static final int NEW_PLOT_OUTLINE_ITEM_BELOW_ACTION = 117; // "newPlotOutlineItemBelow";
    public static final int DELETE_PLOT_OUTLINE_ITEM_ACTION = 118; // "deletePlotOutlineItem";
    public static final int EDIT_PLOT_OUTLINE_ITEM_ACTION = 119; // "editPlotOutlineItem";
    public static final int DELETE_NOTE_ACTION = 120; // "deleteNote";
    public static final int EDIT_NOTE_ACTION = 121; // "editNote";
    public static final int NEW_SCENE_ACTION = 122; // "newScene";
    public static final int NEW_SCENE_BELOW_ACTION = 123; // "newSceneBelow";
    public static final int DELETE_SCENE_ACTION = 124; // "deleteScene";
    public static final int EDIT_SCENE_ACTION = 125; // "editScene";
    public static final int MANAGE_NOTE_TYPES_ACTION = 126; // "manageNoteTypes"
    public static final int NEW_NOTE_TYPE_ACTION = 127; // "newNoteType"
    public static final int MANAGE_ITEM_TYPES_ACTION = 128; // "manageItemTypes"
    public static final int NEW_ITEM_TYPE_ACTION = 129; // "newItemType"

    private Date            sessionStart = new Date ();
    private NoteTypeHandler noteTypeHandler = null;
    private ItemTypeHandler itemTypeHandler = null;
    private EditNoteTypes   noteTypesEdit = null;
    private EditItemTypes   itemTypesEdit = null;
    private ProjectSideBar  sideBar = null;
    
    public ProjectViewer()
    {

        final ProjectViewer _this = this;

        this.noteTypeHandler = new NoteTypeHandler (this);

        this.itemTypeHandler = new ItemTypeHandler (this);

        ProjectViewer.addAssetActionMappings (this,
                                              this.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW),
                                              this.getActionMap ());

        InputMap im = this.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_F2,
                                        0),
                "ideaboard-show");

        ActionMap am = this.getActionMap ();
        
        am.put ("ideaboard-show",
                new ActionAdapter ()
                {
                   
                    public void actionPerformed (ActionEvent ev)
                    {
                   
                        _this.viewIdeaBoard ();
                        
                    }
                    
                });                                              
                
        this.sideBar = new ProjectSideBar (this);
        
    }
    
    public AbstractSideBar getMainSideBar ()
    {
        
        return this.sideBar;
        
    }
    
    private static Action getAddAssetActionListener (final Asset           as,
                                                     final ProjectViewer   pv,
                                                     final PopupsSupported ps)
    {
        
        return new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                AbstractActionHandler aah = new AssetActionHandler (as,
                                                                    pv,
                                                                    AbstractActionHandler.ADD);

                aah.setPopupOver (ps);

                aah.actionPerformed (ev);

            }

        };
        
    }

    public static void addAssetActionMappings (final PopupsSupported parent,
                                                     InputMap        im,
                                                     ActionMap       actions)
    {

        ProjectViewer pv = null;
        PopupsSupported ps = null;

        if (parent instanceof ProjectViewer)
        {
            
            pv = (ProjectViewer) parent;
            
        }
        
        if (parent instanceof QuollPanel)
        {
            
            // Holy moly!
            pv = (ProjectViewer) ((QuollPanel) parent).getProjectViewer ();
            
        }

        final ProjectViewer ppv = pv;
        
        im.put (KeyStroke.getKeyStroke ("ctrl shift " + Character.toUpperCase (Environment.getObjectTypeName (QObject.OBJECT_TYPE).charAt (0))),
                "new" + QObject.OBJECT_TYPE);
        im.put (KeyStroke.getKeyStroke ("ctrl shift " + Character.toUpperCase (Environment.getObjectTypeName (QCharacter.OBJECT_TYPE).charAt (0))),
                "new" + QCharacter.OBJECT_TYPE);
        im.put (KeyStroke.getKeyStroke ("ctrl shift " + Character.toUpperCase (Environment.getObjectTypeName (Location.OBJECT_TYPE).charAt (0))),
                "new" + Location.OBJECT_TYPE);
        im.put (KeyStroke.getKeyStroke ("ctrl shift " + Character.toUpperCase (Environment.getObjectTypeName (ResearchItem.OBJECT_TYPE).charAt (0))),
                "new" + ResearchItem.OBJECT_TYPE);

        actions.put ("new" + QObject.OBJECT_TYPE,
                     ProjectViewer.getAddAssetActionListener (new QObject (),
                                                              ppv,
                                                              parent));
                     
        actions.put ("new" + ResearchItem.OBJECT_TYPE,
                     ProjectViewer.getAddAssetActionListener (new ResearchItem (),
                                                              ppv,
                                                              parent));

        actions.put ("new" + QCharacter.OBJECT_TYPE,
                     ProjectViewer.getAddAssetActionListener (new QCharacter (),
                                                              ppv,
                                                              parent));

        actions.put ("new" + Location.OBJECT_TYPE,
                     ProjectViewer.getAddAssetActionListener (new Location (),
                                                              ppv,
                                                              parent));
        
    }

    public void fillFullScreenTitleToolbar (JToolBar toolbar)
    {
        
        this.fillTitleToolbar (toolbar);

        WordCountTimerBox b = new WordCountTimerBox (this.getFullScreenFrame (),
                                                     Constants.ICON_FULL_SCREEN_ACTION,
                                                     this.getWordCountTimer ());

        b.setBarHeight (20);

        toolbar.add (b);
        
    }
    
    public void fillTitleToolbar (JToolBar toolbar)
    {
        
        final ProjectViewer _this = this;
        
        toolbar.add (UIUtils.createButton (Constants.IDEA_ICON_NAME,
                                           Constants.ICON_TITLE_ACTION,
                                           "Click to open the Idea Board",
                                           new ActionAdapter ()
                                           {
                                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    _this.viewIdeaBoard ();
                                                    
                                                }
                                                
                                           }));
        
    }
        
    public void fillSettingsPopup (JPopupMenu titlePopup)
    {

        final ProjectViewer _this = this;

        JMenuItem mi = null;

        titlePopup.add (this.createMenuItem ("New {Project}",
                                             Constants.NEW_ICON_NAME,
                                             ProjectViewer.NEW_PROJECT_ACTION));

        // Rename project
        titlePopup.add (this.createMenuItem ("Rename this {Project}",
                                             Constants.RENAME_ICON_NAME,
                                             ProjectViewer.RENAME_PROJECT_ACTION));

        // Open project.
        titlePopup.add (this.createMenuItem ("Open {Project}",
                                             Constants.OPEN_PROJECT_ICON_NAME,
                                             ProjectViewer.OPEN_PROJECT_ACTION));

        titlePopup.add (this.createMenuItem ("Statistics",
                                             Constants.CHART_ICON_NAME,
                                             AbstractProjectViewer.SHOW_STATISTICS_ACTION));
                                                     
        // Create Project Snapshot
        titlePopup.add (this.createMenuItem ("Save Snapshot",
                                             Constants.SNAPSHOT_ICON_NAME,
                                             ProjectViewer.CREATE_PROJECT_SNAPSHOT_ACTION));
                                             
        // Close Project
        titlePopup.add (this.createMenuItem ("Close {Project}",
                                             Constants.CLOSE_ICON_NAME,
                                             ProjectViewer.CLOSE_PROJECT_ACTION));
        
        // Delete Project
        titlePopup.add (this.createMenuItem ("Delete {Project}",
                                             Constants.DELETE_ICON_NAME,
                                             ProjectViewer.DELETE_PROJECT_ACTION));

        titlePopup.addSeparator ();

        // Idea Board
        titlePopup.add (this.createMenuItem ("Idea Board",
                                             Constants.IDEA_ICON_NAME,
                                             new ActionAdapter ()
                                             {
                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                
                                                    _this.viewIdeaBoard ();
                                
                                                }
                                
                                            }));

        // Do a Warm-up Exercise
        titlePopup.add (this.createMenuItem ("Do a Warm-up Exercise",
                                             Constants.WARMUPS_ICON_NAME,
                                             AbstractProjectViewer.WARMUP_EXERCISE_ACTION));

        titlePopup.addSeparator ();
        
        // Import File
        titlePopup.add (this.createMenuItem ("Import File",
                                             Constants.PROJECT_IMPORT_ICON_NAME,
                                             new ActionAdapter ()
                                             {
                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                
                                                    _this.showImportProject ();            
                                
                                                }
                                
                                            }));

        // Export Project
        titlePopup.add (this.createMenuItem ("Export {Project}",
                                             Constants.PROJECT_EXPORT_ICON_NAME,
                                             new ActionAdapter ()
                                             {
                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                
                                                    _this.showExportProject ();            
                                
                                                }
                                
                                            }));
        
    }

    public void showExportProject ()
    {
        
        ExportProject ep = new ExportProject (this);

        ep.init ();        
        
    }

    public void showImportProject ()
    {
        
        ImportProject ip = new ImportProject (this);

        ip.init ();        
        
    }

    public void showEditNoteTypes ()
    {
        
        if (this.noteTypesEdit == null)
        {
        
            this.noteTypesEdit = new EditNoteTypes (this);
    
            this.noteTypesEdit.init ();        

            this.noteTypeHandler.setTypesEditor (this.noteTypesEdit);

        } else {
            
            this.noteTypesEdit.setVisible (true);
            
        }
        
    }

    public void showEditItemTypes ()
    {
        
        if (this.itemTypesEdit == null)
        {
        
            this.itemTypesEdit = new EditItemTypes (this);
    
            this.itemTypesEdit.init ();        

            this.itemTypeHandler.setTypesEditor (this.itemTypesEdit);

        } else {
            
            this.itemTypesEdit.setVisible (true);
            
        }
        
        this.fireProjectEvent (ProjectEvent.ITEM_TYPES,
                               ProjectEvent.SHOW);
        
    }

    public ActionListener getAction (int               name,
                                     final NamedObject other)
    {

        ActionListener a = super.getAction (name,
                                            other);

        if (a != null)
        {

            return a;

        }

        final ProjectViewer pv = this;

/*
        if (name == ProjectViewer.EDIT_CHAPTER_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.editChapter ((Chapter) other);

                }

            };

        }
*/
        if (name == ProjectViewer.VIEW_CHAPTER_INFO_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.viewChapterInformation ((Chapter) other);

                }

            };

        }

        if (name == ProjectViewer.NEW_NOTE_TYPE_ACTION)
        {
            
            return new AddNewNoteTypeActionHandler (this);
            
        }

        if (name == ProjectViewer.NEW_ITEM_TYPE_ACTION)
        {
            
            return new AddNewItemTypeActionHandler (this);
            
        }

        if (name == ProjectViewer.MANAGE_NOTE_TYPES_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.showEditNoteTypes ();

                }

            };

        }

        if (name == ProjectViewer.MANAGE_ITEM_TYPES_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.showEditItemTypes ();

                }

            };

        }

        if (name == ProjectViewer.NEW_CHAPTER_ACTION)
        {

            if (other instanceof Chapter)
            {

                Chapter c = (Chapter) other;

                return new AddChapterActionHandler (c.getBook (),
                                                    c,
                                                    pv);

            }

            if (other instanceof Book)
            {

                return new AddChapterActionHandler ((Book) other,
                                                    null,
                                                    pv);

            }

        }
/*
        if (name == ProjectViewer.RENAME_CHAPTER_ACTION)
        {

            return new RenameChapterActionHandler ((Chapter) other,
                                                   pv);

        }
*/
        if (name == ProjectViewer.EDIT_PLOT_OUTLINE_ITEM_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (final ActionEvent ev)
                {

                    final Chapter c = ((ChapterItem) other).getChapter ();

                    pv.viewObject (c);

                    SwingUtilities.invokeLater (new Runner ()
                        {

                            public void run ()
                            {

                                try
                                {

                                    QuollEditorPanel qep = (QuollEditorPanel) pv.getEditorForChapter (c);

                                    qep.editOutlineItem ((OutlineItem) other);

                                } catch (Exception e)
                                {

                                    Environment.logError ("Unable to edit outline item: " +
                                                          other,
                                                          e);

                                    UIUtils.showErrorMessage (pv,
                                                              "Unable to edit Outline Item");

                                }

                            }

                        });

                }

            };

        }

        if (name == ProjectViewer.EDIT_NOTE_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (final ActionEvent ev)
                {

                    final Chapter c = ((Note) other).getChapter ();

                    pv.viewObject (c);

                    SwingUtilities.invokeLater (new Runner ()
                        {

                            public void run ()
                            {

                                try
                                {

                                    QuollEditorPanel qep = (QuollEditorPanel) pv.getEditorForChapter (c);

                                    qep.editNote ((Note) other);

                                } catch (Exception e)
                                {

                                    Environment.logError ("Unable to edit note: " +
                                                          other,
                                                          e);

                                    UIUtils.showErrorMessage (pv,
                                                              "Unable to edit Note");

                                }

                            }

                        });

                }

            };

        }

        if (name == ProjectViewer.EDIT_SCENE_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (final ActionEvent ev)
                {

                    final Chapter c = ((ChapterItem) other).getChapter ();

                    pv.viewObject (c);

                    SwingUtilities.invokeLater (new Runner ()
                        {

                            public void run ()
                            {

                                try
                                {

                                    QuollEditorPanel qep = (QuollEditorPanel) pv.getEditorForChapter (c);

                                    qep.editScene ((Scene) other);

                                } catch (Exception e)
                                {

                                    Environment.logError ("Unable to edit scene: " +
                                                          other,
                                                          e);

                                    UIUtils.showErrorMessage (pv,
                                                              "Unable to edit Scene");

                                }

                            }

                        });

                }

            };

        }

        if (name == ProjectViewer.DELETE_PLOT_OUTLINE_ITEM_ACTION)
        {

            return new DeleteOutlineItemActionHandler ((OutlineItem) other,
                                                       pv);

        }

        if (name == ProjectViewer.DELETE_SCENE_ACTION)
        {

            return new DeleteChapterItemActionHandler ((Scene) other,
                                                       this.getEditorForChapter (((Scene) other).getChapter ()));

        }

        if (name == ProjectViewer.DELETE_NOTE_ACTION)
        {

            return new DeleteNoteActionHandler ((Note) other,
                                                this.getEditorForChapter (((Note) other).getChapter ()));

        }

        if (name == ProjectViewer.NEW_PLOT_OUTLINE_ITEM_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    Chapter c = (Chapter) other;

                    QuollEditorPanel qep = (QuollEditorPanel) pv.getEditorForChapter (c);

                    String text = qep.getEditor ().getText ();

                    int pos = 0;

                    if (text != null)
                    {

                        pos = text.length ();

                    }

                    pv.scrollTo (c,
                                 pos);

                    AbstractActionHandler aah = new OutlineItemChapterActionHandler (c,
                                                                                     qep,
                                                                                     pos);

                    aah.actionPerformed (ev);

                }

            };

        }

        if (name == ProjectViewer.NEW_SCENE_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    Chapter c = (Chapter) other;

                    QuollEditorPanel qep = (QuollEditorPanel) pv.getEditorForChapter (c);

                    String text = qep.getEditor ().getText ();

                    int pos = 0;

                    if (text != null)
                    {

                        pos = text.length ();

                    }

                    pv.scrollTo (c,
                                 pos);

                    Scene s = new Scene (-1,
                                         c);

                    AbstractActionHandler aah = new ChapterItemActionHandler (s,
                                                                              qep,
                                                                              AbstractActionHandler.ADD,
                                                                              pos);

                    aah.actionPerformed (ev);

                }

            };

        }

        if (name == ProjectViewer.NEW_SCENE_BELOW_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    Chapter c = (Chapter) other;

                    QuollEditorPanel qep = (QuollEditorPanel) pv.getEditorForChapter (c);

                    String text = qep.getEditor ().getText ();

                    int pos = 0;

                    if (text != null)
                    {

                        pos = text.length ();

                    }

                    pv.scrollTo (c,
                                 pos);

                    Scene s = new Scene (-1,
                                         c);

                    AbstractActionHandler aah = new ChapterItemActionHandler (s,
                                                                              qep,
                                                                              AbstractActionHandler.ADD,
                                                                              pos);

                    aah.actionPerformed (ev);

                }

            };

        }

        if (name == ProjectViewer.NEW_NOTE_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    Chapter c = (Chapter) other;

                    QuollEditorPanel qep = pv.getEditorForChapter (c);

                    String text = qep.getEditor ().getText ();

                    int pos = 0;

                    if (text != null)
                    {

                        pos = text.length ();

                    }

                    pv.scrollTo (c,
                                 pos);

                    AbstractActionHandler aah = new NoteActionHandler (c,
                                                                       qep,
                                                                       pos);

                    aah.actionPerformed (ev);

                }

            };

        }

        if (name == ProjectViewer.NEW_PLOT_OUTLINE_ITEM_BELOW_ACTION)
        {

            Chapter c = (Chapter) other;

            AbstractActionHandler aah = new OutlineItemChapterActionHandler (c,
                                                                             this.getEditorForChapter (c),
                                                                             0);

            return aah;

        }

        if (name == ProjectViewer.NEW_BOOK_ACTION)
        {

            return UIUtils.getComingSoonAction (pv);

            /*
            return new AddBookActionHandler ((Book) other,
                                     pv);
             */
        }

        throw new IllegalArgumentException ("Action: " +
                                            name +
                                            " not known.");

    }

    public void handleNewProject ()
    {

        Book b = this.proj.getBooks ().get (0);
    
        Chapter c = b.getFirstChapter ();
    
        // Create a new chapter for the book.
        if (c == null)
        {
        
            c = new Chapter (b,
                             Environment.getProperty (Constants.DEFAULT_CHAPTER_NAME_PROPERTY_NAME));
    
            b.addChapter (c);    
            
        }

        // Refresh the chapter tree.
        this.sideBar.reloadTreeForObjectType (c.getObjectType ());
                
        this.handleOpenProject ();

        this.editChapter (c);
        
    }
    /*
    public void newProject (File    dir,
                    String  name,
                    String  filePassword)
                    throws  Exception
    {

    Project p = new Project (name);

    this.newProject (dir,
                 p,
                 filePassword);

    }
     */

    public String getViewerIcon ()
    {

        return this.proj.getObjectType ();

    }

    public String getViewerTitle ()
    {

        return this.proj.getName ();

    }

    public void handleHTMLPanelAction (String v)
    {

        StringTokenizer t = new StringTokenizer (v,
                                                 ",;");
        
        if (t.countTokens () > 1)
        {
        
            while (t.hasMoreTokens ())
            {
                
                String tok = t.nextToken ().trim ();
                
                this.handleHTMLPanelAction (tok);
                
            }

            return;

        }

        if (v.equals ("find"))
        {
            
            this.showFind (null);
            
            return;
            
        }        
        
        if (v.equals ("import"))
        {
        
            this.showImportProject ();
        
            return;
            
        }

        if (v.equals ("export"))
        {
        
            this.showExportProject ();
        
            return;
            
        }
        
        if (v.equals ("problemfinder"))
        {
   
            QuollPanel qp = this.getCurrentlyVisibleTab ();
            
            if (qp instanceof QuollEditorPanel)
            {
                
                ((QuollEditorPanel) qp).showProblemFinder ();
                
            }

            return;
            
        }

        if (v.equals ("ideaboard"))
        {
            
            this.viewIdeaBoard ();
            
            return;
            
        }

        super.handleHTMLPanelAction (v);

    }

    public void handleOpenProject ()
    {
/*
        this.initNoteTree (true);

        this.initProjectItemBoxes ();
  */              
        final ProjectViewer _this = this;

        // See if we should be doing a warmup exercise.
        Properties userProps = Environment.getUserProperties ();

        if ((userProps.getPropertyAsBoolean (Constants.DO_WARMUP_ON_STARTUP_PROPERTY_NAME)) &&
            (Environment.getOpenProjects ().size () == 0))
        {

            javax.swing.Timer t = new javax.swing.Timer (2000,
                                                         new ActionAdapter ()
                                                         {

                                                             public void actionPerformed (ActionEvent ev)
                                                             {

                                                                 WarmupPromptSelect w = new WarmupPromptSelect (_this);

                                                                 w.init ();

                                                                 _this.fireProjectEvent (Warmup.OBJECT_TYPE,
                                                                                         ProjectEvent.WARMUP_ON_STARTUP);

                                                             }

                                                         });

            t.setRepeats (false);
            t.start ();

        }

    }

    public void expandNoteTypeInNoteTree (String type)
    {

        TreeParentNode tpn = new TreeParentNode (Note.OBJECT_TYPE,
                                                 type);

        DefaultTreeModel dtm = (DefaultTreeModel) this.getNoteTree ().getModel ();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

        this.getNoteTree ().expandPath (UIUtils.getTreePathForUserObject (root,
                                                                          tpn));

    }

    private void initNoteTree (boolean restoreSavedOpenTypes)
    {

        if (restoreSavedOpenTypes)
        {

            String openTypes = this.proj.getProperty (Constants.NOTE_TREE_OPEN_TYPES_PROPERTY_NAME);

            if (openTypes != null)
            {

                DefaultTreeModel dtm = (DefaultTreeModel) this.getNoteTree ().getModel ();

                DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot ();

                // Split on :
                StringTokenizer t = new StringTokenizer (openTypes,
                                                         "|");

                while (t.hasMoreTokens ())
                {

                    String tok = t.nextToken ().trim ();

                    TreeParentNode tpn = new TreeParentNode (Note.OBJECT_TYPE,
                                                             tok);

                    this.getNoteTree ().expandPath (UIUtils.getTreePathForUserObject (root,
                                                                                      tpn));

                }

            }

        }

    }

    private void initProjectItemBoxes ()
    {

        String openTypes = this.proj.getProperty (Constants.ASSETS_TREE_OPEN_TYPES_PROPERTY_NAME);

        Set<String> open = new HashSet ();
        
        if (openTypes != null)
        {

            // Split on :
            StringTokenizer t = new StringTokenizer (openTypes,
                                                     "|");

            while (t.hasMoreTokens ())
            {

                String tok = t.nextToken ().trim ();
                
                open.add (tok);
                
            }

        }
        
        this.sideBar.initOpenObjectTypes (open);        
    
    }

    public void handleItemChangedEvent (ItemChangedEvent ev)
    {

        if (ev.getChangedObject () instanceof Chapter)
        {

            this.sideBar.reloadTreeForObjectType (Chapter.OBJECT_TYPE);

        }

        if (ev.getChangedObject () instanceof Note)
        {

            this.sideBar.reloadTreeForObjectType (Note.OBJECT_TYPE);

        }
        
    }

    public void reloadNoteTree ()
    {
        
        this.sideBar.reloadTreeForObjectType (Note.OBJECT_TYPE);
        
    }
    
    public void reloadChapterTree ()
    {

        this.sideBar.reloadTreeForObjectType (Chapter.OBJECT_TYPE);
    
    }

    public void doSaveState ()
    {

        StringBuilder nts = new StringBuilder ();

        Enumeration<TreePath> paths = this.getNoteTree ().getExpandedDescendants (new TreePath (this.getNoteTree ().getModel ().getRoot ()));

        if (paths != null)
        {

            while (paths.hasMoreElements ())
            {

                NamedObject d = (NamedObject) ((DefaultMutableTreeNode) paths.nextElement ().getLastPathComponent ()).getUserObject ();

                if (d instanceof TreeParentNode)
                {

                    String type = d.getName ();

                    if (nts.length () > 0)
                    {

                        nts.append ("|");

                    }

                    nts.append (type);

                }

            }

            try
            {

                this.proj.setProperty (Constants.NOTE_TREE_OPEN_TYPES_PROPERTY_NAME,
                                       nts.toString ());

            } catch (Exception e)
            {

                // Don't worry about this.
                Environment.logError ("Unable to save note tree state for project: " +
                                      this.proj,
                                      e);

            }

        }

        try
        {

            this.proj.setProperty (Constants.ASSETS_TREE_OPEN_TYPES_PROPERTY_NAME,
                                   this.sideBar.getSaveState ());

        } catch (Exception e)
        {

            // Don't worry about this.
            Environment.logError ("Unable to save assets tree state for project: " +
                                  this.proj,
                                  e);

        }

    }

    public QuollEditorPanel getEditorForChapter (Chapter c)
    {

        for (QuollPanel qp : this.getAllQuollPanelsForObject (c))
        {
        
            if (qp instanceof FullScreenQuollPanel)
            {
                
                qp = ((FullScreenQuollPanel) qp).getChild ();
                
            }

            if (qp instanceof QuollEditorPanel)
            {
                
                return (QuollEditorPanel) qp;
                
            }
            
        }
        
        return null;

    }

    public boolean viewTimeline ()
    {

        final ProjectViewer _this = this;

        Timeline tp = null;

        try
        {

            tp = new Timeline (this);

            tp.init ();

        } catch (Exception e) {

            Environment.logError ("Unable to init timeline",
                                  e);

            UIUtils.showErrorMessage (_this,
                                      "Unable to show timeline");

            return false;

        }

        final TabHeader th = this.addPanel (tp);

        this.setPanelVisible (tp);

        return true;

    }
    
    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the chapter has been opened for editing.
     */
    public boolean editChapter (Chapter c)
    {

        // Check our tabs to see if we are already editing this chapter, if so then just switch to it instead.
        QuollEditorPanel qep = (QuollEditorPanel) this.getQuollPanelForObject (c);

        if (qep != null)
        {
        
            this.setPanelVisible (qep);
            
            this.getEditorForChapter (c).getEditor ().grabFocus ();
        
            return true;

        }

        final ProjectViewer _this = this;

        try
        {

            qep = new QuollEditorPanel (this,
                                        c);

            qep.init ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to edit chapter: " +
                                  c,
                                  e);

            UIUtils.showErrorMessage (_this,
                                      "Unable to edit chapter: " +
                                      c.getName ());

            return false;

        }

        final TabHeader th = this.addPanel (qep);

        qep.addActionListener (new ActionAdapter ()
        {
            
        
            public void actionPerformed (ActionEvent ev)
            {
                
                if (ev.getID () == QuollPanel.UNSAVED_CHANGES_ACTION_EVENT)
                {
                    
                    th.setComponentChanged (true);
                                            
                }
                
            }
            
        });

        this.addNameChangeListener (c,
                                    qep);

        // Open the tab :)
        return this.editChapter (c);

    }

    public boolean viewObject (DataObject d)
    {

        if (d instanceof OutlineItem)
        {

            final OutlineItem oi = (OutlineItem) d;

            if (!this.editChapter (oi.getChapter ()))
            {

                return false;

            }

            final ProjectViewer _this = this;

            SwingUtilities.invokeLater (new Runner ()
                {

                    public void run ()
                    {

                        try
                        {

                            _this.getEditorForChapter (oi.getChapter ()).showOutlineItem (oi);

                        } catch (Exception e)
                        {

                            Environment.logError ("Unable to scroll to outline item: " +
                                                  oi,
                                                  e);

                            UIUtils.showErrorMessage (_this,
                                                      "Unable to display Plot Outline Item in Chapter.");

                        }

                    }

                });

            return true;

        }

        if (d instanceof Scene)
        {

            final Scene s = (Scene) d;

            if (!this.editChapter (s.getChapter ()))
            {

                return false;

            }

            final ProjectViewer _this = this;

            SwingUtilities.invokeLater (new Runner ()
                {

                    public void run ()
                    {

                        try
                        {

                            _this.getEditorForChapter (s.getChapter ()).showScene (s);

                        } catch (Exception e)
                        {

                            Environment.logError ("Unable to scroll to scene: " +
                                                  s,
                                                  e);

                            UIUtils.showErrorMessage (_this,
                                                      Environment.replaceObjectNames ("Unable to display {outlineitem} in {chapter}."));

                        }

                    }

                });

            return true;

        }

        if (d instanceof Note)
        {

            final Note n = (Note) d;

            if (n.getObject () != null)
            {

                if (!this.editChapter ((Chapter) n.getObject ()))
                {

                    return false;

                }

                final ProjectViewer _this = this;

                SwingUtilities.invokeLater (new Runner ()
                    {

                        public void run ()
                        {

                            try
                            {

                                _this.getEditorForChapter (((Chapter) n.getObject ())).showNote (n);

                            } catch (Exception e)
                            {

                                Environment.logError ("Unable to scroll to note: " +
                                                      n,
                                                      e);

                                UIUtils.showErrorMessage (_this,
                                                          "Unable to display Note in Chapter.");

                            }

                        }

                    });

            }

            return true;

        }

        if (d.getObjectType ().equals (Project.WORDCOUNTS_OBJECT_TYPE))
        {

            return this.viewWordCountHistory ();

        }

        if (d instanceof Chapter)
        {

            Chapter c = (Chapter) d;

            if (d.getObjectType ().equals (Chapter.INFORMATION_OBJECT_TYPE))
            {

                return this.viewChapterInformation (c);

            } else
            {

                return this.editChapter (c);

            }

        }

        if (d instanceof Asset)
        {

            return this.viewAsset ((Asset) d);

        }

        if (d instanceof Note)
        {

            this.viewNote ((Note) d);

            return true;

        }

        if (d instanceof OutlineItem)
        {

            this.viewOutlineItem ((OutlineItem) d);

            return true;

        }

        // Record the error, then ignore.
        Environment.logError ("Unable to open object: " + d);

        return false;

    }
    
    public void viewNote (Note n)
    {

        try
        {

            // Need to change this.
            if (n.getObject () instanceof Chapter)
            {

                Chapter c = (Chapter) n.getObject ();

                this.editChapter (c);

                QuollEditorPanel qep = this.getEditorForChapter (c);

                qep.showNote (n);

                return;

            }

        } catch (Exception e)
        {

            Environment.logError ("Unable to show note: " +
                                  n,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to show " + Environment.getObjectTypeName (n).toLowerCase () + ".");

        }

    }

    public void viewOutlineItem (OutlineItem n)
    {

        try
        {

            this.editChapter (n.getChapter ());

            QuollEditorPanel qep = this.getEditorForChapter (n.getChapter ());

            qep.showOutlineItem (n);

        } catch (Exception e)
        {

            Environment.logError ("Unable to show outline item: " +
                                  n,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to show " + Environment.getObjectTypeName (n).toLowerCase ());

        }

    }

    public boolean openPanel (String id)
    {
    
        if (id.equals (IdeaBoard.PANEL_ID))
        {

            return this.viewIdeaBoard ();

        }

        if (id.equals (Timeline.PANEL_ID))
        {

            return this.viewTimeline ();
            
        }

        return false;

    }

    public boolean viewIdeaBoard ()
    {

        IdeaBoard avp = (IdeaBoard) this.getQuollPanel (IdeaBoard.PANEL_ID);

        if (avp == null)
        {

            try
            {

                avp = new IdeaBoard (this);

                avp.init ();

            } catch (Exception e)
            {

                Environment.logError ("Unable to view idea board",
                                      e);

                UIUtils.showErrorMessage (this,
                                          "Unable to view idea board");

                return false;

            }

            this.addPanel (avp);

        }

        this.setPanelVisible (avp);

        return true;

    }
    
    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the asset is viewed.
     */
    public boolean viewAsset (Asset a)
    {

        // Check our tabs to see if we are already editing this asset, if so then just switch to it instead.
        AssetViewPanel avp = (AssetViewPanel) this.getQuollPanelForObject (a);

        if (avp != null)
        {

            this.setPanelVisible (avp);
            
            avp.initDividers ();

            return true;

        }

        final ProjectViewer _this = this;

        try
        {

            avp = new AssetViewPanel (this,
                                      a);

            avp.init ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to view asset: " +
                                  a,
                                  e);

            UIUtils.showErrorMessage (_this,
                                      "Unable to view " +
                                      Environment.getObjectTypeName (a) +
                                      ": " +
                                      a.getName ());

            return false;

        }

        this.addPanel (avp);

        this.addNameChangeListener (a,
                                    avp);
        
        // Open the tab :)
        return this.viewAsset (a);

    }

    protected void addNameChangeListener (final NamedObject n,
                                          final QuollPanel  qp)
    {
        
        final ProjectViewer _this = this;
        
        Map events = new HashMap ();
        events.put (NamedObject.NAME,
                    "");
                    
        n.addPropertyChangedListener (new PropertyChangedAdapter ()
        {

            public void propertyChanged (PropertyChangedEvent ev)
            {

                _this.setTabHeaderTitle (qp,
                                         qp.getTitle ());
            
                _this.informTreeOfNodeChange (n,
                                              _this.getTreeForObjectType (n.getObjectType ()));

            }

        },
        events);        
        
    }    
    
    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the chapter information is viewed.
     */
    public boolean viewChapterInformation (final Chapter c)
    {

        ChapterInformationSideBar cb = new ChapterInformationSideBar (this,
                                                                      c);
        
        this.addSideBar ("chapterinfo-" + c.getKey (),
                         cb);
        
        this.showSideBar ("chapterinfo-" + c.getKey ());
    
        if (true)
        {
    
            return true;
        
        }
    
        // Check our tabs to see if we are already editing this asset, if so then just switch to it instead.
        ChapterViewPanel cvp = (ChapterViewPanel) this.getQuollPanel (ChapterViewPanel.getPanelIdForObject (c));

        if (cvp != null)
        {

            this.setPanelVisible (cvp);

            cvp.initDividers ();

            return true;

        }

        final ProjectViewer _this = this;

        try
        {

            cvp = new ChapterViewPanel (this,
                                        c);

            cvp.init ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to view chapter: " +
                                  c,
                                  e);

            UIUtils.showErrorMessage (_this,
                                      "Unable to view information for " +
                                      Environment.getObjectTypeName (c) +
                                      ": " +
                                      c.getName ());

            return false;

        }

        final TabHeader th = this.addPanel (cvp);

        Map events = new HashMap ();
        events.put (NamedObject.NAME,
                    "");

        final QuollPanel qp = cvp;
                    
        c.addPropertyChangedListener (new PropertyChangedAdapter ()
            {

                public void propertyChanged (PropertyChangedEvent ev)
                {

                    th.setTitle (qp.getTitle ());
/*
                _this.informTreeOfNodeChange ((NamedObject) ev.getSource (),
                                              _this.assetsTree);
*/
                }

            },
            events);

        // Open the tab :)
        return this.viewChapterInformation (c);

    }

    public JTree getTreeForObjectType (String objType)
    {
        
        return this.sideBar.getTreeForObjectType (objType);
                
    }
    
    public void addChapterToTreeAfter (Chapter newChapter,
                                       Chapter addAfter)
    {

        DefaultTreeModel model = (DefaultTreeModel) this.getChapterTree ().getModel ();

        DefaultMutableTreeNode cNode = new DefaultMutableTreeNode (newChapter);

        if (addAfter == null)
        {

            // Get the book node.
            TreePath tp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                            newChapter.getBook ());

            if (tp != null)
            {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                model.insertNodeInto (cNode,
                                      (MutableTreeNode) node,
                                      0);

            } else
            {

                DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot ();

                model.insertNodeInto (cNode,
                                      root,
                                      root.getChildCount ());

            }

        } else
        {

            // Get the "addAfter" node.
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                                                     addAfter).getLastPathComponent ();

            model.insertNodeInto (cNode,
                                  (MutableTreeNode) node.getParent (),
                                  node.getParent ().getIndex (node) + 1);

        }

        this.getChapterTree ().setSelectionPath (new TreePath (cNode.getPath ()));

    }

    public JTree getNoteTree ()
    {
        
        return this.getTreeForObjectType (Note.OBJECT_TYPE);
        
    }

    public JTree getChapterTree ()
    {
        
        return this.getTreeForObjectType (Chapter.OBJECT_TYPE);
        
    }
    
    public boolean deleteIdeaType (IdeaType it)
    {

        try
        {

            this.dBMan.deleteObject (it,
                                     false,
                                     null);

            this.proj.removeObject (it);

            this.fireProjectEvent (it.getObjectType (),
                                   ProjectEvent.DELETE,
                                   it);

            return true;

        } catch (Exception e)
        {

            Environment.logError ("Unable to delete idea type: " + it,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to delete Idea Type");

            return false;

        }

    }

    public boolean deleteIdea (Idea i)
    {

        try
        {

            this.dBMan.deleteObject (i,
                                     false,
                                     null);

            this.fireProjectEvent (i.getObjectType (),
                                   ProjectEvent.DELETE,
                                   i);

            return true;

        } catch (Exception e)
        {

            Environment.logError ("Unable to delete idea: " + i,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to delete Idea");

            return false;

        }

    }

    public boolean updateIdeaType (IdeaType it)
    {

        try
        {

            this.dBMan.saveObject (it,
                                   null);

            this.fireProjectEvent (it.getObjectType (),
                                   ProjectEvent.EDIT,
                                   it);

            return true;

        } catch (Exception e)
        {

            Environment.logError ("Unable to save idea type: " + it,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to save Idea Type");

            return false;

        }

    }

    public void deleteChapter (Chapter c)
    {

        try
        {

            // Remove the chapter from the book.
            java.util.Set<NamedObject> otherObjects = c.getOtherObjectsInLinks ();

            this.dBMan.deleteObject (c,
                                     false,
                                     null);

            Book b = c.getBook ();

            b.removeChapter (c);

            this.refreshObjectPanels (otherObjects);

            this.fireProjectEvent (c.getObjectType (),
                                   ProjectEvent.DELETE,
                                   c);

        } catch (Exception e)
        {

            Environment.logError ("Unable to delete chapter: " + c,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to delete " +
                                      Environment.getObjectTypeName (c));

            return;

        }

        // Inform the chapter tree about the change.
        this.sideBar.reloadTreeForObjectType (c.getObjectType ());

        // Remove the tab (if present).
        this.removeAllPanelsForObject (c);

        // Notify the note tree about the change.
        for (Note n : c.getNotes ())
        {

            try
            {

                this.deleteNote (n,
                                 false);

            } catch (Exception e)
            {

                Environment.logError ("Unable to delete note: " + n,
                                      e);

            }

        }

    }

    public void deleteAsset (Asset a)
    {

        final ProjectViewer _this = this;

        // Remove the links.
        try
        {

            // Capture a list of all the object objects in the links, we then need to message
            // the linked to panel of any of those.
            java.util.Set<NamedObject> otherObjects = a.getOtherObjectsInLinks ();

            this.dBMan.deleteObject (a,
                                     false,
                                     null);

            this.proj.removeObject (a);

            this.removeWordFromDictionary (a.getName (),
                                           "project");
            this.removeWordFromDictionary (a.getName () + "'s",
                                           "project");

            this.refreshObjectPanels (otherObjects);

            this.fireProjectEvent (a.getObjectType (),
                                   ProjectEvent.DELETE,
                                   a);

        } catch (Exception e)
        {

            Environment.logError ("Unable to remove links: " +
                                  a,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to remove " + Environment.getObjectTypeName (a));

            return;

        }

        this.sideBar.reloadTreeForObjectType (a.getObjectType ());
        
        // See if there is a panel viewing the character.
        this.removePanel (a);

    }

    public void addNewIdeaType (IdeaType it)
                         throws GeneralException
    {

        this.dBMan.saveObject (it,
                               null);

        this.fireProjectEvent (it.getObjectType (),
                               ProjectEvent.NEW,
                               it);

    }

    public void addNewIdea (Idea i)
                     throws GeneralException
    {

        this.dBMan.saveObject (i,
                               null);

        i.getType ().addIdea (i);

        this.fireProjectEvent (i.getObjectType (),
                               ProjectEvent.NEW,
                               i);

    }

    public void deleteOutlineItem (OutlineItem it,
                                   boolean     doInTransaction)
                            throws GeneralException
    {

        java.util.Set<NamedObject> otherObjects = it.getOtherObjectsInLinks ();

        // Get the editor panel for the item.
        Chapter c = it.getChapter ();

        this.dBMan.deleteObject (it,
                                 false,
                                 null);

        c.removeOutlineItem (it);

        if (it.getScene () != null)
        {

            it.getScene ().removeOutlineItem (it);

        }

        this.refreshObjectPanels (otherObjects);

        QuollEditorPanel qep = this.getEditorForChapter (c);

        if (qep != null)
        {

            qep.removeItem (it);

        }

        this.reloadChapterTree ();
        
    }

    public void deleteChapterItem (ChapterItem ci,
                                   boolean     deleteChildObjects,
                                   boolean     doInTransaction)
                            throws GeneralException
    {

        if (ci.getObjectType ().equals (Scene.OBJECT_TYPE))
        {

            this.deleteScene ((Scene) ci,
                              deleteChildObjects,
                              doInTransaction);

        }

        if (ci.getObjectType ().equals (OutlineItem.OBJECT_TYPE))
        {

            this.deleteOutlineItem ((OutlineItem) ci,
                                    doInTransaction);

        }

        if (ci.getObjectType ().equals (Note.OBJECT_TYPE))
        {

            this.deleteNote ((Note) ci,
                             doInTransaction);

        }

        this.fireProjectEvent (ci.getObjectType (),
                               ProjectEvent.DELETE,
                               ci);

    }

    public void deleteScene (Scene   s,
                             boolean deleteOutlineItems,
                             boolean doInTransaction)
                      throws GeneralException
    {

        java.util.Set<NamedObject> otherObjects = s.getOtherObjectsInLinks ();

        java.util.List<OutlineItem> outlineItems = new ArrayList (s.getOutlineItems ());

        // Get the editor panel for the item.
        Chapter c = s.getChapter ();

        this.dBMan.deleteObject (s,
                                 deleteOutlineItems,
                                 null);

        c.removeScene (s);

        this.refreshObjectPanels (otherObjects);

        QuollEditorPanel qep = this.getEditorForChapter (c);

        if (qep != null)
        {

            for (OutlineItem oi : outlineItems)
            {

                if (deleteOutlineItems)
                {

                    qep.removeItem (oi);
                    
                } else {
                    
                    // Add the item back into the chapter.
                    c.addChapterItem (oi);
                    
                }

            }

            qep.removeItem (s);

        }

        this.reloadChapterTree ();

    }

    public void reloadAssetTree (Asset a)
    {
        
        this.sideBar.reloadTreeForObjectType (a.getObjectType ());
        
    }
    
    public void deleteNote (Note    n,
                            boolean doInTransaction)
                     throws GeneralException
    {

        java.util.Set<NamedObject> otherObjects = n.getOtherObjectsInLinks ();

        NamedObject obj = n.getObject ();

        // Need to get the links, they may not be setup.
        this.setLinks (n);

        this.dBMan.deleteObject (n,
                                 false,
                                 null);

        obj.removeNote (n);

        this.refreshObjectPanels (otherObjects);

        if (obj instanceof Chapter)
        {

            QuollEditorPanel qep = this.getEditorForChapter ((Chapter) obj);

            if (qep != null)
            {

                qep.removeItem (n);

            }

        }

        this.reloadNoteTree ();
        
    }

    public JTree getAssetTree (Asset a)
    {
        
        return this.getTreeForObjectType (a.getObjectType ());
        
    }
    /*
    public void addToAssetTree (Asset a)
    {

        DefaultTreeModel model = (DefaultTreeModel) this.getAssetTree (a).getModel ();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) model.getRoot ();

        int ind = 0;

        // Now work out where it should go.
        en = node.children ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode ccNode = en.nextElement ();

            Asset ast = (Asset) ccNode.getUserObject ();

            if (a.getName ().toLowerCase ().compareTo (ast.getName ().toLowerCase ()) < 0)
            {

                break;

            }

            ind++;

        }

        this.sideBar.reloadTreeForObjectType (a.getObjectType ());
        
        this.getAssetTree (a).setSelectionPath (new TreePath (cNode.getPath ()));

    }
*/
    public void chapterTreeChanged (DataObject d)
    {

        DefaultTreeModel model = (DefaultTreeModel) this.getChapterTree ().getModel ();

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                                                 d).getLastPathComponent ();

        model.nodeStructureChanged (node);

    }

    public void scrollTo (final Chapter c,
                          final int     pos)
    {

        final ProjectViewer _this = this;

        this.editChapter (c);

        SwingUtilities.invokeLater (new Runner ()
            {

                public void run ()
                {

                    try
                    {

                        _this.getEditorForChapter (c).scrollToPosition (pos);
                        _this.getEditorForChapter (c).scrollToPosition (pos);

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to show snippet at position: " +
                                              pos,
                                              e);

                        UIUtils.showErrorMessage (_this,
                                                  "Unable to show snippet in context");

                    }

                }

            });

    }

    public Set<Note> getNotesForType (String t)
    {
        
        Set<Note> notes = this.getAllNotes ();
        
        Set<Note> ret = new TreeSet (new ChapterItemSorter ());

        for (Note n : notes)
        {

            if (n.getType ().equals (t))
            {

                ret.add (n);

            }

        }

        return ret;
        
    }    
    
    public Set<Note> getAllNotes ()
    {

        Set<Note> notes = new HashSet ();

        Book b = this.proj.getBooks ().get (0);

        java.util.List<Chapter> chapters = b.getChapters ();

        for (Chapter c : chapters)
        {

            notes.addAll (c.getNotes ());

        }

        return notes;

    }

    public NoteTypeHandler getNoteTypeHandler ()
    {

        return this.noteTypeHandler;

    }

    public ItemTypeHandler getItemTypeHandler ()
    {

        return this.itemTypeHandler;

    }

    public void updateChapterIndexes (Book b)
                               throws GeneralException
    {

        this.dBMan.updateChapterIndexes (b);

    }

    public void saveProblemFinderIgnores (Chapter    c,
                                          Set<Issue> issues)
                                   throws GeneralException
    {

        ChapterDataHandler dh = (ChapterDataHandler) this.getDataHandler (Chapter.OBJECT_TYPE);

        dh.saveProblemFinderIgnores (c,
                                     issues);

    }

    public Set<Issue> getProblemFinderIgnores (Chapter  c,
                                               Document doc)
                                               throws GeneralException
    {

        ChapterDataHandler dh = (ChapterDataHandler) this.getDataHandler (Chapter.OBJECT_TYPE);

        return dh.getProblemFinderIgnores (c,
                                           doc);

    }

    public String getChapterObjectName ()
    {

        return Environment.getObjectTypeName (Chapter.OBJECT_TYPE);

    }

}