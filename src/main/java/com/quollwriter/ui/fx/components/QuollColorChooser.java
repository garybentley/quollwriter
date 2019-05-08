package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.layout.*;
import javafx.scene.*;
import javafx.event.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollColorChooser extends VBox
{

    private NumberSelector red = null;
    private NumberSelector blue = null;
    private NumberSelector green = null;
    private TextField hex = null;
    private ObjectProperty<Color> colorProp = null;
    private boolean ignoreUpdates = false;
    private FlowPane colorsPane = null;

    private QuollColorChooser (Builder b)
    {

        final QuollColorChooser _this = this;

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        this.colorProp = new SimpleObjectProperty<> ();

        this.red = new NumberSelector (0, 255, 0);
        this.blue = new NumberSelector (0, 255, 0);
        this.green = new NumberSelector (0, 255, 0);
        this.hex = new TextField ();

        if (b.showUserColors)
        {

            Header h = Header.builder ()
                .title (getUILanguageStringProperty (colorchooser,popup,current))
                .build ();

            this.colorsPane = new FlowPane ();
            colorsPane.getStyleClass ().add (StyleClassNames.SWATCHES);

            Set<Color> colors = new LinkedHashSet<> ();
            colors.add (Color.WHITE);
            colors.addAll (UserProperties.userColorsProperty ());
            colors.add (Color.BLACK);
            colors.stream ()
                .forEach (col -> this.createSwatch (col, -1));

            ScrollPane colSp = new ScrollPane (colorsPane);

            Header hc = Header.builder ()
                .title (getUILanguageStringProperty (colorchooser,popup,_new))
                .build ();

            this.getChildren ().addAll (h, colSp, hc);

        }

        Label preview = new Label ();
        preview.getStyleClass ().add (StyleClassNames.PREVIEW);

        this.colorProp.addListener ((pr, oldv, newv) ->
        {

            preview.setBackground (new Background (new BackgroundFill (newv, null, null)));

        });

        List<String> prefix = Arrays.asList (colorchooser,labels);

        Form f = Form.builder ()
            .item (getUILanguageStringProperty (colorchooser,labels,LanguageStrings.red),
                   this.red)
            .item (getUILanguageStringProperty (colorchooser,labels,LanguageStrings.green),
                   this.green)
            .item (getUILanguageStringProperty (colorchooser,labels,LanguageStrings.blue),
                   this.blue)
            .item (getUILanguageStringProperty (colorchooser,labels,LanguageStrings.hex),
                   this.hex)
            .item (new SimpleStringProperty ("Preview"),
                   preview)
            .layoutType (Form.LayoutType.column)
            .confirmButton (new SimpleStringProperty ("Add/Use Color"))
            .cancelButton (getUILanguageStringProperty (buttons,cancel))
            .build ();

        f.addEventHandler (Form.FormEvent.CONFIRM_EVENT,
        ev ->
        {

            _this.fireEvent (ev);

            UserProperties.addUserColor (this.colorProp.getValue ());

            if (this.colorsPane != null)
            {

                this.createSwatch (this.colorProp.getValue (), 1);

            }

        });

        f.addEventHandler (Form.FormEvent.CANCEL_EVENT,
        ev ->
        {

            _this.fireEvent (ev);

        });

        f.setOnConfirm (ev ->
        {

            _this.fireEvent (new ObjectSelectedEvent (_this.colorProp.getValue (),
                                                      ObjectSelectedEvent.SELECTED_EVENT));

        });

        this.red.valueProperty ().addListener ((p, oldv, newv) ->
        {

            if (_this.ignoreUpdates)
            {

                return;

            }

            _this.updateColor ();

        });

        this.blue.valueProperty ().addListener ((p, oldv, newv) ->
        {

            if (_this.ignoreUpdates)
            {

                return;

            }

            _this.updateColor ();

        });

        this.green.valueProperty ().addListener ((p, oldv, newv) ->
        {

            if (_this.ignoreUpdates)
            {

                return;

            }

            _this.updateColor ();

        });

        this.hex.textProperty ().addListener ((p, oldv, newv) ->
        {

            if (_this.ignoreUpdates)
            {

                return;

            }

            _this.updateColor (newv);

        });

        this.colorProp.addListener ((p, oldv, newv) ->
        {

            if (_this.ignoreUpdates)
            {

                return;

            }

            _this.updateColor (newv);

        });

        if (b.color != null)
        {

            this.colorProp.setValue (b.color);

        }

        this.getChildren ().addAll (f);

    }

    private void createSwatch (Color   col,
                               Integer ind)
    {

        //Label swatch = new Label ();
        Label swatch = new Label ();
        swatch.getStyleClass ().add (StyleClassNames.COLORSWATCH);
        swatch.setBackground (new Background (new BackgroundFill (col, null, null)));
        swatch.setOnMouseClicked (ev ->
        {

            if (ev.isPopupTrigger ())
            {

                return;

            }

            this.setColor (col);

            this.fireEvent (new ObjectSelectedEvent (this.colorProp.getValue (),
                                                     ObjectSelectedEvent.SELECTED_EVENT));

        });

        Group g = new Group ();
        g.setUserData (col);
        g.getChildren ().add (swatch);

        if ((col != Color.WHITE)
            &&
            (col != Color.BLACK)
           )
        {

            ContextMenu cm = new ContextMenu ();

            Set<MenuItem> items = new LinkedHashSet<> ();

            items.add (QuollMenuItem.builder ()
                .label (colorchooser,LanguageStrings.swatch,popupmenu,LanguageStrings.items,remove)
                .styleClassName (StyleClassNames.DELETE)
                .onAction (eev ->
                {

                    //UserProperties.removeUserImagePath (s);
                    UserProperties.removeUserColor (col);

                    this.colorsPane.getChildren ().remove (g);

                })
                .build ());

            cm.getItems ().addAll (items);

            swatch.setContextMenu (cm);

        }

        if (ind > -1)
        {

            this.colorsPane.getChildren ().add (ind, g);

        } else {

            this.colorsPane.getChildren ().add (g);

        }

    };


    public void setOnColorSelected (EventHandler<ObjectSelectedEvent> ev)
    {

        this.addEventHandler (ObjectSelectedEvent.SELECTED_EVENT,
                              ev);

    }

    public void setOnCancel (EventHandler<Form.FormEvent> ev)
    {

        this.addEventHandler (Form.FormEvent.CANCEL_EVENT,
                              ev);

    }

    public void setColor (Color c)
    {

        this.colorProp.setValue (c);

    }

    private void updateColor (Color col)
    {

        try
        {

            this.ignoreUpdates = true;

            this.red.setValue ((int) (255 * col.getRed ()));
            this.green.setValue ((int) (255 * col.getGreen ()));
            this.blue.setValue ((int) (255 * col.getBlue ()));
            this.hex.setText (UIUtils.colorToHex (col));

        } finally {

            this.ignoreUpdates = false;

        }

    }

    private void updateColor (String hex)
    {

        try
        {

            this.ignoreUpdates = true;

            Color c = null;

            try
            {

                c = Color.web (hex);

            } catch (Exception e) {

                UIUtils.setTooltip (this.hex,
                                    new SimpleStringProperty ("Error"));

                return;

            }

            this.colorProp.setValue (c);
            this.red.setValue ((int) (255 * c.getRed ()));
            this.green.setValue ((int) (255 * c.getGreen ()));
            this.blue.setValue ((int) (255 * c.getBlue ()));

        } finally {

            this.ignoreUpdates = false;

        }

    }

    // Get the slider values and create a color, then update the hex.
    private void updateColor ()
    {

        try
        {

            this.ignoreUpdates = true;

            this.colorProp.setValue (Color.rgb (this.red.valueProp.getValue (),
                                                this.green.valueProp.getValue (),
                                                this.blue.valueProp.getValue ()));
            this.hex.setText (UIUtils.colorToHex (this.colorProp.getValue ()));

        } finally {

            this.ignoreUpdates = false;

        }

    }

    private class NumberSelector extends HBox
    {

        private IntegerProperty valueProp = null;
        private Slider slider = null;
        private boolean ignoreUpdates = false;

        public NumberSelector (int min,
                               int max,
                               int initialValue)
        {

            final NumberSelector _this = this;

            this.getStyleClass ().add (StyleClassNames.TYPE);

            this.valueProp = new SimpleIntegerProperty ();

            this.slider = new Slider (min,
                                      max,
                                      initialValue);

            Spinner<Integer> spinner = new Spinner<> (min,
                                                      max,
                                                      initialValue);

            spinner.getStyleClass ().add (Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);
            spinner.setEditable (true);

            HBox.setHgrow (slider, Priority.ALWAYS);
            this.getChildren ().addAll (slider, spinner);

            slider.valueProperty ().addListener ((p, oldv, newv) ->
            {

                if (_this.ignoreUpdates)
                {

                    return;

                }

                _this.valueProp.setValue (newv);

            });

            spinner.valueProperty ().addListener ((p, oldv, newv) ->
            {

                if (_this.ignoreUpdates)
                {

                    return;

                }

                _this.valueProp.setValue (newv);

            });

            this.valueProp.addListener ((p, oldv, newv) ->
            {

                try
                {

                    _this.ignoreUpdates = true;

                    slider.setValue (newv.doubleValue ());
                    spinner.valueFactoryProperty ().get ().setValue (newv.intValue ());

                } finally {

                    _this.ignoreUpdates = false;

                }

            });

        }

        public void setValue (int v)
        {

            this.valueProp.setValue (v);

        }

        public IntegerProperty valueProperty ()
        {

            return this.valueProp;

        }

    }

    public ObjectProperty<Color> colorProperty ()
    {

        return this.colorProp;

    }

    /**
     * Get a builder to create a new menu item.
     *
     * Usage: QuollColorChooser.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static QuollColorChooser.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollColorChooser>
    {

        private String styleName = null;
        private Color color = null;
        private boolean showUserColors = false;

        private Builder ()
        {

        }

        @Override
        public QuollColorChooser build ()
        {

            return new QuollColorChooser (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

        public Builder showUserColors (boolean v)
        {

            this.showUserColors = v;
            return this;

        }

        public Builder color (Color col)
        {

            this.color = col;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;

            return this;

        }

    }

}
