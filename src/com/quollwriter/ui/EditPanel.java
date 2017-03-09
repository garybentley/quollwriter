package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import java.util.ArrayList;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.IconProvider;
import com.quollwriter.ui.components.GradientPainter;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.ScrollableBox;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.ui.forms.*;

public abstract class EditPanel extends Box
{

    public static final int EDIT_VISIBLE = 0;
    public static final int SAVED = 1;
    public static final int CANCELLED = 2;
    public static final int VIEW_VISIBLE = 3;
/*
    public Color headerBorderColor = new Color (171,
                                                171,
                                                171);
*/
    private JComponent                     cards = null;
    protected JComponent                   editPanel = null;
    private JLabel editError = null;
    protected JComponent                   viewPanel = null;
    protected Header                       header = null;
    private Box                            panel = null;
    private JComponent                     visiblePanel = null;
    private boolean                        contentVisible = true;
    private boolean                        inited = false;
    private java.util.List<ActionListener> listeners = new ArrayList ();

    public EditPanel ()
    {

        super (BoxLayout.Y_AXIS);

    }

    protected void fireActionEvent (int    id,
                                    String event)
    {

        ActionEvent ev = new ActionEvent (this,
                                          id,
                                          event);

        for (ActionListener l : this.listeners)
        {

            l.actionPerformed (ev);

        }

    }

    public boolean isEditing ()
    {
        
        if (this.editPanel == null)
        {
            
            return false;
            
        }
        
        return this.editPanel.isShowing ();
        
    }
    
    public void removeActionListener (ActionListener l)
    {

        this.listeners.remove (l);

    }

    public void addActionListener (ActionListener l)
    {

        this.listeners.add (l);

    }

