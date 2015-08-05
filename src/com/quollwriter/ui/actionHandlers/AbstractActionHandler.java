package com.quollwriter.ui.actionHandlers;

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.Form;
import com.quollwriter.ui.components.FormAdapter;
import com.quollwriter.ui.components.FormEvent;
import com.quollwriter.ui.components.FormItem;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.PopupAdapter;
import com.quollwriter.ui.components.PopupEvent;
import com.quollwriter.ui.renderers.*;


public abstract class AbstractActionHandler extends FormAdapter
{

    public static final int ADD = 1;
    public static final int EDIT = 2;

    private JTree                   tree = null;
    protected Form                  f = null;
    protected AbstractProjectViewer projectViewer = null;
    protected int                   mode = 0;
    private boolean                 showLinkTo = false;
    protected NamedObject           dataObject = null;
    protected PopupsSupported       popupOver = null;
    protected Component             showPopupAt = null;
    protected String                showPopupAtPosition = null;
    protected Point                 showPopupAtPoint = null;
    private boolean                 addHideControl = false;
    private Object                  highlight = null;
    private ActionListener          onShowAction = null;
    private ActionListener          onHideAction = null;

    public AbstractActionHandler(NamedObject           d,
                                 AbstractProjectViewer pv,
                                 int                   mode)
    {

        this.mode = mode;
        this.dataObject = d;
        this.projectViewer = pv;

    }

    public AbstractActionHandler(NamedObject           d,
                                 AbstractProjectViewer pv,
                                 int                   mode,
                                 boolean               addHideControl)
    {

        this (d,
              pv,
              mode);

        this.addHideControl = addHideControl;

    }

    public void setShowLinkTo (boolean v)
    {

        this.showLinkTo = v;

    }

    public abstract void handleCancel (int mode);

    public abstract boolean handleSave (int mode);

    public abstract String getTitle (int mode);

    public abstract String getIcon (int mode);

    public abstract List<FormItem> getFormItems (int         mode,
                                                 String      selectedText,
                                                 NamedObject obj);

    public abstract JComponent getFocussedField ();

    public abstract int getShowAtPosition ();

    public void setOnShowAction (ActionListener a)
    {
        
        this.onShowAction = a;
        
    }
    
    public void setOnHideAction (ActionListener a)
    {
        
        this.onHideAction = a;
        
    }

    public void setShowPopupAtPoint (Point  p,
                                     String position)
    {

        this.showPopupAtPoint = p;
        this.showPopupAtPosition = position;

    }

    public void setShowPopupAt (Component c,
                                String    position)
    {

        this.showPopupAt = c;
        this.showPopupAtPosition = position;

    }

    public void setPopupOver (PopupsSupported p)
    {

        this.popupOver = p;

    }

