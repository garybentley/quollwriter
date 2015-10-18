package com.quollwriter.editors.ui;

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
import com.quollwriter.data.editors.*;

import com.quollwriter.db.*;

import com.quollwriter.events.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.text.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.editors.ui.panels.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.sidebars.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.*;

import com.quollwriter.ui.components.TabHeader;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.IconProvider;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.ui.*;

public abstract class ProjectSentReceivedViewer<E extends EditorMessage> extends AbstractProjectViewer
{

    protected ProjectSentReceivedSideBar  sideBar = null;
    private DefaultChapterItemViewPopupProvider chapterItemViewPopupProvider = null;
    private IconProvider iconProvider = null;
    private EditorEditor editor = null;
    protected E message = null;
    
    public ProjectSentReceivedViewer (Project      proj,
                                      E            message)
    {

        final ProjectSentReceivedViewer _this = this;

        this.proj = proj;
        this.message = message;
        
        this.chapterItemViewPopupProvider = new DefaultChapterItemViewPopupProvider ()
        {
        
            @Override
            public boolean canEdit (ChapterItem it)
            {
                
                return false;
                
            }
            
            @Override
            public boolean canDelete (ChapterItem it)
            {

                return false;
                
            }

        };
        
        this.chapterItemViewPopupProvider.setShowLinks (false);
        this.chapterItemViewPopupProvider.setFormatDetails (Note.OBJECT_TYPE,
                                                            new NoteFormatDetails ()
                                                            {
                                                                                                                                
                                                                @Override
                                                                public String getTitle (Note item)
                                                                {
                                                                    
                                                                    return "{Comment}";
                                                                    
                                                                }
                                                                
                                                                @Override
                                                                public String getIcon (Note item)
                                                                {
                                                                    
                                                                    return Constants.COMMENT_ICON_NAME;
                                                                    
                                                                }
                                                                
                                                                @Override
                                                                public String getItemDescription (Note item)
                                                                {
                                                                    
                                                                    return item.getDescription ().getMarkedUpText ();
                                                                    
                                                                }
                                                                
                                                                @Override
                                                                public AbstractActionHandler getEditItemActionHandler (Note                item,
                                                                                                                       AbstractEditorPanel ep)
                                                                {
                                                            
                                                                    throw new UnsupportedOperationException ("Not supported for project comments.");
                                                            
                                                                }                                                                
                                                                
                                                                @Override
                                                                public ActionListener getDeleteItemActionHandler (Note                item,
                                                                                                                  AbstractEditorPanel ep,
                                                                                                                  boolean             showAtItem)
                                                                {
                                                            
                                                                    throw new UnsupportedOperationException ("Not supported for project comments.");
                                                            
                                                                }
                                                                
                                                            });
        
        this.iconProvider = new DefaultIconProvider ()
        {
          
            @Override
            public ImageIcon getIcon (String name,
                                      int    type)
            {
                
                if (name.equals (Note.OBJECT_TYPE))
                {
                    
                    name = Constants.COMMENT_ICON_NAME;
                    
                }
                
                return super.getIcon (name,
                                      type);
                
            }
            
        };
                                                            
        this.sideBar = this.getSideBar ();
                        
    }
        
    public abstract ProjectSentReceivedSideBar getSideBar ();
  
    public void close ()
    {
        
        this.close (true,
                    null);
        
    }
    
    @Override
    public boolean close (boolean              noConfirm,
                          final ActionListener afterClose)
    {   

        this.dispose ();
        
        if (afterClose != null)
        {
            
            afterClose.actionPerformed (new ActionEvent (this,
                                                         0,
                                                         "closed"));            
            
        }
        
        return true;
        
    }
		
    @Override
    public Set<String> getTitleHeaderControlIds ()
	{
		
		Set<String> ids = new LinkedHashSet ();

		ids.add (FIND_HEADER_CONTROL_ID);
		ids.add (CLOSE_HEADER_CONTROL_ID);
				
		return ids;
		
	}
    
