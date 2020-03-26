package com.quollwriter.ui.fx.components;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import javafx.event.*;
import javafx.scene.Node;
import javafx.beans.value.*;
import javafx.beans.binding.*;
import javafx.scene.input.*;
import javafx.beans.property.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.image.*;
import javafx.scene.shape.*;
import javafx.geometry.*;
import javafx.concurrent.*;

import org.fxmisc.flowless.*;
import org.fxmisc.wellbehaved.event.*;

import org.fxmisc.richtext.model.*;

import org.jsoup.*;
import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;
import com.quollwriter.uistrings.UILanguageStringsManager;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class QuollTextView extends VBox
{

    private WebViewFitContent text = null;

    private QuollTextView (Builder b)
    {

        this.getStyleClass ().add (StyleClassNames.QTEXTVIEW);

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        this.managedProperty ().bind (this.visibleProperty ());

        this.text = new WebViewFitContent ((link, ev) ->
        {

            UIUtils.openURL (b.viewer != null ? b.viewer :Environment.getFocusedViewer (),
                             link,
                             ev);

        });

        this.visibleProperty ().addListener ((pr, oldv, newv) ->
        {

            this.text.setVisible (newv);

        });

        this.getChildren ().add (this.text);

        this.text.setFormatter (b.formatter);

        if (b.text != null)
        {

            b.text.addListener ((pr, oldv, newv) ->
            {

                this.text.setContent (newv);

            });

            this.text.setContent (b.text.getValue ());

        }

    }

    /**
     *
     * @returns A new builder.
     */
    public static QuollTextView.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, QuollTextView>
    {

        private StringProperty text = null;
        private String styleName = null;
        private AbstractViewer viewer = null;
        private Function<String, String> formatter = null;

        private Builder ()
        {

        }

        public Builder formatter (Function<String, String> f)
        {

            this.formatter = f;
            return this;

        }

        public Builder inViewer (AbstractViewer viewer)
        {

            this.viewer = viewer;
            return this;

        }

        public Builder text (ObjectProperty<StringWithMarkup> t)
        {

            this.text = new SimpleStringProperty ();
            this.text.bind (UILanguageStringsManager.createStringBinding (() ->
            {

                StringWithMarkup sm = t.getValue ();

                if (sm == null)
                {

                    return null;

                }

                return sm.getMarkedUpText ();

            },
            t));
            return this;

        }

        public Builder text (StringWithMarkup t)
        {

            if (t != null)
            {

                this.text = new SimpleStringProperty (t.getMarkedUpText ());

            }

            return this;

        }

        public Builder text (String t)
        {

            this.text = new SimpleStringProperty (t);
            return this;

        }

        public Builder text (StringProperty t)
        {

            this.text = t;
            return this;

        }

        public Builder styleClassName (String n)
        {

            this.styleName = n;
            return this;

        }

        @Override
        public QuollTextView build ()
        {

            return new QuollTextView (this);

        }

        @Override
        public Builder _this ()
        {

            return this;

        }

    }

    public static abstract class AbstractSegment<E extends Node>
    {

        protected Collection<String> styleClassNames = null;

        public abstract E createNode ();

        public void setStyleClassNames (Collection<String> styleClassNames)
        {

            this.styleClassNames = styleClassNames;

        }

    }

    public static abstract class SegmentedSegment<X extends Region> extends AbstractSegment<X>
    {

        protected List<AbstractSegment> segs = null;

        public SegmentedSegment (List<AbstractSegment> segs)
        {

            this.segs = segs;

        }

    }

    public static class HRSegment extends AbstractSegment<Path>
    {

        public HRSegment ()
        {

            super ();

        }

        @Override
        public Path createNode ()
        {

            HLineTo h = new HLineTo ();

            // Return a path that is the width of the bounds.
            Path p = new Path ();
            p.getElements ().add (h);
            // TODO h.xProperty ().bind (p.layoutBounds ().widthProperty ());
            return p;

        }

    }

    public static class ImageSegment extends AbstractSegment<ImageView>
    {

        public ImageSegment (String src)
        {

            // TODO

        }

        @Override
        public ImageView createNode ()
        {

            ImageView iv = new ImageView ();

            if (this.styleClassNames != null)
            {

                iv.getStyleClass ().addAll (this.styleClassNames);

            }

            return iv;

        }

    }

    public static class InlineSegment extends SegmentedSegment<TextFlow>
    {

        protected InlineSegment ()
        {

            super (new ArrayList<> ());

        }

        public InlineSegment (List<AbstractSegment> segs)
        {

            super (segs);

        }

        @Override
        public TextFlow createNode ()
        {

            TextFlow b = new TextFlow ();

            if (this.styleClassNames != null)
            {

                b.getStyleClass ().addAll (this.styleClassNames);

            }

            b.getChildren ().addAll (this.segs.stream ()
                .map (s -> s.createNode ())
                .collect (Collectors.toList ()));

            return b;

        }

    }

    public static class BlockSegment extends SegmentedSegment<VBox>
    {

        private ReadOnlyDoubleProperty widthProp = null;

        public BlockSegment (List<AbstractSegment> segs,
                             ReadOnlyDoubleProperty        w)
        {

            super (segs);

            this.widthProp = w;

        }

        @Override
        public VBox createNode ()
        {

            VBox b = new VBox ();
            b.minWidthProperty ().bind (this.widthProp);
            b.prefWidthProperty ().bind (this.widthProp);

            if (this.styleClassNames != null)
            {

                b.getStyleClass ().addAll (this.styleClassNames);

            }

            b.getChildren ().addAll (this.segs.stream ()
                .map (s -> s.createNode ())
                .collect (Collectors.toList ()));

            return b;

        }

    }

    public static class LISegment extends InlineSegment
    {

        public LISegment (List<AbstractSegment> segs)
        {

            super (segs);

        }

        @Override
        public TextFlow createNode ()
        {

            TextFlow b = super.createNode ();

            ImageView im = new ImageView ();
            im.getStyleClass ().add (StyleClassNames.BULLET);

            b.getChildren ().add (0,
                                  im);

            return b;

        }

    }

    public static class HyperlinkSegment extends InlineSegment
    {

        private Consumer<Node> onClick = null;

        public HyperlinkSegment (List<AbstractSegment> segs,
                                 Consumer<Node>        onClick)
        {
            super (segs);
            this.onClick = onClick;

        }

        @Override
        public TextFlow createNode ()
        {

            TextFlow te = super.createNode ();

            te.setOnMouseClicked (ev ->
            {

                try
                {

                    this.onClick.accept (te);

                } catch (Exception e) {

                    Environment.logError ("Unable to run",
                                          e);

                    // TODO Show error to user.

                }

            });

            return te;
        }

    }

    public static class TextSegment extends AbstractSegment<Text>
    {
        private final String text;

        public TextSegment (String text)
        {

            this.text = text;

        }

        @Override
        public Text createNode ()
        {

            javafx.scene.text.Text te = new javafx.scene.text.Text (text);
            te.setTextOrigin (VPos.TOP);
            te.getStyleClass ().add (StyleClassNames.TEXT);

            return te;
        }

    }

}
