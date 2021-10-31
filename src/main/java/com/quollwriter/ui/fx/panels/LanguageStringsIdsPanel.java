package com.quollwriter.ui.fx.panels;

import java.util.*;

import java.beans.*;

import javafx.collections.*;
import javafx.beans.binding.*;
import javafx.beans.property.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import org.fxmisc.flowless.*;

import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.uistrings.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

public class LanguageStringsIdsPanel extends PanelContent<AbstractLanguageStringsEditor> implements ToolBarSupported
{

    public final static String LIMIT_ERROR_ACTION_NAME = "view-limit-error";
    public static final String LIMIT_NO_VALUE_ACTION_NAME = "view-limit-no-value";
    public static final String NEXT_ACTION_NAME = "next";

    private Set<Value> vals = null;
    private Node parent = null;
    private ObservableList<Value> values = null;
    private Set<Value> allValues = null;
    private Map<Value, LanguageStringsIdBox> idBoxes = new HashMap<> ();
    private VBox content = null;
    private AbstractLanguageStringsEditor editor = null;
    private String limitType = "";
    private boolean showingErrors = false;
    private boolean showOnlyNoValue = false;
    private VirtualFlow<?, ?> virtualFlow = null;

    public LanguageStringsIdsPanel (AbstractLanguageStringsEditor ed,
                                    Node                          parent,
                                    Set<Value>                    values)
    {

        super (ed);

        this.editor = ed;

        this.parent = parent;
        this.allValues = values;
        this.values = FXCollections.observableList (new ArrayList<> (values));

        this.content = new VBox ();

        VBox.setVgrow (this.content,
                       Priority.ALWAYS);

        this.getChildren ().add (this.content);

        this.content.getChildren ().add (Header.builder ()
            .title (new SimpleStringProperty (this.parent.getTitle ()))
            .iconClassName (ed.getBaseStrings ().getSection (this.parent.getSection ()).icon)
            .build ());

        // Check for the section comment.
        if (this.parent.getComment () != null)
        {

            this.content.getChildren ().add (QuollTextView.builder ()
                .inViewer (this.viewer)
                .styleClassName ("sectioncomment")
                .text (this.parent.getComment ())
                .build ());

        }

        this.virtualFlow = VirtualFlow.createVertical (this.values,
        v ->
        {

            LanguageStringsIdBox idb = this.idBoxes.get (v);

            if (idb != null)
            {

                return idb;

            }

            if (v instanceof ImageValue)
            {

                LanguageStringsImageIdBox b = new LanguageStringsImageIdBox ((ImageValue) v,
                                                                             (this.editor.getUserStrings ().containsId (v.getId ()) ? this.editor.getUserStrings ().getImageValue (v.getId ()) : null),
                                                                             this);

                b.init ();
                this.idBoxes.put (v,
                                  b);

                return b;

            }

            if (v instanceof TextValue)
            {

                LanguageStringsTextIdBox b = new LanguageStringsTextIdBox ((TextValue) v,
                                                                           (this.editor.getUserStrings ().containsId (v.getId ()) ? this.editor.getUserStrings ().getTextValue (v.getId ()) : null),
                                                                           this);

                b.init ();

                this.idBoxes.put (v,
                                  b);

                return b;

            }

            return null;

        });

        this.content.getChildren ().add (new VirtualizedScrollPane<> (this.virtualFlow));

        VBox.setVgrow (this.content.getChildren ().get (this.content.getChildren ().size () - 1),
                       Priority.ALWAYS);

        //this.getChildren ().add (new ScrollPane (this.content));

        //this.buildForm (this.parent.getNodeId ());

/*
TODO ?
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
*/

    }

    public Node getParentNode ()
    {

        return this.parent;

    }

    public void moveToNextBox (Value curr)
    {

        int ind = -1;

        for (int i = 0; i < this.values.size (); i++)
        {

            Value v = this.values.get (i);

            if (v.getId ().equals (curr.getId ()))
            {

                ind = i;

                break;

            }

        }

        if (ind > -1)
        {

            ind++;

            if (ind > (this.values.size () - 1))
            {

                ind = 0;

            }

            this.virtualFlow.showAsFirst (ind);

            int _ind = ind;

            UIUtils.forceRunLater (() ->
            {

                LanguageStringsIdBox id = (LanguageStringsIdBox) this.virtualFlow.getCell (_ind);

                id.requestFocus ();

            });

        }

    }