    public void init ()
    {

        if (this.inited)
        {

            return;

        }

        final EditPanel _this = this;

        IconProvider ip = this.getIconProvider ();

        this.panel = new Box (BoxLayout.Y_AXIS);
        this.panel.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.panel.setOpaque (false);
        this.add (this.panel);
/*
 Needs work... set size when reopens (store old size, see accordion) also allow for drag open like accordion
        if (allowSingleClickContentClose)
        {

            this.header.setCursor (Cursor.getPredefinedCursor (Cursor.HAND_CURSOR));
            this.header.setToolTipText ("Click to close the section");

            this.header.addMouseListener (new MouseAdapter ()
            {

                public void mousePressed (MouseEvent eve)
                {

                    _this.contentVisible = !_this.contentVisible;

                    if (_this.visiblePanel != null)
                    {

                        _this.visiblePanel.setVisible (_this.contentVisible);

                        if (_this.getParent () instanceof JSplitPane)
                        {

                            JSplitPane sp = ((JSplitPane) _this.getParent ());

                            sp.setDividerLocation (sp.getSize ().height - _this.getPreferredSize ().height - _this.getInsets ().top - _this.getInsets ().bottom);

                        }

                    }

                }

            });

        }
*/
        this.setOpaque (true);
        this.setBackground (UIUtils.getComponentColor ());
        this.setBorder (null);

        this.panel.setOpaque (true);

        this.cards = new JPanel (new CardLayout ());
        this.cards.setOpaque (false);
        this.panel.add (this.cards);
        
        this.showViewPanel ();
        /*
        this.viewPanel = new Box (BoxLayout.Y_AXIS);
        
        this.viewEdit.add (UIUtils.createScrollPane (this.viewPanel,
                                                     "view"));
        
        this.viewPanel.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.viewPanel.setBorder (UIUtils.createPadding (5, 5, 5, 5));
        this.viewPanel.setMinimumSize (new Dimension (100,
                                                      100));

        JComponent view = null;
        
        // See if there are view items, if so create a form, otherwise
        // call "getEditPanel".
        Set<FormItem> viewItems = this.getViewItems ();

        if ((viewItems != null) &&
            (viewItems.size () > 0))
        {

            view = this.buildPanel (viewItems,
                                    ip,
                                    false,
                                    false);

        } else
        {

            view = this.getViewPanel ();

        }

        if (view != null)
        {

            view.setBackground (UIUtils.getComponentColor ());

            this.viewPanel.add (view);
            this.visiblePanel = view;

            this.fireActionEvent (EditPanel.VIEW_VISIBLE,
                                  "view-visible");

        } 

        this.editPanel = new Box (BoxLayout.Y_AXIS);

        this.viewEdit.add (UIUtils.createScrollPane (this.editPanel,
                                                     "edit"));
        
        this.editPanel.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.editPanel.setBorder (UIUtils.createPadding (5, 5, 5, 5));
        this.editPanel.setMinimumSize (new Dimension (100,
                                                      100));
        
        // See if there are edit items, if so create a form, otherwise
        // call "getEditPanel".
        Set<FormItem> editItems = this.getEditItems ();

        if ((editItems != null) &&
            (editItems.size () > 0))
        {

            this.editPanel = new Form (Form.Layout.stacked,
                                       editItems,
                                       null);

        } else
        {

            this.editPanel = this.getEditPanel ();

            if (this.editPanel != null)
            {

                this.editPanel.setMinimumSize (new Dimension (100,
                                                              100));

                this.editPanel = new JScrollPane (this.editPanel);
                //this.editPanel.setBorder (null);
                this.editPanel.setBorder (UIUtils.createPadding (5, 5, 5, 5));                

            }

        }

        if (this.editPanel != null)
        {

            Box epb = new Box (BoxLayout.Y_AXIS);

            this.editError = UIUtils.createErrorLabel ("Please enter a value.");
            this.editError.setVisible (false);

            this.editError.setBorder (UIUtils.createPadding (5, 0, 5, 5));

            this.editPanel.setOpaque (false);
            this.editPanel.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                          Short.MAX_VALUE));
            this.editPanel.setPreferredSize (new Dimension (Short.MAX_VALUE,
                                                          Short.MAX_VALUE));

            this.editPanel.setAlignmentX (Component.LEFT_ALIGNMENT);

            final String ht = this.getHelpText ();

            if (ht != null)
            {

                FormLayout fl = new FormLayout ("p, 3px, fill:90px:grow",
                                                "top:p");

                PanelBuilder pb = new PanelBuilder (fl);

                CellConstraints cc = new CellConstraints ();

                ImagePanel helpII = new ImagePanel (ip.getIcon ("help",
                                                                Constants.ICON_PANEL_SECTION_ACTION),
                                                    null);
                helpII.setAlignmentX (Component.LEFT_ALIGNMENT);

                pb.add (helpII,
                        cc.xy (1,
                               1));

                pb.add (UIUtils.createHelpTextPane (ht,
                                                    Environment.getFocusedViewer ()),
                        cc.xy (3,
                               1));

                this.helpBox = pb.getPanel ();
                this.helpBox.setOpaque (false);
                this.helpBox.setVisible (false);

                this.helpBox.setAlignmentX (Component.LEFT_ALIGNMENT);

                this.helpBox.setBorder (UIUtils.createPadding (0, 0, 5, 0));

                epb.add (this.helpBox);

            }

            epb.add (this.editError);

            epb.add (this.editPanel);

            epb.add (Box.createVerticalGlue ());

            this.editPanel = epb;

            this.editPanel.setBorder (UIUtils.createPadding (5, 5, 5, 5));

            this.panel.add (this.editPanel);

            this.editPanel.setOpaque (false);

            this.editPanel.setVisible (false);
            this.editPanel.setAlignmentX (Component.LEFT_ALIGNMENT);

            java.util.List<JComponent> buttons = new ArrayList ();

            this.edit = UIUtils.createButton (ip.getIcon (Constants.EDIT_ICON_NAME,
                                                          Constants.ICON_PANEL_SECTION_ACTION),
                                              "Click to edit this section",
                                              null);

            buttons.add (this.edit);

            this.save = UIUtils.createButton (ip.getIcon (Constants.SAVE_ICON_NAME,
                                                          Constants.ICON_PANEL_SECTION_ACTION),
                                              "Click to edit this section",
                                              null);

            buttons.add (this.save);
            this.save.setVisible (false);
            
            this.cancel = UIUtils.createButton (ip.getIcon ("cancel",
                                                            Constants.ICON_PANEL_SECTION_ACTION),
                                               "Click to cancel editing",
                                               null);
            this.cancel.setVisible (false);

            buttons.add (this.cancel);

            if (ht != null)
            {

                this.help = UIUtils.createButton (ip.getIcon ("help",
                                                              Constants.ICON_PANEL_SECTION_ACTION),
                                                   "Click to view/hide the help for this section",
                                                   null);
                this.help.setVisible (false);

                buttons.add (this.help);

                this.help.addMouseListener (new MouseEventHandler ()
                {

                    @Override
                    public void handlePress (MouseEvent ev)
                    {

                        _this.helpBox.setVisible (!_this.helpBox.isVisible ());

                        _this.validate ();
                        _this.repaint ();

                    }

                });

            }

            this.header.setControls (UIUtils.createButtonBar (buttons));

            this.edit.addMouseListener (new MouseEventHandler ()
            {

                @Override
                public void handlePress (MouseEvent ev)
                {

                    _this.doEdit ();
                                
                }

            });

            this.save.addMouseListener (new MouseEventHandler ()
            {

                @Override
                public void handlePress (MouseEvent ev)
                {

                    _this.doSave ();
                                
                }

            });

            this.cancel.addMouseListener (new MouseEventHandler ()
            {

                @Override
                public void handlePress (MouseEvent ev)
                {

                    _this.doCancel ();

                }

            });

            if (this.viewPanel == null)
            {

                this.editPanel.setVisible (true);
                this.visiblePanel = this.editPanel;

                this.fireActionEvent (EditPanel.EDIT_VISIBLE,
                                      "edit-visible");

            }

        }
*/
        this.inited = true;

    }
    
