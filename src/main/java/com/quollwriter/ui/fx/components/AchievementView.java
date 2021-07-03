package com.quollwriter.ui.fx.components;

import javafx.scene.layout.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.effect.*;
import javafx.scene.control.*;

import com.quollwriter.achievements.rules.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.data.*;
import com.quollwriter.*;

// TODO Use a Skin?

public class AchievementView extends HBox
{

    public AchievementView (AchievementRule ar,
                            boolean         achieved,
                            IPropertyBinder binder)
    {

        this.getStyleClass ().add (StyleClassNames.ACHIEVEMENT);
        this.setId ("achievement-" + ar.getId ());
        this.getStyleClass ().add (ar.getIcon ());
        this.pseudoClassStateChanged (StyleClassNames.ACHIEVED_PSEUDO_CLASS, achieved);
        this.managedProperty ().bind (this.visibleProperty ());

        //HBox tb = new HBox ();

        Group g = new Group ();

        IconBox h = null;

        try
        {

            UserConfigurableObjectType t = Environment.getUserConfigurableObjectType (ar.getIcon ());

            if ((t != null)
                &&
                (!Chapter.OBJECT_TYPE.equals (ar.getIcon ()))
               )
            {

                h = IconBox.builder ()
                    .binder (binder)
                    .image (t.icon24x24Property ())
                    .build ();

            } else {

                h = IconBox.builder ()
                    .iconName (ar.getIcon ())
                    .build ();

            }

        } catch (Exception e) {

            // Ignore?

        }

        g.getChildren ().add (h);
/*
        ImageView image = new ImageView ();
        image.getStyleClass ().add (StyleClassNames.ICON);
        image.managedProperty ().bind (image.visibleProperty ());
*/
        Pane ach = new Pane ();
        ach.getStyleClass ().add (StyleClassNames.ACHIEVED);
        ach.managedProperty ().bind (ach.visibleProperty ());
/*
        ImageView image2 = new ImageView ();
        image2.getStyleClass ().add (StyleClassNames.ACHIEVED);
        image2.managedProperty ().bind (image2.visibleProperty ());
*/
        g.setBlendMode (BlendMode.SRC_OVER);
        //g.getChildren ().add (image);
        g.getChildren ().add (ach);

        Label title = new Label ();
        title.getStyleClass ().add (StyleClassNames.TITLE);
        title.textProperty ().bind (ar.nameProperty ());

        HBox.setHgrow (title,
                       Priority.ALWAYS);
/*
        tb.getChildren ().addAll (g, title);
        tb.getStyleClass ().add (StyleClassNames.HEADER);
*/

        BasicHtmlTextFlow rt = BasicHtmlTextFlow.builder ()
        //QuollTextView rt = QuollTextView.builder ()
            .text (ar.descriptionProperty ())
            .styleClassName (StyleClassNames.DESCRIPTION)
            .build ();

        VBox b = new VBox ();
        b.getChildren ().addAll (title, rt);

        this.getChildren ().addAll (g, b);

    }

}
