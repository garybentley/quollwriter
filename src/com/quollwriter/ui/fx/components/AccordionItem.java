package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class AccordionItem extends VBox implements Stateful
{

    protected Header header = null;
    private Node content = null;
    private String accId = null;

    private AccordionItem (Builder b)
    {

        final AccordionItem _this = this;

        if (b.title == null)
        {

            throw new IllegalArgumentException ("A title must be provided.");

        }

        if (b.content == null)
        {

            throw new IllegalArgumentException ("A content node must be provided.");

        }

        if (b.accId == null)
        {

            throw new IllegalArgumentException ("An accordion id must be provided.");

        }

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
            .cursor (Cursor.HAND)
            .contextMenu (b.contextMenuItemSupplier)
            .build ();

        this.header.titleLabelProperty ().getValue ().setOnMouseClicked (ev ->
        {

            _this.content.setVisible (!_this.content.isVisible ());

        });

        this.getChildren ().add (this.header);

        b.content.getStyleClass ().add (StyleClassNames.CONTENT);

        this.content = b.content;

        this.content.setVisible (b.open);

        this.getChildren ().add (b.content);

    }

    public void init (State s)
    {

        this.setContentVisible (s.getAsBoolean (State.Key.open));

    }

    public State getState ()
    {

        State s = new State ();
        s.set (State.Key.open,
               this.content.isVisible ());

        return s;

    }

    public String getAccordionId ()
    {

        return this.accId;

    }

    public void setContentVisible (boolean v)
    {

        this.content.setVisible (v);

    }

    public boolean isContentVisible ()
    {

        return this.content.isVisible ();

    }

    /**
     * Get a builder to create a new AccordionItem.
     *
     * Usage: AccordionItem.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static AccordionItem.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, AccordionItem>
    {

        private StringProperty title = null;
        private Node content = null;
        private Set<Node> headerCons = null;
        private String styleName = null;
        private String accId = null;
        private boolean open = true;
        private Supplier<Set<MenuItem>> contextMenuItemSupplier = null;

        private Builder ()
        {

        }

        @Override
        public AccordionItem build ()
        {

            return new AccordionItem (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder styleClassName (String name)
        {

            this.styleName = name;
            return this;

        }

        public Builder content (Node n)
        {

            this.content = n;
            return this;

        }

        public Builder title (String... ids)
        {

            return this.title (getUILanguageStringProperty (ids));

        }

        public Builder title (List<String> prefix,
                              String... ids)
        {

            return this.title (getUILanguageStringProperty (Utils.newList (prefix,ids)));

        }

        public Builder accordionId (String id)
        {

            this.accId = id;
            return this;

        }

        public Builder title (StringProperty prop)
        {

            this.title = prop;
            return this;

        }

        public Builder headerControls (Set<Node> cons)
        {

            this.headerCons = cons;
            return this;

        }

        public Builder open (boolean v)
        {

            this.open = v;
            return this;

        }

        public Builder contextMenu (final Set<MenuItem> items)
        {

            this.contextMenuItemSupplier = new Supplier<Set<MenuItem>> ()
            {

                @Override
                public Set<MenuItem> get ()
                {

                    return items;

                }

            };

            return this;

        }

        public Builder contextMenu (Supplier<Set<MenuItem>> items)
        {

            this.contextMenuItemSupplier = items;
            return this;

        }

    }

}