    public void showEditPanel ()
    {
        
        this.showEdit ();
        
    }
    
    public void showViewPanel ()
    {
        
        final EditPanel _this = this;
        
        if (this.viewPanel != null)
        {
            
            this.viewPanel.removeAll ();
            
        } else {
            
            this.viewPanel = new Box (BoxLayout.Y_AXIS);
                        
            this.cards.add (this.viewPanel,
                            "view");
            
            this.viewPanel.setAlignmentX (Component.LEFT_ALIGNMENT);
            this.viewPanel.setMinimumSize (new Dimension (100,
                                                          100));
                    
            this.viewPanel.setMaximumSize (new Dimension (Short.MAX_VALUE,
                                                          Short.MAX_VALUE));

        }

        //IconProvider ip = this.getIconProvider ();
        /*
        Header header = new Header (Environment.replaceObjectNames (this.getTitle ()),
                                    ip.getIcon ("header",
                                                Constants.ICON_PANEL_SECTION),
                                    null);
                                    
        header.setAlignmentX (Component.LEFT_ALIGNMENT);
        header.setOpaque (false);
        header.setBorder (UIUtils.createBottomLineWithPadding (0, 3, 3, 0));
        header.setFont (header.getFont ().deriveFont ((float) UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));
        header.setTitleColor (UIUtils.getTitleColor ());
        header.setPaintProvider (new GradientPainter (com.quollwriter.ui.UIUtils.getComponentColor (),
                                                      com.quollwriter.ui.UIUtils.getComponentColor ()));
                                            
                
        this.viewPanel.add (header);
*/
        
        IconProvider ip = this.getIconProvider ();                    
        
        JComponent view = null;
        
        // See if there are view items, if so create a form, otherwise
        // call "getEditPanel".
        Set<FormItem> viewItems = this.getViewItems ();

        if ((viewItems != null) &&
            (viewItems.size () > 0))
        {
            view = this.buildPanel (viewItems,
                                    ip,
                                    false,
                                    false);

        } else
        {

            view = this.getViewPanel ();

        }

        if (view != null)
        {
        
            java.util.List<JComponent> buttons = new ArrayList ();
    
            JButton edit = UIUtils.createButton (ip.getIcon (Constants.EDIT_ICON_NAME,
                                                          Constants.ICON_PANEL_SECTION_ACTION),
                                              "Click to edit this section",
                                              new ActionListener ()
                                              {
                                                
                                                  @Override
                                                  public void actionPerformed (ActionEvent ev)
                                                  {
                                                    
                                                      _this.doEdit ();
                                                    
                                                  }
                                                
                                              });
        
            buttons.add (edit);
                
            this.viewPanel.add (EditPanel.createHeader (this.getTitle (),
                                                        ip.getIcon ("header",
                                                                    Constants.ICON_PANEL_SECTION),
                                                        UIUtils.createButtonBar (buttons)));
        
        
            //header.setControls (UIUtils.createButtonBar (buttons));
        
            Border b = UIUtils.createPadding (0, 3, 0, 0);
                        
            if (view instanceof JTree)
            {
            
                view.setBorder (UIUtils.createPadding (3, 0, 5, 0));
            
                JScrollPane sp = UIUtils.createScrollPane (view);
                
                sp.setBorder (b);
            
                this.viewPanel.add (sp);
            
            } else {
            
                if (view instanceof JScrollPane)
                {
                    
                    view.setBorder (b);
                    
                    this.viewPanel.add (view);
                   
                } else {
            
                    view.setBorder (UIUtils.createPadding (10, 5, 5, 5));            
            
                    Box _view = new ScrollableBox (BoxLayout.Y_AXIS);
                    _view.setOpaque (false);
                    _view.add (view);
                
                    JComponent sp = UIUtils.createScrollPane (_view);
                    
                    sp.setBorder (b);
            
                    this.viewPanel.add (sp);

                }
    
            } 
                    
        }

        this.fireActionEvent (EditPanel.VIEW_VISIBLE,
                              "view-visible");

        ((CardLayout) this.cards.getLayout ()).show (this.cards,
                                                     "view");
        
    }        
    
