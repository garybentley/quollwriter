package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class Form extends VBox
{

    private BasicHtmlTextFlow error = null;
    private HBox errorBox = null;
    private ButtonBar buttonBar = null;

    public enum LayoutType
    {

        stacked,
        column;

    }

    private Form (Builder b)
    {

        final Form _this = this;

        this.setFillWidth (true);
        this.getStyleClass ().add (StyleClassNames.FORM);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.description != null)
        {

            BasicHtmlTextFlow desc = BasicHtmlTextFlow.builder ()
                .styleClassName (StyleClassNames.DESCRIPTION)
                .text (b.description)
                .withViewer (b.viewer)
                .build ();

            this.getChildren ().add (desc);

        }

        // Add an error
        this.errorBox = new HBox ();
        this.errorBox.getStyleClass ().add (StyleClassNames.ERROR);
        ImageView img = new ImageView ();
        this.error = BasicHtmlTextFlow.builder ()
            .styleClassName (StyleClassNames.MESSAGE)
            .withViewer (b.viewer)
            .build ();
        HBox.setHgrow (this.error,
                       Priority.ALWAYS);
        this.errorBox.getChildren ().addAll (img, this.error);
        this.errorBox.managedProperty ().bind (this.errorBox.visibleProperty ());
        this.errorBox.setVisible (false);
        this.getChildren ().add (this.errorBox);

        if (b.items != null)
        {

            GridPane gp = new GridPane ();
            gp.getStyleClass ().add (StyleClassNames.ITEMS);

            // We just have 2 columns here with an increasing row index.

            int r = 0;
            int lc = 0;
            int cc = 1;

            if (b.layoutType == LayoutType.stacked)
            {

                cc = 0;

            }

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
                VBox cb = new VBox ();
                cb.getChildren ().add (c);
                cb.setFillWidth (true);
                cb.getStyleClass ().add (StyleClassNames.CONTROL);

                ColumnConstraints _cc = new ColumnConstraints ();
                _cc.setHgrow (Priority.ALWAYS);
                gp.getColumnConstraints ().add (_cc);
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

            // TODO Make this configurable, esp for other OSes
            ButtonBar bar = new ButtonBar ("OC");
            //"OC+");
            this.buttonBar = bar;
            bar.getButtons ().addAll (b.buttons);

            HBox h = new HBox ();
            h.getStyleClass ().add (StyleClassNames.BUTTONS);
            h.getChildren ().add (bar);

            this.getChildren ().add (h);

        }

    }

    public void showError (StringProperty err)
    {

        this.error.textProperty ().bind (err);
        this.errorBox.setVisible (true);

    }

    public void hideError ()
    {

        this.errorBox.setVisible (false);

    }

    public void setOnCancel (EventHandler<FormEvent> h)
    {

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

        for (Node n : this.buttonBar.getButtons ())
        {

            if (!(n instanceof Button))
            {

                continue;

            }

            Button b = (Button) n;

            if (ButtonBar.getButtonData (b) == ButtonBar.ButtonData.CANCEL_CLOSE)
            {

                return b;

            }

        }

        return null;

    }

    public Button getConfirmButton ()
    {

        if (this.buttonBar == null)
        {

            return null;

        }

        for (Node n : this.buttonBar.getButtons ())
        {

            if (!(n instanceof Button))
            {

                continue;

            }

            Button b = (Button) n;

            if (ButtonBar.getButtonData (b) == ButtonBar.ButtonData.OK_DONE)
            {

                return b;

            }

        }

        return null;

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
        private AbstractViewer viewer = null;

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

        public Builder withViewer (AbstractViewer viewer)
        {

            this.viewer = viewer;
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

            return this.cancelButton (getUILanguageStringProperty (label));

        }

        public Builder confirmButton (StringProperty label)
        {

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

    private static class Item
    {

        public Label label = null;
        public Node control = null;

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
