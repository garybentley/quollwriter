package com.quollwriter.ui.fx;

import java.util.*;
import java.util.stream.*;

import javafx.beans.value.*;
import javafx.scene.*;
import javafx.geometry.*;
import javafx.scene.layout.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.util.*;
import javafx.collections.*;

import com.quollwriter.*;
import com.quollwriter.data.IPropertyBinder;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.ui.fx.components.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class TextPropertiesPanel extends VBox
{

    private ComboBox<Font> font = null;
    private Spinner<Integer> fontSize = null;
    private Slider fontSizeSlider = null;
    private ChoiceBox<StringProperty> alignment = null;
    private Spinner<Double> lineSpacing = null;
    private Slider lineSpacingSlider = null;
    private Slider textBorderSlider = null;
    private Spinner<Integer> textBorder = null;
    private CheckBox indentFirstLine = null;
    private CheckBox highlightWritingLine = null;

    public TextPropertiesPanel (AbstractProjectViewer viewer,
                                TextProperties        props)
    {

        this.managedProperty ().bind (this.visibleProperty ());

        Form.Builder fb = Form.builder ();

        this.font = new ComboBox<> ();
        this.font.getStyleClass ().add (StyleClassNames.FONTFAMILY);
        this.font.setEditable (false);
        Callback<ListView<Font>, ListCell<Font>> fact = lv ->
        {

            return new ListCell<Font> ()
            {

                @Override
                protected void updateItem (Font f, boolean empty)
                {

                    super.updateItem (f, empty);

                    if (f == null)
                    {

                        return;

                    }

                    this.setText (f.getName ());
                    this.setFont (f);

                }

            };

        };

        viewer.schedule (() ->
        {

            ObservableList<Font> fs = FXCollections.observableList (Font.getFamilies ().stream ()
                .map (n ->
                {

                    try
                    {

                        Font f = Font.font (n);

                        if (f == null)
                        {

                            return null;

                        }

                        return f;

                    } catch (Exception e) {

                        return null;

                    }

                })
                .filter (n -> n != null)
                .collect (Collectors.toList ()));

            UIUtils.runLater (() ->
            {

                this.font.setItems (fs);

                this.font.getSelectionModel ().select (Font.font (props.getFontFamily ()));

                this.font.getSelectionModel ().selectedItemProperty ().addListener ((pr, oldv, newv) ->
                {

                    props.setFontFamily (newv.getFamily ());

                });

            });

        },
        -1,
        -1);

        this.font.setButtonCell (fact.call (null));
        this.font.setCellFactory (fact);

        // Add the font item.
        fb.item (getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,LanguageStrings.font,text),
                 this.font);

        // Font size
        HBox b = new HBox ();
        b.getStyleClass ().add (StyleClassNames.FONTSIZE);

        int minFontSize = 8;
        int maxFontSize = 50;

        this.fontSizeSlider = new Slider (minFontSize, maxFontSize, props.getFontSize ());
        this.fontSizeSlider.setBlockIncrement (1);
        this.fontSizeSlider.setMajorTickUnit (1);
        HBox.setHgrow (this.fontSizeSlider,
                       Priority.ALWAYS);
        this.fontSize = new Spinner<> (new SpinnerValueFactory.IntegerSpinnerValueFactory (minFontSize, maxFontSize, props.getFontSize ()));
        this.fontSize.setEditable (true);
        this.fontSize.getStyleClass ().add (Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);
        HBox.setHgrow (this.fontSize,
                       Priority.NEVER);

        this.fontSize.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            this.fontSizeSlider.setValue (newv.intValue ());
            props.setFontSize (newv.intValue ());

        });

        this.fontSizeSlider.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            this.fontSize.getValueFactory ().setValue (newv.intValue ());

        });

        b.getChildren ().addAll (this.fontSizeSlider, this.fontSize);

        // Add the font size.
        fb.item (getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,fontsize,text),
                 b);

        Set<StringProperty> aitems = new LinkedHashSet<> ();

        aitems.add (getUILanguageStringProperty (textalignments,left));
                    //QTextEditor.ALIGN_LEFT);
        aitems.add (getUILanguageStringProperty (textalignments,justified));
        //QTextEditor.ALIGN_JUSTIFIED);
        aitems.add (getUILanguageStringProperty (textalignments,right));

        String al = props.getAlignment ();

        int selInd = 0;

        if (al.equals (TextEditor.ALIGN_JUSTIFIED))
        {

            selInd = 1;

        }

        if (al.equals (TextEditor.ALIGN_RIGHT))
        {

            selInd = 2;

        }

        this.alignment = QuollChoiceBox.builder ()
            .items (aitems)
            .styleClassName ("alignment")
            .selectedIndex (selInd)
            .onSelected (ev ->
            {

                QuollChoiceBox cb = (QuollChoiceBox) ev.getSource ();

                int v = cb.getSelectionModel ().getSelectedIndex ();

                if (v == 0)
                {

                    props.setAlignment (TextEditor.ALIGN_LEFT);

                }

                if (v == 1)
                {

                    props.setAlignment (TextEditor.ALIGN_JUSTIFIED);

                }

                if (v == 2)
                {

                    props.setAlignment (TextEditor.ALIGN_RIGHT);

                }

            })
            .build ();

        // Alignment.
        fb.item (getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,LanguageStrings.alignment,text),
                 this.alignment);

        // Line spacing.
        b = new HBox ();
        b.getStyleClass ().add (StyleClassNames.LINESPACING);

        double minLS = 0.5;
        double maxLS = 2.5;
        double stepBy = 0.1;

        this.lineSpacingSlider = new Slider (minLS, maxLS, props.getLineSpacing ());
        this.lineSpacingSlider.setBlockIncrement (1);
        this.lineSpacingSlider.setMajorTickUnit (0.1f);
        this.lineSpacingSlider.setMinorTickCount (0);
        this.lineSpacingSlider.setSnapToTicks (true);
        HBox.setHgrow (this.lineSpacingSlider,
                       Priority.ALWAYS);
        this.lineSpacing = new Spinner<> (new SpinnerValueFactory.DoubleSpinnerValueFactory (minLS, maxLS, props.getLineSpacing (), stepBy));
        this.lineSpacing.setEditable (true);
        this.lineSpacing.getStyleClass ().add (Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);
        HBox.setHgrow (this.lineSpacing,
                       Priority.NEVER);

        this.lineSpacing.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            this.lineSpacingSlider.setValue (newv.doubleValue ());
            props.setLineSpacing (newv.floatValue ());

        });

        this.lineSpacingSlider.valueProperty ().addListener ((pr, oldv, newv) ->
        {

            this.lineSpacing.getValueFactory ().setValue ((double) Math.round ((newv.doubleValue () * 10f)) / 10f);

        });

        b.getChildren ().addAll (this.lineSpacingSlider, this.lineSpacing);

        // Add the line spacing.
        fb.item (getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,linespacing,text),
                 b);

        // Text border.
         b = new HBox ();
         b.getStyleClass ().add (StyleClassNames.TEXTBORDER);

         int min = 0;
         int max = 150;
         int stepByW = 1;

         this.textBorderSlider = new Slider (min, max, props.getTextBorder ());
         this.textBorderSlider.setBlockIncrement (1);
         this.textBorderSlider.setMajorTickUnit (1);
         this.textBorderSlider.setMinorTickCount (0);
         this.textBorderSlider.setSnapToTicks (true);
         HBox.setHgrow (this.textBorderSlider,
                        Priority.ALWAYS);

         this.textBorder = new Spinner<> (new SpinnerValueFactory.IntegerSpinnerValueFactory (min, max, props.getTextBorder (), stepByW));
         this.textBorder.setEditable (true);
         this.textBorder.getStyleClass ().add (Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL);
         HBox.setHgrow (this.textBorder,
                        Priority.NEVER);

         this.textBorder.valueProperty ().addListener ((pr, oldv, newv) ->
         {

             this.textBorderSlider.setValue (newv.intValue ());
             props.setTextBorder (newv.intValue ());

         });

         this.textBorderSlider.valueProperty ().addListener ((pr, oldv, newv) ->
         {

             this.textBorder.getValueFactory ().setValue (newv.intValue ());

         });

         b.getChildren ().addAll (this.textBorderSlider, this.textBorder);

         // Add the text border width.
         fb.item (getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,textborder,text),
                  b);

        this.indentFirstLine = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,indentfirstline,text))
            .selected (props.getFirstLineIndent ())
            .build ();

        this.indentFirstLine.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            props.setFirstLineIndent (newv);

        });

        // TODO fb.item (this.indentFirstLine);

        this.highlightWritingLine = QuollCheckBox.builder ()
            .label (getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,highlightwritingline,text))
            .selected (props.isHighlightWritingLine ())
            .build ();

        this.highlightWritingLine.selectedProperty ().addListener ((pr, oldv, newv) ->
        {

            props.setHighlightWritingLine (newv);

        });

        fb.item (this.highlightWritingLine);

        fb.item (getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,highlightlinecolor,text),
                 UIUtils.createColorSelectorSwatch (viewer,
                                                    "writing-line-color-chooser",
                                                    getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,highlightlinecolor,popup,title),
                                                    props.getWritingLineColor (),
                                                    col ->
                                                    {

                                                        props.setWritingLineColor (col);

                                                    }));

        fb.item (getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,textcolor,text),
                 UIUtils.createColorSelectorSwatch (viewer,
                                                    "text-color-chooser",
                                                    getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,textcolor,popup,title),
                                                    props.getTextColor (),
                                                    col ->
                                                    {

                                                        props.setTextColor (col);

                                                    }));

        fb.item (getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,bgcolor,text),
                 UIUtils.createColorSelectorSwatch (viewer,
                                                    "text-color-chooser",
                                                    getUILanguageStringProperty (project,LanguageStrings.sidebar,textproperties,bgcolor,popup,title),
                                                    props.getBackgroundColor (),
                                                    col ->
                                                    {

                                                        props.setBackgroundColor (col);

                                                    }));

        Form ff = fb.build ();

        this.getChildren ().add (ff);

    }

}
