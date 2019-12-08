package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.event.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollCheckBox2 extends HBox
{

    private CheckBox checkbox = null;
    private BooleanProperty selectedProp = null;
    private BasicHtmlTextFlow text = null;
    private ContextMenu contextMenu = null;

    private QuollCheckBox2 (Builder b)
    {

        this.checkbox = new CheckBox ();
        this.checkbox.setContentDisplay (ContentDisplay.GRAPHIC_ONLY);
        this.getStyleClass ().add ("qcheckbox");

        this.text = BasicHtmlTextFlow.builder ()
            .text (b.label)
            .styleClassName (StyleClassNames.TEXT)
            .build ();

        this.text.addEventHandler (MouseEvent.MOUSE_RELEASED,
                                   ev ->
        {

            if (ev.isPopupTrigger ())
            {

                return;

            }

            this.checkbox.setSelected (!this.checkbox.isSelected ());

        });

        this.addEventHandler (ContextMenuEvent.CONTEXT_MENU_REQUESTED,
                              ev ->
        {

            if (ev.isConsumed ())
            {

                return;

            }

            if (ev.getSource () instanceof QuollCheckBox2)
            {

                if (this.contextMenu != null)
                {

                    this.contextMenu.show (this, ev.getScreenX(), ev.getScreenY());
                    ev.consume();

                }

            }

        });

        this.getChildren ().addAll (this.checkbox, this.text);

        if (b.tooltip != null)
        {

            UIUtils.setTooltip (this,
                                b.tooltip);

        }

        if (b.userProp != null)
        {

            this.setSelected (UserProperties.getAsBoolean (b.userProp));

            this.selectedProperty ().addListener ((pr, oldv, newv) ->
            {

                UserProperties.set (b.userProp,
                                    this.isSelected ());

            });

        } else {

            this.setSelected (b.selected);

        }

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.onAction != null)
        {

            this.checkbox.setOnAction (b.onAction);
            this.text.addEventHandler (MouseEvent.MOUSE_RELEASED,
                                       ev ->
            {

                if (ev.isPopupTrigger ())
                {

                    return;

                }

                b.onAction.handle (new ActionEvent (this.checkbox, this.checkbox));

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

    public BooleanProperty selectedProperty ()
    {

        return this.checkbox.selectedProperty ();

    }

    public void setSelected (boolean v)
    {

        this.checkbox.setSelected (v);

    }

    public boolean isSelected ()
    {

        return this.checkbox.isSelected ();

    }

    public static QuollCheckBox2.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollCheckBox2>
    {

        private StringProperty label = null;
        private StringProperty tooltip = null;
        private String styleName = null;
        private Boolean selected = false;
        private EventHandler<ActionEvent> onAction = null;
        private String userProp = null;

        private Builder ()
        {

        }

        public Builder userProperty (String name)
        {

            this.userProp = name;
            return this;

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

        public Builder selected (Boolean v)
        {

            this.selected = v;
            return this;

        }

        public Builder onAction (EventHandler<ActionEvent> h)
        {

            this.onAction = h;
            return this;

        }

        @Override
        public QuollCheckBox2 build ()
        {

            return new QuollCheckBox2 (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

}