    public void moveToPreviousBox (Value curr)
    {

        int ind = -1;

        for (int i = 0; i < this.values.size (); i++)
        {

            Value v = this.values.get (i);

            if (v.getId ().equals (curr.getId ()))
            {

                ind = i;

                break;

            }

        }

        if (ind > -1)
        {

            ind--;

            if (ind < 0)
            {

                ind = this.values.size () - 1;

            }

            LanguageStringsIdBox id = (LanguageStringsIdBox) this.virtualFlow.getCell (ind);

            this.virtualFlow.showAsFirst (ind);

            int _ind = ind;

            UIUtils.forceRunLater (() ->
            {

                //LanguageStringsIdBox id = (LanguageStringsIdBox) this.virtualFlow.getCell (_ind);

                id.requestFocus ();

            });

        }

    }

    public void showAll ()
                         throws GeneralException
    {

        this.saveValues ();

        this.values.clear ();

        this.values.addAll (this.allValues);

    }

    public void showOnlyErrors ()
                         throws GeneralException
    {

        this.saveValues ();

        this.values.clear ();

        List<Value> vals = new ArrayList<> ();

        for (Value v : this.allValues)
        {

            int scount = 0;

            if (v instanceof TextValue)
            {

                TextValue tv = (TextValue) v;

                scount = tv.getSCount ();

                if (BaseStrings.getErrors (tv.getRawText (),
                                           BaseStrings.toId (v.getId ()),
                                           scount,
                                           this.viewer).size () > 0)
               {

                   vals.add (v);

               }

           }

        }

        this.values.addAll (vals);

    }

    public void showOnlyNoValue ()
                          throws GeneralException
    {

        this.saveValues ();

        this.values.clear ();

        List<Value> vals = new ArrayList<> ();

        for (Value v : this.allValues)
        {

            int scount = 0;

            if (v instanceof TextValue)
            {

                TextValue tv = (TextValue) v;

                if (tv.getRawText () == null)
                {

                   vals.add (v);

               }

           }

           if (v instanceof ImageValue)
           {

               ImageValue iv = (ImageValue) v;

               if (iv.getImageFile () == null)
               {

                   vals.add (v);

               }

           }

        }

        this.values.addAll (vals);
/*
        boolean show = true;

        if (this.limitType.equals ("novalue"))
        {

            show = false;
            this.limitType = "";

        } else {

            this.limitType = "novalue";

        }

        for (javafx.scene.Node c : this.content.getChildren ())
        {

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

        //this.getToolBarButton (LIMIT_ERROR_ACTION_NAME).setSelected (false);
        //this.getToolBarButton (LIMIT_NO_VALUE_ACTION_NAME).setSelected (show);
*/
    }

    @Override
    public String getPanelId ()
    {

        return BaseStrings.toId (this.parent.getId ());

    }

    public void scrollToNode (String id)
    {

        for (javafx.scene.Node c : this.content.getChildren ())
        {

            if (c instanceof LanguageStringsIdBox)
            {

                final LanguageStringsIdBox box = (LanguageStringsIdBox) c;

/*
TODO
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
                */

            }

        }

    }

    public void updatePreviews ()
    {

        for (javafx.scene.Node c : this.content.getChildren ())
        {

            if (c instanceof LanguageStringsTextIdBox)
            {

                LanguageStringsTextIdBox b = (LanguageStringsTextIdBox) c;

                b.showPreview ();

            }

        }

    }

    public void saveValues ()
                     throws GeneralException
    {

        for (LanguageStringsIdBox v : this.idBoxes.values ())
        {

            v.saveValue ();

        }

    }

