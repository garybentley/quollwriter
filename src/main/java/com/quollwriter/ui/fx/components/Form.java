package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class Form extends VBox
{

    private ErrorBox errorBox = null;
    private URLActionHandler errorHandler = null;
    private QuollButtonBar buttonBar = null;

    public enum LayoutType
    {

        stacked,
        column;

    }

    private Form (Builder b)
    {

        final Form _this = this;

        this.setFillWidth (true);
        VBox.setVgrow (this,
                       Priority.ALWAYS);
        this.getStyleClass ().add (StyleClassNames.FORM);
        this.getStyleClass ().add (b.layoutType == LayoutType.column ? StyleClassNames.COLUMN : StyleClassNames.STACKED);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.description != null)
        {

            BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
                .styleClassName (StyleClassNames.DESCRIPTION)
                .text (b.description)
                .withHandler (b.handler)
                .build ();

            this.getChildren ().add (desc);

        }

        // Add an error
        this.errorBox = ErrorBox.builder ()
            .build ();
        this.errorHandler = b.handler;
        this.getChildren ().add (this.errorBox);

        int r = 0;

        GridPane gp = new GridPane ();
        gp.getStyleClass ().add (StyleClassNames.ITEMS);

        if (b.items != null)
        {

            // We just have 2 columns here with an increasing row index.

            int lc = 0;
            int cc = 1;

            if (b.layoutType == LayoutType.stacked)
            {

                cc = 0;

            }

            if (b.layoutType == LayoutType.column)
            {

                ColumnConstraints _cc = new ColumnConstraints ();
                _cc.setHgrow (Priority.NEVER);
                _cc.setHalignment (HPos.RIGHT);
                gp.getColumnConstraints ().add (_cc);

            }

            ColumnConstraints _cc = new ColumnConstraints ();
            _cc.setFillWidth (true);
            _cc.setHgrow (Priority.ALWAYS);
            _cc.setHalignment (HPos.LEFT);
            gp.getColumnConstraints ().add (_cc);

            for (Item i : b.items)
            {

                Label l = i.label;

                if (l != null)
                {

                    l.getStyleClass ().add (StyleClassNames.LABEL);
                    GridPane.setRowIndex (l, r);
                    GridPane.setColumnIndex (l, lc);

                    if (b.layoutType == LayoutType.stacked)
                    {

                        r++;

                    }

                    gp.getChildren ().add (l);

                }

                Node c = i.control;

                if (c instanceof TextField)
                {

                    TextField tf = (TextField) c;

                    UIUtils.addDoOnReturnPressed (tf,
                                                  () ->
                    {

                        this.fireEvent (new FormEvent (this,
                                                       FormEvent.CONFIRM_EVENT));

                    });

                }

                if (c instanceof TextArea)
                {

                    TextArea tf = (TextArea) c;

                    UIUtils.addDoOnReturnPressed (tf,
                                                  () ->
                    {

                        this.fireEvent (new FormEvent (this,
                                                       FormEvent.CONFIRM_EVENT));

                    });

                }

                if (c instanceof QuollTextArea)
                {

                    QuollTextArea tf = (QuollTextArea) c;
                    UIUtils.addDoOnReturnPressed (tf,
                                                  () ->
                    {

                        this.fireEvent (new FormEvent (this,
                                                       FormEvent.CONFIRM_EVENT));

                    });

                }

                VBox cb = new VBox ();
                cb.getChildren ().add (c);
                cb.setFillWidth (true);
                cb.getStyleClass ().add (StyleClassNames.CONTROL);

                GridPane.setRowIndex (cb, r);
                GridPane.setColumnIndex (cb, cc);

                if (l != null)
                {

                    l.setLabelFor (c);

                }

                gp.getChildren ().addAll (cb);

                r++;

            }

            this.getChildren ().add (gp);

        }

        if (b.confirm != null)
        {

            b.confirm.setOnAction (ev ->
            {

                _this.fireEvent (new FormEvent (_this,
                                                FormEvent.CONFIRM_EVENT));

            });

        }

        if (b.cancel != null)
        {

            b.cancel.setOnAction (ev ->
            {

                _this.fireEvent (new FormEvent (_this,
                                                FormEvent.CANCEL_EVENT));

            });

        }

        if ((b.buttons != null)
            &&
            (b.buttons.size () > 0)
           )
        {

            this.buttonBar = QuollButtonBar.builder ()
                .buttons (b.buttons)
                .build ();

            int cc = 1;

            if (b.layoutType == LayoutType.stacked)
            {

                cc = 0;

            }

            GridPane.setRowIndex (this.buttonBar, r);
            GridPane.setColumnIndex (this.buttonBar, cc);

            gp.getChildren ().add (this.buttonBar);

        }

    }

    public void showErrors (Set<StringProperty> errs)
    {

        this.errorBox.setErrors (errs);
        this.errorBox.setVisible (true);

    }

    public void showError (StringProperty... errs)
    {

        Set<StringProperty> e = new LinkedHashSet<> ();
        Collections.addAll (e, errs);
        this.showErrors (e);

    }

    public void hideError ()
    {

        this.errorBox.setVisible (false);

    }

    public void setOnCancel (EventHandler<FormEvent> h)
    {

        if (h == null)
        {

            return;

        }

        this.addEventHandler (FormEvent.CANCEL_EVENT,
                              h);

    }

    public void setOnConfirm (EventHandler<FormEvent> h)
    {

        this.addEventHandler (FormEvent.CONFIRM_EVENT,
                              h);

    }

    public Button getCancelButton ()
    {

        if (this.buttonBar == null)
        {

            return null;

        }

        return this.buttonBar.getCancelButton ();

    }

    public Button getConfirmButton ()
    {

        if (this.buttonBar == null)
        {

            return null;

        }

        return this.buttonBar.getConfirmButton ();

    }

    /**
     * Get a builder to create a new QuollPopup.
     *
     * Usage: QuollPopup.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static Form.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, Form>
    {

        private StringProperty description = null;
        private String styleName = null;
        private Set<Item> items = new LinkedHashSet<> ();
        private Set<Button> buttons = new LinkedHashSet<> ();
        private Button confirm = null;
        private Button cancel = null;
        private LayoutType layoutType = LayoutType.stacked;
        private URLActionHandler handler = null;

        private Builder ()
        {

        }

        @Override
        public Form build ()
        {

            return new Form (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder layoutType (LayoutType t)
        {

            this.layoutType = t;
            return this;

        }

        public Builder withHandler (URLActionHandler handler)
        {

            this.handler = handler;
            return this;

        }

        public Builder styleClassName (String name)
        {

            this.styleName = name;
            return this;

        }

        public Builder button (Button button)
        {

            this.buttons.add (button);

            return this;

        }

        public Builder button (StringProperty            label,
                               String                    styleName,
                               ButtonBar.ButtonData      type,
                               EventHandler<ActionEvent> onAction)
        {

            return this.button (QuollButton.builder ()
                .label (label)
                .buttonType (type)
                .onAction (onAction)
                .styleClassName (styleName)
                .build ());

        }

        public Builder items (Item... items)
        {

            this.items.addAll (Arrays.asList (items));
            return this;

        }

        public Builder items (Collection<Item> items)
        {

            this.items.addAll (items);
            return this;

        }

        public Builder item (Item item)
        {

            this.items.add (item);
            return this;

        }

        public Builder item (Node      control)
        {

            return this.item (null,
                              control);

        }

        public Builder item (StringProperty label,
                             Node           control)
        {

            Label t = null;

            if (label != null)
            {

                t = new Label ();
                t.textProperty ().bind (label);

            }

            return this.item (new Form.Item (t,
                                             control));

        }

        public Builder cancelButton (StringProperty label)
        {

            if (label == null)
            {

                return this;

            }

            Button b = QuollButton.builder ()
                .label (label)
                .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
                .styleClassName (StyleClassNames.CANCEL)
                .build ();

            this.cancel = b;

            return this.button (b);

        }

        public Builder cancelButton (String... label)
        {

            if (label == null)
            {

                return this;

            }

            return this.cancelButton (getUILanguageStringProperty (label));

        }

        public Builder confirmButton (StringProperty label)
        {

            if (label == null)
            {

                return this;

            }

            Button b = QuollButton.builder ()
                .label (label)
                .buttonType (ButtonBar.ButtonData.OK_DONE)
                .styleClassName (StyleClassNames.CONFIRM)
                .build ();

            this.confirm = b;

            return this.button (b);

        }

        public Builder confirmButton (String... label)
        {

            if (label == null)
            {

                return this;

            }

            return this.confirmButton (getUILanguageStringProperty (label));

        }

        public Builder description (List<String> prefix,
                                    String...    ids)
        {

            List<String> _ids = new ArrayList<> (prefix);

            if (ids != null)
            {

                for (String s : ids)
                {

                    _ids.add (s);

                }

            }

            this.description = getUILanguageStringProperty (ids);

            return this;

        }

        public Builder description (StringProperty prop)
        {

            this.description = prop;
            return this;

        }

        public Builder description (String... ids)
        {

            List<String> _ids = new ArrayList<> ();

            if (ids != null)
            {

                for (String s : ids)
                {

                    _ids.add (s);

                }

            }

            this.description = getUILanguageStringProperty (ids);

            return this;

        }

    }

    public static class Item
    {

        public Label label = null;
        public Node control = null;

        public Item (Node c)
        {

            this.control = c;

        }

        public Item (StringProperty l,
                     Node           c)
        {

            this.label = QuollLabel.builder ()
                .label (l)
                .build ();

            this.control = c;

        }

        public Item (Label l,
                     Node  c)
        {

            this.label = l;
            this.control = c;

        }

    }

    public static class FormEvent extends Event
    {

        public static final EventType<FormEvent> CONFIRM_EVENT = new EventType<> ("form.confirm");
        public static final EventType<FormEvent> CANCEL_EVENT = new EventType<> ("form.cancel");

        private Form form = null;

        public FormEvent (Form                 form,
                          EventType<FormEvent> type)
        {

            super (type);

            this.form = form;

        }

        public Form getForm ()
        {

            return this.form;

        }

    }

}
