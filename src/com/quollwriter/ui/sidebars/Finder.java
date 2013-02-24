package com.quollwriter.ui.sidebars;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.text.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.data.*;
import com.quollwriter.ui.renderers.*;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.ActionAdapter;

public class Finder extends AbstractSideBar
{

    private JTextField text = null;
    private ChapterResultsBox chapterResults = null;
    
    private NoteResultsBox noteResults = null;
    private ChapterItemResultsBox sceneResults = null;
    private ChapterItemResultsBox outlineItemResults = null;
    private Object highlightId = null;
    private QTextEditor highlightedEditor = null;
    private Box content = null;

    private Map<String, AssetResultsBox> assetResults = new LinkedHashMap ();
        
    public Finder (AbstractProjectViewer v)
    {
        
        super (v);

        this.assetResults.put (QCharacter.OBJECT_TYPE,
                               null);
        this.assetResults.put (Location.OBJECT_TYPE,
                               null);
        this.assetResults.put (QObject.OBJECT_TYPE,
                               null);
        this.assetResults.put (ResearchItem.OBJECT_TYPE,
                               null);
        
        this.init ();
        
    }

    public void setText (String t)
    {
        
        this.text.setText (t);
        
        this.search ();
        
    }

    public boolean canClose ()
    {
        
        return true;
        
    }
    
    public String getTitle ()
    {
        
        return "Find";
        
    }
    
    public boolean removeOnClose ()
    {
        
        return false;
        
    }
    
    public void onClose ()
    {
        
    }
    
    public String getIconType ()
    {
        
        return Constants.FIND_ICON_NAME;
        
    }
    
    public List<JButton> getHeaderControls ()
    {
        
        return null;
        
    }
    
    public JComponent getContent ()
    {
        
        Box b = new Box (BoxLayout.Y_AXIS);

        final Finder _this = this;
        
        this.text = UIUtils.createTextField ();
        this.text.setBorder (new CompoundBorder (new EmptyBorder (5, 10, 5, 10),
                                                 this.text.getBorder ()));
        
        this.setMinimumSize (new Dimension (250,
                                            250));

        this.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                            Short.MAX_VALUE));

        this.projectViewer.fireProjectEvent (ProjectEvent.FIND,
                                             ProjectEvent.SHOW);

        KeyAdapter vis = new KeyAdapter ()
        {

            private Timer searchT = new Timer (750,
                                               new ActionAdapter ()
                                               {

                                                  public void actionPerformed (ActionEvent ev)
                                                  {

                                                      _this.search ();

                                                  }

                                                });

            public void keyPressed (KeyEvent ev)
            {

                this.searchT.setRepeats (false);
                this.searchT.stop ();

                // If enter was pressed then search, don't start the timer.
                if (ev.getKeyCode () == KeyEvent.VK_ENTER)
                {
                    
                    _this.search ();
                    return;
                    
                }

                this.searchT.start ();

            }

        };

        this.text.addKeyListener (vis);
        this.text.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                 this.text.getPreferredSize ().height));

        b.add (this.text);

        this.content = new Box (BoxLayout.Y_AXIS);
        this.content.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.content.setOpaque (false);
        this.content.setBorder (new EmptyBorder (0, 0, 0, 0));

        b.add (this.wrapInScrollPane (this.content));

        // This is pretty nasty but can wait until the next version to clean up.
        this.chapterResults = (this.projectViewer instanceof ProjectViewer ? new ChapterResultsBox (this) : new WarmupsResultsBox (this));
        this.chapterResults.setVisible (false);
        this.chapterResults.init ();

        this.noteResults = new NoteResultsBox (this);
        this.noteResults.init ();
        this.noteResults.setVisible (false);

        this.sceneResults = new ChapterItemResultsBox (Scene.OBJECT_TYPE,
                                                       this);
        this.sceneResults.init ();
        this.sceneResults.setVisible (false);

        this.outlineItemResults = new ChapterItemResultsBox (OutlineItem.OBJECT_TYPE,
                                                             this);
        this.outlineItemResults.init ();
        this.outlineItemResults.setVisible (false);

        this.content.add (this.chapterResults);
        this.content.add (this.noteResults);
        this.content.add (this.sceneResults);
        this.content.add (this.outlineItemResults);

        for (String type : this.assetResults.keySet ())
        {
            
            AssetResultsBox ab = this.assetResults.get (type);
            
            if (ab == null)
            {
                
                ab = new AssetResultsBox (type,
                                          this);
                ab.setVisible (false);
                ab.init ();
                
                this.assetResults.put (type,
                                       ab);
                
                this.content.add (ab);
                
            }
            
        }        
        
        return b;        
        
    }
        
    public void init ()
    {

        super.init ();
    
        this.setPreferredSize (new Dimension (300,
                                              500));
                
    }

    public void onShow (String text)
    {
        
        if (text != null)
        {
            
            this.text.setText (text);
            
            this.search ();
            
        }
        
        this.text.grabFocus ();        
        
    }
    
    private void search ()
    {

        this.clearHighlight ();
        
        for (AssetResultsBox b : this.assetResults.values ())
        {
            
            b.clearResults ();
            
        }
        
        this.noteResults.clearResults ();
        
        this.sceneResults.clearResults ();        

        this.outlineItemResults.clearResults ();        
                        
        String t = this.text.getText ().trim ();
        
        if (t.length () == 0)
        {
            
            return;
            
        }
     
        // Get the snippets.
        Map<Chapter, List<Segment>> snippets = UIUtils.getTextSnippets (t,
                                                                        this.projectViewer.getProject ());

        if (snippets.size () > 0)
        {

            this.chapterResults.showResults (snippets);
            
        }

        for (String type : this.assetResults.keySet ())
        {
                    
            Class c = Asset.getAssetClass (type);
                    
            Set<Asset> objs = UIUtils.getAssetsContaining (t,
                                                           c,
                                                           this.projectViewer.getProject ());

            if (objs.size () > 0)
            {                                                           
            
                AssetResultsBox b = this.assetResults.get (type);
                
                b.showResults (objs);

            }

        }
                                                           
        Set<Note> notes = UIUtils.getNotesContaining (t,
                                                      this.projectViewer.getProject ());

        if (notes.size () > 0)
        {                                                      
        
            this.noteResults.showResults (notes);
            
        }
        
        Set<OutlineItem> oitems = UIUtils.getOutlineItemsContaining (t,
                                                                     this.projectViewer.getProject ());

        if (oitems.size () > 0)
        {                                                                     
        
            this.outlineItemResults.showResults (oitems);
            
        }

        Set<Scene> scenes = UIUtils.getScenesContaining (t,
                                                         this.projectViewer.getProject ());

        if (scenes.size () > 0)
        {                                                         
        
            this.sceneResults.showResults (scenes);
            
        }
                
        this.validate ();
        this.repaint ();

    }

    public void clearHighlight ()
    {
        
        if (this.highlightedEditor != null)
        {
            
            this.highlightedEditor.removeHighlight (this.highlightId);

        }
        
    }
