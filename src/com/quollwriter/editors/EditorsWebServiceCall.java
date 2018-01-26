package com.quollwriter.editors;

import java.io.*;
import java.net.*;
import java.util.*;

import com.quollwriter.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

// Sort of following the builder pattern here, more for ease of use and construction.

public class EditorsWebServiceCall
{

    private EditorsWebServiceHandler.Service service = null;
    private String id = null;
    private Object data = null;
    private String method = null;
    private Map<String, String> headers = null;
    private EditorsWebServiceAction onComplete = null;
    private EditorsWebServiceAction onError = null;
    private String sessionId = null;

    public EditorsWebServiceCall ()
    {

    }

    public EditorsWebServiceCall (EditorsWebServiceHandler.Service service,
                                  String                           id,
                                  Object                           data,
                                  String                           method,
                                  Map<String, String>              headers,
                                  EditorsWebServiceAction          onComplete,
                                  EditorsWebServiceAction          onError)
    {

        this.service = service;
        this.data = data;
        this.method = method;
        this.headers = headers;
        this.id = id;
        this.onComplete = onComplete;
        this.onError = onError;

    }

    public EditorsWebServiceCall headers (Map<String, String> headers)
    {

        this.headers = headers;

        return this;

    }

    public EditorsWebServiceCall method (String method)
    {

        this.method = method;

        return this;

    }

    public EditorsWebServiceCall data (Object data)
    {

        this.data = data;

        return this;

    }

    public EditorsWebServiceCall id (String id)
    {

        this.id = id;

        return this;

    }

    public EditorsWebServiceCall service (EditorsWebServiceHandler.Service service)
    {

        this.service = service;

        return this;

    }

    public EditorsWebServiceCall sessionId (String s)
    {

        this.sessionId = s;

        return this;

    }

    public EditorsWebServiceCall onComplete (EditorsWebServiceAction onComplete)
    {

        this.onComplete = onComplete;

        return this;

    }

    public EditorsWebServiceCall onError (EditorsWebServiceAction onError)
    {

        this.onError = onError;

        return this;

    }

    private String encodeParameters (Object data)
                                     throws UnsupportedEncodingException
    {

        if (data == null)
        {

            return "";

        }

        if (data instanceof String)
        {

            return data.toString ();

        }

        if (data instanceof Map)
        {

            Map m = (Map) data;

            StringBuilder b = new StringBuilder ();

            Iterator iter = m.keySet ().iterator ();

            while (iter.hasNext ())
            {

                Object k = iter.next ();

                if (k == null)
                {

                    continue;

                }

                Object v = m.get (k);

                if ((b.length () > 0)
                    &&
                    (!b.toString ().endsWith ("&"))
                   )
                {

                    b.append ("&");

                }

                if (v instanceof Collection)
                {

                    Collection c = (Collection) v;

                    Iterator citer = c.iterator ();

                    while (citer.hasNext ())
                    {

                        Object co = citer.next ();

                        if (co == null)
                        {

                            continue;

                        }

                        if ((b.length () > 0)
                            &&
                            (!b.toString ().endsWith ("&"))
                           )
                        {

                            b.append ("&");

                        }

                        b.append (k);
                        b.append ("=");
                        b.append (URLEncoder.encode (co.toString (),
                                                     "utf-8"));

                    }

                } else {

                    b.append (k.toString ());
                    b.append ("=");
                    b.append (URLEncoder.encode (v.toString (),
                                                 "utf-8"));

                }

            }

            return b.toString ();

        }

        return "";

    }

