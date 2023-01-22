package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.event.*;
import javafx.util.*;
import javafx.collections.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollComboBox extends ComboBox<StringProperty>
{

    private QuollComboBox (Builder b)
    {

        this.getItems ().addAll (b.items);
        this.setVisibleRowCount (b.items.size ());

        this.getSelectionModel ().select (b.selectedInd);

        if (b.onSelected != null)
        {

            this.addEventHandler (ActionEvent.ANY,
                                  b.onSelected);

        }

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        Callback<ListView<StringProperty>, ListCell<StringProperty>> cellFactory = (lv ->
        {

            return new ListCell<StringProperty> ()
            {

                @Override
                protected void updateItem (StringProperty item,
                                           boolean        empty)
                {

                    super.updateItem (item,
                                      empty);

                    if (empty || item == null)
                    {

                        this.textProperty ().unbind ();
                        setText ("");

                    } else {

                        this.textProperty ().bind (item);

                    }

                }

            };

        });

        this.setCellFactory (cellFactory);
        this.setButtonCell (cellFactory.call (null));

    }

    public static QuollComboBox.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollComboBox>
    {

        private String styleName = null;
        private Set<StringProperty> items = null;
        private EventHandler<ActionEvent> onSelected = null;
        private int selectedInd = 0;

        private Builder ()
        {

        }

        @Override
        public QuollComboBox build ()
        {

            return new QuollComboBox (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder items (StringProperty... items)
        {

            this.items = new LinkedHashSet<> (Arrays.asList (items));
            return this;

        }

        public Builder items (Set<StringProperty> items)
        {

            this.items = items;
            return this;

        }

        public Builder styleClassName (String name)
        {

            this.styleName = name;
            return this;

        }

        public Builder onSelected (EventHandler<ActionEvent> ev)
        {

            this.onSelected = ev;
            return this;

        }

        public Builder selectedIndex (int i)
        {

            this.selectedInd = i;
            return this;

        }

    }

}
