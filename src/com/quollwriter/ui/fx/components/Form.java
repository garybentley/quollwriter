package com.quollwriter.ui.fx.components;

import java.util.*;

import javafx.beans.property.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.quollwriter.ui.fx.*;
import com.quollwriter.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class Form extends VBox
{

    private ButtonBar buttonBar = null;

    public enum LayoutType
    {

        stacked,
        column;

    }

    private Form (Builder b)
    {

        this.getStyleClass ().add (StyleClassNames.FORM);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        if (b.description != null)
        {

            Text desc = new Text ();
            desc.getStyleClass ().add (StyleClassNames.DESCRIPTION);
            desc.textProperty ().bind (b.description);

            this.getChildren ().add (desc);

        }

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
                l.getStyleClass ().add (StyleClassNames.LABEL);
                GridPane.setRowIndex (l, r);
                GridPane.setColumnIndex (l, lc);

                if (b.layoutType == LayoutType.stacked)
                {

                    r++;

                }

                Node c = i.control;
                c.getStyleClass ().add (StyleClassNames.CONTROL);
                GridPane.setRowIndex (c, r);
                GridPane.setColumnIndex (c, cc);

                l.setLabelFor (c);

                gp.getChildren ().addAll (l, c);

                r++;

            }

            this.getChildren ().add (gp);

        }

        if ((b.buttons != null)
            &&
            (b.buttons.size () > 0)
           )
        {

            ButtonBar bar = new ButtonBar ();
            this.buttonBar = bar;
            bar.getStyleClass ().add (StyleClassNames.BUTTONS);
            bar.getButtons ().addAll (b.buttons);

            this.getChildren ().add (bar);

        }

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

            if (ButtonBar.getButtonData (b) == ButtonBar.ButtonData.APPLY)
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
        private LayoutType layoutType = LayoutType.stacked;

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

        public Builder item (Node      control,
                             String... label)
        {

            return this.item (getUILanguageStringProperty (label),
                              control);

        }

        public Builder item (StringProperty label,
                             Node           control)
        {

            Label t = new Label ();
            t.textProperty ().bind (label);

            return this.item (new Form.Item (t,
                                             control));

        }

        public Builder cancelButton (StringProperty            label,
                                     EventHandler<ActionEvent> onAction)
        {

            return this.button (label,
                                StyleClassNames.CANCEL,
                                ButtonBar.ButtonData.CANCEL_CLOSE,
                                onAction);

        }

        public Builder cancelButton (EventHandler<ActionEvent> onAction,
                                     String...                 label)
        {

            return this.cancelButton (getUILanguageStringProperty (label),
                                      onAction);

        }

        public Builder confirmButton (StringProperty            label,
                                      EventHandler<ActionEvent> onAction)
        {

            return this.button (label,
                                StyleClassNames.CONFIRM,
                                ButtonBar.ButtonData.APPLY,
                                onAction);

        }

        public Builder confirmButton (EventHandler<ActionEvent> onAction,
                                      String...                 label)
        {

            return this.confirmButton (getUILanguageStringProperty (label),
                                       onAction);

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

}
