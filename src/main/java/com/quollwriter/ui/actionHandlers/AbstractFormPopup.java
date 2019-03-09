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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.LinkedHashMap;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.gentlyweb.properties.*;

import com.quollwriter.*;

import com.quollwriter.data.*;

import com.quollwriter.ui.*;
import com.quollwriter.ui.forms.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.QTextEditor;
import com.quollwriter.ui.components.PopupAdapter;
import com.quollwriter.ui.components.PopupEvent;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.renderers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public abstract class AbstractFormPopup<E extends AbstractProjectViewer, O extends NamedObject> extends AbstractAction
{

    public static final int ADD = 1;
    public static final int EDIT = 2;

    private QPopup popup = null;
    private JTree                   tree = null;
    private Form                  f = null;
    protected E                     viewer = null;
    protected int                   mode = 0;
    private boolean                 showLinkTo = false;
    protected O                     object = null;
    private PopupsSupported       popupOver = null;
    private Component             showPopupAt = null;
    private String                showPopupAtPosition = null;
    //protected Point                 showPopupAtPoint = null;
    private Object                  highlight = null;
    private ActionListener          onShowAction = null;
    private ActionListener          onHideAction = null;
    private boolean callCancelOnClose = false;

    public AbstractFormPopup (O   d,
                              E   pv,
                              int mode)
    {

        this.mode = mode;
        this.object = d;
        this.viewer = pv;

        final AbstractFormPopup _this = this;

        this.popup = new QPopup (this.getTitle (),
                                 this.getIcon (Constants.ICON_POPUP),
                                 null);

        // TODO: Fix this to allow the escape, currently it will go into a loop.
        this.popup.setAllowRemoveOnEscape (false);

    }

    public AbstractFormPopup (O           d,
                              E           pv,
                              int         mode,
                              boolean     addHideControl)
    {

        this (d,
              pv,
              mode);

        if (addHideControl)
        {

            final AbstractFormPopup _this = this;

            JButton bt = UIUtils.createButton (Environment.getIcon (Constants.CANCEL_ICON_NAME,
                                                                    Constants.ICON_POPUP),
                                               getUIString (actions,clicktoclose),
                                               //"Click to close",
                                               new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    _this.close ();

                }

            });

            List<JComponent> buts = new ArrayList ();
            buts.add (bt);

            this.popup.getHeader ().setControls (UIUtils.createButtonBar (buts));

        }

    }

    @Override
    public void actionPerformed (ActionEvent ev)
    {

        if ((ev.getActionCommand () != null)
            &&
            (ev.getActionCommand ().equals ("link"))
           )
        {

            this.setShowLinkTo (true);

        }

        this.showPopup ();

    }

    public E getViewer ()
    {

        return this.viewer;

    }

    public void setShowLinkTo (boolean v)
    {

        this.showLinkTo = v;

    }

    public void setCallCancelOnClose (boolean v)
    {

        this.callCancelOnClose = v;

    }

    public abstract void handleCancel ();

    public abstract boolean handleSave ();

    public abstract Set<String> getFormErrors ();

    public abstract String getTitle ();

    public abstract Icon getIcon (int iconType);

    public abstract Set<FormItem> getFormItems (String selectedText);

    public abstract JComponent getFocussedField ();

    public ActionListener getSaveAction ()
    {

        final AbstractFormPopup _this = this;

        return new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                _this.save ();

            }

        };

    }

    public void setOnShowAction (ActionListener a)
    {

        this.onShowAction = a;

    }

    public void setOnHideAction (ActionListener a)
    {

        this.onHideAction = a;

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

        final AbstractFormPopup _this = this;

        Set<FormItem> items = new LinkedHashSet ();

        Set<FormItem> _items = this.getFormItems (selectedText);

        if (_items != null)
        {

            items.addAll (_items);

        }

        if (this.object != null)
        {

            this.tree = new JTree ();

            this.tree.setCellRenderer (new SelectableProjectTreeCellRenderer ());

            final JScrollPane treeScroll = UIUtils.createScrollPane (this.tree,
                                                                     150);

            treeScroll.setBorder (null);

            List exclude = new ArrayList ();
            exclude.add (this.object);

            // Painful but just about the only way.
            this.viewer.setLinks (this.object);

            // Get all the "other objects" for the links the note has.
            Iterator<Link> it = this.object.getLinks ().iterator ();

            Set links = new HashSet ();

            while (it.hasNext ())
            {

                links.add (it.next ().getOtherObject (this.object));

            }

            DefaultTreeModel m = new DefaultTreeModel (com.quollwriter.ui.UIUtils.createLinkToTree (this.viewer.getProject (),
                                                                                                    exclude,
                                                                                                    links,
                                                                                                    true));

            this.tree.setModel (m);

            UIUtils.expandPathsForLinkedOtherObjects (this.tree,
                                                      this.object);

            treeScroll.setOpaque (false);

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
                    /*
                    _this.f.getContent ().setPreferredSize (null);

                    _this.f.getContent ().setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                             _this.f.getContent ().getPreferredSize ().height));
*/
                    _this.popup.resize ();

                }

                @Override
                public void treeExpanded (TreeExpansionEvent ev)
                {
                    /*
                    _this.getContent ().setPreferredSize (null);

                    _this.getContent ().setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                          _this.getContent ().getPreferredSize ().height));
                    */
                    _this.popup.resize ();

                }

            });

            this.tree.addMouseListener (new MouseAdapter ()
                {

                    private void selectAllChildren (DefaultTreeModel       model,
                                                    DefaultMutableTreeNode n,
                                                    boolean                v)
                    {

                        Enumeration<TreeNode> en = n.children ();

                        while (en.hasMoreElements ())
                        {

                            DefaultMutableTreeNode c = (DefaultMutableTreeNode) en.nextElement ();

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

            final String hideLabel = getUIString (linkedto, LanguageStrings.tree,clicktohide);
            //"Click to hide the item tree";
            final String showLabel = getUIString (linkedto, LanguageStrings.tree,clicktoview);
            //"Click to link to other items";

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

            items.add (new AnyFormItem (null,
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
/*
                        _this.getContent ().setPreferredSize (null);

                        _this.getContent ().setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                              _this.getContent ().getPreferredSize ().height));
*/
                        _this.popup.resize ();

                    }

                });

        }

        Map<Form.Button, ActionListener> buttons = new LinkedHashMap ();

        buttons.put (Form.Button.save,
                     new ActionListener ()
                     {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.save ();

                        }

                     });

        buttons.put (Form.Button.cancel,
                     new ActionListener ()
                     {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            _this.cancel ();

                        }

                     });

        this.f = UIUtils.createForm (items,
                                     buttons);

        this.popup.setContent (this.f);

        this.f.setBorder (UIUtils.createPadding (10, 10, 10, 10));

        this.popup.getContent ().setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                                   this.popup.getContent ().getPreferredSize ().height));

    }

    public Point getShowAtPosition ()
    {

        Point p = null;

        int ph = 0;
        int pw = 0;

        if (this.popupOver == null)
        {

            this.popupOver = this.viewer;

        }

        p = UIUtils.getCenterShowPosition ((Component) this.popupOver,
                                           this.popup);

        return p;

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

                QuollPanel qp = this.viewer.getCurrentlyVisibleTab ();

                if (qp instanceof AbstractEditorPanel)
                {

                    AbstractEditorPanel qep = (AbstractEditorPanel) qp;

                    editor = qep.getEditor ();

                }

            }

        }

        return editor;

    }

    public void hidePopup ()
    {

        this.popup.setVisible (false);

    }

    public void showPopup ()
    {

        this.showPopup (this.getShowAtPosition ());

    }

    public void showPopup (Point showAt)
    {

        if (this.popup.isShowing ())
        {

            return;

        }

        if (this.popupOver == null)
        {

            // Get the currently displayed panel.
            this.popupOver = this.viewer;

        }

        final AbstractFormPopup _this = this;

        QTextEditor editor = this.getEditor ();

        this.initForm (((editor != null) ? editor.getSelectedText () : null));

        if ((this.onShowAction != null)
            ||
            (this.onHideAction != null)
           )
        {

            this.popup.addPopupListener (new PopupAdapter ()
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
        /*
        this.getContent ().setPreferredSize (null);
        this.getContent ().setPreferredSize (new Dimension (UIUtils.getPopupWidth (),
                                             this.getContent ().getPreferredSize ().height));
*/

        this.popup.setDraggable ((Component) this.popupOver);

        this.popupOver.showPopupAt (this.popup,
                                    showAt,
                                    false);

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

        UIUtils.doLater (new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                JComponent c = _this.getFocussedField ();

                if (c != null)
                {

                    c.grabFocus ();

                }

            }

        });

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

    private void removeHighlight ()
    {

        QTextEditor editor = this.getEditor ();

        if (editor != null)
        {

            editor.removeAllHighlights (null);

        }

    }

    public void close ()
    {

        if (this.callCancelOnClose)
        {

            this.cancel ();

            return;

        }

        this.removeHighlight ();

        this.hidePopup ();

        this.popup.removeFromParent ();

    }

    public void cancel ()
    {

        this.handleCancel ();

        this.removeHighlight ();

        this.hidePopup ();

        this.popup.removeFromParent ();

    }

    public void save ()
    {

        try
        {

            Set<String> errs = this.getFormErrors ();

            if ((errs != null)
                &&
                (errs.size () > 0)
               )
            {

                this.f.showErrors (errs);

                this.popup.resize ();

                return;

            }

            if (this.handleSave ())
            {

                if (this.object != null)
                {

                    // Get all the link items from the tree.
                    DefaultTreeModel dtm = (DefaultTreeModel) this.tree.getModel ();

                    Set<Link> s = new HashSet ();

                    try
                    {

                        this.getSelectedObjects ((DefaultMutableTreeNode) dtm.getRoot (),
                                                 this.object,
                                                 s);

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to get objects to link to for: " +
                                              this.object,
                                              e);

                        UIUtils.showErrorMessage (this.viewer,
                                                  getUIString (linkedto,save,actionerror));
                                                  //"An internal error has occurred.\n\nUnable to add/edit object.");

                        this.removeHighlight ();

                        return;

                    }

                    // Save the links
                    try
                    {

                        this.viewer.saveLinks (this.object,
                                               s);

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to save links for: " +
                                              this.object,
                                              e);

                        UIUtils.showErrorMessage (this.viewer,
                                                  getUIString (linkedto,save,actionerror));
                                                  //"An internal error has occurred.\n\nUnable to save links.");

                        this.removeHighlight ();

                        return;

                    }

                    // Tell any quollpanel viewing the associated object to refresh.
                    this.viewer.refreshViewPanel (this.object);

                }

                this.f = null;

                this.removeHighlight ();

                this.hidePopup ();

                this.popup.removeFromParent ();

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
                                 this.object,
                                 s);

        return s;

    }

    private void getSelectedObjects (DefaultMutableTreeNode n,
                                     NamedObject            addTo,
                                     Set                    s)
                              throws GeneralException
    {

        Enumeration<TreeNode> en = n.children ();

        while (en.hasMoreElements ())
        {

            DefaultMutableTreeNode nn = (DefaultMutableTreeNode) en.nextElement ();

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
