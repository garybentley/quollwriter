package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.event.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.image.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollLabel2 extends HBox
{

    private ImageView image = null;
    private BasicHtmlTextFlow text = null;
    private ContextMenu contextMenu = null;

    private QuollLabel2 (Builder b)
    {

        this.managedProperty ().bind (this.visibleProperty ());
        this.image = new ImageView ();
        this.image.managedProperty ().bind (this.image.visibleProperty ());
        this.getStyleClass ().add ("qlabel");

        this.text = BasicHtmlTextFlow.builder ()
            .text (b.label)
            .styleClassName (StyleClassNames.TEXT)
            .build ();

        this.addEventHandler (ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                              ev ->
        {

            if (ev.isConsumed ())
            {

                return;

            }

            if (ev.getSource () instanceof QuollLabel2)
            {

                if (this.contextMenu != null)
                {

                    this.contextMenu.show (this, ev.getScreenX(), ev.getScreenY());
                    ev.consume();

                }

            }

        });

        this.getChildren ().addAll (this.image, this.text);

        if (b.tooltip != null)
        {

            UIUtils.setTooltip (this,
                                b.tooltip);

        }

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.onAction != null)
        {

            this.text.addEventHandler (MouseEvent.MOUSE_RELEASED,
                                       ev ->
            {

                if (ev.isPopupTrigger ())
                {

                    return;

                }

                b.onAction.handle (new ActionEvent (this, this));

            });

            this.image.addEventHandler (MouseEvent.MOUSE_RELEASED,
                                        ev ->
            {

                if (ev.isPopupTrigger ())
                {

                    return;

                }

                b.onAction.handle (new ActionEvent (this, this));

            });

        }

    }

    public void setContextMenu (ContextMenu m)
    {

        this.contextMenu = m;

    }


/*
    public String getText ()
    {

        return this.text.getText ();

    }
*/
    public void setText (String text)
    {

        this.text.setText (text);

    }

    public StringProperty textProperty ()
    {

        return this.text.textProperty ();

    }

    public static QuollLabel2.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollLabel2>
    {

        private StringProperty label = null;
        private StringProperty tooltip = null;
        private String styleName = null;
        private EventHandler<ActionEvent> onAction = null;

        private Builder ()
        {

        }

        public Builder label (List<String> prefix,
                              String...    ids)
        {

            return this.label (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder label (String... ids)
        {

            return this.label (getUILanguageStringProperty (ids));

        }

        public Builder label (StringProperty prop)
        {

            this.label = prop;
            return this;

        }

        public Builder tooltip (List<String> prefix,
                                String...    ids)
        {

            return this.tooltip (getUILanguageStringProperty (Utils.newList (prefix, ids)));

        }

        public Builder tooltip (String... ids)
        {

            return this.tooltip (getUILanguageStringProperty (ids));

        }

        public Builder tooltip (StringProperty prop)
        {

            this.tooltip = prop;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;
            return this;

        }

        public Builder onAction (EventHandler<ActionEvent> h)
        {

            this.onAction = h;
            return this;

        }

        @Override
        public QuollLabel2 build ()
        {

            return new QuollLabel2 (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