    private void showEdit ()
    {
        
        final EditPanel _this = this;
        
        if (this.editPanel != null)
        {
            
            this.editPanel.removeAll ();
            
        } else {
            
            this.editPanel = new Box (BoxLayout.Y_AXIS);
            this.editPanel.setOpaque (false);
            this.editPanel.setMaximumSize (new java.awt.Dimension (Short.MAX_VALUE,
                                                                   Short.MAX_VALUE));
            this.cards.add (this.editPanel,
                            "edit");
                    
            this.editPanel.setAlignmentX (Component.LEFT_ALIGNMENT);

        }
        
        final String ht = this.getHelpText ();
        
        IconProvider ip = this.getIconProvider ();
        
        java.util.List<JComponent> buttons = new ArrayList ();

        JButton save = UIUtils.createButton (ip.getIcon (Constants.SAVE_ICON_NAME,
                                                      Constants.ICON_PANEL_SECTION_ACTION),
                                             "Click to save the details",
                                             new ActionListener ()
                                          {
                                            
                                              @Override
                                              public void actionPerformed (ActionEvent ev)
                                              {
                                                
                                                  _this.doSave ();
                                                
                                              }
                                            
                                          });

        buttons.add (save);
            
        JButton cancel = UIUtils.createButton (ip.getIcon (Constants.CANCEL_ICON_NAME,
                                                           Constants.ICON_PANEL_SECTION_ACTION),
                                               "Click to cancel the editing",
                                               new ActionListener ()
                                               {
                                                
                                                  @Override
                                                  public void actionPerformed (ActionEvent ev)
                                                  {
                                                    
                                                      _this.doCancel ();
                                                    
                                                  }
                                                
                                               });
        buttons.add (cancel);
/*
        if (ht != null)
        {

            final JComponent _hb = helpBox;
        
            JButton help = UIUtils.createButton (ip.getIcon (Constants.HELP_ICON_NAME,
                                                             Constants.ICON_PANEL_SECTION_ACTION),
                                                 "Click to view/hide the help",
                                                 new ActionListener ()
                                                 {
                                                  
                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {
                                                        
                                                        _hb.setVisible (!_hb.isVisible ());

                                                        _this.validate ();
                                                        _this.repaint ();                                                        
                                                        
                                                    }
                                                    
                                                 });

            buttons.add (help);

        }
        */
        this.editPanel.add (EditPanel.createHeader ("Edit",
                                                    ip.getIcon (Constants.EDIT_ICON_NAME,
                                                                Constants.ICON_PANEL_SECTION),
                                                    UIUtils.createButtonBar (buttons)));
        
        this.editError = UIUtils.createErrorLabel ("Please enter a value.");
        this.editError.setVisible (false);
        this.editError.setOpaque (false);

        this.editError.setBorder (UIUtils.createPadding (5, 3, 5, 5));

        JComponent helpBox = null;
        
        if (ht != null)
        {

            FormLayout fl = new FormLayout ("p, 3px, fill:90px:grow",
                                            "top:p");

            PanelBuilder pb = new PanelBuilder (fl);

            CellConstraints cc = new CellConstraints ();

            ImagePanel helpII = new ImagePanel (ip.getIcon ("help",
                                                            Constants.ICON_PANEL_SECTION_ACTION),
                                                null);
            helpII.setAlignmentX (Component.LEFT_ALIGNMENT);

            pb.add (helpII,
                    cc.xy (1,
                           1));

            pb.add (UIUtils.createHelpTextPane (ht,
                                                Environment.getFocusedViewer ()),
                    cc.xy (3,
                           1));

            helpBox = pb.getPanel ();
            helpBox.setOpaque (false);
            helpBox.setVisible (false);

            helpBox.setAlignmentX (Component.LEFT_ALIGNMENT);

            helpBox.setBorder (UIUtils.createPadding (0, 0, 5, 0));

            this.editPanel.add (helpBox);

        }

        this.editPanel.add (this.editError);

        JComponent edit = null;
        
        // See if there are edit items, if so create a form, otherwise
        // call "getEditPanel".
        Set<FormItem> editItems = this.getEditItems ();

        if ((editItems != null) &&
            (editItems.size () > 0))
        {

            edit = new Form (Form.Layout.stacked,
                             editItems,
                             null);

        } else {

            edit = this.getEditPanel ();

        }
        
        Border b = UIUtils.createPadding (3, 3, 0, 0);
        
        if (edit instanceof JTree)
        {
        
            edit.setBorder (UIUtils.createPadding (3, 0, 5, 5));
        
            JScrollPane sp = UIUtils.createScrollPane (edit);
        
            sp.setBorder (b);
            
            this.editPanel.add (sp);
        
        } else {
        
            if (edit instanceof JScrollPane)
            {
                
                edit.setBorder (b);

                this.editPanel.add (edit);
        
            } else {
        
                edit.setBorder (UIUtils.createPadding (3, 0, 5, 5));

                Box _edit = new ScrollableBox (BoxLayout.Y_AXIS);
                _edit.setOpaque (false);
                _edit.add (edit);
                _edit.setMaximumSize (new java.awt.Dimension (Short.MAX_VALUE,
                                                              Short.MAX_VALUE));
            
                JComponent sp = UIUtils.createScrollPane (_edit);
                
                sp.setBorder (null);
                this.editPanel.add (sp);

            }
                
        } 

        ((CardLayout) this.cards.getLayout ()).show (this.cards,
                                                     "edit");
        
        this.fireActionEvent (EditPanel.EDIT_VISIBLE,
                              "edit-visible");
     
    }
    
