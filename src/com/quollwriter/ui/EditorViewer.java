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
import java.util.LinkedHashSet;
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


public class EditorViewer extends AbstractProjectViewer
{

    public static final String TAB_OBJECT_TYPE = "tab";
    public static final int    NEW_NOTE_ACTION = 103; // "newNote";
    public static final int    VIEW_EDITOR_CHAPTER_INFO_ACTION = 105; // "viewEditorChapterInfo";

    public static final int DELETE_NOTE_ACTION = 120; // "deleteNote";
    public static final int EDIT_NOTE_ACTION = 121; // "editNote";
    public static final int MANAGE_NOTE_TYPES_ACTION = 126; // "manageNoteTypes"
    public static final int NEW_NOTE_TYPE_ACTION = 127; // "newNoteType"

    private Date            sessionStart = new Date ();
    private NoteTypeHandler noteTypeHandler = null;
    private EditNoteTypes   noteTypesEdit = null;
    private EditorProjectSideBar  sideBar = null;
    
    public EditorViewer()
    {

        final EditorViewer _this = this;

        ObjectProvider<Note> noteProvider = new GeneralObjectProvider<Note> ()
        {
            
            public Set<Note> getAll ()
            {

                return (Set<Note>) _this.getAllNotes ();
                
            }
            
            public Note getByKey (Long key)
            {
                
                throw new UnsupportedOperationException ("Not supported");
                
            }
            
            public void save (Note   obj)
                              throws GeneralException
            {
                
                _this.saveObject (obj,
                                  true);
            
            }
            
            public void saveAll (java.util.List<Note> objs)
                                 throws    GeneralException
            {
                
                _this.saveObjects (objs,
                                   false);
                
            }
            
        };
        
        this.noteTypeHandler = new NoteTypeHandler (this,
                                                    noteProvider);

        Set<String> objTypes = new LinkedHashSet ();
        objTypes.add (Chapter.OBJECT_TYPE);
        objTypes.add (Note.OBJECT_TYPE);
                                                    
        this.sideBar = new EditorProjectSideBar (this);
        
    }
    
    public void initActionMappings (ActionMap am)
    {

        final EditorViewer _this = this;
    
        super.initActionMappings (am);
                
    }
    
    public void initKeyMappings (InputMap im)
    {
        
        super.initKeyMappings (im);
                
    }

    public void showObjectInTree (String      treeObjType,
                                  NamedObject obj)
    {
        
        this.sideBar.showObjectInTree (treeObjType,
                                       obj);
        
    }
    
    public void reloadTreeForObjectType (String objType)
    {
        
        this.sideBar.reloadTreeForObjectType (objType);
        
    }
    
    public void reloadTreeForObjectType (NamedObject obj)
    {
        
        this.sideBar.reloadTreeForObjectType (obj.getObjectType ());
        
    }

    public AbstractSideBar getMainSideBar ()
    {
        
        return this.sideBar;
        
    }

    public TypesHandler getObjectTypesHandler (String objType)
    {
        
        if (objType.equals (Note.OBJECT_TYPE))
        {
            
            return this.noteTypeHandler;
            
        }
                
        return null;
        
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
        
        final EditorViewer _this = this;
                
    }
        
    public void fillSettingsPopup (JPopupMenu titlePopup)
    {

        final EditorViewer _this = this;

        JMenuItem mi = null;

        // Open project.
        titlePopup.add (this.createMenuItem ("Open {Project}",
                                             Constants.OPEN_PROJECT_ICON_NAME,
                                             EditorViewer.OPEN_PROJECT_ACTION));
                                             
        // Close Project
        titlePopup.add (this.createMenuItem ("Close {Project}",
                                             Constants.CLOSE_ICON_NAME,
                                             EditorViewer.CLOSE_PROJECT_ACTION));
        
        // Delete Project
        titlePopup.add (this.createMenuItem ("Delete {Project}",
                                             Constants.DELETE_ICON_NAME,
                                             EditorViewer.DELETE_PROJECT_ACTION));

        //titlePopup.addSeparator ();
                
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

    public Action getAction (int               name,
                                     final NamedObject other)
    {

        Action a = super.getAction (name,
                                            other);

        if (a != null)
        {

            return a;

        }

        final EditorViewer pv = this;

        if (name == EditorViewer.VIEW_EDITOR_CHAPTER_INFO_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.viewEditorChapterInformation ((Chapter) other);

                }

            };

        }

        if (name == EditorViewer.NEW_NOTE_TYPE_ACTION)
        {
            
            return new AddNewNoteTypeActionHandler (this);
            
        }