    private void initForm (String selectedText)
    {

        List<FormItem> items = new ArrayList ();

        items.addAll (this.getFormItems (this.mode,
                                         selectedText,
                                         this.dataObject));

        if (this.dataObject != null)
        {

            final AbstractActionHandler _this = this;
        
            this.tree = new JTree ();

            this.tree.setCellRenderer (new SelectableProjectTreeCellRenderer ());

            final JScrollPane treeScroll = new JScrollPane (this.tree)
            {
            
                public Dimension getPreferredSize ()
                {
                    
                    Dimension d = _this.tree.getPreferredSize ();
                                        
                    if (d.height > 150)
                    {
                        
                        d.height = 150;
                        
                    }
                                                           
                    return d;
                    
                }
                
            };
            
            treeScroll.setBorder (null);
            
            List exclude = new ArrayList ();
            exclude.add (this.dataObject);

            // Painful but just about the only way.
            this.projectViewer.setLinks (this.dataObject);

            // Get all the "other objects" for the links the note has.
            Iterator<Link> it = this.dataObject.getLinks ().iterator ();

            Set links = new HashSet ();

            while (it.hasNext ())
            {

                links.add (it.next ().getOtherObject (this.dataObject));

            }

            DefaultTreeModel m = new DefaultTreeModel (com.quollwriter.ui.UIUtils.createLinkToTree (this.projectViewer.getProject (),
                                                                                                    exclude,
                                                                                                    links,
                                                                                                    true));

            this.tree.setModel (m);

            UIUtils.expandPathsForLinkedOtherObjects (this.tree,
                                                      this.dataObject);

            //this.tree.setModel (null);
            /*
            treeScroll.setBorder (new CompoundBorder (treeScroll.getBorder (),
                                                      new MatteBorder (3,
                                                                       3,
                                                                       3,
                                                                       3,
                                                                       this.tree.getBackground ())));
                                                                       */
            treeScroll.setOpaque (false);
            /*
            treeScroll.setPreferredSize (new Dimension (treeScroll.getPreferredSize ().width,
                                                        150));
              */                                          
            this.tree.setOpaque (true);
            this.tree.setBorder (null);
            treeScroll.setOpaque (true);

            this.tree.setRootVisible (false);
            this.tree.setShowsRootHandles (true);
            this.tree.setScrollsOnExpand (true);

            // Never toggle.
            this.tree.setToggleClickCount (-1);
            treeScroll.setVisible (this.showLinkTo);
            treeScroll.setAlignmentX (Component.LEFT_ALIGNMENT);

            final JTree tr = this.tree;

            this.tree.addTreeExpansionListener (new TreeExpansionListener ()
            {

                @Override
                public void treeCollapsed (TreeExpansionEvent ev)
                {
                    
                    _this.f.getContent ().setPreferredSize (null);
                    
                    _this.f.getContent ().setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                             _this.f.getContent ().getPreferredSize ().height));

                    _this.showPopup (true);
                    
                }
            
                @Override
                public void treeExpanded (TreeExpansionEvent ev)
                {
                    
                    _this.f.getContent ().setPreferredSize (null);
                    
                    _this.f.getContent ().setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                             _this.f.getContent ().getPreferredSize ().height));
                    
                    _this.showPopup (true);
                    
                }

            });
            
            this.tree.addMouseListener (new MouseAdapter ()
                {

                    private void selectAllChildren (DefaultTreeModel       model,
                                                    DefaultMutableTreeNode n,
                                                    boolean                v)
                    {

                        Enumeration<DefaultMutableTreeNode> en = n.children ();

                        while (en.hasMoreElements ())
                        {

                            DefaultMutableTreeNode c = en.nextElement ();

                            SelectableDataObject s = (SelectableDataObject) c.getUserObject ();

                            s.selected = v;

                            // Tell the model that something has changed.
                            model.nodeChanged (c);

                            // Iterate.
                            this.selectAllChildren (model,
                                                    c,
                                                    v);

                        }

                    }

                    public void mousePressed (MouseEvent ev)
                    {

                        TreePath tp = tr.getPathForLocation (ev.getX (),
                                                             ev.getY ());

                        if (tp != null)
                        {

                            DefaultMutableTreeNode n = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                            // Tell the model that something has changed.
                            DefaultTreeModel model = (DefaultTreeModel) tr.getModel ();

                            SelectableDataObject s = (SelectableDataObject) n.getUserObject ();

                            /*
                                        if ((ev.getClickCount () == 2)
                                            &&
                                            (n.getChildCount () > 0)
                                           )
                                        {

                                            this.selectAllChildren (model,
                                                                    n,
                                                                    s.selected);

                                        } else {

                                            s.selected = !s.selected;

                                        }
                             */
                            s.selected = !s.selected;

                            model.nodeChanged (n);
                            
                        }

                    }

                });

            this.tree.putClientProperty (com.jgoodies.looks.Options.TREE_LINE_STYLE_KEY,
                                         com.jgoodies.looks.Options.TREE_LINE_STYLE_NONE_VALUE);

            this.tree.putClientProperty ("Tree.paintLines",
                                         Boolean.FALSE);

            final Box                   linkToP = new Box (BoxLayout.Y_AXIS);

            final String hideLabel = "Click to hide the item tree";
            final String showLabel = "Click to link to other items";
            
            final JLabel linkToLabel = UIUtils.createClickableLabel ((this.showLinkTo ? hideLabel : showLabel),
                                                                     Environment.getIcon (Link.OBJECT_TYPE,
                                                                                          Constants.ICON_MENU));
            linkToLabel.setBorder (new EmptyBorder (0,
                                                    0,
                                                    2,
                                                    0));

            linkToP.add (linkToLabel);
            linkToP.add (treeScroll);

            linkToLabel.setAlignmentX (Component.LEFT_ALIGNMENT);

            items.add (new FormItem ("",
                                     linkToP));

            linkToLabel.addMouseListener (new MouseAdapter ()
                {

                    public void mousePressed (MouseEvent ev)
                    {

                        Form form = _this.f;

                        treeScroll.setVisible (!treeScroll.isVisible ());

                        if (treeScroll.isVisible ())
                        {

                            linkToLabel.setText (hideLabel);
                            linkToLabel.setIcon (Environment.getIcon (Link.OBJECT_TYPE,
                                                                      Constants.ICON_MENU));

                        } else
                        {

                            linkToLabel.setText (showLabel);
                            linkToLabel.setIcon (Environment.getIcon (Link.OBJECT_TYPE,
                                                                      Constants.ICON_MENU));


                        }

                        _this.f.getContent ().setPreferredSize (null);
                        
                        _this.f.getContent ().setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                                 _this.f.getContent ().getPreferredSize ().height));

                        _this.showPopup (true);

                    }

                });

        }

        String title = this.getTitle (this.mode);

        title = Environment.replaceObjectNames (title);
        
        String icon = null;

        icon = this.getIcon (this.mode);

        /*
            if (this.dataObject == null)
            {

                icon = this.getIcon (this.mode);

            } else {

                icon = this.dataObject.getObjectType ();

            }
         */
        this.f = new Form (title,
                           Environment.getIcon (icon,
                                                Constants.ICON_POPUP),
                           items,
                           this.projectViewer,
                           Form.SAVE_CANCEL_BUTTONS,
                           this.addHideControl);

        this.f.getContent ().setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                this.f.getContent ().getPreferredSize ().height));
                           
        this.f.addFormListener (this);

    }

    private void showPopup (boolean showOnly)
    {

        Point p = null;

        if ((this.popupOver instanceof ChapterItemViewer) &&
            (this.dataObject instanceof ChapterItem))
        {

            ChapterItemViewer qep = (ChapterItemViewer) this.popupOver;

            int y = 0;

            Point lastMousePosition = qep.getLastMousePosition ();
            
            int at = this.getShowAtPosition ();

            QTextEditor editor = qep.getEditor ();

            if (at == -1)
            {

                // Calculate where it should be displayed.
                String sel = editor.getSelectedText ();

                if ((sel != null) &&
                    (!sel.trim ().equals ("")))
                {

                    // We have some text so use "at"...
                    at = editor.getSelectionStart ();

                } else
                {

                    int c = editor.getCaret ().getDot ();

                    if (c >= 0)
                    {

                        at = c;

                    } else
                    {

                        if (lastMousePosition != null)
                        {

                            at = editor.viewToModel (new Point (lastMousePosition.x,
                                                                lastMousePosition.y));

                        } else
                        {

                            at = editor.getText ().length () - 1;

                        }

                    }

                }

            }

            // See if the data object is a positionable object.
            if (this.dataObject instanceof ChapterItem)
            {

                ((ChapterItem) this.dataObject).setPosition (at);

            }

            Rectangle r = null;

            try
            {

                r = editor.modelToView (at);

            } catch (Exception e)
            {

                Environment.logError ("Position: " +
                                      at +
                                      " is not valid.",
                                      e);

                return;

            }

            JScrollPane scrollPane = qep.getScrollPane ();

            y = r.y + 22 - scrollPane.getVerticalScrollBar ().getValue ();

            if ((y < 0) ||
                (y > (scrollPane.getViewport ().getViewRect ().height + scrollPane.getVerticalScrollBar ().getValue ())))
            {

                // Recalculate y since we have moved the scroll position.
                y = r.y + 22; // - scrollPane.getVerticalScrollBar ().getValue ();

            }

            // Adjust the bounds so that the form is fully visible.
            if ((y + this.f.getPreferredSize ().height) > (scrollPane.getViewport ().getViewRect ().height + scrollPane.getVerticalScrollBar ().getValue ()))
            {

                y = y - 22 - this.f.getPreferredSize ().height;

            }

            y -= this.f.getPreferredSize ().height;
            
            int xOffset = 36;

            if (this.dataObject instanceof OutlineItem)
            {

                xOffset = 22;

            }
            
            p = new Point (qep.getIconColumn ().getWidth () - xOffset,
                           y);

            /*
            this.f.setBounds (qep.getIconColumn ().getWidth () - yOffset,
                  y,
                  this.f.getPreferredSize ().width,
                  this.f.getPreferredSize ().height);
             */
            if ((this.mode == AbstractActionHandler.ADD) &&
                (this.dataObject instanceof ChapterItem) &&
                (!showOnly))
            {

                try
                {

                    qep.addItem ((ChapterItem) this.dataObject);

                } catch (Exception e)
                {

                    Environment.logError ("Unable to add item: " +
                                          this.dataObject +
                                          " to editor panel",
                                          e);

                }

            }

        } else
        {

            int ph = 0;
            int pw = 0;

            if (this.showPopupAt == null)
            {

                //p = ((Container) this.popupOver).getMousePosition (true);
                
                if (p == null)
                {
                    
                    // Show in center.
                    p = UIUtils.getCenterShowPosition ((Component) this.popupOver,
                                                       this.f);
                    
                }
                /*
                if (this.popupOver instanceof JComponent)
                {

                    JComponent so = (JComponent) this.popupOver;
    
                    Insets ins = so.getInsets ();
    
                    p.x -= ins.left;
                    p.y -= ins.top;

                }
*/
            } else
            {

                p = this.showPopupAt.getLocation ();
                
                p = SwingUtilities.convertPoint (this.showPopupAt,
                                                 0,
                                                 0,
                                                 (Component) this.popupOver);

                if (this.popupOver instanceof JComponent)
                {
                    
                    JComponent so = (JComponent) this.popupOver;
    
                    Insets ins = so.getInsets ();
    
                    p.x -= ins.left;
                    p.y -= ins.top;

                }

                ph = this.showPopupAt.getHeight ();
                pw = this.showPopupAt.getWidth ();

                // Was end of block here.
                if (this.showPopupAtPosition == null)
                {
    
                    this.showPopupAtPosition = "above";
    
                }
    
                if (this.showPopupAtPoint != null)
                {
    
                    p = this.showPopupAtPoint;
    
                }

                if (this.showPopupAtPosition != null)
                {
    
                    if (this.showPopupAtPosition.equals ("above"))
                    {
    
                        p.y = p.y - this.f.getPreferredSize ().height - ph;
    
                    }
    
                    if (this.showPopupAtPosition.equals ("below"))
                    {
    
                        p.y = p.y + ph;
    
                    }
    
                }

            }
                
        }

        if (this.f.isShowing ())
        {
            
            p = this.f.getLocation ();
            
        }
        
        this.popupOver.showPopupAt (this.f,
                                    p,
                                    false);
/*
        if (this.onShowAction != null)
        {

            this.onShowAction.actionPerformed (new ActionEvent (this,
                                                                1,
                                                                "onShow"));
                                    
        }
        */
    }

    private QTextEditor getEditor ()
    {

        QTextEditor editor = null;

        if (this.popupOver instanceof AbstractEditorPanel)
        {

            AbstractEditorPanel qep = (AbstractEditorPanel) this.popupOver;

            editor = qep.getEditor ();

        } else {

            if (this.popupOver instanceof AbstractProjectViewer)
            {
    
                QuollPanel qp = this.projectViewer.getCurrentlyVisibleTab ();
    
                if (qp instanceof AbstractEditorPanel)
                {
    
                    AbstractEditorPanel qep = (AbstractEditorPanel) qp;
    
                    editor = qep.getEditor ();
    
                }
    
            } 

        }

        return editor;

    }

    public void actionPerformed (ActionEvent ev)
    {

        if ((this.f != null) &&
            (this.f.isVisible ()))
        {

            return;

        }

        if (this.popupOver == null)
        {

            // Get the currently displayed panel.
            this.popupOver = this.projectViewer;

        }

        final AbstractActionHandler _this = this;
        
        QTextEditor editor = this.getEditor ();

        this.initForm (((editor != null) ? editor.getSelectedText () : null));

        if ((this.onShowAction != null)
            ||
            (this.onHideAction != null)
           )
        {

            this.f.addPopupListener (new PopupAdapter ()
            {
             
                public void popupShown (PopupEvent ev)
                {
                    
                    if (_this.onShowAction != null)
                    {
                        
                        _this.onShowAction.actionPerformed (new ActionEvent (_this,
                                                                             1,
                                                                             "onShow"));
                        
                    }
                    
                }
                
                public void popupHidden (PopupEvent ev)
                {

                    if (_this.onHideAction != null)
                    {
                        
                        _this.onHideAction.actionPerformed (new ActionEvent (_this,
                                                                             1,
                                                                             "onHide"));
                        
                    }
                    
                }

            });
            
        }
        
        this.f.getContent ().setPreferredSize (null);        
        this.f.getContent ().setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                this.f.getContent ().getPreferredSize ().height));

        this.showPopup (false);

        int s = -1;
        int e = -1;

        if (editor != null)
        {

            s = editor.getSelectionStart ();
            e = editor.getSelectionEnd ();

            if (s >= e)
            {

                s = -1;
                e = -1;

            }

        }

        this.getFocussedField ().grabFocus ();

        if (editor != null)
        {
/*
            editor.setSelectionStart (s);
            
            if (e > s)
            {
                
                editor.setSelectionEnd (e);

            }                        
  */      
            if (e > s)
            {

                try
                {

                    this.highlight = editor.addHighlight (s,
                                                          e,
                                                          null,
                                                          false);

                } catch (Exception ex)
                {

                }

            }

        }

    }

    public void submitForm ()
    {

        FormEvent ev = new FormEvent (this.f,
                                      FormEvent.SAVE,
                                      FormEvent.SAVE_ACTION_NAME);

        this.actionPerformed (ev);

    }

    private void removeHighlight ()
    {

        QTextEditor editor = this.getEditor ();

        if (editor != null)
        {

            editor.removeAllHighlights (null);

        }

    }

    public void actionPerformed (FormEvent ev)
    {

        try
        {

            if (ev.getID () == FormEvent.CANCEL)
            {
    
                this.handleCancel (this.mode);
    
                this.removeHighlight ();
    
                return;
    
            }

            if (this.handleSave (this.mode))
            {
    
                if (this.dataObject != null)
                {
    
                    // Get all the link items from the tree.
                    DefaultTreeModel dtm = (DefaultTreeModel) this.tree.getModel ();
    
                    Set s = new HashSet ();
    
                    try
                    {
    
                        this.getSelectedObjects ((DefaultMutableTreeNode) dtm.getRoot (),
                                                 this.dataObject,
                                                 s);
    
                    } catch (Exception e)
                    {
    
                        Environment.logError ("Unable to get objects to link to for: " +
                                              this.dataObject,
                                              e);
    
                        UIUtils.showErrorMessage (this.projectViewer,
                                                  "An internal error has occurred.\n\nUnable to add/edit object.");
    
                        this.removeHighlight ();
    
                        return;
    
                    }
    
                    // Save the links
                    try
                    {
    
                        this.projectViewer.saveLinks (this.dataObject,
                                                      s);
    
                    } catch (Exception e)
                    {
    
                        Environment.logError ("Unable to save links for: " +
                                              this.dataObject,
                                              e);
    
                        UIUtils.showErrorMessage (this.projectViewer,
                                                  "An internal error has occurred.\n\nUnable to save links.");
    
                        this.removeHighlight ();
    
                        return;
    
                    }
    
                    // Tell any quollpanel viewing the associated object to refresh.
                    this.projectViewer.refreshViewPanel (this.dataObject);
    
                }
    
                this.f.hideForm ();
    
                this.f = null;
    
                this.removeHighlight ();
    
            }

        } catch (Exception e) {
            
            Environment.logError ("Unable to perform action",
                                  e);
            
        }

    }

    public Set getSelectedLinks ()
                          throws GeneralException
    {

        // Get all the link items from the tree.
        DefaultTreeModel dtm = (DefaultTreeModel) this.tree.getModel ();

        Set s = new HashSet ();

        this.getSelectedObjects ((DefaultMutableTreeNode) dtm.getRoot (),
                                 this.dataObject,
                                 s);

        return s;

    }

    private void getSelectedObjects (DefaultMutableTreeNode n,
                                     NamedObject            addTo,
                                     Set                    s)
                              throws GeneralException
    {

        Enumeration<DefaultMutableTreeNode> en = n.children ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode nn = en.nextElement ();

            SelectableDataObject sd = (SelectableDataObject) nn.getUserObject ();

            if (sd.obj == null)
            {
                
                throw new GeneralException ("Unable to get user object for tree node: " + nn);
                
            }
            
            if (sd.selected)
            {

                s.add (new Link (addTo,
                                 sd.obj));

            }

            this.getSelectedObjects (nn,
                                     addTo,
                                     s);

        }

    }

}