    public void doEdit ()
    {
                    
        this.showEdit ();        
        
    }
    
    public void doCancel ()
    {

        this.editError.setVisible (false);

        if (!this.handleCancel ())
        {

            this.validate ();
            this.repaint ();

            return;

        }
        
        this.fireActionEvent (EditPanel.CANCELLED,
                              "cancelled");

        this.showViewPanel ();
        
/*        
        //this.visiblePanel = null;

        if (this.viewPanel != null)
        {

            this.viewPanel.setVisible (true);

            this.fireActionEvent (EditPanel.CANCELLED,
                                  "cancelled");

            this.fireActionEvent (EditPanel.VIEW_VISIBLE,
                                  "view-visible");

            this.visiblePanel = this.viewPanel;

        }
        */
/*
        this.editPanel.setVisible (false);
        this.edit.setToolTipText ("Click to edit this section");

        this.edit.setVisible (true);
        this.save.setVisible (false);
        this.cancel.setVisible (false);
        this.help.setVisible (false);
        this.helpBox.setVisible (false);
*/
        this.repaint ();

    }

    public void showEditError (Set<String> ms)
    {
        
        if ((ms == null)
            ||
            (ms.size () == 0)
           )
        {
            
            return;
            
        }
        
        if (ms.size () == 1)
        {
            
            this.showEditError (ms.iterator ().next ());
            
            return;
            
        }
        
        StringBuilder b = new StringBuilder ("<ul>");
        
        for (String s : ms)
        {
            
            b.append ("<li>");
            b.append (s);
            b.append ("</li>");
            
        }
        
        b.append ("</ul>");
        
        this.showEditError (b.toString ());
        
    }
    
    public void showEditError (String m)
    {

        if (m == null)
        {

            return;

        }

        this.editError.setText (m);

        this.editError.setVisible (true);
System.out.println ("HRE");
        this.validate ();
        this.repaint ();

    }

    public Header getHeader ()
    {

        this.init ();

        return this.header;

    }

    public Box getPanel ()
    {

        this.init ();

        return this.panel;

    }

    public void repaint ()
    {

        super.repaint ();

        if (this.getParent () != null)
        {

            this.getParent ().repaint ();

        }

    }