        if (name == EditorViewer.MANAGE_NOTE_TYPES_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    pv.showEditNoteTypes ();

                }

            };

        }

        if (name == EditorViewer.EDIT_NOTE_ACTION)
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

                                    EditorEditorPanel qep = (EditorEditorPanel) pv.getEditorForChapter (c);

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

        if (name == EditorViewer.DELETE_NOTE_ACTION)
        {

            return new DeleteChapterItemActionHandler ((Note) other,
                                                       this,
                                                       false);

        }

        if (name == EditorViewer.NEW_NOTE_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    Chapter c = (Chapter) other;

                    EditorEditorPanel qep = pv.getEditorForChapter (c);

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

        throw new IllegalArgumentException ("Action: " +
                                            name +
                                            " not known.");

    }

    public void handleNewProject ()
    {

        throw new UnsupportedOperationException ("Not supported for editor mode.");
        
    }

    public String getViewerIcon ()
    {

        return "editors";//this.proj.getObjectType ();

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
        
        super.handleHTMLPanelAction (v);

    }

    public void handleOpenProject ()
    {

        final EditorViewer _this = this;

        // See if we should be doing a warmup exercise.
        Properties userProps = Environment.getUserProperties ();

        this.initProjectItemBoxes ();
        
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

    }

    public EditorEditorPanel getEditorForChapter (Chapter c)
    {

        for (QuollPanel qp : this.getAllQuollPanelsForObject (c))
        {
        
            if (qp instanceof FullScreenQuollPanel)
            {
                
                qp = ((FullScreenQuollPanel) qp).getChild ();
                
            }

            if (qp instanceof EditorEditorPanel)
            {
                
                return (EditorEditorPanel) qp;
                
            }
            
        }
        
        return null;

    }
    
    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the chapter has been opened for editing.
     */
    public boolean editChapter (Chapter c)
    {

        // Check our tabs to see if we are already editing this chapter, if so then just switch to it instead.
        EditorEditorPanel qep = (EditorEditorPanel) this.getQuollPanelForObject (c);

        if (qep != null)
        {
        
            this.setPanelVisible (qep);
            
            this.getEditorForChapter (c).getEditor ().grabFocus ();
        
            return true;

        }

        final EditorViewer _this = this;

        try
        {

            qep = new EditorEditorPanel (this,
                                         c);

            qep.init ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to edit chapter: " +
                                  c,
                                  e);

            UIUtils.showErrorMessage (_this,
                                      "Unable to edit {chapter}: " +
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

        if (d instanceof Note)
        {

            final Note n = (Note) d;

            if (n.getObject () != null)
            {

                if (!this.editChapter ((Chapter) n.getObject ()))
                {

                    return false;

                }

                final EditorViewer _this = this;

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
                                                          "Unable to display {Note} in {Chapter}.");

                            }

                        }

                    });

            }

            return true;

        }

        if (d instanceof Chapter)
        {

            Chapter c = (Chapter) d;

            if (d.getObjectType ().equals (Chapter.INFORMATION_OBJECT_TYPE))
            {

                return this.viewEditorChapterInformation (c);

            } else
            {

                return this.editChapter (c);

            }

        }

        if (d instanceof Note)
        {

            this.viewNote ((Note) d);

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

                EditorEditorPanel qep = this.getEditorForChapter (c);

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

    public boolean openPanel (String id)
    {
    
        return false;

    }
    
    protected void addNameChangeListener (final NamedObject n,
                                          final QuollPanel  qp)
    {
        
        final EditorViewer _this = this;
        
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
    public boolean viewEditorChapterInformation (final Chapter c)
    {
/*
        ChapterInformationSideBar cb = new ChapterInformationSideBar (this,
                                                                      c);
        
        this.addSideBar ("chapterinfo-" + c.getKey (),
                         cb);
        
        this.showSideBar ("chapterinfo-" + c.getKey ());
  */  
        return true;
    
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
    
    public void deleteObject (NamedObject o,
                              boolean     deleteChildObjects)
                       throws GeneralException
    {
        
        if (o instanceof ChapterItem)
        {
            
            this.deleteChapterItem ((ChapterItem) o,
                                    deleteChildObjects,
                                    true);
                        
            return;
        
        }

        this.deleteObject (o);
                        
    }
    
    public void deleteObject (NamedObject o)
                              throws      GeneralException
    {
        
        if (o instanceof Chapter)
        {
            
            this.deleteChapter ((Chapter) o);
            
        }
        
    }    
    
    public void deleteChapterItem (ChapterItem ci,
                                   boolean     deleteChildObjects,
                                   boolean     doInTransaction)
                            throws GeneralException
    {

        if (ci.getObjectType ().equals (Note.OBJECT_TYPE))
        {

            this.deleteNote ((Note) ci,
                             doInTransaction);

        }

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

        this.fireProjectEvent (n.getObjectType (),
                               ProjectEvent.DELETE,
                               n);
        
        this.refreshObjectPanels (otherObjects);

        if (obj instanceof Chapter)
        {

            EditorEditorPanel qep = this.getEditorForChapter ((Chapter) obj);

            if (qep != null)
            {

                qep.removeItem (n);

            }

        }

        this.reloadNoteTree ();

        this.reloadChapterTree ();
        
    }

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

        final EditorViewer _this = this;

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

    public void updateChapterIndexes (Book b)
                               throws GeneralException
    {

        this.dBMan.updateChapterIndexes (b);

    }

    public String getChapterObjectName ()
    {

        return Environment.getObjectTypeName (Chapter.OBJECT_TYPE);

    }

    public void deleteChapter (Chapter c)
    {
        
        throw new UnsupportedOperationException ("Not supported");
        
    }

}
