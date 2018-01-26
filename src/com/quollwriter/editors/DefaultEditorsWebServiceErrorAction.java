package com.quollwriter.editors;

import java.awt.event.*;

import java.net.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.data.editors.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class DefaultEditorsWebServiceErrorAction implements EditorsWebServiceAction
{

    private EditorsWebServiceHandler handler = null;

    public DefaultEditorsWebServiceErrorAction (EditorsWebServiceHandler handler)
    {

        this.handler = handler;

    }

    protected void handleForbiddenResponse ()
    {

        EditorsEnvironment.clearUserPassword ();

        AbstractViewer viewer = Environment.getFocusedViewer ();

        if (viewer == null)
        {

            throw new IllegalStateException ("Unable to handle response, no viewer available.");

        }

        UIUtils.doLater (new ActionListener ()
        {

            public void actionPerformed (ActionEvent ev)
            {

                AbstractViewer viewer = Environment.getFocusedViewer ();

                EditorsUIUtils.showLoginError (getUIString (editors,login,error,maxloginattempts));
                //"You have reached the maximum number of login attempts possible.  Please try logging in again in a few minutes.");

            }

        });

    }

    protected void handleUnauthorizedResponse (EditorsWebServiceResult res,
                                               ActionListener          onLogin,
                                               ActionListener          onCancel)
    {

        AbstractViewer viewer = Environment.getFocusedViewer ();

        if (viewer == null)
        {

            throw new IllegalStateException ("Unable to handle response, no viewer available.");

        }

        if (res.getErrorType ().equals ("InvalidCredentials"))
        {

            EditorsEnvironment.clearUserPassword ();

            // Need to login again, probably the password has been changed.
            EditorsUIUtils.showLoginError (getUIString (editors,login,error,invalidcredentials),
                                           //"Your login details do not appear to be correct.",
                                           onLogin,
                                           onCancel);

            return;

        }

        if (res.getErrorType ().equals ("AccountNotActive"))
        {

            // Need to activate the account first.
            EditorsUIUtils.showLoginError (getUIString (editors,login,error,inactiveaccount));
            //"You must activate your account (check your email) first.");

            return;

        }

        // See if we have login credentials, if so try getting a new session.
        if ((res.getErrorType ().equals ("SessionExpired"))
            ||
            (res.getErrorType ().equals ("UnknownSessionId"))
           )
        {

            EditorAccount acc = EditorsEnvironment.getUserAccount ();

            if (acc.getPassword () == null)
            {

                // How did we wind up here?
                EditorsUIUtils.showLoginError (getUIString (editors,login,error,invalidcredentials),
                                               //"Your login details do not appear to be correct.",
                                               onLogin,
                                               onCancel);

                return;

            }

            acc.setWebServiceSessionId (null);

            this.handler.login (onLogin,
                                null);

            return;

        }

        EditorsEnvironment.clearUserPassword ();

        // Something unexpected happened, just show the login again.
        EditorsUIUtils.showLoginError (getUIString (editors,login,error,invalidcredentials),
                                       //"Your login details do not appear to be correct.",
                                       onLogin,
                                       onCancel);

    }

    public void processResult (final EditorsWebServiceResult res)
    {

        if (!res.isError ())
        {



        }

        // Has the current session expired?
        if (res.code == HttpURLConnection.HTTP_UNAUTHORIZED)
        {

            this.handleUnauthorizedResponse (res,
                                             new ActionListener ()
                                             {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    EditorAccount acc = EditorsEnvironment.getUserAccount ();

                                                    // Recall.
                                                    res.getCall ().sessionId (acc.getWebServiceSessionId ()).call ();

                                                }

                                             },
                                             null);

            return;

        }

        if (res.code == HttpURLConnection.HTTP_FORBIDDEN)
        {

            this.handleForbiddenResponse ();

            return;

        }

        // Get the response code.
        if (res.code != HttpURLConnection.HTTP_OK)
        {

            EditorsUIUtils.showResultError (res);

        }

    }

}