    public ActionListener getDoSaveAction ()
    {

        final EditPanel _this = this;

        return new ActionAdapter ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                _this.doSave ();

            }

        };

    }

    public boolean doSave ()
    {

        if (!this.handleSave ())
        {

            this.repaint ();

            return false;

        }

        this.showViewPanel ();

        this.repaint ();

        return true;

    }

    public abstract void refreshViewPanel ();

    public abstract String getTitle ();

    public abstract String getHelpText ();

    public abstract JComponent getEditPanel ();

    public abstract JComponent getViewPanel ();

    public abstract Set<FormItem> getEditItems ();

    public abstract Set<FormItem> getViewItems ();
    
    public abstract boolean handleSave ();

    public abstract boolean handleCancel ();

    public abstract void handleEditStart ();

    public abstract IconProvider getIconProvider ();

    private Header createHeader (String       title,
                                 String       iconType,
                                 IconProvider ip,
                                 boolean      bottomBorder)
    {

        Header h = new Header (title,
                               ((iconType == null) ? null : ip.getIcon (iconType,
                                                                        Constants.ICON_PANEL_SECTION)),
                               null);

        h.setFont (h.getFont ().deriveFont (UIUtils.getScaledFontSize (10)).deriveFont (Font.PLAIN));
        h.setTitleColor (UIUtils.getTitleColor ());
        h.setOpaque (false);
        h.setBackground (UIUtils.getComponentColor ());
        h.setPaintProvider (null);

        h.setAlignmentX (Component.LEFT_ALIGNMENT);

        if (bottomBorder)
        {

            h.setBorder (new CompoundBorder (new MatteBorder (0,
                                                              0,
                                                              1,
                                                              0,
                                                              Environment.getBorderColor ()),
                                             new EmptyBorder (0,
                                                              0,
                                                              2,
                                                              0)));

        } else {

            h.setBorder (new EmptyBorder (0,
                                          0,
                                          0,
                                          0));

        }

        return h;

    }

    private JPanel buildPanel (Set<FormItem> items,
                               IconProvider             ip,
                               boolean                  scrollPaneBorder,
                               boolean                  headerBottomBorder)
    {
    
        StringBuilder rowSpec = new StringBuilder ();

        for (FormItem item : items)
        {

            if (rowSpec.length () > 0)
            {

                rowSpec.append (", 10px, ");

            }

            rowSpec.append ("p, 5px, ");
            rowSpec.append (((item.getFormatSpec () != null) ? item.getFormatSpec () : "p"));

        }

        FormLayout fl = new FormLayout ("3px, 100px:grow, 3px",
                                        rowSpec.toString ());

        PanelBuilder pb = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int row = 1;

        for (FormItem item : items)
        {

            if (item.getLabel () != null)
            {

                pb.addLabel (item.getLabel ().toString (),
                        cc.xywh (1,
                                 row,
                                 3,
                                 1));

        /*
                pb.add (this.createHeader (item.label.toString (),
                                           null,
                                           ip,
                                           headerBottomBorder),
                        cc.xywh (1,
                                 row,
                                 3,
                                 1));
*/
            }

            row += 2;

            Component c = item.getComponent ();

            if (c instanceof JComboBox)
            {

                Box tb = new Box (BoxLayout.X_AXIS);
                tb.add (c);
                tb.add (Box.createHorizontalGlue ());

                c = tb;

            }

            if ((item.getComponent () instanceof JTextArea) ||
                (item.getComponent () instanceof JEditorPane))
            {

                JScrollPane sp = new JScrollPane (item.getComponent ());

                if (!scrollPaneBorder)
                {

                    sp.setBorder (null);
                    sp.setOpaque (false);
                    sp.getViewport ().setOpaque (false);

                }

                c = sp;

            }

            if ((!scrollPaneBorder) &&
                (item.getComponent () instanceof JTextField))
            {

                ((JComponent) item.getComponent ()).setBorder (null);
                ((JComponent) item.getComponent ()).setOpaque (false);

            }

            pb.add (c,
                    cc.xy (2,
                           row));

            row += 2;

        }

        return pb.getPanel ();

    }

    public static Header createHeader (String     title,
                                       Icon       icon,
                                       JComponent controls)
    {
/*        
                                    Environment.getIcon (iconType,
                                                         Constants.ICON_PANEL_SECTION),
*/
        Header header = new Header (Environment.replaceObjectNames (title),
                                    icon,
                                    null);
                                    
        header.setAlignmentX (Component.LEFT_ALIGNMENT);
        header.setOpaque (false);
        header.setBorder (UIUtils.createBottomLineWithPadding (0, 3, 3, 0));
        header.setFont (header.getFont ().deriveFont ((float) UIUtils.getScaledFontSize (14)).deriveFont (Font.PLAIN));
        header.setTitleColor (UIUtils.getTitleColor ());
        header.setPaintProvider (new GradientPainter (com.quollwriter.ui.UIUtils.getComponentColor (),
                                                      com.quollwriter.ui.UIUtils.getComponentColor ()));

        header.setControls (controls);
                                                      
        return header;        
        
    }
    
}
