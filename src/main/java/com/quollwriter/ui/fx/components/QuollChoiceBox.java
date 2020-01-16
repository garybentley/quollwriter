package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.stream.*;

import javafx.util.*;
import javafx.beans.property.*;
import javafx.event.*;
import javafx.collections.*;
import javafx.scene.control.*;

import com.quollwriter.*;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.ui.fx.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollChoiceBox extends ChoiceBox<StringProperty>
{

    private Map<String, StringProperty> vals = null;

    private QuollChoiceBox (Builder b)
    {

        if (b.items != null)
        {

            this.updateItems (FXCollections.observableArrayList (b.items));

        }

        if (b.binder != null)
        {

            b.binder.addChangeListener (UILanguageStringsManager.uilangProperty (),
                                        (pr, oldv, newv) ->
            {

                this.vals = this.getItems ().stream ()
                    .collect (Collectors.toMap (o -> o.getValue (),
                                                o -> o));
                this.requestLayout ();

            });

        }

        this.setConverter (new StringConverter<StringProperty> ()
        {

            @Override
            public StringProperty fromString (String s)
            {

                return vals.get (s);

            }

            @Override
            public String toString (StringProperty s)
            {

                return s.getValue ();

            }

        });

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

    }

    public void updateItems (ObservableList<StringProperty> its)
    {

        this.vals = its.stream ()
            .collect (Collectors.toMap (o -> o.getValue (),
                                        o -> o));
        this.getItems ().clear ();
        this.getItems ().addAll (its);

    }

    public static QuollChoiceBox.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollChoiceBox>
    {

        private String styleName = null;
        private Set<StringProperty> items = null;
        private EventHandler<ActionEvent> onSelected = null;
        private int selectedInd = 0;
        private IPropertyBinder binder = null;

        private Builder ()
        {

        }

        @Override
        public QuollChoiceBox build ()
        {

            return new QuollChoiceBox (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder binder (IPropertyBinder b)
        {

            this.binder = b;
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
