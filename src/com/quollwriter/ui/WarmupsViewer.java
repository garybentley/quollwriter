package com.quollwriter.ui;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;

import java.net.*;

import java.security.*;

import java.text.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.text.*;

import com.gentlyweb.properties.*;

import com.gentlyweb.utils.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;

import com.quollwriter.db.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.events.*;

import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.DocumentAdapter;
import com.quollwriter.ui.components.TabHeader;
import com.quollwriter.ui.components.Runner;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.sidebars.*;

public class WarmupsViewer extends AbstractProjectViewer
{

    public static final String WORD_COUNT_TIMER_HEADER_CONTROL_ID = "wordCountTimer";
    
    public static final int EDIT_WARMUP_ACTION = 201;
    public static final int NEW_WARMUP_ACTION = 202;
    public static final int DELETE_WARMUP_ACTION = 203;
    public static final int CONVERT_TO_PROJECT_ACTION = 204;
    public static final int RENAME_WARMUP_ACTION = 205;

    private ChapterCounts          startWordCounts = new ChapterCounts ();
    private java.util.List<Warmup> warmups = null;
    private SimpleDateFormat       sdf = null;
    private WarmupsSideBar sideBar = null;

    public WarmupsViewer()
    {

        final WarmupsViewer _this = this;
    
        this.sdf = new SimpleDateFormat ("dd MMM yyyy");

        this.sideBar = new WarmupsSideBar (this);
                                                
    }

    public void reloadTreeForObjectType (String objType)
    {
        
    }
    
    public void showObjectInTree (String      treeObjType,
                                  NamedObject obj)
    {
                
    }    
    
    public void reloadTreeForObjectType (NamedObject obj)
    {
        
        this.reloadTreeForObjectType (obj.getObjectType ());
        
    }

    public JTree getWarmupsTree ()
    {
        
        return this.sideBar.getWarmupsTree ();
                
    }
    
    public AbstractSideBar getMainSideBar ()
    {
        
        return this.sideBar;
        
    }
    
    public void addWarmupToTree (Chapter newChapter)
    {

        DefaultTreeModel model = (DefaultTreeModel) this.getWarmupsTree ().getModel ();

        DefaultMutableTreeNode cNode = new DefaultMutableTreeNode (newChapter);

        // Get the book node.
        TreePath tp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                        this.proj);

        if (tp != null)
        {

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

            model.insertNodeInto (cNode,
                                  (MutableTreeNode) node,
                                  0);

        }

        this.getWarmupsTree ().setSelectionPath (new TreePath (cNode.getPath ()));

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

        final WarmupsViewer _this = this;

        if (name == RENAME_WARMUP_ACTION)
        {

            return new RenameWarmupActionHandler ((Chapter) other,
                                                  this);

        }        
        