    public void call ()
    {

        if (this.service == null)
        {

            throw new IllegalArgumentException ("No service specified.");

        }

        if (this.onComplete == null)
        {

            throw new IllegalArgumentException ("onComplete must be specified.");

        }

        if (this.onError == null)
        {

            throw new IllegalArgumentException ("onError must be specified.");

        }

        if (this.method == null)
        {

            this.method = "GET";

        }

        final EditorsWebServiceCall _this = this;

        new Thread (new Runnable ()
        {

            public void run ()
            {

                String url = _this.service.getURL ();

                if (id != null)
                {

                    url += "/" + id;

                }

                try
                {

                    String enc = JSONEncoder.encode (_this.data);

                    boolean isGet = _this.method.equalsIgnoreCase ("get");

                    boolean supportsOutput = (!_this.method.equalsIgnoreCase ("get") && !_this.method.equalsIgnoreCase ("delete"));

                    if ((!supportsOutput)
                        &&
                        (data != null)
                       )
                    {

                        // Need to encode as url parameters instead of posting data.
                        url += "?" + _this.encodeParameters (_this.data);

                    }

                    URL u = new URL (url);

                    Environment.logDebugMessage ("Calling web service with url: " + _this.method + ":" + url);

                    HttpURLConnection conn = (HttpURLConnection) u.openConnection ();

                    if (_this.sessionId != null)
                    {

                        Environment.logDebugMessage ("Setting web service authorization header to: " + _this.sessionId);

                        conn.setRequestProperty ("Authorization",
                                                 _this.sessionId);

                    }

                    if (_this.headers != null)
                    {

                        Iterator<String> iter = _this.headers.keySet ().iterator ();

                        while (iter.hasNext ())
                        {

                            String key = iter.next ();

                            conn.setRequestProperty (key,
                                                     _this.headers.get (key));

                        }

                    }

                    conn.setRequestMethod (_this.method);

                    if (!isGet)
                    {

                        conn.setRequestProperty ("content-length",
                                                 "" + enc.length ());

                    }

                    conn.setDoInput (true);
                    conn.setDoOutput (true);
                    conn.connect ();

                    if (supportsOutput)
                    {

                        OutputStream out = conn.getOutputStream ();
                        out.write (enc.getBytes ());

                        out.flush ();
                        out.close ();

                    }

                    // Try and get input stream, not all responses allow it.
                    InputStream in = null;

                    Environment.logDebugMessage ("Got web service response code: " + conn.getResponseCode ());

                    final int resCode = conn.getResponseCode ();

                    if (resCode != HttpURLConnection.HTTP_OK)
                    {

                        in = conn.getErrorStream ();

                    } else {

                        in = conn.getInputStream ();

                    }

                    String r = null;

                    if (in != null)
                    {

                        StringBuilder b = new StringBuilder ();

                        BufferedReader bin = new BufferedReader (new InputStreamReader (in));

                        // Read everything.
                        char chars[] = new char[8192];

                        int count = 0;

                        while ((count = bin.read (chars,
                                                  0,
                                                  8192)) != -1)
                        {

                            b.append (chars,
                                      0,
                                      count);

                        }

                        String s = b.toString ();

                        if (s.startsWith (Constants.JSON_RETURN_PREFIX))
                        {

                            s = s.substring (Constants.JSON_RETURN_PREFIX.length ());

                        }

                        r = s;

                    }

                    final String ret = r;

                    conn.disconnect ();

                    final EditorsWebServiceResult res = new EditorsWebServiceResult (_this,
                                                                                     resCode,
                                                                                     ret);

                    if (res.code != HttpURLConnection.HTTP_OK)
                    {

                        _this.onError.processResult (res);

                        return;

                        /*
                        // Has the current session expired?
                        if (res.code == HttpURLConnection.HTTP_UNAUTHORIZED)
                        {

                            _this.handleUnauthorizedResponse (res,
                                                              new ActionListener ()
                                                              {

                                                                  public void actionPerformed (ActionEvent ev)
                                                                  {

                                                                      _this.callService (service,
                                                                                         id,
                                                                                         data,
                                                                                         method,
                                                                                         headers,
                                                                                         onComplete,
                                                                                         onError);

                                                                  }

                                                              },
                                                              null);

                            return;

                        }

                        if (resCode == HttpURLConnection.HTTP_FORBIDDEN)
                        {

                            _this.handleForbiddenResponse (viewer);

                            return;

                        }

                        // Get the response code.
                        if (resCode != HttpURLConnection.HTTP_OK)
                        {

                            // Deal with the error.
                            Environment.logError ("Unable to process " + conn.getRequestMethod () + " request to: " +
                                                  url +
                                                  ", got response: " + resCode + "/" +
                                                  conn.getResponseMessage () + ", error: " + ret);

                        }

                        if (onError != null)
                        {

                            UIUtils.doLater (new ActionListener ()
                            {

                                public void actionPerformed (ActionEvent ev)
                                {

                                    try
                                    {

                                        onError.processResult (res);

                                    } catch (Exception e) {

                                        Environment.logError ("Unable to process onError",
                                                              e);

                                    }

                                }

                            });

                            return;

                        } else {

                            xxx

                            UIUtils.showErrorMessage (viewer,
                                                      "Sorry, an unexpected error has occurred with the Editors service.  The service responded with: " + res.getErrorMessage ());

                        }

                        return;
                        */
                    }

                    _this.onComplete.processResult (res);

                } catch (Exception e) {

                    Environment.logError ("Unable to call server with url: " + url,
                                          e);

                    try
                    {

                        onError.processResult (new EditorsWebServiceResult (_this,
                                                                            -1,
                                                                            "\"Unable to make call to server.\""));

                    } catch (Exception ee) {

                        Environment.logError ("Unable to process onError",
                                              ee);

                    }

                }

            }

        }).start ();

    }

}