    public String getIdValue (String id)
    {

        for (LanguageStringsIdBox v : this.idBoxes.values ())
        {

            if (v instanceof LanguageStringsTextIdBox)
            {

                LanguageStringsTextIdBox b = (LanguageStringsTextIdBox) v;

                if (b.getForId ().equals (id))
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

        for (LanguageStringsIdBox co : this.idBoxes.values ())
        {

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

        for (LanguageStringsIdBox co : this.idBoxes.values ())
        {

            LanguageStringsIdBox b = (LanguageStringsIdBox) co;

            if (b.hasUserValue ())
            {

                c++;

            }

        }

        return c;

    }
/*
    private void buildForm (String idPrefix)
    {

        // Check for the section comment.
        if (this.parent.getComment () != null)
        {

            this.content.getChildren ().add (QuollTextView.builder ()
                .inViewer (this.viewer)
                .styleClassName ("sectioncomment")
                .text (this.parent.getComment ())
                .build ());

        }

        for (Value v : this.values)
        {

            if (v instanceof ImageValue)
            {

                LanguageStringsImageIdBox b = new LanguageStringsImageIdBox ((ImageValue) v,
                                                                             (this.editor.getUserStrings ().containsId (v.getId ()) ? this.editor.getUserStrings ().getImageValue (v.getId ()) : null),
                                                                             this);

                b.init ();

                this.content.getChildren ().add (b);

            }

            if (v instanceof TextValue)
            {

                LanguageStringsTextIdBox b = new LanguageStringsTextIdBox ((TextValue) v,
                                                                           (this.editor.getUserStrings ().containsId (v.getId ()) ? this.editor.getUserStrings ().getTextValue (v.getId ()) : null),
                                                                           this);

                b.init ();

                this.content.getChildren ().add (b); // scount

            }

        }

    }
*/
    public AbstractLanguageStringsEditor getEditor ()
    {

        return this.editor;

    }

    @Override
    public Set<javafx.scene.Node> getToolBarItems ()
    {

        Set<javafx.scene.Node> items = new LinkedHashSet<> ();

        items.add (QuollButton.builder ()
            .tooltip ("Click to limit the view to Ids with one or more errors")
            .iconName (StyleClassNames.ERRORS)
            .onAction (ev ->
            {

                //this.limitType = "";
                if (this.showingErrors)
                {

                    try
                    {

                        this.showingErrors = false;
                        this.showAll ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to show errors",
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         "Unable to show errors");

                    }


                } else {

                    try
                    {

                        this.showingErrors = true;
                        this.showOnlyErrors ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to show errors",
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         "Unable to show errors");

                    }

                }

            })
            .build ());

        items.add (QuollButton.builder ()
            .tooltip ("Click to limit the view to Ids with no value")
            .iconName (StyleClassNames.CLEAR)
            .onAction (ev ->
            {

                if (this.showOnlyNoValue)
                {

                    try
                    {

                        this.showOnlyNoValue = false;
                        this.showAll ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to show only no value",
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         "Unable to show all items.");

                    }


                } else {

                    try
                    {

                        this.showOnlyNoValue = true;
                        this.showOnlyNoValue ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to show items with no value",
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         "Unable to show items with no value.");

                    }

                }

            })
            .build ());

        return items;

    }

    @Override
    public Panel createPanel ()
    {

        Map<KeyCombination, Runnable> am = new HashMap<> ();

        am.put (new KeyCodeCombination (KeyCode.E,
                                        KeyCombination.SHORTCUT_DOWN),
                () ->
                {

                    try
                    {

                        this.showOnlyErrors ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to show errors",
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         "Unable to show errors");

                    }

                });

        am.put (new KeyCodeCombination (KeyCode.N,
                                        KeyCombination.SHORTCUT_DOWN),
                () ->
                {

                    try
                    {

                        this.showOnlyNoValue = true;
                        this.showOnlyNoValue ();

                    } catch (Exception e) {

                        Environment.logError ("Unable to show items with no value",
                                              e);

                        ComponentUtils.showErrorMessage (this.viewer,
                                                         "Unable to show items with no value.");

                    }

                });

        StringProperty title = new SimpleStringProperty (String.format ("%1$s (%2$s)",
                                                                        (this.parent.getTitle () != null ? this.parent.getTitle () : this.parent.getNodeId ()),
                                                                        Environment.formatNumber (this.values.size ())));

        Panel panel = Panel.builder ()
            .title (title)
            .content (this)
            .styleClassName (StyleClassNames.LANGUAGESTRINGS)
            .styleSheet (StyleClassNames.LANGUAGESTRINGS)
            //.panelId (this.object.getObjectReference ().asString ())
            .actionMappings (am)
            .panelId ("langstrings" + this.parent.getNodeId ())
            .build ();

        return panel;

    }

}
