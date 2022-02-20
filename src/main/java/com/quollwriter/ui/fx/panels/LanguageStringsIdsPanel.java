package com.quollwriter.ui.fx.panels;

import java.util.*;
import java.util.stream.*;

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
    private BooleanProperty showingErrorsProp = null;
    private BooleanProperty showOnlyNoValueProp = null;
    private VirtualFlow<?, ?> virtualFlow = null;
    private VirtualizedScrollPane<?> scroll = null;

    public LanguageStringsIdsPanel (AbstractLanguageStringsEditor ed,
                                    Node                          parent)
//                                    Set<Value>                    userValues)
    {

        super (ed);

        this.showingErrorsProp = new SimpleBooleanProperty (false);
        this.showOnlyNoValueProp = new SimpleBooleanProperty (false);

        this.editor = ed;

        this.parent = parent;
        //this.allValues = values;
        this.allValues = parent.getValues (null);

        this.values = FXCollections.observableList (new ArrayList<> (this.allValues));

        this.getBinder ().addChangeListener (ed.nodeFilterProperty (),
                                             (pr, oldv, newv) ->
        {

            try
            {

                this.saveValues ();

            } catch (Exception e) {

                Environment.logError ("Unable to save values",
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 "Unable to save values.");
                return;


            }

            this.values.clear ();

            if (newv != null)
            {

                Filter<Node> f = this.viewer.getNodeFilter ();

                List<Value> vals = this.allValues.stream ()
                    .filter (v -> f.accept (v))
                    .collect (Collectors.toList ());
                this.values.addAll (vals);

            } else {

                this.values.addAll (this.allValues);

            }

        });

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

        QuollLabel showingErrors = QuollLabel.builder ()
            .styleClassName ("errorsonly")
            .label (new SimpleStringProperty ("Only showing values that have one or more errors in the text."))
            .build ();
        showingErrors.setVisible (false);

        QuollLabel showingNoValue = QuollLabel.builder ()
            .styleClassName ("novalue")
            .label (new SimpleStringProperty ("Only showing values that have no text provided yet."))
            .build ();
        showingNoValue.setVisible (false);

        VBox m = new VBox ();
        m.getStyleClass ().add ("messages");

        m.getChildren ().addAll (showingErrors, showingNoValue);

        this.content.getChildren ().add (m);

        this.showingErrorsProp.addListener ((pr, oldv, newv) ->
        {

            showingErrors.setVisible (newv);

        });

        this.showOnlyNoValueProp.addListener ((pr, oldv, newv) ->
        {

            showingNoValue.setVisible (newv);

        });

        this.createView ();

        this.content.getChildren ().add (this.scroll);

    }

    private void createView ()
    {

        this.values.clear ();

        if (this.viewer.getNodeFilter () != null)
        {

            Filter<Node> f = this.viewer.getNodeFilter ();

            List<Value> vals = this.allValues.stream ()
                .filter (v -> f.accept (v))
                .collect (Collectors.toList ());
            this.values.addAll (vals);

        } else {

            this.values.addAll (this.allValues);

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

        this.scroll = new VirtualizedScrollPane<> (this.virtualFlow);

        this.scroll.estimatedScrollYProperty ().addListener ((pr, oldv, newv) ->
        {

            this.scroll.pseudoClassStateChanged (StyleClassNames.SCROLLING_PSEUDO_CLASS, newv.doubleValue () > 0);

        });

        VBox.setVgrow (this.scroll, Priority.ALWAYS);

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

                if (BaseStrings.getErrors ((this.editor.getUserStrings ().containsId (v.getId ()) ? this.editor.getUserStrings ().getTextValue (v.getId ()).getRawText () : null),
                //tv.getRawText (),
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

        var vals = new ArrayList<Value> ();

        for (Value v : this.allValues)
        {

            int scount = 0;

            if (v instanceof TextValue)
            {

                TextValue tv = (TextValue) v;

                TextValue uv = this.editor.getUserStrings ().getTextValue (v.getId (),
                                                                           true);

                if ((uv == null)
                    ||
                    (uv.getRawText () == null)
                    ||
                    (uv.getRawText ().equals (""))
                   )
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

        UIUtils.forceRunLater (() ->
        {

            this.virtualFlow.show (0);

        });

    }

    @Override
    public String getPanelId ()
    {

        return BaseStrings.toId (this.parent.getId ());

    }

    public void scrollToNode (String id)
    {

        for (int i = 0; i < this.values.size (); i++)
        {

            if (BaseStrings.toId (this.values.get (i).getId ()).equals (id))
            {

                this.virtualFlow.showAsFirst (i);// 10d);

            }
/*
TODO???
                int _i = i;

                LanguageStringsIdBox r = this.idBoxes.get (this.values.get (_i));

                Border b = r.getBorder ();
                Paint pb = b.getLeftStroke ();

                if (!(pb instanceof Color))
                {

                    return;

                }

                UIUtils.forceRunLater (() ->
                {

                    Transition t = new Transition ()
                    {

                        {
                            setCycleDuration (Duration.millis (2000));
                            setCycleCount (2);
                        }

                        @Override
                        protected void interpolate (double v)
                        {



                        }

                    };

                });

                break;

            }
*/
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

        for (Value v : this.allValues)
        {

            LanguageStringsIdBox co = this.idBoxes.get (v);

            if (co != null)
            {

                if (co instanceof LanguageStringsTextIdBox)
                {

                    LanguageStringsTextIdBox b = (LanguageStringsTextIdBox) co;

                    c += b.getErrorCount ();

                }

            } else {

                if (v instanceof TextValue)
                {

                    TextValue _nv = this.editor.getUserStrings ().getTextValue (v.getId (),
                                                                                true);

                    if (_nv != null)
                    {

                        int x = BaseStrings.getErrors (_nv.getRawText (),
                                                       BaseStrings.toId (v.getId ()),
                                                       ((TextValue) v).getSCount (),
                                                       this.editor).size ();

                        c += x;

                    }

                }

            }

        }

        return c;

    }

    public int getUserValueCount ()
    {

        int c = 0;

        for (Value v : this.allValues)
        {

            if (this.viewer.getNodeFilter () != null)
            {

                if (!this.viewer.getNodeFilter ().accept (v))
                {

                    continue;

                }

            }

            LanguageStringsIdBox b = this.idBoxes.get (v);

            if (b != null)
            {

                if (b.hasUserValue ())
                {

                    c++;

                }

            } else {

                if (this.editor.getUserStrings ().containsId (v.getId ()))
                {

                    c++;

                }

            }

        }
/*
        for (LanguageStringsIdBox co : this.idBoxes.values ())
        {

            LanguageStringsIdBox b = (LanguageStringsIdBox) co;

            if (this.viewer.getNodeFilter () != null)
            {

                if (!this.viewer.getNodeFilter ().accept (b.getBaseValue ()))
                {

                    continue;

                }

            }

            if (b.hasUserValue ())
            {

                c++;

            }

        }
*/
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

    @Override
    public State getState ()
    {

        State s = super.getState ();

        s.set ("scroll",
               this.scroll.estimatedScrollYProperty ().getValue ());

        return s;

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        super.init (s);

        if (s == null)
        {

            return;

        }

        double scroll = (double) s.getAsFloat ("scroll",
                                               0f);

        if (scroll > 0)
        {

            this.scroll.applyCss ();
            this.scroll.layout ();
            UIUtils.forceRunLater (() ->
            {

                this.scroll.scrollYToPixel (scroll);

            });

        }

    }

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
                if (this.showingErrorsProp.getValue ())
                {

                    try
                    {

                        this.showingErrorsProp.setValue (false);
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

                        this.showingErrorsProp.setValue (true);
                        this.showOnlyNoValueProp.setValue (false);
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

                if (this.showOnlyNoValueProp.getValue ())
                {

                    try
                    {

                        this.showOnlyNoValueProp.setValue (false);
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

                        this.showOnlyNoValueProp.setValue (true);
                        this.showingErrorsProp.setValue (false);
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

        //return items;
        return null;//new HashSet<> ();

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

                        this.showOnlyNoValueProp.setValue (true);
                        this.showingErrorsProp.setValue (false);
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
