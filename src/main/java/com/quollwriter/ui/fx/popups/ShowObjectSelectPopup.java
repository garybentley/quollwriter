package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.text.*;

import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.geometry.*;
import javafx.scene.input.*;
import javafx.collections.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ShowObjectSelectPopup<T> extends PopupContent
{

    private Builder<T> builder = null;
    private ScrollPane scroll = null;
    private FocusModel<T> focusModel = null;
    private SingleSelectionModel<T> selectionModel = null;

    private ShowObjectSelectPopup (Builder<T> b)
    {

        super (b.viewer);

        this.builder = b;

        this.selectionModel = new SingleSelectionModel<T> ()
        {

            @Override
            protected int getItemCount ()
            {

                return b.objs.size ();

            }

            @Override
            protected T getModelItem (int ind)
            {

                return b.objs.get (ind);

            }

        };

        this.focusModel = new FocusModel<T> ()
        {

            @Override
            protected int getItemCount ()
            {

                return b.objs.size ();

            }

            @Override
            protected T getModelItem (int ind)
            {

                if ((ind < 0)
                    ||
                    (ind > b.objs.size () - 1)
                   )
                {

                    return null;

                }

                return b.objs.get (ind);

            }

        };

        final ShowObjectSelectPopup _this = this;

        // We don't need to have this reactive to changes, maybe add later.
        VBox vb = new VBox ();
        vb.getStyleClass ().add (StyleClassNames.ITEMS);

        for (T o : b.objs)
        {

            Node n = b.cellProvider.apply (o, this);
            n.setUserData (o);

            vb.getChildren ().add (n);

        }

        ListChangeListener<T> l = ev ->
        {

            while (ev.next ())
            {

                for (T el : ev.getRemoved ())
                {

                    Node r = null;

                    for (Node n : vb.getChildren ())
                    {

                        if (n.getUserData ().equals (el))
                        {

                            r = n;
                            break;

                        }

                    }

                    vb.getChildren ().remove (r);

                }

                vb.getChildren ().addAll (ev.getFrom (),
                                          ev.getAddedSubList ().stream ()
                                            .map (v ->
                                            {

                                                Node n = b.cellProvider.apply (v, this);
                                                n.setUserData (v);
                                                return n;

                                            })
                                            .collect (Collectors.toList ()));

            }

            if (vb.getChildren ().size () > 0)
            {

                vb.getChildren ().get (0).pseudoClassStateChanged (StyleClassNames.FIRST_PSEUDO_CLASS, true);
                vb.getChildren ().get (vb.getChildren ().size () - 1).pseudoClassStateChanged (StyleClassNames.LAST_PSEUDO_CLASS, true);

            }

            UIUtils.forceRunLater (() ->
            {

                int c = vb.getChildren ().size ();

                if (vb.getChildren ().size () > b.visibleCount)
                {

                    c = b.visibleCount;

                }

                vb.applyCss ();
                vb.layout ();

                double h = 1;

                for (int i = 0; i < c; i++)
                {

                    h += ((Region) vb.getChildren ().get (i)).getHeight ();

                }

                this.scroll.setPrefHeight (h);
                this.scroll.setMinHeight (h);
                this.scroll.setMaxHeight (h);

                //vb.getChildren ().get (this.focusModel.getFocusedIndex ()).pseudoClassStateChanged (StyleClassNames.FOCUSED_PSEUDO_CLASS, true);

            });

        };

        b.objs.addListener (l);

        if (vb.getChildren ().size () > 0)
        {

            vb.getChildren ().get (0).pseudoClassStateChanged (StyleClassNames.FIRST_PSEUDO_CLASS, true);
            vb.getChildren ().get (vb.getChildren ().size () - 1).pseudoClassStateChanged (StyleClassNames.LAST_PSEUDO_CLASS, true);

        }

        this.selectionModel.selectedIndexProperty ().addListener ((pr, oldv, newv) ->
        {

            if ((oldv.intValue () > -1)
                &&
                (oldv.intValue () < vb.getChildren ().size ())
               )
            {

                vb.getChildren ().get (oldv.intValue ()).pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, false);

            }

            vb.getChildren ().get (newv.intValue ()).pseudoClassStateChanged (StyleClassNames.SELECTED_PSEUDO_CLASS, true);

        });

        this.focusModel.focusedIndexProperty ().addListener ((pr, oldv, newv) ->
        {

            if ((oldv.intValue () > -1)
                &&
                (oldv.intValue () < vb.getChildren ().size ())
               )
            {

                vb.getChildren ().get (oldv.intValue ()).pseudoClassStateChanged (StyleClassNames.FOCUSED_PSEUDO_CLASS, false);

            }

            if ((newv.intValue () > -1)
                &&
                (newv.intValue () < vb.getChildren ().size ())
               )
            {

                vb.getChildren ().get (newv.intValue ()).pseudoClassStateChanged (StyleClassNames.FOCUSED_PSEUDO_CLASS, true);

                UIUtils.scrollIntoView (vb.getChildren ().get (newv.intValue ()),
                                        VPos.TOP);

            }

        });

        if (b.allowNavigationByKeys)
        {

            vb.setOnKeyPressed (ev ->
            {

                if (ev.getCode () == KeyCode.ENTER)
                {

                    this.selectionModel.select (this.focusModel.getFocusedIndex ());

                }

                if (ev.getCode () == KeyCode.UP)
                {

                    this.updateSelection (-1,
                                          b.objs.size ());

                }

                if (ev.getCode () == KeyCode.DOWN)
                {

                    this.updateSelection (1,
                                          b.objs.size ());

                }

                ev.consume ();

            });

        }

        VBox content = new VBox ();
        this.getChildren ().add (content);

        if (b.above != null)
        {

            content.getChildren ().add (b.above);

        }

        this.scroll = new ScrollPane (vb);

        content.getChildren ().add (this.scroll);

        if (b.below != null)
        {

            content.getChildren ().add (b.below);

        }

        UIUtils.forceRunLater (() ->
        {

            int c = vb.getChildren ().size ();

            if (vb.getChildren ().size () > b.visibleCount)
            {

                c = b.visibleCount;

            }

            vb.applyCss ();
            vb.layout ();

            double h = 1;

            for (int i = 0; i < c; i++)
            {

                h += ((Region) vb.getChildren ().get (i)).getHeight ();

            }

            this.scroll.setPrefHeight (h);
            this.scroll.setMinHeight (h);
            this.scroll.setMaxHeight (h);

        });

    }

    private void updateSelection (int incr,
                                  int size)
    {

        int i = this.focusModel.getFocusedIndex ();

        i += incr;

        int s = size;

        if (i < 0)
        {

            i = s + i;

        }

        if (i > s - 1)
        {

            i -= s;

        }

        this.focusModel.focus (i);

    }

    public SingleSelectionModel<T> getSelectionModel ()
    {

        return this.selectionModel;

    }

    public FocusModel<T> getFocusModel ()
    {

        return this.focusModel;

    }

    @Override
    public QuollPopup createPopup ()
    {

        List<String> styleSheets = new ArrayList<> ();
        styleSheets.add (StyleClassNames.OBJECTSELECT);

        if (builder.styleSheets != null)
        {

            styleSheets.addAll (builder.styleSheets);

        }

        QuollPopup p = QuollPopup.builder ()
            .title (builder.title)
            .styleClassName (builder.styleName)
            .styleSheets (styleSheets)
            .headerIconClassName (builder.headerIconClassName)
            .hideOnEscape (true)
            .withClose (builder.withClose)
            .content (this)
            .popupId (builder.popupId)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .showAt (builder.showAt,
                     builder.showWhere)
            .build ();

        p.requestFocus ();

        return p;

    }

    /**
     * Get a builder.
     *
     * @returns A new builder.
     */
    public static <T> ShowObjectSelectPopup.Builder<T> builder ()
    {

        return new Builder<> ();

    }

    public static class Builder<T> implements IBuilder<Builder<T>, ShowObjectSelectPopup<T>>
    {

        private StringProperty title = null;
        private StringProperty desc = null;
        private String styleName = null;
        private BiFunction<T, ShowObjectSelectPopup, Node> cellProvider = null;
        private String popupId = null;
        private AbstractViewer viewer = null;
        private ObservableList<T> objs = null;
        private String headerIconClassName = null;
        private Node above = null;
        private Node below = null;
        private Node showAt = null;
        private Side showWhere = null;
        private List<String> styleSheets = new ArrayList<> ();
        private boolean withClose = true;
        private int visibleCount = 7;
        private boolean allowNavigationByKeys = false;

        private Builder ()
        {

        }

        @Override
        public ShowObjectSelectPopup<T> build ()
        {

            return new ShowObjectSelectPopup<> (this);

        }

        @Override
        public Builder<T> _this ()
        {

            return this;

        }

        public Builder<T> visibleCount (int v)
        {

            this.visibleCount = v;
            return _this ();

        }

        public Builder<T> styleSheet (List<String> ss)
        {

            if (ss != null)
            {

                this.styleSheets.addAll (ss);

            }

            return _this ();

        }

        public Builder<T> styleSheet (String... s)
        {

            if (s != null)
            {

                for (String st : s)
                {

                    this.styleSheets.add (st);

                }

            }

            return _this ();

        }

        public Builder<T> allowNavigationByKeys (boolean v)
        {

            this.allowNavigationByKeys = v;
            return _this ();

        }

        public Builder<T> showAt (Node n,
                                  Side where)
        {

            this.showAt = n;
            this.showWhere = where;
            return _this ();

        }

        public Builder<T> showAboveObjects (Node n)
        {

            this.above = n;
            return _this ();

        }

        public Builder<T> showBelowObjects (Node n)
        {

            this.below = n;
            return _this ();

        }

        public Builder<T> objects (ObservableList<T> objs)
        {

            this.objs = objs;
            return this;

        }

        public Builder<T> withViewer (AbstractViewer v)
        {

            this.viewer = v;
            return this;

        }

        public Builder<T> popupId (String id)
        {

            this.popupId = id;
            return this;

        }

        public Builder<T> headerIconClassName (String c)
        {

            this.headerIconClassName = c;
            return this;

        }

        public Builder<T> cellProvider (BiFunction<T, ShowObjectSelectPopup, Node> prov)
        {

            this.cellProvider = prov;
            return this;

        }

        public Builder<T> styleClassName (String n)
        {

            this.styleName = n;
            return this;

        }

        public Builder<T> title (StringProperty prop)
        {

            this.title = prop;
            return this;

        }

        public Builder<T> title (List<String> prefix,
                                 String...    ids)
        {

            return this.title (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder<T> title (String... ids)
        {

            return this.title (getUILanguageStringProperty (ids));

        }

        public Builder<T> description (StringProperty prop)
        {

            this.desc = prop;
            return this;

        }

        public Builder<T> description (List<String> prefix,
                                       String...    ids)
        {

            return this.description (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder<T> description (String... ids)
        {

            return this.description (getUILanguageStringProperty (ids));

        }

        public Builder<T> withClose (boolean v)
        {

            this.withClose = v;
            return this;

        }

    }

}