    @Override
    public void init ()
               throws Exception
    {

        super.init ();
    /*
        final ProjectSentReceivedViewer _this = this;
    
        JToolBar titleC = UIUtils.createButtonBar (new ArrayList ());

        titleC.add (UIUtils.createButton (Constants.FIND_ICON_NAME,
                                          Constants.ICON_TITLE_ACTION,
                                          "Click to open the find",
                                          new ActionAdapter ()
                                          {
                                            
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                
                                                  _this.showFind (null);
                                                
                                              }
                                            
                                          }));

        titleC.add (UIUtils.createButton (Constants.CLOSE_ICON_NAME,
                                          Constants.ICON_TITLE_ACTION,
                                          "Click to close",
                                          new ActionAdapter ()
                                          {
                                            
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                
                                                  _this.close ();
                                                
                                              }
                                            
                                          }));
                                          
        this.setViewerControls (titleC);
        */
        this.initSideBars ();
        
        this.initWindow ();
                        
    }

    public IconProvider getIconProvider ()
    {
        
        return this.iconProvider;
        
    }
    
    public ChapterItemViewPopupProvider getChapterItemViewPopupProvider ()
    {
        
        return this.chapterItemViewPopupProvider;
        
    }
    
    public void initActionMappings (ActionMap am)
    {
    
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
    
    public void fillFullScreenTitleToolbar (JToolBar toolbar)
    {
        
        this.fillTitleToolbar (toolbar);
        
    }
    
    public void fillTitleToolbar (JToolBar toolbar)
    {
                                              
    }
        
    public void fillSettingsPopup (JPopupMenu titlePopup)
    {
                
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

        throw new IllegalArgumentException ("Action: " +
                                            name +
                                            " not known.");

    }

    public void handleNewProject ()
    {
    
        throw new UnsupportedOperationException ("Not supported for viewing project comments.");
        
    }

    public String getViewerIcon ()
    {

        return Constants.COMMENT_ICON_NAME;

    }

    public String getViewerTitle ()
    {

        return "{Comments} on: " + this.proj.getName ();

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
        
        throw new UnsupportedOperationException ("Not supported for viewing project comments.");
        
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

	@Override
	public void saveProject ()
	{
		
		
		
	}
	
    public void doSaveState ()
    {

    }

    public ChapterCommentsPanel getEditorForChapter (Chapter c)
    {

        for (QuollPanel qp : this.getAllQuollPanelsForObject (c))
        {
        
            if (qp instanceof FullScreenQuollPanel)
            {
                
                qp = ((FullScreenQuollPanel) qp).getChild ();
                
            }

            if (qp instanceof ChapterCommentsPanel)
            {
                
                return (ChapterCommentsPanel) qp;
                
            }
            
        }
        
        return null;

    }
    
    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the chapter has been opened for editing.
     */
    public boolean editChapter (Chapter        c,
                                ActionListener doAfterOpen)
    {

        // Check our tabs to see if we are already editing this chapter, if so then just switch to it instead.
        ChapterCommentsPanel qep = (ChapterCommentsPanel) this.getQuollPanelForObject (c);

        if (qep != null)
        {
        
            this.setPanelVisible (qep);

            qep.getEditor ().grabFocus ();

            if (doAfterOpen != null)
            {
                
                UIUtils.doActionWhenPanelIsReady (qep,
                                                  doAfterOpen,
                                                  c,
                                                  "afterview");
                
            }
        
            return true;

        }

        final ProjectSentReceivedViewer _this = this;

        try
        {

            qep = new ChapterCommentsPanel (this,
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

        /*
         *TODO: Change name if "parent" chapter changes name?
        this.addNameChangeListener (c,
                                    qep);
*/
        // Open the tab :)
        return this.editChapter (c,
                                 doAfterOpen);

    }

    public boolean viewObject (DataObject d)
    {
    
        return this.viewObject (d,
                                null);
        
    }
    
    public boolean viewObject (final DataObject     d,
                               final ActionListener doAfterView)
    {

        if (d instanceof Note)
        {

            final Note n = (Note) d;

            if (n.getObject () != null)
            {

                final Chapter c = (Chapter) n.getObject ();
            
                final ProjectSentReceivedViewer _this = this;
            
                ActionListener onOpen = new ActionListener ()
                {
                  
                    public void actionPerformed (ActionEvent ev)
                    {
                      
                        try
                        {

                            _this.getEditorForChapter (c).showNote (n);
  
                        } catch (Exception e) {
  
                            Environment.logError ("Unable to scroll to note: " +
                                                  n,
                                                  e);
  
                            UIUtils.showErrorMessage (_this,
                                                      "Unable to display {comment}.");
  
                        }
                        
                        if (doAfterView != null)
                        {
                            
                            doAfterView.actionPerformed (ev);
                          
                        }
                      
                    }
                  
                };
            
                return this.editChapter (c,
                                         onOpen);
            
            }

        }

        if (d instanceof Chapter)
        {

            Chapter c = (Chapter) d;

            return this.editChapter (c,
                                     doAfterView);

        }

        // Record the error, then ignore.
        Environment.logError ("Unable to open object: " + d);

        return false;

    }

    public boolean openPanel (String id)
    {
    
        return false;

    }
            
    public JTree getTreeForObjectType (String objType)
    {
        
        return this.sideBar.getTreeForObjectType (objType);
                
    }
        
    public JTree getChapterTree ()
    {
        
        return this.getTreeForObjectType (Chapter.OBJECT_TYPE);
        
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

        final ProjectSentReceivedViewer _this = this;

        this.editChapter (c,
                          new ActionListener ()
        {
            
            public void actionPerformed (ActionEvent ev)
            {

                try
                {

                    _this.getEditorForChapter (c).scrollToPosition (pos);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to show snippet at position: " +
                                          pos,
                                          e);

                    UIUtils.showErrorMessage (_this,
                                              "Unable to scroll to position.");

                }
                
            }

        });

    }

    public void updateChapterIndexes (Book   b)
                               throws GeneralException
    {
        
        throw new UnsupportedOperationException ("Not supported for project comments.");        
        
    }

    public void deleteChapter (Chapter c)
    {
        
        throw new UnsupportedOperationException ("Not supported for project comments.");                
        
    }

    public void deleteObject (NamedObject o)
                  throws      GeneralException
    {
        
        throw new UnsupportedOperationException ("Not supported for project comments.");                
        
    }
    
    public void deleteObject (NamedObject o,
                              boolean     deleteChildObjects)
                       throws GeneralException
    {
        
        throw new UnsupportedOperationException ("Not supported for project comments.");                
        
    }

    public String getChapterObjectName ()
    {

        return Environment.getObjectTypeName (Chapter.OBJECT_TYPE);

    }
    
    public TypesHandler getObjectTypesHandler (String objType)
    {
        
        return null;
        
    }        

    @Override
    public void setLinks (NamedObject o)
    {

        // No links for viewing comments.

    }

    public Set<FindResultsBox> findText (String t)
    {
        
        Set<FindResultsBox> res = new LinkedHashSet ();
        
        // Get the snippets.
        Map<Chapter, java.util.List<Segment>> snippets = UIUtils.getTextSnippets (t,
                                                                                  this);

        if (snippets.size () > 0)
        {

            res.add (new ChapterFindResultsBox (Environment.getObjectTypeNamePlural (Chapter.OBJECT_TYPE),
                                                Chapter.OBJECT_TYPE,
                                                Chapter.OBJECT_TYPE,
                                                this,
                                                snippets));
            
        }
                                                           
        Set<Note> notes = UIUtils.getNotesContaining (t,
                                                      this.proj);

        if (notes.size () > 0)
        {                                                      
        
            res.add (new NamedObjectFindResultsBox<Note> ("{Comments}",
                                                          Constants.COMMENT_ICON_NAME,
                                                          Note.OBJECT_TYPE,
                                                          this,
                                                          notes));
            
        }
                
        return res;
        
    }
    
}
