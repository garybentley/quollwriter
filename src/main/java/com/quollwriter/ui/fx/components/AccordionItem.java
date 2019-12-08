package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import javafx.util.*;
import javafx.animation.*;
import animatefx.animation.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class AccordionItem extends VBox implements Stateful
{

    protected Header header = null;
    private Node openContent = null;
    private Node closedContent = null;
    private String accId = null;
    private BooleanProperty contentVisibleProp = null;
    private boolean open = false;

    private AccordionItem (Builder b)
    {

        final AccordionItem _this = this;

        if (b.title == null)
        {

            throw new IllegalArgumentException ("A title must be provided.");

        }

        if (b.openContent == null)
        {

            throw new IllegalArgumentException ("An open content node must be provided.");

        }

        this.contentVisibleProp = new SimpleBooleanProperty ();
        this.accId = b.accId;

        this.getStyleClass ().add (StyleClassNames.ACCORDION);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        this.header = Header.builder ()
            .controls (b.headerCons)
            .title (b.title)
            .tooltip (project,sidebar,section,title,tooltip)
            // TODO REmove, use css .cursor (Cursor.HAND)
            .contextMenu (b.contextMenuItemSupplier)
            .build ();

        this.header.titleLabelProperty ().getValue ().setOnMouseClicked (ev ->
        {

            if (ev.isPopupTrigger ())
            {

                return;

            }

            this.toggleState ();

        });

        this.getChildren ().add (this.header);

        b.openContent.managedProperty ().bind (b.openContent.visibleProperty ());
        b.openContent.getStyleClass ().add (StyleClassNames.CONTENT);
        this.openContent = b.openContent;
        this.openContent.pseudoClassStateChanged (StyleClassNames.OPEN_PSEUDO_CLASS, true);

        if (b.closedContent != null)
        {

            b.closedContent.managedProperty ().bind (b.closedContent.visibleProperty ());
            b.closedContent.getStyleClass ().add (StyleClassNames.CONTENT);

            this.closedContent = b.closedContent;
            this.closedContent.pseudoClassStateChanged (StyleClassNames.CLOSED_PSEUDO_CLASS, true);

            this.getChildren ().add (this.closedContent);

            this.contentVisibleProp.addListener ((pr, old, newv) ->
            {

                this.closedContent.setVisible (!newv);

            });

        }

        this.setState (b.open);

        this.getChildren ().add (this.openContent);

    }

    private void toggleState ()
    {

        this.setState (!this.open);

    }

    public Header getHeader ()
    {

        return this.header;

    }

    public void init (State s)
    {

        if (s == null)
        {

            return;

        }

        this.setState (s.getAsBoolean (State.Key.open));

    }

    public State getState ()
    {

        State s = new State ();
        s.set (State.Key.open,
               this.open);

        return s;

    }

    public String getAccordionId ()
    {

        return this.accId;

    }

    public BooleanProperty contentVisibleProperty ()
    {

        // TODO Make read only.
        return this.contentVisibleProp;

    }

    private void setState (boolean open)
    {

        this.openContent.setVisible (open);

        this.open = this.openContent.isVisible ();
        this.contentVisibleProp.setValue (this.open);

    }

    public void setContentVisible (boolean v)
    {

        this.setState (v);

    }

    public void close ()
    {

        this.setState (false);

    }

    public void open ()
    {

        this.setState (true);

    }

    public boolean isOpen ()
    {

        return this.open;

    }

    /**
     * Get a builder to create a new AccordionItem.
     *
     * Usage: AccordionItem.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder<X extends Builder<X>> implements IBuilder<X, AccordionItem>
     //implements IBuilder<Builder, AccordionItem>
    {

        private StringProperty title = null;
        private Node openContent = null;
        private Node closedContent = null;
        private Set<Node> headerCons = null;
        private String styleName = null;
        private String accId = null;
        private boolean open = true;
        private Supplier<Set<MenuItem>> contextMenuItemSupplier = null;

        protected Builder ()
        {

        }

        @Override
        public AccordionItem build ()
        {

            return new AccordionItem (this);

        }

        @Override
        public X _this ()
        {

            return (X) this;

        }

        public X styleClassName (String name)
        {

            this.styleName = name;
            return _this ();

        }

        public X openContent (Node n)
        {

            this.openContent = n;
            return _this ();

        }

        public X closedContent (Node n)
        {

            this.closedContent = n;
            return _this ();

        }

        public X title (String... ids)
        {

            return this.title (getUILanguageStringProperty (ids));

        }

        public X title (List<String> prefix,
                        String... ids)
        {

            return this.title (getUILanguageStringProperty (Utils.newList (prefix,ids)));

        }

        public X accordionId (String id)
        {

            this.accId = id;
            return _this ();

        }

        public X title (StringProperty prop)
        {

            this.title = prop;
            return _this ();

        }

        public X headerControls (Set<Node> cons)
        {

            this.headerCons = cons;
            return _this ();

        }

        public X open (boolean v)
        {

            this.open = v;
            return _this ();

        }

        public X contextMenu (final Set<MenuItem> items)
        {

            this.contextMenuItemSupplier = new Supplier<Set<MenuItem>> ()
            {

                @Override
                public Set<MenuItem> get ()
                {

                    return items;

                }

            };

            return _this ();

        }

        public X contextMenu (Supplier<Set<MenuItem>> items)
        {

            this.contextMenuItemSupplier = items;
            return _this ();

        }

    }

}
