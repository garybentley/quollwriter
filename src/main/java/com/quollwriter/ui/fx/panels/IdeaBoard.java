package com.quollwriter.ui.fx.panels;

import java.util.*;
import java.util.stream.*;
import java.io.*;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.image.*;
import javafx.geometry.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.*;
import com.quollwriter.data.comparators.*;
import com.quollwriter.text.Paragraph;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class IdeaBoard extends PanelContent<ProjectViewer> //implements ToolBarSupported
{

    public static final String PANEL_ID = "ideaboard";

    private VerticalLayout categories = null;
    private Map<IdeaType, TypeBox> ideaTypeBoxes = new HashMap<> ();
    private ScrollPane scrollpane = null;
    private boolean customLayout = false;

    public IdeaBoard (ProjectViewer viewer)
    {

        super (viewer);

        this.getBackgroundPane ().setDragImportAllowed (true);

        Set<Node> controls = new LinkedHashSet<> ();

        controls.add (QuollButton.builder ()
            .tooltip (ideaboard,headercontrols,items,_new,tooltip)
            .iconName (StyleClassNames.ADD)
            .onAction (ev ->
            {

                this.showAddNewIdeaType ();

            })
            .build ());

        controls.add (UIUtils.createHelpPageButton (this.viewer,
                                                    "idea-board/overview",
                                                    null));


        VBox b = new VBox ();
        b.getChildren ().add (Header.builder ()
            .title (ideaboard,title)
            .styleClassName (StyleClassNames.IDEABOARD)
            .controls (controls)
            .build ());

        this.categories = new VerticalLayout ();
        this.categories.getStyleClass ().add (StyleClassNames.CATEGORIES);

        this.scrollpane = new ScrollPane (this.categories);

        this.scrollpane.addEventFilter (DragEvent.ANY,
                                        ev ->
        {

            List<File> files = ev.getDragboard ().getFiles ();

            if ((files != null)
                &&
                (files.size () > 0)
               )
            {

                if (files.size () > 1)
                {

                    return;

                }

                File f = files.get (0);

                if (UIUtils.isImageFile (f))
                {

                    // Forward the event to the bg pane.
                    this.getBackgroundPane ().fireEvent (ev);

                }

            }

        });

        this.scrollpane.setOnContextMenuRequested (ev ->
        {

            Set<MenuItem> its = new LinkedHashSet<> ();

            its.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (ideaboard,popupmenu,items,_new))
                .iconName (StyleClassNames.ADD)
                .onAction (eev ->
                {

                    this.showAddNewIdeaType ();

                })
                .build ());

            if (this.customLayout)
            {

                its.add (QuollMenuItem.builder ()
                    .label (getUILanguageStringProperty (ideaboard,popupmenu,items,sortname))
                    .iconName (StyleClassNames.SORT)
                    .onAction (eev ->
                    {

                        this.showInAlphaOrder ();

                    })
                    .build ());

            }

            its.add (QuollMenuItem.builder ()
                .label (getUILanguageStringProperty (ideaboard,popupmenu,items,selectbackground))
                .iconName (StyleClassNames.SELECTBG)
                .onAction (eev ->
                {

                    this.runCommand (CommandIds.selectbackground);

                })
                .build ());

            UIUtils.showContextMenu (this.categories,
                                     its,
                                     ev.getScreenX (),
                                     ev.getScreenY ());

            ev.consume ();

        });

        VBox.setVgrow (this.scrollpane,
                       Priority.ALWAYS);

        b.getChildren ().add (this.scrollpane);

        this.getChildren ().add (b);

        // Get all the current idea types.
        ObservableSet<IdeaType> its = this.viewer.getProject ().getIdeaTypes ();

        if (its.size () == 0)
        {

            try
            {

                this.viewer.setIgnoreProjectEvents (true);

                // Add the default types, they are inserted in reverse since
                // we insert at the top of the container rather than adding to the bottom.
                this.addNewType (getUILanguageStringProperty (objectnames,plural, com.quollwriter.data.Scene.OBJECT_TYPE).getValue (),
                                //"{Scenes}"),
                                 "scene",
                                 false);

                this.addNewType (getUILanguageStringProperty (ideaboard,ideatypes,defaulttypes,dialogue).getValue (),
                                //"Dialogue",
                                 "dialogue",
                                 false);

                this.addNewType (getUILanguageStringProperty (ideaboard,ideatypes,defaulttypes,other).getValue (),
                                //"Other",
                                 null,
                                 false);

                for (UserConfigurableObjectType type : this.viewer.getProject ().getAssetUserConfigurableObjectTypes (true))
                {

                    this.addNewType (type.getObjectTypeNamePlural (),
                                     type.getObjectTypeId (),
                                     false);

                }

                this.addNewType (getUILanguageStringProperty (objectnames,plural, Chapter.OBJECT_TYPE).getValue (),
                                //"{Chapters}"),
                                 "chapter",
                                 false);

                this.viewer.setIgnoreProjectEvents (false);

                //its = this.viewer.getProject ().getIdeaTypes ();

            } catch (Exception e) {

                Environment.logError ("Unable to add default idea types",
                                      e);

            }

        }

        this.getBinder ().addSetChangeListener (this.viewer.getProject ().getIdeaTypes (),
                                                ev ->
        {

            if (ev.wasAdded ())
            {

                this.addType (ev.getElementAdded (),
                              0);

            }

            if (ev.wasRemoved ())
            {

                this.categories.getChildren ().remove (this.ideaTypeBoxes.get (type));
                this.ideaTypeBoxes.remove (type);

            }

        });

    }

    @Override
    public State getState ()
    {

        State s = super.getState ();

        if (s == null)
        {

            s = new State ();

        }

        s.set (State.Key.scrollpanev,
               this.scrollpane.getVvalue ());

        List<String> refs = new ArrayList<> ();

        for (Node n : this.categories.getChildren ())
        {

            if (n instanceof TypeBox)
            {

                TypeBox b = (TypeBox) n;

                IdeaType t = b.getIdeaType ();

                ObjectReference ref = t.getObjectReference ();

                refs.add (ref.asString ());

                s.set (ref.asString (),
                       b.getState ());

            }

        }

        s.set ("customLayout",
               this.customLayout);
        s.set ("ideatypes",
               refs);

        return s;

    }

    private void showInAlphaOrder ()
    {

        this.customLayout = false;

        this.categories.getChildren ().clear ();

        this.ideaTypeBoxes.keySet ().stream ()
            .sorted (new NamedObjectSorter (this.viewer.getProject ()))
            .forEach (it ->
            {

                this.categories.getChildren ().add (this.ideaTypeBoxes.get (it));

            });

    }

    @Override
    public void init (State state)
               throws GeneralException
    {

        super.init (state);

        ObservableSet<IdeaType> its = this.viewer.getProject ().getIdeaTypes ();

        Map<String, IdeaType> trefs = new LinkedHashMap<> ();

        for (IdeaType it : its)
        {

            trefs.put (it.getObjectReference ().asString (),
                       it);

        }

        List<String> refs = null;

        if (state == null)
        {

            state = new State ();

        } else {

            refs = state.getAsList ("ideatypes",
                                    String.class);

        }

        // Legacy
        if (refs == null)
        {

            refs = new ArrayList<> ();

            for (IdeaType it : its)
            {

                refs.add (it.getObjectReference ().asString ());

            }

        }

        State st = state;

        if (state.getAsBoolean ("customLayout"))
        {

            for (String r : refs)
            {

                IdeaType it = trefs.get (r);

                if (it != null)
                {

                    TypeBox b = this.addType (it,
                                              -1);
                    b.init (state.getAsState (r));

                }

            }

            this.customLayout = true;

        } else {

            refs.stream ()
                .map (r -> trefs.get (r))
                .sorted (new NamedObjectSorter (this.viewer.getProject ()))
                .forEach (it ->
                {

                    try
                    {

                        TypeBox b = this.addType (it,
                                                  -1);

                        b.init (st.getAsState (it.getObjectReference ().asString ()));

                    } catch (Exception e) {

                        Environment.logError ("Unable to add idea type: " + it,
                                              e);

                    }

                });

        }
/*
        for (String r : refs)
        {

            IdeaType it = trefs.get (r);

            if (it != null)
            {

                TypeBox b = this.addType (it,
                                          -1);
                b.init (state.getAsState (r));

            }

        }
*/
        if (this.scrollpane != null)
        {

            double v = state.getAsFloat (State.Key.scrollpanev,
                                         0f);

            UIUtils.runLater (() ->
            {

                this.scrollpane.setVvalue (v);

            });

        }

    }

    public void setCustomLayout (boolean v)
    {

        this.customLayout = v;

    }

    public void addNewType (String  name,
                            String  iconType,
                            boolean showAdd)
                     throws GeneralException
    {

        IdeaType it = new IdeaType ();
        it.setName (name);
        it.setIconType (iconType);

        this.viewer.addNewIdeaType (it);

        if (showAdd)
        {

            UIUtils.runLater (() ->
            {

                this.ideaTypeBoxes.get (it).showAddNewIdea ();

            });

        }

    }

    public void deleteIdea (Idea idea)
    {

        try
        {

            this.viewer.deleteIdea (idea);

        } catch (Exception e) {

            Environment.logError ("Unable to delete idea: " + idea,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (ideaboard,ideas,delete,actionerror));
                                      //"Unable to delete Idea");

        }

    }

    public void deleteIdeaType (IdeaType type)
    {

        try
        {

            this.viewer.deleteIdeaType (type);

            TypeBox tb = this.ideaTypeBoxes.get (type);

            tb.dispose ();
            this.ideaTypeBoxes.remove (type);

            this.categories.getChildren ().remove (tb);

        } catch (Exception e) {

            Environment.logError ("Unable to delete idea type: " + type,
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (ideaboard,ideatypes,delete,actionerror));

        }

    }

    private TypeBox addType (IdeaType type,
                             int      addAt)
    {

        TypeBox ic = new TypeBox (type,
                                  this);

        this.ideaTypeBoxes.put (type,
                                ic);

        if (addAt > -1)
        {

            this.categories.getChildren ().add (addAt,
                                                ic);

        } else {

            this.categories.getChildren ().add (ic);

        }

        return ic;

    }

    public void showAddNewIdeaType ()
    {

        String pid = "newideatype";

        if (this.viewer.getPopupById (pid) != null)
        {

            this.viewer.getPopupById (pid).show ();
            return;

        }

        QuollPopup.textEntryBuilder ()
            .popupId (pid)
            .title (getUILanguageStringProperty (ideaboard,ideatypes,_new,title))
            .description (getUILanguageStringProperty (ideaboard,ideatypes,_new,text))
            .styleClassName (StyleClassNames.ADD)
            .withViewer (this.viewer)
            .validator (v ->
            {

                if ((v == null)
                    ||
                    (v.trim ().length () == 0)
                   )
                {

                    return getUILanguageStringProperty (ideaboard,ideatypes,_new,errors,novalue);

                }

                if (this.viewer.getProject ().getIdeaTypes ().stream ()
                    .filter (it -> it.getName ().equalsIgnoreCase (v))
                    .findFirst ()
                    .orElse (null) != null)
                {

                    return getUILanguageStringProperty (ideaboard,ideatypes,_new,errors,valueexists);

                }

                return null;

            })
            .confirmButtonLabel (getUILanguageStringProperty (ideaboard,ideatypes,_new,confirm))
            .onConfirm (ev ->
            {

                TextField tf = (TextField) ev.getForm ().lookup ("#text");

                String v = tf.getText ().trim ();

                try
                {

                    this.addNewType (v,
                                     null,
                                     true);

                } catch (Exception e)
                {

                    ev.consume ();

                    Environment.logError ("Unable to add new idea type with name: " +
                                          v,
                                          e);

                    ComponentUtils.showErrorMessage (this.viewer,
                                                     getUILanguageStringProperty (ideaboard,ideatypes,_new,actionerror));

                }

            })
            .build ();

    }

    public VerticalLayout getLayout ()
    {

        return this.categories;

    }

    @Override
    public Panel createPanel ()
    {

        Panel panel = Panel.builder ()
            .title (getUILanguageStringProperty (ideaboard,title))
            .content (this)
            .styleClassName (StyleClassNames.IDEABOARD)
            .styleSheet (StyleClassNames.IDEABOARD)
            .panelId (PANEL_ID)
            // TODO .headerControls ()
            .toolbar (() ->
            {

                return new LinkedHashSet<Node> ();

            })
            .build ();

        return panel;

    }

    private static class TypeBox extends VBox implements Stateful
    {

        private static final DataFormat dataformat = new DataFormat ("qw/ideatype");

        private VBox view = null;
        private Node helpText = null;
        private TextViewEditBox newIdea = null;
        private IdeaType type = null;
        private IdeaBoard board = null;
        private IPropertyBinder binder = new PropertyBinder ();
        private Map<Idea, IdeaBox> ideaBoxes = new HashMap<> ();

        public TypeBox (IdeaType  type,
                        IdeaBoard board)
        {

            this.type = type;
            this.board = board;

            final TypeBox _this = this;

            StringProperty title = new SimpleStringProperty ();

            Runnable setTitle = () ->
            {

                title.setValue (String.format ("%1$s (%2$s)",
                                               type.getName (),
                                               type.getIdeas ().size ()));

            };

            this.binder.addChangeListener (type.nameProperty (),
                                           (pr, oldv, newv) ->
            {

                setTitle.run ();

            });

            setTitle.run ();

            this.binder.addSetChangeListener (type.getIdeas (),
                                              ev ->
            {

                setTitle.run ();

                if (ev.wasAdded ())
                {

                    IdeaBox ib = new IdeaBox (ev.getElementAdded (),
                                              this.board.getViewer (),
                                              true);
                    this.view.getChildren ().add (0,
                                                  ib);
                    this.ideaBoxes.put (ev.getElementAdded (),
                                        ib);

                }

                if (ev.wasRemoved ())
                {

                    IdeaBox ib = this.ideaBoxes.get (ev.getElementRemoved ());
                    ib.dispose ();
                    this.ideaBoxes.remove (ev.getElementRemoved ());
                    this.view.getChildren ().remove (ib);

                }

            });

            Set<Node> headerCons = new LinkedHashSet<> ();

            headerCons.add (QuollButton.builder ()
                .tooltip (ideaboard,ideatypes,LanguageStrings.view,headercontrols,items,newidea,tooltip)
                .iconName (StyleClassNames.ADD)
                .onAction (ev ->
                {

                    this.showAddNewIdea ();

                })
                .build ());

            Header h = Header.builder ()
                .title (title)
                .controls (headerCons)
                .onlyShowToolbarOnMouseOver (true)
                .contextMenu (() ->
                {

                    Set<MenuItem> its = new LinkedHashSet<> ();
                    its.add (QuollMenuItem.builder ()
                        .label (ideaboard,ideatypes,LanguageStrings.view,popupmenu,items,newidea)
                        .iconName (StyleClassNames.ADD)
                        .onAction (ev ->
                        {

                            this.showAddNewIdea ();

                        })
                        .build ());

                    its.add (QuollMenu.builder ()
                        .label (ideaboard,ideatypes,LanguageStrings.view,popupmenu,items,sort)
                        .styleClassName (StyleClassNames.SORT)
                        .items (() ->
                        {

                            Set<MenuItem> sits = new LinkedHashSet<> ();

                            ToggleGroup tg = new ToggleGroup ();

                            String sb = _this.type.getSortBy ();

                            if (sb == null)
                            {

                                sb = IdeaType.SORT_BY_RATING;

                            }

                            RadioMenuItem mi = new RadioMenuItem ();
                            mi.setToggleGroup (tg);
                            mi.textProperty ().bind (getUILanguageStringProperty (ideaboard,ideatypes,LanguageStrings.view,sortrating));
                            mi.getStyleClass ().add (StyleClassNames.STAR);
                            mi.setSelected (sb.equals (IdeaType.SORT_BY_RATING));
                            mi.setOnAction (ev ->
                            {

                                _this.sortIdeas (IdeaType.SORT_BY_RATING);

                                _this.board.getViewer ().fireProjectEvent (ProjectEvent.Type.idea,
                                                                           ProjectEvent.Action.sort,
                                                                           IdeaType.SORT_BY_RATING);

                            });

                            sits.add (mi);

                            mi = new RadioMenuItem ();
                            mi.setToggleGroup (tg);
                            mi.textProperty ().bind (getUILanguageStringProperty (ideaboard,ideatypes,LanguageStrings.view,sortdate));
                            mi.getStyleClass ().add (StyleClassNames.DATE);
                            mi.setSelected (sb.equals (IdeaType.SORT_BY_DATE));
                            mi.setOnAction (ev ->
                            {

                                _this.sortIdeas (IdeaType.SORT_BY_DATE);

                                _this.board.getViewer ().fireProjectEvent (ProjectEvent.Type.idea,
                                                                           ProjectEvent.Action.sort,
                                                                           IdeaType.SORT_BY_RATING);

                            });

                            sits.add (mi);

                            mi = new RadioMenuItem ();
                            mi.setToggleGroup (tg);
                            mi.textProperty ().bind (getUILanguageStringProperty (ideaboard,ideatypes,LanguageStrings.view,sortalpha));
                            mi.getStyleClass ().add (StyleClassNames.ALPHA);
                            mi.setSelected (sb.equals (IdeaType.SORT_BY_TEXT));
                            mi.setOnAction (ev ->
                            {

                                _this.sortIdeas (IdeaType.SORT_BY_TEXT);

                                _this.board.getViewer ().fireProjectEvent (ProjectEvent.Type.idea,
                                                                           ProjectEvent.Action.sort,
                                                                           IdeaType.SORT_BY_RATING);

                            });

                            sits.add (mi);

                            return sits;

                        })
                        .build ());

                    its.add (QuollMenuItem.builder ()
                        .label (ideaboard,ideatypes,LanguageStrings.view,popupmenu,items, (this.view.isVisible () ? hide : show))
                        .iconName (this.view.isVisible () ? StyleClassNames.HIDE : StyleClassNames.SHOW)
                        .onAction (ev ->
                        {

                            _this.showIdeas (!this.view.isVisible ());

                        })
                        .build ());

                    its.add (QuollMenuItem.builder ()
                        .label (ideaboard,ideatypes,LanguageStrings.view,popupmenu,items,LanguageStrings.edit)
                        .iconName (StyleClassNames.EDIT)
                        .onAction (ev ->
                        {

                            String pid = "editideatype" + _this.type.getObjectReference ().asString ();

                            if (this.board.getViewer ().getPopupById (pid) != null)
                            {

                                this.board.getViewer ().getPopupById (pid).show ();
                                return;

                            }

                            QuollPopup.textEntryBuilder ()
                                .popupId (pid)
                                .title (getUILanguageStringProperty (ideaboard,ideatypes,edit,LanguageStrings.title))
                                .description (getUILanguageStringProperty (ideaboard,ideatypes,edit,text))
                                .styleClassName (StyleClassNames.EDIT)
                                .withViewer (this.board.getViewer ())
                                .validator (v ->
                                {

                                    if ((v == null)
                                        ||
                                        (v.trim ().length () == 0)
                                       )
                                    {

                                        return getUILanguageStringProperty (ideaboard,ideatypes,edit,errors,novalue);

                                    }

                                    if (this.board.getViewer ().getProject ().getIdeaTypes ().stream ()
                                        .filter (it -> it.getName ().equalsIgnoreCase (v) && !it.equals (_this.type))
                                        .findFirst ()
                                        .orElse (null) != null)
                                    {

                                        return getUILanguageStringProperty (ideaboard,ideatypes,edit,errors,valueexists);

                                    }

                                    return null;

                                })
                                .confirmButtonLabel (getUILanguageStringProperty (ideaboard,ideatypes,edit,confirm))
                                .onConfirm (eev ->
                                {

                                    TextField tf = (TextField) eev.getForm ().lookup ("#text");

                                    String v = tf.getText ().trim ();

                                    try
                                    {

                                        _this.type.setName (v);

                                        this.board.getViewer ().updateIdeaType (_this.type);

                                    } catch (Exception e)
                                    {

                                        ev.consume ();

                                        Environment.logError ("Unable to save idea type: " + _this.type,
                                                              e);

                                        ComponentUtils.showErrorMessage (this.board.getViewer (),
                                                                         getUILanguageStringProperty (ideaboard,ideatypes,edit,actionerror));

                                    }

                                })
                                .build ();

                        })
                        .build ());

                    its.add (QuollMenuItem.builder ()
                        .label (ideaboard,ideatypes,LanguageStrings.view,popupmenu,items,delete)
                        .iconName (StyleClassNames.DELETE)
                        .onAction (ev ->
                        {

                            if (_this.type.getIdeas ().size () == 0)
                            {

                                _this.board.deleteIdeaType (_this.type);

                            } else
                            {

                                String pid = "delete" + _this.type.getObjectReference ().asString ();

                                if (_this.board.getViewer ().getPopupById (pid) != null)
                                {

                                    return;

                                }

                                QuollPopup qp = UIUtils.showDeleteObjectPopup (getUILanguageStringProperty (ideaboard,ideatypes,delete,deletetype),
                                                                               getUILanguageStringProperty (objectnames,singular,IdeaType.OBJECT_TYPE),
                                                                               StyleClassNames.DELETE,
                                                                          getUILanguageStringProperty (ideaboard,ideatypes,delete,warning),
                                                                          // On confirm.
                                                                          eev ->
                                                                          {

                                                                              _this.board.deleteIdeaType (_this.type);


                                                                          },
                                                                          // ON cancel
                                                                          eev ->
                                                                          {

                                                                          },
                                                                          _this.board.getViewer ());

                                qp.setPopupId (pid);

                            }

                        })
                        .build ());

                    return its;

                })
                .build ();

            h.getTitle ().addEventHandler (MouseEvent.MOUSE_CLICKED,
                                           ev ->
            {

                if (ev.getButton () != MouseButton.PRIMARY)
                {

                    return;

                }

                this.showIdeas (!this.view.isVisible ());
                ev.consume ();

            });

            h.setOnDragDetected (ev ->
            {

                Dragboard db = this.startDragAndDrop (TransferMode.MOVE);
                db.setDragView (this.snapshot (null, null));
                ClipboardContent c = new ClipboardContent ();
                c.put (TypeBox.dataformat,
                       this.type.getObjectReference ().asString ());
                db.setContent (c);
                this.pseudoClassStateChanged (StyleClassNames.DRAGGING_PSEUDO_CLASS, true);
                ev.consume ();

            });

            this.setOnDragExited (ev ->
            {

                this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            });

            this.setOnDragOver (ev ->
            {

                this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

                Dragboard db = ev.getDragboard ();

                Object o = db.getContent (TypeBox.dataformat);

                if (o != null)
                {

                    this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, true);

                    ev.acceptTransferModes (TransferMode.ANY);

                }

                ev.consume ();

            });

            this.setOnDragDropped (ev ->
            {

                this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

                Dragboard db = ev.getDragboard ();

                Object o = db.getContent (TypeBox.dataformat);

                if (o != null)
                {

                    for (IdeaType it : this.board.ideaTypeBoxes.keySet ())
                    {

                        if (it.getObjectReference ().asString ().equals (o.toString ()))
                        {

                            TypeBox b = this.board.ideaTypeBoxes.get (it);

                            int ind = this.board.getLayout ().getChildren ().indexOf (this);

                            this.board.setCustomLayout (true);
                            this.board.getLayout ().getChildren ().remove (b);

                            this.board.getLayout ().getChildren ().add (ind,
                                                                        b);

                        }

                    }

                }

                ev.setDropCompleted (true);
                ev.consume ();

            });

            this.setOnDragDone (ev ->
            {

                this.pseudoClassStateChanged (StyleClassNames.DRAGOVER_PSEUDO_CLASS, false);

            });

            this.setOnDragEntered (ev ->
            {

                ev.consume ();

            });

            this.getStyleClass ().add (StyleClassNames.IDEATYPE);
            this.getChildren ().add (h);

            if (type.getIconType () != null)
            {

                h.getStyleClass ().add (type.getIconType ());

                UserConfigurableObjectType t = null;

                if (!type.getIconType ().equals (Chapter.OBJECT_TYPE))
                {

                    try
                    {

                        // TODO: Not a good hack...
                        if (type.getIconType ().startsWith ("asset:"))
                        {

                            t = this.board.getViewer ().getProject ().getUserConfigurableObjectType (Long.parseLong (type.getIconType ().substring ("asset:".length ())));
                            //t = Environment.getUserConfigurableObjectType (Long.parseLong (type.getIconType ().substring ("asset:".length ())));

                        } else {

                            t = this.board.getViewer ().getProject ().getUserConfigurableObjectType (type.getIconType ());
                            //t = Environment.getUserConfigurableObjectType (type.getIconType ());

                        }

                    } catch (Exception e) {

                        Environment.logError ("Unable to get user object type for: " +
                                              type.getIconType (),
                                              e);

                    }

                }

                if (t != null)
                {

                    h.getIcon ().setImage (t.icon16x16Property (),
                                           board.getBinder ());
/*
                    UIUtils.setBackgroundImage (h.getIcon (),
                                                t.icon16x16Property (),
                                                board.getBinder ());
*/
                } else {

                    h.setIconClassName (type.getIconType ());

                }

            } else {

                h.setIconClassName (StyleClassNames.IDEA);

            }

            this.view = new VBox ();
            this.view.managedProperty ().bind (this.view.visibleProperty ());
            this.view.getStyleClass ().add (StyleClassNames.IDEAS);

            this.newIdea = TextViewEditBox.builder ()
                .editPlaceHolder (getUILanguageStringProperty (ideaboard,ideas,_new,text,tooltip))
                .formattingEnabled (true)
                .styleClassName (StyleClassNames.NEWIDEA)
                .withViewer (this.board.getViewer ())
                .editOnly (true)
                .onSave (newText ->
                {

                    if (!newText.hasText ())
                    {

                        return false;

                    }

                    Idea i = new Idea ();
                    i.setDescription (newText);
                    i.setType (this.type);

                    // Ask the project viewer to save the new object.
                    try
                    {

                        board.getViewer ().addNewIdea (i);

                        this.view.setVisible (true);
                        this.newIdea.setText (new StringWithMarkup (""));
                        this.newIdea.setVisible (false);

                        return true;

                    } catch (Exception e)
                    {

                        Environment.logError ("Unable to save new idea: " +
                                              i,
                                              e);

                        ComponentUtils.showErrorMessage (board.getViewer (),
                                                         getUILanguageStringProperty (ideaboard,ideas,_new,actionerror));
                                                  //"Unable to save new Idea.");

                        return false;

                    }

                })
                .onCancel (ev ->
                {

                    this.newIdea.setText (new StringWithMarkup (""));
                    this.newIdea.setVisible (false);

                })
                .build ();
            this.newIdea.managedProperty ().bind (this.newIdea.visibleProperty ());
            this.newIdea.setVisible (false);

            this.helpText = QuollTextView.builder ()
                .styleClassName (StyleClassNames.HELP)
                .text (getUILanguageStringProperty (ideaboard,ideatypes,LanguageStrings.view,noideas))
                .inViewer (this.board.getViewer ())
                .build ();
            this.helpText.managedProperty ().bind (this.helpText.visibleProperty ());
            this.helpText.setVisible (false);

            this.helpText.addEventHandler (MouseEvent.MOUSE_RELEASED,
                                           ev ->
            {

                this.helpText.setVisible (false);
                this.showAddNewIdea ();

            });

            this.getChildren ().addAll (this.newIdea, this.helpText, this.view);
            this.addIdeasToView ();

        }

        public void dispose ()
        {

            this.binder.dispose ();

        }

        public void showIdeas (boolean vis)
        {

            this.view.setVisible (vis);

            if (this.view.isVisible ())
            {

                this.helpText.setVisible (this.type.getIdeas ().size () == 0);

            } else {

                this.helpText.setVisible (false);

            }

        }

        @Override
        public State getState ()
        {

            State s = new State ();

            s.set ("visible",
                   this.view.isVisible ());

            return s;

        }

        @Override
        public void init (State state)
                   throws GeneralException
        {

            // Todo... XXX
            if (state == null)
            {

                return;

            }

            this.showIdeas (state.getAsBoolean ("visible"));

        }

        public void sortIdeas (String sortBy)
        {

            this.type.setSortBy (sortBy);

            try
            {

                this.board.getViewer ().saveObject (this.type,
                                                    false);

            } catch (Exception e)
            {

                Environment.logError ("Unable to update idea type: " +
                                      this.type +
                                      ", cannot set sort type: " +
                                      sortBy,
                                      e);

            }

            this.addIdeasToView ();

        }

        private void addIdeasToView ()
        {

            String sb = this.type.getSortBy ();

            if (sb == null)
            {

                sb = IdeaType.SORT_BY_RATING;

            }

            List<Idea> ideas = new ArrayList<> (this.type.getIdeas ());

            Collections.sort (ideas,
                              new IdeaTypeComparator (sb));

            List<Node> children = this.view.getChildren ();

            if (children.size () == 0)
            {

                for (Idea i : ideas)
                {

                    this.ideaBoxes.put (i,
                                        new IdeaBox (i,
                                                     this.board.getViewer (),
                                                     false));

                }

            }

            this.view.getChildren ().clear ();

            for (Idea i : ideas)
            {

                this.view.getChildren ().add (this.ideaBoxes.get (i));

            }

            /*
            this.ideaBoxes.values ().stream ()
                .forEach (ib -> ib.dispose ());
            this.ideaBoxes.clear ();

            List<Idea> ideas = new ArrayList<> (this.type.getIdeas ());

            Collections.sort (ideas,
                              new IdeaTypeComparator (sb));

            this.view.getChildren ().addAll (ideas.stream ()
                .map (i ->
                {

                    IdeaBox ib = new IdeaBox (i,
                                              this.board.getViewer (),
                                              false);

                    this.ideaBoxes.put (i,
                                        ib);

                    return ib;

                })
                .collect (Collectors.toList ()));
*/

            this.view.getChildren ().stream ()
                .forEach (el ->
                {

                    el.pseudoClassStateChanged (StyleClassNames.LAST_PSEUDO_CLASS, false);

                });

            if (this.view.getChildren ().size () > 0)
            {

                this.view.getChildren ().get (ideas.size () - 1).pseudoClassStateChanged (StyleClassNames.LAST_PSEUDO_CLASS, true);

            }

            this.view.setVisible (ideas.size () > 0);

        }

        public IdeaType getIdeaType ()
        {

            return this.type;

        }

        public void showAddNewIdea ()
        {

            this.newIdea.setVisible (true);
            this.helpText.setVisible (false);

        }

    }

    public static class IdeaBox extends VBox
    {

        private Idea idea = null;
        private TextViewEditBox edit = null;
        private VBox view = null;
        private IPropertyBinder binder = new PropertyBinder ();

        public IdeaBox (Idea          i,
                        ProjectViewer viewer,
                        boolean       showFull)
        {

            final IdeaBox _this = this;

            this.idea = i;
            this.setUserData (this.idea);
            this.getStyleClass ().add (StyleClassNames.IDEA);

            List<String> prefix = Arrays.asList (ideaboard,ideas,LanguageStrings.view);

            this.view = new VBox ();
            this.view.getStyleClass ().add (StyleClassNames.VIEW);
            this.view.managedProperty ().bind (this.view.visibleProperty ());
            //this.view.setVisible (false);
            this.getChildren ().add (this.view);

            StringProperty shortDescP = new SimpleStringProperty ();
            // TODO Use QuollTextView
            //BasicHtmlTextFlow shortDesc = BasicHtmlTextFlow.builder ()
            QuollTextView shortDesc = QuollTextView.builder ()
                .styleClassName (StyleClassNames.SHORTTEXT)
                //.withHandler (viewer)
                .inViewer (viewer)
                .text (shortDescP)
                .build ();
            shortDesc.managedProperty ().bind (shortDesc.visibleProperty ());
            shortDesc.setVisible (!showFull);

            this.view.getChildren ().add (shortDesc);

            StringProperty fullDescP = new SimpleStringProperty ();

            // TODO Use QuollTextView
            //BasicHtmlTextFlow fullDesc = BasicHtmlTextFlow.builder ()
            QuollTextView fullDesc = QuollTextView.builder ()
                .styleClassName (StyleClassNames.FULLTEXT)
                //.withHandler (viewer)
                .inViewer (viewer)
                .text (fullDescP)
                .build ();
            fullDesc.managedProperty ().bind (fullDesc.visibleProperty ());
            fullDesc.setVisible (showFull);

            this.view.getChildren ().add (fullDesc);

            Runnable setFullP = () ->
            {

                StringWithMarkup sm = this.idea.getDescription ();

                fullDescP.setValue (UIUtils.markupText (sm,
                                                        viewer,
                                                        null));
                //fullDescP.setValue (sm.getMarkedUpText ());

            };

            Runnable setShortP = () ->
            {

                StringWithMarkup sm = this.idea.getDescription ();

                String tt = UIUtils.markupText (sm,
                                                viewer,
                                                null);

                Paragraph p = new Paragraph (tt,
                //sm.getText (),
                                             0);

                String firstSent = "";

                if (p.getSentenceCount () > 0)
                {

                    firstSent = p.getFirstSentence ().markupAsHTML (sm.getMarkup ());

                    if (p.getSentenceCount () > 1)
                    {

                        firstSent += getUILanguageStringProperty (ideaboard,ideas,LanguageStrings.view,shorttext,more).getValue ();

                    }

                }

                //shortDesc.setText (firstSent);
                shortDescP.setValue (firstSent);

            };

            setFullP.run ();
            setShortP.run ();

            this.binder.addChangeListener (this.idea.descriptionProperty (),
                                           (pr, oldv, newv) ->
            {

                setFullP.run ();
                setShortP.run ();

            });

            ToolBar tb = new ToolBar ();
            HBox tbb = new HBox ();
            tbb.managedProperty ().bind (tbb.visibleProperty ());
            tbb.setVisible (showFull);

            tb.getItems ().add (QuollButton.builder ()
                .iconName (StyleClassNames.EDIT)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,buttons,LanguageStrings.edit,tooltip)))
                .onAction (ev ->
                {

                    this.edit.setText (this.idea.getDescription ());
                    this.edit.showEdit ();
                    this.view.setVisible (false);
                    this.edit.setVisible (true);

                })
                .build ());

            tb.getItems ().add (QuollMenuButton.builder ()
                .iconName (StyleClassNames.CONVERT)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,buttons,convert,tooltip)))
                .items (() ->
                {

                    Set<MenuItem> items = new LinkedHashSet<> ();

                    items.add (QuollMenuItem.builder ()
                        .iconName (Chapter.OBJECT_TYPE)
                        .label (getUILanguageStringProperty (objectnames,singular,Chapter.OBJECT_TYPE))
                        .onAction (eev ->
                        {

                            final Chapter ch = viewer.getProject ().getBook (0).getLastChapter ();

                            Chapter newCh = new Chapter (ch.getBook (),
                                                         null);

                            newCh.setDescription (_this.idea.getDescription ());

                            viewer.showAddNewChapter (newCh,
                                                      viewer.getProject ().getBook (0).getLastChapter ());

                        })
                        .build ());

                    try
                    {

                        items.addAll (UIUtils.getNewAssetMenuItems (viewer,
                                                                    null,
                                                                    idea.getDescription ()));

                    } catch (Exception e) {

                        Environment.logError ("Unable to add new asset menu items.",
                                              e);

                    }

                    return items;

                })
                .build ());

            tb.getItems ().add (QuollButton.builder ()
                .iconName (StyleClassNames.DELETE)
                .styleClassName (StyleClassNames.DELETE)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,buttons,delete,tooltip)))
                .onAction (ev ->
                {

                    List<String> prefix2 = Arrays.asList (ideaboard,ideas,delete,popup);

                    String pid = "delete" + _this.idea.getObjectReference ().asString ();

                    if (viewer.getPopupById (pid) != null)
                    {

                        return;

                    }

                    QuollPopup.questionBuilder ()
                        .styleClassName (StyleClassNames.DELETE)
                        .withViewer (viewer)
                        .popupId (pid)
                        .title (getUILanguageStringProperty (Utils.newList (prefix2,title)))
                        .message (getUILanguageStringProperty (Utils.newList (prefix2,text)))
                        .confirmButtonLabel (getUILanguageStringProperty (Utils.newList (prefix2,buttons,confirm)))
                        .cancelButtonLabel (getUILanguageStringProperty (Utils.newList (prefix2,buttons,cancel)))
                        .showAt (tb.lookupAll (".button.delete").iterator ().next (),
                                 Side.BOTTOM)
                        .onConfirm (eev ->
                        {

                            try
                            {

                                viewer.deleteIdea (_this.idea);

                            } catch (Exception e) {

                                Environment.logError ("Unable to delete idea: " + _this.idea,
                                                      e);

                                ComponentUtils.showErrorMessage (viewer,
                                                                 getUILanguageStringProperty (ideaboard,ideas,delete,actionerror));

                            }

                            viewer.getPopupById (pid).close ();

                        })
                        .build ();

                })
                .build ());

            tb.getItems ().add (QuollButton.builder ()
                .iconName (StyleClassNames.UP)
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,buttons,hide,tooltip)))
                .onAction (ev ->
                {

                    shortDesc.setVisible (true);
                    fullDesc.setVisible (false);
                    tbb.setVisible (false);

                })
                .build ());

            shortDesc.addEventHandler (MouseEvent.MOUSE_CLICKED,
                                       ev ->
            {

                if (ev.getButton () != MouseButton.PRIMARY)
                {

                    return;

                }

                shortDesc.setVisible (false);
                fullDesc.setVisible (true);
                tbb.setVisible (true);
                ev.consume ();

            });

            HBox.setHgrow (tb,
                           Priority.ALWAYS);
            tbb.getChildren ().add (tb);

            StarBar sb = StarBar.builder ()
                .tooltip (getUILanguageStringProperty (Utils.newList (prefix,buttons,rating,tooltip)))
                .value (this.idea.getRating ())
                .build ();
            sb.valueProperty ().addListener ((pr, oldv, newv) ->
            {

                try
                {

                    this.idea.setRating (newv.intValue ());

                    viewer.updateIdea (this.idea);

                    viewer.fireProjectEvent (ProjectEvent.Type.idea,
                                             ProjectEvent.Action.rate,
                                             this.idea);

                } catch (Exception e) {

                    Environment.logError ("Unable to update idea: " + this.idea,
                                          e);

                    ComponentUtils.showErrorMessage (viewer,
                                                     getUILanguageStringProperty (ideaboard,ideas,save,actionerror));

                }

            });
            tbb.getChildren ().add (sb);

            this.view.getChildren ().add (tbb);

            this.edit = TextViewEditBox.builder ()
                .styleClassName (StyleClassNames.EDIT)
                .withViewer (viewer)
                .formattingEnabled (true)
                .editOnly (true)
                .onSave (newText ->
                {

                    try
                    {

                        this.idea.setDescription (newText);

                        viewer.updateIdea (this.idea);

                        this.edit.setVisible (false);
                        this.view.setVisible (true);

                        return true;

                    } catch (Exception e) {

                        Environment.logError ("Unable to update idea: " + this.idea,
                                              e);

                        ComponentUtils.showErrorMessage (viewer,
                                                         getUILanguageStringProperty (ideaboard,ideas,save,actionerror));

                        return false;

                    }

                })
                .onCancel (ev ->
                {

                    this.edit.setVisible (false);
                    this.view.setVisible (true);

                })
                .build ();

            this.edit.managedProperty ().bind (this.edit.visibleProperty ());
            this.getChildren ().add (this.edit);
            this.edit.setVisible (false);

        }

        public void dispose ()
        {

            this.binder.dispose ();

        }

        public Idea getIdea ()
        {

            return this.idea;

        }

    }

}
