package com.quollwriter.ui;

import java.util.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import java.awt.event.*;

import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.ui.panels.*;
import com.quollwriter.ui.components.*;

public class LanguageStringsIdsPanel extends BasicQuollPanel<AbstractLanguageStringsEditor>
{

    public final static String LIMIT_ERROR_ACTION_NAME = "view-limit-error";
    public static final String LIMIT_NO_VALUE_ACTION_NAME = "view-limit-no-value";
    public static final String NEXT_ACTION_NAME = "next";

    private String parentId = null;
    private Set<Value> vals = null;
    private Node parent = null;
    private Set<Value> values = null;
    private Box content = null;
    private AbstractLanguageStringsEditor editor = null;
    private String limitType = "";

    public LanguageStringsIdsPanel (AbstractLanguageStringsEditor ed,
                                    Node                          parent,
                                    Set<Value>                    values)
    {

        super (ed,
               parent.getNodeId (),
               null);

        final LanguageStringsIdsPanel _this = this;

        this.editor = ed;

        this.parent = parent;
        this.values = values;

        //this.node = this.editor.baseStrings.getNode (id);

        String title = (this.parent.getTitle () != null ? this.parent.getTitle () : this.parent.getNodeId ());

        // Replace the kludge to limit the length of the title.
        title = Utils.replaceString (title,
                                           "<br />&nbsp;&nbsp;",
                                           " ");

        this.setTitle (String.format ("%s (%s)",
                                      title,
                                      Environment.formatNumber (this.values.size ())));

        this.content = new ScrollableBox (BoxLayout.Y_AXIS);
        this.content.setAlignmentY (Component.TOP_ALIGNMENT);
        this.content.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.content.setFocusTraversalPolicy (new java.awt.FocusTraversalPolicy ()
        {

            @Override
            public Component getDefaultComponent (Container cont)
            {

                return this.getFirstComponent (cont);

            }

            @Override
            public Component getFirstComponent (Container cont)
            {

                for (int i = 0; i < cont.getComponentCount (); i++)
                {

                    Component c = cont.getComponent (i);

                    if (c instanceof LanguageStringsIdBox)
                    {

                        return ((LanguageStringsIdBox) c).getFocusableComponent ();

                    }

                }

                return null;

            }

            @Override
            public Component getLastComponent (Container cont)
            {

                LanguageStringsIdBox b = null;

                for (int i = cont.getComponentCount () - 1; i > -1; i--)
                {

                    Component c = cont.getComponent (i);

                    if (c instanceof LanguageStringsIdBox)
                    {

                        b = (LanguageStringsIdBox) c;

                        break;

                    }

                }

                if (b != null)
                {

                    return b.getFocusableComponent ();

                }

                return null;

            }

            @Override
            public Component getComponentAfter (Container cont,
                                                Component comp)
            {

                Container parent = comp.getParent ();

                while (parent != null)
                {

                    if (parent instanceof LanguageStringsIdBox)
                    {

                        if (comp instanceof JTextField)
                        {

                            JTextField f = (JTextField) comp;

                            if (!f.isEditable ())
                            {

                                LanguageStringsIdBox b = (LanguageStringsIdBox) parent;

                                return b.getFocusableComponent ();

                            }

                        }

                        LanguageStringsIdBox b = (LanguageStringsIdBox) parent;

                        int i = 0;

                        for (; i < cont.getComponentCount (); i++)
                        {

                            if (b == cont.getComponent (i))
                            {

                                i++;

                                break;

                            }

                        }

                        if (i < cont.getComponentCount ())
                        {

                            Component x = cont.getComponent (i);

                            if (x instanceof LanguageStringsIdBox)
                            {

                                return ((LanguageStringsIdBox) x).getFocusableComponent ();

                            }

                        }

                        break;

                    }

                    parent = parent.getParent ();

                }

                return null;

            }

            @Override
            public Component getComponentBefore (Container cont,
                                                 Component comp)
            {

                Container parent = comp.getParent ();

                while (parent != null)
                {

                    if (parent instanceof LanguageStringsIdBox)
                    {

                        LanguageStringsIdBox b = (LanguageStringsIdBox) parent;

                        int i = 0;

                        for (; i < cont.getComponentCount (); i++)
                        {

                            if (b == cont.getComponent (i))
                            {

                                i--;

                                break;

                            }

                        }

                        if (i > -1)
                        {

                            Component x = cont.getComponent (i);

                            if (x instanceof LanguageStringsIdBox)
                            {

                                return ((LanguageStringsIdBox) x).getFocusableComponent ();

                            }

                        }

                        break;

                    }

                    parent = parent.getParent ();

                }

                return null;

            }

        });

        this.content.setFocusTraversalPolicyProvider (true);

        this.actions = this.content.getActionMap ();

        this.actions.put (LIMIT_ERROR_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              @Override
                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.showOnlyErrors ();

                              }

                          });

        this.actions.put (LIMIT_NO_VALUE_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              @Override
                              public void actionPerformed (ActionEvent ev)
                              {

                                  _this.showOnlyNoValue ();

                              }

                          });

        this.actions.put (NEXT_ACTION_NAME,
                          new ActionAdapter ()
                          {

                              @Override
                              public void actionPerformed (ActionEvent ev)
                              {


                              }

                          });

        InputMap im = this.content.getInputMap (JComponent.WHEN_IN_FOCUSED_WINDOW);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_E,
                                        InputEvent.CTRL_MASK),
                LIMIT_ERROR_ACTION_NAME);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_N,
                                        InputEvent.CTRL_MASK),
               LIMIT_NO_VALUE_ACTION_NAME);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_KP_LEFT,
                                        InputEvent.CTRL_MASK),
               NEXT_ACTION_NAME);

        im.put (KeyStroke.getKeyStroke (KeyEvent.VK_LEFT,
                                        InputEvent.CTRL_MASK),
               NEXT_ACTION_NAME);

    }

    public Node getParentNode ()
    {

        return this.parent;

    }

    public void showOnlyErrors ()
    {

        boolean show = true;

        if (this.limitType.equals ("errors"))
        {

            show = false;
            this.limitType = "";

        } else {

            this.limitType = "errors";

        }

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component c = this.content.getComponent (i);

            if (c instanceof LanguageStringsIdBox)
            {

                c.setVisible (true);

                if (show)
                {

                    LanguageStringsIdBox b = (LanguageStringsIdBox) c;

                    if (!b.hasErrors ())
                    {

                        c.setVisible (false);

                    }

                }

            }

        }

        this.getToolBarButton (LIMIT_ERROR_ACTION_NAME).setSelected (show);
        this.getToolBarButton (LIMIT_NO_VALUE_ACTION_NAME).setSelected (false);

        this.validate ();
        this.repaint ();

    }

    public void showOnlyNoValue ()
    {

        boolean show = true;

        if (this.limitType.equals ("novalue"))
        {

            show = false;
            this.limitType = "";

        } else {

            this.limitType = "novalue";

        }

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component c = this.content.getComponent (i);

            if (c instanceof LanguageStringsIdBox)
            {

                c.setVisible (true);

                if (show)
                {

                    LanguageStringsIdBox b = (LanguageStringsIdBox) c;

                    if (b.hasUserValue ())
                    {

                        c.setVisible (false);

                    }

                }

            }

        }

        this.getToolBarButton (LIMIT_ERROR_ACTION_NAME).setSelected (false);
        this.getToolBarButton (LIMIT_NO_VALUE_ACTION_NAME).setSelected (show);

        this.validate ();
        this.repaint ();

    }

    @Override
    public String getPanelId ()
    {

        return BaseStrings.toId (this.parent.getId ());

    }

    @Override
    public boolean isWrapContentInScrollPane ()
    {

        return true;

    }

    public void scrollToNode (String id)
    {

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component c = this.content.getComponent (i);

            if (c instanceof LanguageStringsIdBox)
            {

                final LanguageStringsIdBox box = (LanguageStringsIdBox) c;

                if (BaseStrings.toId (box.baseValue.getId ()).equals (id))
                {

                    final Border origBorder = box.getBorder ();

                    final Color col = UIUtils.getBorderHighlightColor ();

                    final int r = col.getRed ();
                    final int g = col.getGreen ();
                    final int b = col.getBlue ();

                    PropertyChangeListener l = new PropertyChangeListener ()
                    {

                        @Override
                        public void propertyChange (PropertyChangeEvent ev)
                        {

                            Color c = new Color (r,
                                                 g,
                                                 b,
                                                ((Number) ev.getNewValue ()).intValue ());

                            box.setBorder (new CompoundBorder (new MatteBorder (3, 3, 3, 3, c),
                                                               UIUtils.createPadding (3, 3, 3, 3)));

                        }

                    };

                    final javax.swing.Timer cycle = UIUtils.createCyclicAnimator (l,
                                                                l,
                                                                60,
                                                                1500,
                                                                0,
                                                                255,
                                                                2,
                                                                new ActionListener ()
                                                                {

                                                                   @Override
                                                                   public void actionPerformed (ActionEvent ev)
                                                                   {

                                                                       box.setBorder (origBorder);

                                                                   }

                                                                });

                    UIUtils.doLater (new ActionListener ()
                    {

                        @Override
                        public void actionPerformed (ActionEvent ev)
                        {

                            UIUtils.scrollIntoView (box);

                            cycle.start ();

                        }

                    });

                }

            }

        }

    }

    public void updatePreviews ()
    {

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component c = this.content.getComponent (i);

            if (c instanceof LanguageStringsTextIdBox)
            {

                LanguageStringsTextIdBox b = (LanguageStringsTextIdBox) c;

                b.showPreview ();

            }

        }

    }

    @Override
    public JComponent getContent ()
    {

        final LanguageStringsIdsPanel _this = this;

        this.buildForm (this.parent.getNodeId ());

        this.content.add (Box.createVerticalGlue ());

        this.updatePreviews ();

        return this.content;

    }

    public void saveValues ()
                     throws GeneralException
    {

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component c = this.content.getComponent (i);

            if (c instanceof LanguageStringsTextIdBox)
            {

                LanguageStringsTextIdBox b = (LanguageStringsTextIdBox) c;

                b.saveValue ();

            }

            if (c instanceof LanguageStringsImageIdBox)
            {

                LanguageStringsImageIdBox b = (LanguageStringsImageIdBox) c;

                b.saveValue ();

            }

        }

    }

    public String getIdValue (String id)
    {

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component c = this.content.getComponent (i);

            if (c instanceof LanguageStringsTextIdBox)
            {

                LanguageStringsTextIdBox b = (LanguageStringsTextIdBox) c;

                if (b.getId ().equals (id))
                {

                    return b.getUserValue ();

                }

            }

        }

        return null;

    }

    public int getErrorCount ()
    {

        int c = 0;

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component co = this.content.getComponent (i);

            if (co instanceof LanguageStringsTextIdBox)
            {

                LanguageStringsTextIdBox b = (LanguageStringsTextIdBox) co;

                if (b.hasErrors ())
                {

                    c++;

                }

            }

        }

        return c;

    }

    public int getUserValueCount ()
    {

        int c = 0;

        for (int i = 0; i < this.content.getComponentCount (); i++)
        {

            Component co = this.content.getComponent (i);

            if (co instanceof LanguageStringsIdBox)
            {

                LanguageStringsIdBox b = (LanguageStringsIdBox) co;

                if (b.hasUserValue ())
                {

                    c++;

                }

            }

        }

        return c;

    }

    private void createComment (String comment)
    {

        JComponent c = UIUtils.createHelpTextPane (comment,
                                                   this.getViewer ());
        c.setAlignmentX (LEFT_ALIGNMENT);
        c.setBorder (UIUtils.createPadding (0, 15, 5, 5));

        this.content.add (c);

    }

    private void buildForm (String idPrefix)
    {

        // Check for the section comment.
        if (this.parent.getComment () != null)
        {

            this.createComment (this.parent.getComment ());

        }

        for (Value v : this.values)
        {

            if (v instanceof ImageValue)
            {

                LanguageStringsImageIdBox b = new LanguageStringsImageIdBox ((ImageValue) v,
                                                                             (this.editor.userStrings.containsId (v.getId ()) ? this.editor.userStrings.getImageValue (v.getId ()) : null),
                                                                             this);

                b.init ();

                this.content.add (b);

            }

            if (v instanceof TextValue)
            {

                LanguageStringsTextIdBox b = new LanguageStringsTextIdBox ((TextValue) v,
                                                                           (this.editor.userStrings.containsId (v.getId ()) ? this.editor.userStrings.getTextValue (v.getId ()) : null),
                                                                           this);

                b.init ();

                this.content.add (b); // scount

            }

        }

    }

    public AbstractLanguageStringsEditor getEditor ()
    {

        return this.editor;

    }

    @Override
    public void fillToolBar (JToolBar toolBar,
                             boolean  fullScreen)
    {

        toolBar.add (this.createToolbarButton (Constants.ERROR_ICON_NAME,
                                               "Click to limit the view to Ids with one or more errors",
                                               LIMIT_ERROR_ACTION_NAME));

        toolBar.add (this.createToolbarButton ("no-value", //Constants.CLEAR_ICON_NAME,
                                               "Click to limit the view to Ids with no value",
                                               LIMIT_NO_VALUE_ACTION_NAME));
/*
        toolBar.add (this.createToolbarButton (Constants.NEXT_ICON_NAME,
                                               "Go to the next section",
                                               NEXT_ACTION_NAME));
*/
    }

    @Override
    public void fillPopupMenu (MouseEvent ev,
                               JPopupMenu popup)
    {

    }

}