/*
    public JButton[] getButtons ()
    {

        final Finder _this = this;

        JButton b = new JButton ("Finish");

        b.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.clearHighlight ();

                    _this.close ();

                }

            });

        JButton[] buts = new JButton[1];
        buts[0] = b;

        return buts;

    }
*/
    public void showSegment (Object  o,
                             Segment s)
    {

        this.clearHighlight ();
                
        if (o instanceof Chapter)
        {

            AbstractEditorPanel aep = null;
        
            Chapter c = (Chapter) o;        
        
            if (this.projectViewer instanceof ProjectViewer)
            {
            
                ProjectViewer pv = (ProjectViewer) this.projectViewer;
        
                aep = pv.getEditorForChapter (c);
        
            }
        
            if (this.projectViewer instanceof WarmupsViewer)
            {
                
                WarmupsViewer wv = (WarmupsViewer) this.projectViewer;
                
                aep = wv.getEditorForWarmup (c);
                
            }
        
            this.projectViewer.viewObject ((DataObject) o);
            
            try
            {
                
                aep.scrollToPosition (s.getBeginIndex ());
                
            } catch (Exception e) {
                
                Environment.logError ("Unable to scroll to: " + s.getBeginIndex (),
                                      e);
                
                return;
                
            }
            /*
            pv.scrollTo (c,
                         s.getBeginIndex ());
        */
            final QTextEditor ed = aep.getEditor ();
                        
            this.highlightId = ed.addHighlight (s.getBeginIndex (),
                                                s.getEndIndex (),
                                                null,
                                                true);
    
            this.highlightedEditor = ed;

        }
        
    }

    public class AssetResultsBox extends ResultsBox
    {
        
        public AssetResultsBox (String objType,
                                Finder finder)
        {
            
            super (objType,
                   finder);
            
        }

        public void showResults (Set<Asset> objs)
        {

            this.setVisible (true);
        
            DefaultMutableTreeNode tn = new DefaultMutableTreeNode (this.finder.projectViewer.getProject ());
    
            UIUtils.createTree (objs,
                                tn);
            
            ((DefaultTreeModel) this.tree.getModel ()).setRoot (tn);
            
            this.count = objs.size ();
            
            this.updateItemCount (this.count);

            this.setContentVisible (false);
                
            this.validate ();
            this.repaint ();
            
        }
        
    }
    
    public class WarmupsResultsBox extends ChapterResultsBox
    {
        
        public WarmupsResultsBox (Finder finder)
        {
            
            super (Warmup.OBJECT_TYPE,
                   finder);
            
        }
        
    }
    
    public class ChapterResultsBox extends ResultsBox
    {
        
        public ChapterResultsBox (String objType,
                                  Finder finder)
        {
            
            super (objType,
                   finder);
            
        }
        
        public ChapterResultsBox (Finder finder)
        {
            
            this (Chapter.OBJECT_TYPE,
                  finder);
            
        }

        public void handleViewObject (TreePath tp,
                                      Object   o)
        {
                   
            if (o instanceof Segment)
            {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                // Get the offset.
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent ();

                Chapter c = (Chapter) parent.getUserObject ();

                Segment s = (Segment) o;

                this.finder.showSegment (c,
                                         s);

            } else {
                
                this.toggleTreePath (tp);
                                
            }
           
        }
        
        public void init (JTree tree)
        {
            
            tree.setCellRenderer (new ChapterSnippetsTreeCellRenderer ());//new MultiLineTreeCellRenderer (this));
            
        }

        public void showResults (Map<Chapter, List<Segment>> snippets)
        {

            this.setVisible (true);
            
            DefaultMutableTreeNode tn = new DefaultMutableTreeNode (this.finder.projectViewer.getProject ());
    
            UIUtils.createTree (snippets,
                                tn);
    
            // Create the tree.
            ((DefaultTreeModel) this.tree.getModel ()).setRoot (tn);
                
            int c = 0;
            
            for (NamedObject n : snippets.keySet ())
            {
                
                c += snippets.get (n).size ();
                
            }
            
            this.count = c;
            
            this.updateItemCount (this.count);

            if (c > 0)
            {
                
                this.setContentVisible (true);
                
            }
    
            this.validate ();
            this.repaint ();
    
        }
        
    }
    
    public abstract class ResultsBox extends ProjectObjectsAccordionItem
    {

        protected Finder finder = null;
        protected int count = 0;
        
        public ResultsBox (String objType,
                           Finder finder)
        {
                                                
            super (objType,
                   finder.projectViewer);
            
            this.finder = finder;
                                        
        }
        
        public boolean showItemCountOnHeader ()
        {
            
            return true;
            
        }
        
        public int getItemCount ()
        {
            
            return this.count;
            
        }

        public void reloadTree (JTree tree)
        {
            
        }        
        
        public void init (JTree tree)
        {
            
            // Let subclasses override for their own behaviour.
            
        }
                
        public void showTreePopupMenu (MouseEvent ev)
        {
            
        }
        
        public TreeCellEditor getTreeCellEditor (AbstractProjectViewer pv,
                                                 JTree                 tree)
        {
            
            return null;
            
        }
        
        public int getViewObjectClickCount (Object d)
        {
            
            return 1;
            
        }
        
        public boolean isTreeEditable ()
        {
            
            return false;
            
        }
        
        public boolean isDragEnabled ()
        {
            
            return false;
            
        }
        
        public DragActionHandler getTreeDragActionHandler (AbstractProjectViewer pv,
                                                           JTree                 tree)
        {
            
            return null;
            
        }
        
        
        public void clearResults ()
        {
            
            ((DefaultTreeModel) this.tree.getModel ()).setRoot (null);
            
            this.setVisible (false);
            
        }

    }

    public class NoteResultsBox extends ResultsBox
    {
        
        public NoteResultsBox (final Finder finder)
        {
                                     
            super (Note.OBJECT_TYPE,
                   finder);

        }

        public void showResults (Set<Note> objs)
        {

            this.setVisible (true);
        
            DefaultMutableTreeNode tn = new DefaultMutableTreeNode (this.finder.projectViewer.getProject ());
    
            UIUtils.createTree (objs,
                                tn);
            
            ((DefaultTreeModel) this.tree.getModel ()).setRoot (tn);
            
            this.count = objs.size ();
            
            this.updateItemCount (this.count);

            if (this.count > 0)
            {
                
                this.setContentVisible (true);
                
            }
    
            this.validate ();
            this.repaint ();
            
        }

    }

    public class ChapterItemResultsBox extends ResultsBox
    {
        
        public ChapterItemResultsBox (String objType,
                                      Finder finder)
        {
                                                
            super (objType,
                   finder);
                
        }
        
        public void showResults (Set<? extends ChapterItem> objs)
        {
            
            this.setVisible (true);
        
            DefaultMutableTreeNode tn = new DefaultMutableTreeNode (this.finder.projectViewer.getProject ());
    
            UIUtils.createTree (objs,
                                tn);
            
            ((DefaultTreeModel) this.tree.getModel ()).setRoot (tn);
            
            this.count = objs.size ();
            
            this.updateItemCount (this.count);

            if (this.count > 0)
            {
                
                this.setContentVisible (true);
                
            }
    
            this.validate ();
            this.repaint ();
            
        }
        
    }

}
