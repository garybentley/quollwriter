package com.quollwriter.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.jgoodies.forms.factories.*;

import com.quollwriter.*;

import com.quollwriter.ui.components.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class PasswordInputWindow extends TextInputWindow
{

    private String iconType = null;
    private JButton[] buttons = null;
    private String headerTitle = null;
    private String windowTitle = null;
    private String message = null;

    public PasswordInputWindow()
    {

        this.text = UIUtils.createPasswordField ();

    }

    public static PasswordInputWindow create (String title,
                                              String icon,
                                              String message,
                                              String confirmButtonLabel,
                                              final ValueValidator<String> validator,
                                              final ActionListener onConfirm,
                                              final ActionListener onCancel)
    {

        final PasswordInputWindow ti = new PasswordInputWindow ();

        ti.addWindowListener (new WindowAdapter ()
        {

            @Override
            public void windowClosing (WindowEvent ev)
            {

                if (onCancel != null)
                {

                    onCancel.actionPerformed (new ActionEvent (ti,
                                                               0,
                                                               "cancel"));

                }

            }

        });

        ti.setHeaderTitle (title);
        ti.setMessage (message);

        ti.setHeaderIconType (icon);

        ti.setWindowTitle (title);

        JButton confirm = null;
        JButton cancel = UIUtils.createButton (getUIString (LanguageStrings.buttons, LanguageStrings.cancel),
                                               new ActionListener ()
        {

            @Override
            public void actionPerformed (ActionEvent ev)
            {

                if (onCancel != null)
                {

                    onCancel.actionPerformed (new ActionEvent (ti,
                                                               0,
                                                               "cancel"));

                }

                ti.close ();

            }

        });

        if (onConfirm != null)
        {

            ActionListener confirmAction = new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    if (validator != null)
                    {

                        String mess = validator.isValid (ti.getText ());

                        if (mess != null)
                        {

                            ti.setError (mess);

                            ti.showError (true);

                            ti.resize ();

                            return;

                        }

                    }

                    onConfirm.actionPerformed (new ActionEvent (ti,
                                                                0,
                                                                ti.getText ()));

                    ti.close ();

                }

            };

            confirm = UIUtils.createButton (confirmButtonLabel,
                                            confirmAction);

            UIUtils.addDoActionOnReturnPressed (ti.getTextField (),
                                                confirmAction);

        }

        JButton[] buts = null;

        if (confirm != null)
        {

            buts = new JButton[] { confirm, cancel };

        } else {

            buts = new JButton[] { cancel };

        }

        ti.setButtons (buts);

        ti.init ();

        return ti;

    }

}
