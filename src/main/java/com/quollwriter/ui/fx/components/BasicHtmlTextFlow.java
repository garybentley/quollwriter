package com.quollwriter.ui.fx.components;

import javafx.collections.*;
import javafx.beans.property.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.text.*;

import org.jsoup.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.viewers.*;

public class BasicHtmlTextFlow extends TextFlow
{

    private StringProperty textProp = null;
    private AbstractViewer viewer = null;

    private BasicHtmlTextFlow (Builder b)
    {

        final BasicHtmlTextFlow _this = this;

        this.textProp = new SimpleStringProperty ();

        this.viewer = b.viewer;

        if (b.styleName != null)
        {

            this.getStyleClass ().add (b.styleName);

        }

        this.setText (b.text);
        this.textProp.addListener ((p, oldv, newv) ->
        {

            _this.update ();

        });

        this.update ();

    }

    public AbstractViewer getViewer ()
    {

        return this.viewer;

    }

    public StringProperty textProperty ()
    {

        return this.textProp;

    }

    public void setText (StringProperty text)
    {

        this.textProp.unbind ();

        if (text == null)
        {

            return;

        }

        this.textProp.bind (text);

    }

    public void setText (String text)
    {

        this.textProp.unbind ();
        this.textProp.setValue (text);

    }

    private void update ()
    {

        final BasicHtmlTextFlow _this = this;

        ObservableList<Node> children = this.getChildren ();

        children.clear ();

        String text = this.textProp.getValue ();

        if ((text == null)
            ||
            (text.length () == 0)
           )
        {

            return;

        }

        org.jsoup.nodes.Document doc = Jsoup.parse (text);

        for (org.jsoup.nodes.Node n : doc.body ().childNodes ())
        {

            if (n instanceof org.jsoup.nodes.TextNode)
            {

                org.jsoup.nodes.TextNode tn = (org.jsoup.nodes.TextNode) n;
                String t = tn.text ();

                // TODO Expand, consider using: https://code.i-harness.com/en/q/f2c1b
                t = StringUtils.replaceString (t,
                                               "&nbsp;",
                                               "\u00A0");

                children.add (new Text (t));

            }

            if (n instanceof org.jsoup.nodes.Element)
            {

                org.jsoup.nodes.Element el = (org.jsoup.nodes.Element) n;

                if (el.tagName ().equalsIgnoreCase ("br"))
                {

                    Text _t = new Text ("\n");
                    children.add (_t);
                    continue;

                }

                if (el.tagName ().equalsIgnoreCase ("a"))
                {

                    Hyperlink _t = new Hyperlink (el.ownText ());
                    _t.setOnMouseClicked (ev ->
                    {

                        UIUtils.openURL (_this.viewer,
                                         el.attributes ().get ("href"));

                    });
                    children.add (_t);
                    continue;

                }

                Text t = new Text (el.ownText ());
                t.getStyleClass ().add (el.tagName ());
                children.add (t);

            }

        }

    }

    /**
     * Get a builder to create a new AccordionItem.
     *
     * Usage: AccordionItem.builder ().styleName ("hello").build ();
     * @returns A new builder.
     */
    public static BasicHtmlTextFlow.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder implements IBuilder<Builder, BasicHtmlTextFlow>
    {

        private StringProperty text = null;
        private AbstractViewer viewer = null;
        private String styleName = null;

        private Builder ()
        {

        }

        @Override
        public BasicHtmlTextFlow build ()
        {

            return new BasicHtmlTextFlow (this);

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

        public Builder withViewer (AbstractViewer v)
        {

            this.viewer = v;
            return this;

        }

        public Builder text (String text)
        {

            this.text = new SimpleStringProperty (text);
            return this;

        }

        public Builder text (StringProperty text)
        {

            this.text = text;
            return this;

        }

    }

}
