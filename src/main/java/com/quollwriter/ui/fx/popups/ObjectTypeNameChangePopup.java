package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.text.*;

import javafx.geometry.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.beans.property.*;

import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class ObjectTypeNameChangePopup extends PopupContent
{

    public static final String POPUP_ID = "objecttypenamechange";

    private Map<String, TextField> singular = new HashMap<> ();
    private Map<String, TextField> plural = new HashMap<> ();

    public ObjectTypeNameChangePopup (AbstractViewer viewer)
    {

        super (viewer);

        VBox content = new VBox ();
        content.getChildren ().add (BasicHtmlTextFlow.builder ()
            .text (getUILanguageStringProperty (objectnames,changer,LanguageStrings.popup,text))
            .withHandler (viewer)
            .build ());

        List<String> objTypes = new ArrayList<> ();
        objTypes.add (Chapter.OBJECT_TYPE);
        objTypes.add (OutlineItem.OBJECT_TYPE);
        objTypes.add (Scene.OBJECT_TYPE);
        objTypes.add (Note.OBJECT_TYPE);
        objTypes.add (Project.OBJECT_TYPE);
        objTypes.add (Warmup.OBJECT_TYPE);
        objTypes.add (EditorEditor.OBJECT_TYPE);

        GridPane gp = new GridPane ();
        gp.getStyleClass ().add (StyleClassNames.ITEMS);
        ColumnConstraints col0 = new ColumnConstraints ();
        col0.setHgrow (Priority.NEVER);

        ColumnConstraints col1 = new ColumnConstraints ();
        col1.setHgrow (Priority.ALWAYS);
        //col1.setPercentWidth (50d);
        col1.setHalignment (HPos.LEFT);

        ColumnConstraints col2 = new ColumnConstraints ();
        col2.setHgrow (Priority.ALWAYS);
        //col2.setPercentWidth (50d);
        col2.setHalignment (HPos.LEFT);

        gp.getColumnConstraints ().add (col0);
        gp.getColumnConstraints ().add (col1);
        gp.getColumnConstraints ().add (col2);

        int row = 0;
        gp.add (QuollLabel.builder ()
                    .styleClassName (StyleClassNames.TITLE)
                    .label (objectnames,changer,LanguageStrings.popup,labels,LanguageStrings.singular)
                    .build (),
                1,
                row);

        gp.add (QuollLabel.builder ()
                    .styleClassName (StyleClassNames.TITLE)
                    .label (objectnames,changer,LanguageStrings.popup,labels,LanguageStrings.plural)
                    .build (),
                2,
                row);

        row++;

        for (String ot : objTypes)
        {

            HBox h = new HBox ();
            h.getStyleClass ().add (StyleClassNames.ICONBOX);
            Pane p = new Pane ();
            p.getStyleClass ().add (ot + "-" + StyleClassNames.ICON);
            p.getStyleClass ().add (StyleClassNames.ICON);
            h.getChildren ().add (p);

            Label l = new Label ();
            l.setGraphic (h);

            ImageView iv = new ImageView ();
            iv.getStyleClass ().add (ot);

            gp.add (l,
            //iv,
                    0,
                    row);

            QuollTextField sf = QuollTextField.builder ()
                .text (Environment.getObjectTypeName (ot).getValue ())
                .build ();

            gp.add (sf,
                    1,
                    row);

            this.singular.put (ot,
                               sf);

            QuollTextField pf = QuollTextField.builder ()
                .text (Environment.getObjectTypeNamePlural (ot).getValue ())
                .build ();

            gp.add (pf,
                    2,
                    row);

            this.plural.put (ot,
                             pf);

            row++;

        }

        content.getChildren ().add (gp);

        content.getChildren ().add (QuollButtonBar.builder ()
            .button (QuollButton.builder ()
                        .label (objectnames,changer,LanguageStrings.popup,buttons,save)
                        .buttonType (ButtonBar.ButtonData.APPLY)
                        .onAction (ev ->
                        {

                            this.save ();

                        })
                        .build ())
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.CANCEL_CLOSE)
                        .label (objectnames,changer,LanguageStrings.popup,buttons,cancel)
                        .onAction (ev ->
                        {

                            this.close ();

                        })
                        .build ())
            .button (QuollButton.builder ()
                        .buttonType (ButtonBar.ButtonData.OTHER)
                        .label (objectnames,changer,LanguageStrings.popup,buttons,reset)
                        .onAction (ev ->
                        {

                            this.reset ();

                        })
                        .build ())
            .build ());
        this.getChildren ().add (content);

    }

    private void reset ()
    {

        try
        {

            Environment.resetObjectTypeNamesToDefaults ();

            for (String ot : this.singular.keySet ())
            {

                TextField f = this.singular.get (ot);

                f.setText (Environment.getObjectTypeName (ot).getValue ());

            }

            for (String ot : this.plural.keySet ())
            {

                TextField f = this.plural.get (ot);

                f.setText (Environment.getObjectTypeNamePlural (ot).getValue ());

            }

        } catch (Exception e) {

            Environment.logError ("Unable to modify names",
                                  e);

            ComponentUtils.showErrorMessage (this.viewer,
                                             getUILanguageStringProperty (objectnames,changer,resetchange,actionerror));
                                      //"Unable to modify names");

            return;

        }

    }

    private void save ()
    {

        final Map<String, StringProperty> sing = new HashMap<> ();
        final Map<String, StringProperty> plur = new HashMap<> ();

        Set<UserConfigurableObjectType> updateTypes = new HashSet<> ();
        for (String ot : this.singular.keySet ())
        {

            TextField f = this.singular.get (ot);

            String s = f.getText ().trim ();

            if (!s.equals (Environment.getObjectTypeName (ot)))
            {

                sing.put (ot,
                          new SimpleStringProperty (s));

            }

        }

        for (String ot : this.plural.keySet ())
        {

            TextField f = this.plural.get (ot);

            String p = f.getText ().trim ();

            if (!p.equals (Environment.getObjectTypeNamePlural (ot)))
            {

                plur.put (ot,
                          new SimpleStringProperty (p));

            }

        }

        boolean changing = false;

        if ((sing.size () > 0)
            ||
            (plur.size () > 0)
           )
        {

            changing = true;

        }

        if (changing)
        {

            try
            {

                Environment.updateUserObjectTypeNames (sing,
                                                       plur);

            } catch (Exception e) {

                Environment.logError ("Unable to modify names",
                                      e);

                ComponentUtils.showErrorMessage (this.viewer,
                                                 getUILanguageStringProperty (objectnames,changer,confirmchange,actionerror));
                                          //"Unable to modify names");

                return;

            }

        }

        this.close ();

    }

    @Override
    public QuollPopup createPopup ()
    {

        QuollPopup p = QuollPopup.builder ()
            .title (objectnames,changer, LanguageStrings.popup,title)
            .styleClassName (StyleClassNames.OBJECTTYPENAMECHANGE)
            .styleSheet (StyleClassNames.OBJECTTYPENAMECHANGE)
            .headerIconClassName (StyleClassNames.EDIT)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .removeOnClose (true)
            .withViewer (this.viewer)
            .build ();

        p.requestFocus ();

        return p;

    }

}