        if (name == CONVERT_TO_PROJECT_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    Chapter c = (Chapter) other;

                    AbstractEditorPanel aep = _this.getEditorForChapter (c);

                    Warmup w = _this.getWarmupForChapter (c);

                    _this.showConvertWarmupToProject (w,
                                                      aep);
                                                
                }

            };

        }

        if (name == EDIT_WARMUP_ACTION)
        {

            return new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.viewObject (other);

                }

            };

        }

        if (name == NEW_WARMUP_ACTION)
        {

            return this.getAction (AbstractProjectViewer.WARMUP_EXERCISE_ACTION);

        }

        if (name == DELETE_WARMUP_ACTION)
        {

            return new DeleteWarmupActionHandler ((Chapter) other,
                                                  this);
        
        }

        throw new IllegalArgumentException ("Action: " +
                                            name +
                                            " not known.");

    }

    private void showConvertWarmupToProject (final Warmup              warmup,
                                             final AbstractEditorPanel panel)
    {
        
        final WarmupsViewer _this = this;
        
        final QPopup popup = UIUtils.createClosablePopup ("Convert {warmup} to {a project}",
                                             Environment.getIcon (Project.OBJECT_TYPE,
                                                                  Constants.ICON_POPUP),
                                             null);        
        
        NewProjectPanel npanel = new NewProjectPanel ()
        {
          
            @Override
            public boolean createProject (Container parent)
            {
                
                if (!this.checkForm (_this))
                {

                    popup.resize ();
                
                    return false;

                }
                
                Project p = new Project (this.getName ());

                Book b = new Book (p,
                                   p.getName ());

                // Create a new chapter for the book.
                Chapter c = new Chapter (b,
                                         Environment.getProperty (Constants.DEFAULT_CHAPTER_NAME_PROPERTY_NAME));

                b.addChapter (c);

                c.setName (warmup.getChapter ().getName ());
                c.setText (panel.getEditor ().getTextWithMarkup ());

                p.addBook (b);

                String pwd = this.getPassword ();

                try
                {

                    ProjectViewer pj = new ProjectViewer ();
    
                    pj.init ();
                
                    pj.newProject (this.getSaveDirectory (),
                                   p,
                                   pwd);

                    _this.fireProjectEvent (Warmup.OBJECT_TYPE,
                                            ProjectEvent.CONVERT_TO_PROJECT,
                                            warmup);                                   
                                   
                    pj.createActionLogEntry (p,
                                             "Project created from warmup, prompt id: " +
                                             warmup.getPrompt ().getId () +
                                             " and chapter: " +
                                             warmup.getChapter ().getName () +
                                             " (" +
                                             warmup.getChapter ().getKey () +
                                             ")");

                } catch (Exception e)
                {

                    Environment.logError ("Unable to create new project: " +
                                          p,
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              "Unable to create new project: " + p.getName ());

                    return false;

                }
                
                return true;
                
            }
            
        };        
        
        JComponent newpanel = npanel.createPanel (this,
                                                  new ActionListener ()
                                                  {
                                                    
                                                      public void actionPerformed (ActionEvent ev)
                                                      {
                                                        
                                                          popup.removeFromParent ();
                                                        
                                                      }
                                                    
                                                  },
                                                  true,
                                                  new ActionListener ()
                                                  {
                                                    
                                                      public void actionPerformed (ActionEvent ev)
                                                      {
                                                        
                                                          popup.removeFromParent ();
                                                        
                                                      }
                                                    
                                                  },
                                                  true);
                    
        Box content = new Box (BoxLayout.Y_AXIS);
        
        JTextPane help = UIUtils.createHelpTextPane ("To create the new {Project} enter the name below, select the directory it should be saved to and press the Create button.  Your warm-up text will be added as a {chapter} to the {Project}.",
                                                     this);        
               
        help.setBorder (null);
        help.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                         500));        
        content.add (help);
        
        content.add (Box.createVerticalStrut (5));
        
        content.add (newpanel);
                                                  
        popup.setContent (content);
        
        content.setBorder (UIUtils.createPadding (10, 10, 10, 10));        
        content.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                         content.getPreferredSize ().height));        

        popup.setDraggable (this);
                          
        popup.resize ();
        this.showPopupAt (popup,
                          UIUtils.getCenterShowPosition (this,
                                                         popup),
                          false);
        
    }
    
    public void doSaveState ()
    {

    }

    public void handleNewProject ()
                           throws Exception
    {

        if ((this.proj.getBooks () == null) ||
            (this.proj.getBooks ().size () == 0) ||
            (this.proj.getBooks ().get (0).getChapters ().size () == 0))
        {

            Book b = new Book (this.proj,
                               this.proj.getName ());

            this.proj.addBook (b);

        }

        this.handleOpenProject ();

    }

    public void addNewWarmup (Warmup w)
                       throws GeneralException
    {

        Book b = this.proj.getBooks ().get (0);

        Date d = new Date ();

        String name = this.sdf.format (d);

        int max = 0;

        // See if we already have a chapter with that name.
        for (Chapter c : b.getChapters ())
        {

            if (c.getName ().startsWith (name))
            {

                // See if there is a number at the end.
                int ind = c.getName ().indexOf ("(",
                                                name.length ());

                if (ind != -1)
                {

                    // Get the number (if present)
                    String n = c.getName ().substring (ind + 1);

                    n = StringUtils.replaceString (n,
                                                   "(",
                                                   "");
                    n = StringUtils.replaceString (n,
                                                   ")",
                                                   "");

                    try
                    {

                        int m = Integer.parseInt (n);

                        if (m > max)
                        {

                            max = m;

                        }

                    } catch (Exception e)
                    {

                        // Ignore.

                    }

                } else
                {

                    if (max == 0)
                    {

                        max = 1;

                    }

                }

            }

        }

        if (max > 0)
        {

            max++;

            name = name + " (" + max + ")";

        }

        // Create a new chapter for the book.
        Chapter c = new Chapter (b,

                                 // Default to todays date.
                                 name);

        b.addChapter (c);

        // Create a new warmup.
        w.setChapter (c);

        java.util.List objs = new ArrayList ();
        objs.add (c);
        objs.add (w);

        // Add the chapter.
        try
        {

            this.saveObjects (objs,
                              true);

        } catch (Exception e)
        {

            throw new GeneralException ("Unable to save new chapter/warmup",
                                        e);

        }

        this.warmups.add (w);

        this.viewObject (w);

        WarmupEditorPanel wep = (WarmupEditorPanel) this.getEditorForWarmup (c);

        wep.startWarmup ();

        this.addWarmupToTree (w.getChapter ());

        this.fireProjectEvent (Warmup.OBJECT_TYPE,
                               ProjectEvent.NEW,
                               w);

        // this.reloadWarmupsTree ();
    }

    public void handleOpenProject ()
                            throws Exception
    {

        // Get all the warmups.
        this.warmups = (java.util.List<Warmup>) this.dBMan.getObjects (Warmup.class,
                                                                       this.proj,
                                                                       null,
                                                                       true);

        this.proj.getBooks ().get (0).setChapterSorter (new ChapterSorter (ChapterSorter.DATE_CREATED,
                                                                           ChapterSorter.DESC));

        final WarmupsViewer _this = this;

        SwingUtilities.invokeLater (new Runner ()
        {

            public void run ()
            {

                // Get the word counts.
                _this.startWordCounts = _this.getAllChapterCounts ();

            }

        });

    }

    public void handleItemChangedEvent (ItemChangedEvent ev)
    {

        if (ev.getChangedObject () instanceof Chapter)
        {

            this.reloadWarmupsTree ();

        }

    }

    public void reloadWarmupsTree ()
    {

        ((DefaultTreeModel) this.getWarmupsTree ().getModel ()).reload ();

    }

    public Chapter getChapterCurrentlyEdited ()
    {

        QuollPanel qp = this.getCurrentlyVisibleTab ();
        
        if (qp instanceof WarmupEditorPanel)
        {
        
            WarmupEditorPanel qep = (WarmupEditorPanel) qp;

            if (qep != null)
            {
    
                // Get the chapter.
                return qep.getChapter ();
    
            }

        }
            
        return null;

    }

    public WarmupEditorPanel getEditorForWarmup (Chapter c)
    {

        QuollPanel qp = this.getQuollPanel (c.getObjectReference ().asString ());

        if (qp != null)
        {

            if (qp instanceof FullScreenQuollPanel)
            {
                
                return (WarmupEditorPanel) ((FullScreenQuollPanel) qp).getChild ();
            
            } else {                

                WarmupEditorPanel qep = (WarmupEditorPanel) qp;
    
                if (qep.getChapter () == c)
                {
    
                    return qep;
    
                }

            }

        }

        return null;

    }
    
    public Warmup getWarmupForChapter (Chapter c)
    {

        for (Warmup w : this.warmups)
        {

            if (w.getChapter () == c)
            {

                return w;

            }

        }

        return null;

    }

    public boolean openPanel (String id)
    {

        return false;

    }

    public boolean viewObject (DataObject d,
                               ActionListener doAfterView)
    {
        
        throw new UnsupportedOperationException ("Not yet supported");        
        
    }
    
    public boolean viewObject (DataObject d)
    {

        if (d instanceof Warmup)
        {

            return this.editWarmup ((Warmup) d);

        }

        if (d instanceof Chapter)
        {

            // Get the warmup for the chapter.
            Warmup w = this.getWarmupForChapter ((Chapter) d);

            if (w == null)
            {

                throw new IllegalArgumentException ("No warmup found for chapter: " +
                                                    d);

            }

            return this.editWarmup (w);

        }

        throw new IllegalArgumentException ("Object: " +
                                            d +
                                            " not yet supported.");

    }

    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the chapter has been opened for editing.
     */
    public boolean editWarmup (Warmup w)
    {

        final Chapter c = w.getChapter ();

        // Check our tabs to see if we are already editing this warmup, if so then just switch to it instead.
        QuollPanel qp = this.getQuollPanel (c.getObjectReference ().asString ());

        if (qp != null)
        {

            this.setPanelVisible (qp);

            //this.sideBar.setObjectSelectedInSidebar (w);
            
            ((AbstractEditorPanel) qp).getEditor ().grabFocus ();

            return true;

        }

        final WarmupsViewer _this = this;

        WarmupEditorPanel wep = null;

        try
        {

            wep = new WarmupEditorPanel (this,
                                         w);

            wep.init ();

        } catch (Exception e)
        {

            Environment.logError ("Unable to edit warmup: " +
                                  c,
                                  e);

            UIUtils.showErrorMessage (_this,
                                      "Unable to edit warmup: " +
                                      c.getName ());

            return false;

        }

        final TabHeader th = this.addPanel (wep);

        wep.getEditor ().getDocument ().addDocumentListener (new DocumentAdapter ()
        {

            public void insertUpdate (DocumentEvent ev)
            {

                // Change the tab name.
                th.setComponentChanged (true);

            }

            public void removeUpdate (DocumentEvent ev)
            {

                // Change the tab name.
                th.setComponentChanged (true);

            }

        });
            
        final QuollPanel _wep = wep;

        wep.addObjectPropertyChangedListener (new PropertyChangedListener ()
        {

            @Override
            public void propertyChanged (PropertyChangedEvent ev)
            {

                if (ev.getChangeType ().equals (NamedObject.NAME))
                {
            
                    _this.setTabHeaderTitle (_wep,
                                             _wep.getTitle ());
                
                    _this.informTreeOfNodeChange (c,
                                                  _this.getWarmupsTree ());

                }
                                                  
            }

        });

        // Open the tab :)
        return this.editWarmup (w);

    }

    public TypesHandler getObjectTypesHandler (String objType)
    {
        
        return null;
        
    }
    
    public void updateChapterIndexes (Book b)
                               throws GeneralException
    {
        
        throw new UnsupportedOperationException ("Not supported");
        
    }
    
    @Override
    public void deleteChapter (Chapter c)
    {

        this.deleteWarmup (c);

    }

    public void deleteObject (NamedObject o,
                              boolean     deleteChildObjects)
                       throws GeneralException
    {

        if (o instanceof Chapter)
        {
            
            this.deleteChapter ((Chapter) o);
             
        } else {
            
            this.deleteObject (o);
            
        }
    
    }    
    
    public void deleteObject (NamedObject o)
                              throws      GeneralException
    {
        
        this.deleteObject (o,
                           true);
        
    }
    
    public void deleteWarmup (Chapter c)
    {

        try
        {

            // Get the warmup and delete it.
            Warmup w = this.getWarmupForChapter (c);

            if (w == null)
            {
                
                Environment.logError ("Unable to find warmup for chapter: " +
                                      c);
                
                UIUtils.showErrorMessage (this,
                                          "Unable to delete {warmup}.");
                
                return;
                
            }
                        
            Set<DataObject> toDelete = new LinkedHashSet ();
            
            toDelete.add (w);
            toDelete.add (c);
            
            this.dBMan.deleteObjects (toDelete,
                                      null);

            this.warmups.remove (w);

            // Remove the chapter from the book.
            Book b = c.getBook ();

            b.removeChapter (c);
            
            this.fireProjectEvent (w.getObjectType (),
                                   ProjectEvent.DELETE,
                                   w);

        } catch (Exception e)
        {

            Environment.logError ("Unable to delete warmup: " + c,
                                  e);

            UIUtils.showErrorMessage (this,
                                      "Unable to delete warm-up.");

            return;

        }

        // Inform the chapter tree about the change.
        this.sideBar.reloadTree ();
        
        // Remove the tab (if present).
        this.removeAllPanelsForObject (c);

    }

    public void repaintWarmupsTree ()
    {

        this.getWarmupsTree ().repaint ();

    }

    public void chapterTreeChanged (DataObject d)
    {

        UIUtils.treeChanged (d,
                             this.getWarmupsTree ());

    }

    public void scrollTo (final Chapter c,
                          final int     pos)
    {

        final WarmupsViewer _this = this;

        Warmup w = this.getWarmupForChapter (c);

        if (w == null)
        {

            return;

        }

        this.editWarmup (w);

        SwingUtilities.invokeLater (new Runner ()
            {

                public void run ()
                {

                    try
                    {

                        _this.getEditorForWarmup (c).scrollToPosition (pos);
                        _this.getEditorForWarmup (c).scrollToPosition (pos);

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

    public ChapterCounts getAllChapterCounts ()
    {

        Book b = this.proj.getBooks ().get (0);

        java.util.List<Chapter> chapters = b.getChapters ();

        ChapterCounts achc = new ChapterCounts ();

        for (Chapter c : chapters)
        {

            WarmupEditorPanel qep = this.getEditorForWarmup (c);

            String text = null;

            if (qep != null)
            {

                text = qep.getEditor ().getText ();

            } else
            {

                text = c.getChapterText ();

            }

            achc.add (new ChapterCounts (text));

        }

        return achc;

    }

    public void fillSettingsPopup (JPopupMenu titlePopup)
    {

        titlePopup.add (this.createMenuItem ("New {Project}",
                                             Constants.NEW_ICON_NAME,
                                             AbstractProjectViewer.NEW_PROJECT_ACTION));

        titlePopup.add (this.createMenuItem ("Rename {Project}",
                                             Constants.EDIT_ICON_NAME,
                                             AbstractProjectViewer.RENAME_PROJECT_ACTION));

        titlePopup.add (this.createMenuItem ("Open {Project}",
                                             Constants.OPEN_PROJECT_ICON_NAME,
                                             AbstractProjectViewer.OPEN_PROJECT_ACTION));

        titlePopup.add (this.createMenuItem ("Close {Project}",
                                             Constants.CLOSE_ICON_NAME,
                                             AbstractProjectViewer.CLOSE_PROJECT_ACTION));
        
        titlePopup.add (this.createMenuItem ("Delete {Project}",
                                             Constants.DELETE_ICON_NAME,
                                             ProjectViewer.DELETE_PROJECT_ACTION));
        
        titlePopup.addSeparator ();

        // Do a New Warm-up Exercise
        titlePopup.add (this.createMenuItem ("Do a {Warmup} Exercise",
                                             Constants.WARMUPS_ICON_NAME,
                                             AbstractProjectViewer.WARMUP_EXERCISE_ACTION));
        
    }
    
    public String getViewerIcon ()
    {

        return "warmups";

    }
    
    public String getChapterObjectName ()
    {

        return Environment.getObjectTypeName (Warmup.OBJECT_TYPE);

    }

    public String getViewerTitle ()
    {

        return this.proj.getName ();

    }
    
    public void fillFullScreenTitleToolbar (JToolBar toolbar)
    {

        WordCountTimerBox wct = new WordCountTimerBox (this.getFullScreenFrame (),
                                                       Constants.ICON_FULL_SCREEN_ACTION,
                                                       this.getWordCountTimer ());

        wct.setBarHeight (24);
                                                  
        toolbar.add (wct);
    
    }    
    
    @Override
    public Set<String> getTitleHeaderControlIds ()
	{
		
		Set<String> ids = new LinkedHashSet ();

		ids.add (WORD_COUNT_TIMER_HEADER_CONTROL_ID);

        ids.add (DO_WARMUP_HEADER_CONTROL_ID);
        
        ids.addAll (super.getTitleHeaderControlIds ());
				
		return ids;
		
	}
    
	@Override
    public JComponent getTitleHeaderControl (String id)
	{
		
		if (id == null)
		{
			
			return null;
			
		}
		
		final WarmupsViewer _this = this;
		
		JComponent c = null;
		
		if (id.equals (WORD_COUNT_TIMER_HEADER_CONTROL_ID))
		{
			
            WordCountTimerBox wct = new WordCountTimerBox (this,
                                                         Constants.ICON_FULL_SCREEN_ACTION,
                                                         this.getWordCountTimer ());
    
            wct.setBarHeight (20);

            return wct;
            										  
		}

        return super.getTitleHeaderControl (id);
        
    }
    
    public void fillTitleToolbar (JToolBar toolbar)
    {
    
        WordCountTimerBox wct = new WordCountTimerBox (this,
                                                     Constants.ICON_FULL_SCREEN_ACTION,
                                                     this.getWordCountTimer ());

        wct.setBarHeight (20);
                                                  
        toolbar.add (wct);
        
/*
        wct.setBorder (new CompoundBorder (new EmptyBorder (0, 5, 0, 5),
                                                           wct.getBorder ()));
  */                                      
/*        
        toolbar.add (UIUtils.createButton (Constants.DOWN_ICON_NAME,
                                           Constants.ICON_TITLE_ACTION,
                                           "Click to show/hide the prompt",
                                           new ActionAdapter ()
                                           {
                                                
                                                public void actionPerformed (ActionEvent ev)
                                                {
                                                    
                                                    _this.togglePrompt ();
                                                    
                                                    JButton b = (JButton) ev.getSource ();
                                                    
                                                    b.setIcon (Environment.getIcon ((_this.promptNotify.isVisible () ? Constants.UP_ICON_NAME : Constants.DOWN_ICON_NAME),
                                                                                    Constants.ICON_TITLE_ACTION));
                                                    
                                                }
                                                
                                           }));    
  */  
    }

    public Set<FindResultsBox> findText (String t)
    {
        
        Set<FindResultsBox> res = new LinkedHashSet ();
        
        // Get the snippets.
        Map<Chapter, java.util.List<Segment>> snippets = UIUtils.getTextSnippets (t,
                                                                                  this);

        if (snippets.size () > 0)
        {

            res.add (new ChapterFindResultsBox (Environment.getObjectTypeNamePlural (Warmup.OBJECT_TYPE),
                                                Warmup.OBJECT_TYPE,
                                                Warmup.OBJECT_TYPE,
                                                this,
                                                snippets));
            
        }

        return res;
        
    }
    
}
