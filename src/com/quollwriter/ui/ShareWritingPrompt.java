package com.quollwriter.ui;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.*;


public class ShareWritingPrompt extends PopupWindow
{

    private String prompt = null;

    public ShareWritingPrompt(AbstractProjectViewer v,
                              String                prompt)
    {

        super (v);

        this.prompt = prompt;

    }

    public String getWindowTitle ()
    {

        return "Share your prompt";

    }

    public String getHeaderTitle ()
    {

        return "Share your prompt";

    }

    public String getHeaderIconType ()
    {

        return "share";

    }

    public String getHelpText ()
    {

        return "Please note that all submitted prompts are subject to approval before being released to others.<br /><br />You can specify your name and a website that you want to link to.  The website must be one of the following:<ul><li>A link to your personal website/blog.</li><li>A link to where the story can be found online (either purchased or read).</li><li>A link to a writing site that is designed to help writers.</li></ul>";

    }

    public JComponent getContentPanel ()
    {

        Box b = new Box (BoxLayout.Y_AXIS);

        b.add (UIUtils.createBoldSubHeader ("Your prompt",
                                            null));

        JTextArea t = UIUtils.createTextArea (3);
        t.setText (this.prompt);
        t.setCaretPosition (0);

        JScrollPane sp = new JScrollPane (t);
        sp.setBorder (null);
        sp.setAlignmentX (java.awt.Component.LEFT_ALIGNMENT);
        b.add (sp);

        b.add (Box.createVerticalStrut (20));
        b.add (UIUtils.createBoldSubHeader ("Your details",
                                            null));

        FormLayout fl = new FormLayout ("10px, right:p, 6px, 200px, 10px",
                                        "p, 6px, p");

        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        builder.addLabel ("Name",
                          cc.xy (2,
                                 1));

        JTextField name = UIUtils.createTextField ();

        builder.add (name,
                     cc.xy (4,
                            1));

        builder.addLabel ("Link to Story/Book",
                          cc.xy (2,
                                 3));

        JTextField link = UIUtils.createTextField ();

        builder.add (link,
                     cc.xy (4,
                            3));

        JPanel p = builder.getPanel ();
        p.setOpaque (false);
        p.setAlignmentX (java.awt.Component.LEFT_ALIGNMENT);
        p.setBorder (new EmptyBorder (5,
                                      0,
                                      5,
                                      0));

        b.add (p);

        return b;

    }

    public JButton[] getButtons ()
    {

        final ShareWritingPrompt _this = this;

        JButton b = new JButton ("Share");
        JButton c = new JButton ("Cancel");

        c.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    _this.close ();

                }

            });

        JButton[] buts = new JButton[2];
        buts[0] = b;
        buts[1] = c;

        return buts;

    }

}
