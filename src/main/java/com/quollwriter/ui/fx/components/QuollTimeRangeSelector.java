package com.quollwriter.ui.fx.components;

import java.util.*;
import java.time.*;
import java.time.format.*;
import java.text.*;

import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class QuollTimeRangeSelector extends HBox
{

    private ComboBox<String> fromHours = null;
    private ComboBox<String> fromMins = null;
    private ComboBox<String> toHours = null;
    private ComboBox<String> toMins = null;

    private ObjectProperty<LocalTime> fromProp = null;
    private ObjectProperty<LocalTime> toProp = null;

    private QuollTimeRangeSelector (Builder b)
    {

        this.fromProp = new SimpleObjectProperty<> ();
        this.toProp = new SimpleObjectProperty<> ();

        LocalTime now = LocalTime.now ();

        DecimalFormat numFormat = new DecimalFormat ("00");

        this.getStyleClass ().add (StyleClassNames.TIMESELECT);

        this.fromHours = new ComboBox<> ();

        ObservableList<String> fhoursVals = FXCollections.observableArrayList ();

        for (int i = 0; i < 24; i++)
        {

            fhoursVals.add (numFormat.format (i));

        }

        this.fromHours.getItems ().addAll (fhoursVals);

        int fh = 0;
        int fm = 0;

        if (b.from != null)
        {

            fm = b.from.getMinute ();

            fh = b.from.getHour ();
            this.fromProp.setValue (b.from);

        } else {

            if (b.now)
            {

                fh = now.getHour ();
                fm = now.getMinute ();
                this.fromProp.setValue (now);

            }

        }

        fm = (int) (5 * Math.ceil (Math.abs (fm /5)));

        this.fromMins = new ComboBox<> ();

        ObservableList<String> fminsVals = FXCollections.observableArrayList ();

        for (int i = 0; i < 60; i = i + 5)
        {

            fminsVals.add (numFormat.format (i));

        }

        this.fromMins.getItems ().addAll (fminsVals);

        this.fromHours.getSelectionModel ().select (numFormat.format (fh));
        this.fromMins.getSelectionModel ().select (numFormat.format (fm));

        int tm = 0;
        int th = 0;

        if (b.to != null)
        {

            tm = b.to.getMinute ();
            th = b.to.getHour ();
            this.toProp.setValue (b.to);

        } else {

            if (b.now)
            {

                now = now.plusHours (12);
                th = now.getHour ();
                tm = now.getMinute ();
                this.toProp.setValue (now);

            }

        }

        tm = (int) (5 * Math.ceil (Math.abs (tm /5)));

        this.toHours = new ComboBox<> ();
        this.toMins = new ComboBox<> ();

        ObservableList<String> thoursVals = FXCollections.observableArrayList ();

        for (int i = 0; i < 24; i++)
        {

            thoursVals.add (numFormat.format (i));

        }

        this.toHours.getItems ().addAll (thoursVals);

        ObservableList<String> tminsVals = FXCollections.observableArrayList ();

        for (int i = 0; i < 60; i = i + 5)
        {

            tminsVals.add (numFormat.format (i));

        }

        this.toMins.getItems ().addAll (tminsVals);

        this.toHours.getSelectionModel ().select (numFormat.format (th));
        this.toMins.getSelectionModel ().select (numFormat.format (tm));

        this.toHours.getSelectionModel ().selectedItemProperty ().addListener ((pr, oldv, newv) ->
        {

            this.updateTo ();

        });

        this.toMins.getSelectionModel ().selectedItemProperty ().addListener ((pr, oldv, newv) ->
        {

            this.updateTo ();

        });

        this.fromHours.getSelectionModel ().selectedItemProperty ().addListener ((pr, oldv, newv) ->
        {

            this.updateFrom ();

        });

        this.fromMins.getSelectionModel ().selectedItemProperty ().addListener ((pr, oldv, newv) ->
        {

            this.updateFrom ();

        });

        List<String> prefix = Arrays.asList (times,range,select);

        this.getChildren ().addAll (
            QuollLabel.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,between)))
                .build (),
            this.fromHours,
            QuollLabel.builder ()
                .label (new SimpleStringProperty (":"))
                .styleClassName ("colon")
                .build (),
            this.fromMins,
            QuollLabel.builder ()
                .label (getUILanguageStringProperty (Utils.newList (prefix,and)))
                .build (),
            this.toHours,
            QuollLabel.builder ()
                .label (new SimpleStringProperty (":"))
                .styleClassName ("colon")
                .build (),
            this.toMins);

    }

    private void updateTo ()
    {

        this.toProp.setValue (this.getTo ());

    }

    private void updateFrom ()
    {

        this.fromProp.setValue (this.getFrom ());

    }

    public ObjectProperty<LocalTime> fromProperty ()
    {

        return this.fromProp;

    }

    public ObjectProperty<LocalTime> toProperty ()
    {

        return this.toProp;

    }

    public LocalTime getFrom ()
    {

        return LocalTime.parse (String.format ("%1$s:%2$s",
                                               this.fromHours.getSelectionModel ().getSelectedItem (),
                                               this.fromMins.getSelectionModel ().getSelectedItem ()),
                                DateTimeFormatter.ofPattern ("kk:mm"));

    }

    public LocalTime getTo ()
    {

        return LocalTime.parse (String.format ("%1$s:%2$s",
                                               this.toHours.getSelectionModel ().getSelectedItem (),
                                               this.toMins.getSelectionModel ().getSelectedItem ()),
                                DateTimeFormatter.ofPattern ("kk:mm"));

    }

    public static QuollTimeRangeSelector.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollTimeRangeSelector>
    {

        private String styleName = null;
        private LocalTime from = null;
        private LocalTime to = null;
        private boolean now = false;

        private Builder ()
        {

        }

        @Override
        public QuollTimeRangeSelector build ()
        {

            return new QuollTimeRangeSelector (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;

            return this;

        }

        public Builder to (LocalTime t)
        {

            this.to = t;
            return this;

        }

        public Builder from (LocalTime t)
        {

            this.from = t;
            return this;

        }

        public Builder now ()
        {

            this.now = true;
            return this;

        }

    }

}
