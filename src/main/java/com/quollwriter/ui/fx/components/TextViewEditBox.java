package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

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
    private Function<StringWithMarkup, Boolean> onSave = null;
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
        this.view.managedProperty ().bind (this.view.visibleProperty ());

        this.edit = new VBox ();
        this.edit.getStyleClass ().add (StyleClassNames.EDIT);
        this.edit.managedProperty ().bind (this.edit.visibleProperty ());
        this.edit.setVisible (false);

        this.viewPlaceHolder = b.viewPlaceHolder;

        this.editText = new TextArea ();
        this.editText.promptTextProperty ().bind (b.editPlaceHolder);
        this.edit.getChildren ().add (this.editText);

        if (b.editPlaceHolder != null)
        {

            this.editText.promptTextProperty ().bind (b.editPlaceHolder);

        }

        this.onSave = b.onSave;

        UIUtils.addDoOnReturnPressed (this.editText,
                                      () ->
        {

            this.doSave ();

        });

        QuollButton saveB = QuollButton.builder ()
            .tooltip (b.saveButtonTooltip)
            .styleClassName (StyleClassNames.SAVE)
            .buttonType (ButtonBar.ButtonData.APPLY)
            .onAction (ev ->
            {

                this.doSave ();

            })
            .build ();

        QuollButton cancelB = QuollButton.builder ()
            .tooltip (b.cancelButtonTooltip)
            .styleClassName (StyleClassNames.CANCEL)
            .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
            .onAction (b.onCancel)
            .build ();

        cancelB.addEventHandler (ActionEvent.ACTION,
                                 ev ->
        {

            if (b.onCancel != null)
            {

                b.onCancel.handle (ev);

            }

            this.showView ();

        });

        ToolBar bb = new ToolBar ();
        bb.getStyleClass ().add (StyleClassNames.BUTTONS);
        bb.getItems ().addAll (saveB, cancelB);

        this.edit.getChildren ().add (bb);

        this.setText (b.text);

        this.getChildren ().addAll (this.view, this.edit);

    }

    private void doSave ()
    {

        StringWithMarkup ntext = new StringWithMarkup (this.editText.getText ());

        if (this.onSave != null)
        {

            if (!this.onSave.apply (ntext))
            {

                return;

            }

        }

        this.text = ntext;

        this.showView ();

    }

    public StringWithMarkup getText ()
    {

        return this.text;

    }

    public void setText (StringWithMarkup t)
    {

        final TextViewEditBox _this = this;

        this.view.getChildren ().clear ();

        if ((t == null)
            ||
            (!t.hasText ())
           )
        {

            if (this.viewPlaceHolder != null)
            {

                QuollHyperlink l = QuollHyperlink.builder ()
                    .label (this.viewPlaceHolder)
                    .styleClassName (StyleClassNames.PLACEHOLDER)
                    .onAction (ev -> _this.showEdit ())
                    .build ();

                this.view.getChildren ().add (l);

            }

            // Add our placeholder instead.

        } else {

            // Handle bulleted.
            this.view.getChildren ().add (BasicHtmlTextFlow.builder ()
                .text (t.getMarkedUpText ())
                .build ());

        }

        this.text = t;

    }

    public void showEdit ()
    {

        this.view.setVisible (false);
        this.edit.setVisible (true);

        if (this.text != null)
        {

            this.editText.setText (this.text.getText ());

        }

        if (this.onEdit != null)
        {

            this.onEdit.handle (new ActionEvent (this, this));

        }

    }

    public void showView ()
    {

        this.view.setVisible (true);
        this.edit.setVisible (false);

        this.setText (this.text);

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
        private Function<StringWithMarkup, Boolean> onSave = null;
        private EventHandler<ActionEvent> onCancel = null;
        private EventHandler<ActionEvent> onEdit = null;
        private EventHandler<ActionEvent> onView = null;
        private StringWithMarkup text = null;
        private StringProperty saveButtonTooltip = null;
        private StringProperty cancelButtonTooltip = null;

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

        public Builder saveButtonTooltip (StringProperty p)
        {

            this.saveButtonTooltip = p;
            return this;

        }

        public Builder cancelButtonTooltip (StringProperty p)
        {

            this.cancelButtonTooltip = p;
            return this;

        }

        public Builder onSave (Function<StringWithMarkup, Boolean> ev)
        {

            this.onSave = ev;
            return this;

        }

        public Builder onCancel (EventHandler<ActionEvent> ev)
        {

            this.onCancel = ev;
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
