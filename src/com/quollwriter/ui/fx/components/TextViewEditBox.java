package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.event.*;
import javafx.scene.text.*;

import com.quollwriter.data.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class TextViewEditBox extends StackPane
{

    private VBox view = null;
    private VBox edit = null;
    private TextArea editText = null;
    private StringWithMarkup text = null;
    private boolean typed = false;
    private EventHandler<ActionEvent> onView = null;
    private EventHandler<ActionEvent> onEdit = null;
    private StringProperty viewPlaceHolder = null;

    private TextViewEditBox (Builder b)
    {

        final TextViewEditBox _this = this;

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        //TODO Add? this.textProp = new SimpleStringProperty ();

        this.onView = b.onView;
        this.onEdit = b.onEdit;

        StackPane sp = new StackPane ();

        this.view = new VBox ();
        this.view.getStyleClass ().add (StyleClassNames.VIEW);

        this.edit = new VBox ();
        this.edit.getStyleClass ().add (StyleClassNames.EDIT);

        this.viewPlaceHolder = b.viewPlaceHolder;

        this.editText = new TextArea ();
        this.edit.getChildren ().add (this.editText);

        if (b.editPlaceHolder != null)
        {

            this.editText.setText (b.editPlaceHolder.getValue ());

        }

        this.editText.setOnKeyPressed (ev ->
        {

            if ((ev.isControlDown ())
                &&
                (ev.getCode () == KeyCode.ENTER)
               )
            {

                if (b.onSave != null)
                {

                    b.onSave.handle (new ActionEvent (_this,
                                                      _this));

                }

            }

        });

        java.util.List<String> prefix = new ArrayList<> ();
        // TODO Remove reference to project OR add string props for the tooltips...
        prefix.add (LanguageStrings.project);
        prefix.add (LanguageStrings.sidebar);
        prefix.add (LanguageStrings.chapterinfo);
        prefix.add (LanguageStrings.edit);

        QuollButton saveB = QuollButton.builder ()
            .tooltip (prefix,buttons,save,tooltip)
            .styleClassName (StyleClassNames.SAVE)
            .buttonType (ButtonBar.ButtonData.APPLY)
            .onAction (ev ->
            {

                _this.text = new StringWithMarkup (_this.editText.getText ());

                if (b.onSave != null)
                {

                    b.onSave.handle (ev);

                }

                _this.showView ();

            })
            .build ();

        QuollButton cancelB = QuollButton.builder ()
            .tooltip (prefix,buttons,cancel,tooltip)
            .styleClassName (StyleClassNames.CANCEL)
            .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
            .onAction (b.onCancel)
            .build ();

        cancelB.addEventHandler (ActionEvent.ACTION,
                                 ev ->
        {

            _this.showView ();

        });

        ButtonBar bb = new ButtonBar ();
        bb.getButtons ().addAll (saveB, cancelB);
        this.edit.getChildren ().add (bb);

        if (b.text != null)
        {

            this.setText (b.text);

        }

    }

    public StringWithMarkup getText ()
    {

        return this.text;

    }

    public void setText (StringWithMarkup t)
    {

        final TextViewEditBox _this = this;

        this.view.getChildren ().removeAll ();

        if ((t == null)
            ||
            (t.hasText ())
           )
        {

            if (this.viewPlaceHolder != null)
            {

                Hyperlink l = new Hyperlink ();
                l.textProperty ().bind (this.viewPlaceHolder);
                l.getStyleClass ().add (StyleClassNames.PLACEHOLDER);
                l.setOnAction (ev -> _this.showEdit ());

                this.view.getChildren ().add (l);

            }

            // Add our placeholder instead.

        } else {

            // Handle bulleted.
            Text text = new Text (t.getText ());

            this.view.getChildren ().add (text);

        }

        this.editText.setText (t.getText ());

        this.text = t;

    }

    public void showEdit ()
    {

        this.edit.setViewOrder (0d);
        this.view.setViewOrder (1d);

        if (this.onEdit != null)
        {

            this.onEdit.handle (new ActionEvent (this, this));

        }

    }

    public void showView ()
    {

        this.edit.setViewOrder (1d);
        this.view.setViewOrder (0d);

        if (this.onView != null)
        {

            this.onView.handle (new ActionEvent (this, this));

        }

    }

    /**
     * Get a builder to create a new TextViewEditBox.
     *
     * Usage: TextViewEditBox.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static TextViewEditBox.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, TextViewEditBox>
    {

        private StringProperty editPlaceHolder = null;
        private StringProperty viewPlaceHolder = null;
        private String styleName = null;
        private boolean bulleted = false;
        private boolean showTyping = false;
        private EventHandler<ActionEvent> onSave = null;
        private EventHandler<ActionEvent> onCancel = null;
        private EventHandler<ActionEvent> onEdit = null;
        private EventHandler<ActionEvent> onView = null;
        private StringWithMarkup text = null;

        private Builder ()
        {

        }

        @Override
        public TextViewEditBox build ()
        {

            return new TextViewEditBox (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder onSave (EventHandler<ActionEvent> ev)
        {

            this.onSave = ev;
            return this;

        }

        public Builder styleClassName (String name)
        {

            this.styleName = name;
            return this;

        }

        public Builder editPlaceHolder (StringProperty prop)
        {

            this.editPlaceHolder = prop;
            return this;

        }

        public Builder viewPlaceHolder (StringProperty prop)
        {

            this.viewPlaceHolder = prop;
            return this;

        }

        public Builder bulleted (boolean v)
        {

            this.bulleted = v;
            return this;

        }

        public Builder text (StringWithMarkup t)
        {

            this.text = t;
            return this;

        }

        public Builder showTypingIndicator (boolean v)
        {

            this.showTyping = v;
            return this;

        }

    }

}
