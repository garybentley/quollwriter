package com.quollwriter;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import javax.activation.*;

import org.jdom.*;

import com.gentlyweb.xml.*;
import com.gentlyweb.utils.*;

import com.quollwriter.ui.*;
import com.quollwriter.data.editors.*;

public class EditorsWebServiceHandler
{
    
    public static final int MAX_IMAGE_SIZE = 300 * 1024;
    
    private String sessionId = null;

    private EditorAccount acc = null;
    private boolean operationInProgress = false;
    private boolean inited = false;
    
    public class FieldNames
    {
        
        public static final String genres = "genres";
        public static final String email = "email";
        public static final String id = "id";
        public static final String password = "password";
        public static final String lastModified = "lastModified";
        public static final String avatarImage = "avatarImage";
        public static final String avatarImageFileType = "avatarImageFileType";
        public static final String about = "about";
        public static final String wordCountLengths = "wordCountLengths";
        public static final String wordCountLength = "wordCountLength";
        public static final String name = "name";
        public static final String description = "description";
        public static final String expectations = "expectations";
        
    }
    
    public enum Service
    {
        
        accounts ("accounts", Environment.getProperty (Constants.ACCOUNTS_WEBSERVICE_URL_PROPERTY_NAME)),
        authors ("authors", Environment.getProperty (Constants.AUTHORS_WEBSERVICE_URL_PROPERTY_NAME)),
        projects ("projects", Environment.getProperty (Constants.PROJECTS_WEBSERVICE_URL_PROPERTY_NAME)),
        login ("login", Environment.getProperty (Constants.LOGIN_WEBSERVICE_URL_PROPERTY_NAME)),
        editors ("editors", Environment.getProperty (Constants.EDITORS_WEBSERVICE_URL_PROPERTY_NAME));
        
        private final String type;
        private final String url;
        
        Service (String type,
                 String url)
        {
            
            this.type = type;
            this.url = url;
            
        }
        
        public String getURL ()
        {
            
            return this.url;
            
        }
        
    }
    
    public EditorsWebServiceHandler ()
    {
                        
    }
    
    public void init ()
    {
        
        if (this.inited)
        {
            
            return;
            
        }
        
        if (this.operationInProgress)
        {
            
            return;
            
        }
        
        final EditorsWebServiceHandler _this = this;
        
        new Thread (new Runnable ()
        {
            
            public void run ()
            {
                
                _this.operationInProgress = true;
                
                String em = Environment.getUserProperties ().getProperty (Constants.QW_EDITORS_EMAIL_PROPERTY_NAME);
                
                if (em != null)
                {
                
                    _this.acc = new EditorAccount ();
                    
                    // Encrypt somehow?
                    _this.acc.setEmail (em);
                    _this.acc.setPassword (Environment.getUserProperties ().getProperty (Constants.QW_EDITORS_PASSWORD_PROPERTY_NAME));
        
                    // Login and get the author information.
                    try
                    {
                    
                        _this.loadAuthor ();
                                                
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to load author information",
                                              e);
                        
                    }
                    
                    try
                    {
                                            
                        _this.loadEditor ();
                                                
                    } catch (Exception e) {
                        
                        Environment.logError ("Unable to load editor information",
                                              e);
                        
                    }

                    _this.inited = true;
                    
                    _this.operationInProgress = false;
                    
                }        
                
                
            }
            
        }).start ();
        
    }
    
    private Object doPost (Service service,
                           String  id,
                           Object  data)
                           throws  Exception
    {
    
        return this.doPostOrPut (service,
                                 id,
                                 data,
                                 true);
        
    }
        
    private Object doPut (Service service,
                          String  id,
                          Object  data)
                          throws  Exception
    {
    
        return this.doPostOrPut (service,
                                 id,
                                 data,
                                 false);
        
    }

    private Object doPostOrPut (Service service,
                                String  id,
                                Object  data,
                                boolean isPost)
                           throws  Exception
    {
    
        if ((this.acc != null)
            &&
            (this.sessionId == null)
            &&
            (service != Service.login)
           )
        {
            
            Environment.logDebugMessage ("Logging in");
            
            // Do the login.
            this.login ();
            
            if (this.sessionId == null)
            {
                
                throw new GeneralException ("Unable to login");
                
            }
            
        }
    
        try
        {

            String enc = JSONEncoder.encode (data);
        
            String url = service.getURL ();
            
            if (id != null)
            {
                
                url += "/" + id;
                
            }
            
            URL u = new URL (url);
            
            Environment.logDebugMessage ("Calling url: " + url + ", method: " + (isPost ? "POST" : "PUT"));
                
            HttpURLConnection conn = (HttpURLConnection) u.openConnection ();
            
            if (this.sessionId != null)
            {
                
                Environment.logDebugMessage ("Setting session id: " + this.sessionId);
                
                conn.setRequestProperty ("Cookie",
                                         "JSESSIONID=" + this.sessionId);
                
            }
            
            conn.setRequestMethod ((isPost ? "POST" : "PUT"));
            conn.setRequestProperty ("content-length",
                                     "" + enc.length ());
            conn.setDoInput (true);
            conn.setDoOutput (true);
            conn.connect ();
                                    
            OutputStream out = conn.getOutputStream ();
            out.write (enc.getBytes ());
            
            out.flush ();
            out.close ();
                        
            // Try and get input stream, not all responses allow it.
            InputStream in = null;

            Environment.logDebugMessage ("Got response code: " + conn.getResponseCode ());
            
            if (conn.getResponseCode () != HttpURLConnection.HTTP_OK)
            {

                in = conn.getErrorStream ();
                        
            } else {
                
                in = conn.getInputStream ();
                
            }
                        
            Object ret = null;
            
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
                
                String pref = "for(;;);";
                
                if (s.startsWith (pref))
                {
                    
                    s = s.substring (pref.length ());
                    
                }
    
                // JSON decode.
                ret = JSONDecoder.decode (s);

            }
                
            // Get the response code.
            if (conn.getResponseCode () != HttpURLConnection.HTTP_OK)
            {
                
                if (conn.getResponseCode () == HttpURLConnection.HTTP_UNAUTHORIZED)
                {
                    
                    if (service == Service.login)
                    {
                        
                        Environment.logDebugMessage ("Unable to login, reason: " +
                                                     ret);
                        
                        // This means we can't login.
                        throw new GeneralException ("Unable to login, reason: " +
                                                    ret);
                        
                    }

                    if (this.sessionId != null)
                    {
                        
                        Environment.logDebugMessage ("Got: " + conn.getResponseCode () + ", trying to login, message was: " + ret);
                        
                        // Try again but login, our session probably timed out.
                        this.sessionId = null;
                        
                        return this.doPostOrPut (service,
                                                 id,
                                                 data,
                                                 isPost);
                        
                    }
                    
                }
                
                // Deal with the error.
                throw new GeneralException ("Unable to process " + conn.getRequestMethod () + " request to: " +
                                            url +
                                            ", got response: " + conn.getResponseCode () + "/" +
                                            conn.getResponseMessage () + ", error: " + ret);
                
            }

            conn.disconnect ();
            
            return ret;
            
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to make call",
                                        e);
            
        }        
        
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
    
    private Object doGet (Service service,
                          String  id,
                          Object  data)
                          throws  Exception
    {
    
        if ((this.acc != null)
            &&
            (this.sessionId == null)
            &&
            (service != Service.login)
           )
        {
            
            Environment.logDebugMessage ("Logging in");
            
            // Do the login.
            this.login ();

            if (this.sessionId == null)
            {
                
                throw new GeneralException ("Unable to login");
                
            }
            
        }
    
        try
        {

            String params = this.encodeParameters (data);
        
            String url = service.getURL ();
            
            if (id != null)
            {
                
                url += "/" + id;
                
            }
            
            if (params.length () > 0)
            {
                
                url += "?" + params;
                
            }
            
            URL u = new URL (url);
            
            Environment.logDebugMessage ("Calling url: " + url);
                
            HttpURLConnection conn = (HttpURLConnection) u.openConnection ();
            
            if (this.sessionId != null)
            {
                
                Environment.logDebugMessage ("Setting session id: " + this.sessionId);
                
                conn.setRequestProperty ("Cookie",
                                         "JSESSIONID=" + this.sessionId);
                
            }
            
            conn.setRequestMethod ("GET");
            conn.setDoInput (true);
            conn.connect ();
                                                            
            // Try and get input stream, not all responses allow it.
            InputStream in = null;

            Environment.logDebugMessage ("Got response code: " + conn.getResponseCode ());
            
            if (conn.getResponseCode () != HttpURLConnection.HTTP_OK)
            {

                in = conn.getErrorStream ();
                        
            } else {
                
                in = conn.getInputStream ();
                
            }
                        
            Object ret = null;
            
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
                
                String pref = "for(;;);";
                
                if (s.startsWith (pref))
                {
                    
                    s = s.substring (pref.length ());
                    
                }
    
                // JSON decode.
                ret = JSONDecoder.decode (s);

            }
                
            // Get the response code.
            if (conn.getResponseCode () != HttpURLConnection.HTTP_OK)
            {
                
                if (conn.getResponseCode () == HttpURLConnection.HTTP_UNAUTHORIZED)
                {
                    
                    if (service == Service.login)
                    {
                        
                        Environment.logDebugMessage ("Unable to login, reason: " +
                                                     ret);
                        
                        // This means we can't login.
                        throw new GeneralException ("Unable to login, reason: " +
                                                    ret);
                        
                    }
                    
                    if (this.sessionId != null)
                    {
                        
                        Environment.logDebugMessage ("Got 401, trying to login, message was: " + ret);
                        
                        // Try again but login, our session probably timed out.
                        this.sessionId = null;
                        
                        return this.doGet (service,
                                           id,
                                           data);
                        
                    }
                
                }
                
                // Deal with the error.
                throw new GeneralException ("Unable to process get request to: " +
                                            url +
                                            ", got response: " + conn.getResponseCode () + "/" +
                                            conn.getResponseMessage () + ", error: " + ret);
                
            }

            conn.disconnect ();
            
            return ret;
            
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to make call",
                                        e);
            
        }        
        
    }

    private boolean isModified (Service service,
                                String  id,
                                long    lastModified)
                                throws  Exception
    {
    
        if ((this.acc != null)
            &&
            (this.sessionId == null)
            &&
            (service != Service.login)
           )
        {
            
            // Do the login.
            this.login ();
            
        }
    
        try
        {

            String url = service.getURL ();
            
            if (id != null)
            {
                
                url += "/" + id;
                
            }
            
            URL u = new URL (url);
            
            Environment.logDebugMessage ("Performing is modified check with: " + url);
            
            HttpURLConnection conn = (HttpURLConnection) u.openConnection ();
            
            if (this.sessionId != null)
            {
                
                conn.setRequestProperty ("Cookie",
                                         "JSESSIONID=" + this.sessionId);
                
            }
            
            if (lastModified > -1)
            {
                
                SimpleDateFormat format = new SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss zzz");

                String d = format.format (lastModified);
                
                Environment.logDebugMessage ("Performing is modified check with date: " + lastModified);
                
                conn.setRequestProperty ("If-modified-since",
                                         d);
                
            }
            
            conn.setRequestMethod ("HEAD");
            conn.connect ();
                                    
            if (conn.getResponseCode () == HttpURLConnection.HTTP_OK)
            {

                return true;
            
            }
            
            if (conn.getResponseCode () == HttpURLConnection.HTTP_NOT_MODIFIED)
            {
                
                return false;
                
            }
            
            throw new IllegalStateException ("Unexpected status: " + conn.getResponseCode () + ", " + conn.getResponseMessage ());
            
        } catch (Exception e) {
            
            throw new GeneralException ("Unable to make call",
                                        e);
            
        }        
        
    }

    public void saveProject (String                        name,
                             String                        desc,
                             Set<String>                   genres,
                             String                        expectations,
                             EditorProject.WordCountLength wordCountLength,
                             AbstractProjectViewer         pv)
                             throws                        Exception
    {

        EditorProject ep = pv.getProject ().getEditorProject ();
        
        if (ep == null)
        {
            
            this.createProject (name,
                                desc,
                                genres,
                                expectations,
                                wordCountLength,
                                pv);
            
        } else {
                        
            this.updateProject (name,
                                desc,
                                genres,
                                expectations,
                                wordCountLength,
                                pv);
            
        }
                
    }

    private Map getProjectData (String                        name,
                                String                        desc,
                                Set<String>                   genres,
                                String                        expectations,
                                EditorProject.WordCountLength wordCountLength)
    {

        Map data = new HashMap ();
        data.put (FieldNames.name,
                  name);
        data.put (FieldNames.description,
                  desc);
        data.put (FieldNames.genres,
                  genres);
        data.put (FieldNames.expectations,
                  expectations);
        data.put (FieldNames.wordCountLength,
                  wordCountLength.getType ());

        return data;
        
    }
    
    public void updateProject (String                        name,
                               String                        desc,
                               Set<String>                   genres,
                               String                        expectations,
                               EditorProject.WordCountLength wordCountLength,
                               AbstractProjectViewer         pv)
                               throws                        Exception
    {

        EditorProject ep = pv.getProject ().getEditorProject ();

        if (ep == null)
        {
            
            throw new IllegalStateException ("Unable to update project, no project available.");
            
        }

        if (ep.getId () == null)
        {
            
            throw new IllegalStateException ("Unable to update project, project has no id.");
            
        }

        if (this.acc == null)
        {
            
            throw new IllegalStateException ("Unable to create project, no account available.");
            
        }

        if (this.acc.getAuthor () == null)
        {
            
            throw new IllegalStateException ("Unable to create project, no author available.");
            
        }

        Map data = this.getProjectData (name,
                                        desc,
                                        genres,
                                        expectations,
                                        wordCountLength);

        Object d = this.doPut (Service.projects,
                               ep.getId (),
                               data);        

        this.fillProject (ep,
                          name,
                          desc,
                          genres,
                          expectations,
                          wordCountLength);
        
        this.saveProjectToLocal (ep,
                                 pv);
                
    }
    
    private void fillProject (EditorProject                 ep,
                              String                        name,
                              String                        desc,
                              Set<String>                   genres,
                              String                        expectations,
                              EditorProject.WordCountLength wordCountLength)
    {

        ep.setName (name);
        ep.setDescription (desc);
        ep.setExpectations (expectations);
        ep.setGenres (genres);
        ep.setWordCountLength (wordCountLength);
        
    }
    
    private void saveProjectToLocal (EditorProject         ep,
                                     AbstractProjectViewer pv)
                                     throws                Exception
    {
        
        pv.saveObject (ep,
                       true);

        pv.getProject ().setEditorProject (ep);
        
    }
    
    public void deleteProject (EditorProject proj)
    {
        
    }
    
    private void createProject (String                        name,
                                String                        desc,
                                Set<String>                   genres,
                                String                        expectations,
                                EditorProject.WordCountLength wordCountLength,
                                AbstractProjectViewer         pv)
                                throws                        Exception
    {

        if (this.acc == null)
        {
            
            throw new IllegalStateException ("Unable to create project, no account available.");
            
        }

        if (this.acc.getAuthor () == null)
        {
            
            throw new IllegalStateException ("Unable to create project, no author available.");
            
        }
        
        EditorProject ep = pv.getProject ().getEditorProject ();

        if (ep != null)
        {
            
            throw new IllegalStateException ("Already have a project.");
            
        }

        Map data = this.getProjectData (name,
                                        desc,
                                        genres,
                                        expectations,
                                        wordCountLength);

        Object d = this.doPost (Service.projects,
                                null,
                                data);        

        ep = new EditorProject ();                                
        this.fillProject (ep,
                          name,
                          desc,
                          genres,
                          expectations,
                          wordCountLength);

        ep.setId (d.toString ());
        
        this.saveProjectToLocal (ep,
                                 pv);
        
        pv.getProject ().setEditorProject (ep);
        
    }
    
    public List<EditorEditor> findEditors ()
                                           throws Exception
    {
        
        Map parms = new HashMap ();
        
        Object d = this.doGet (Service.editors,
                               null,
                               parms);        
        
        List<EditorEditor> ret = new ArrayList ();
        
        if (d instanceof List)
        {
            
            List l = (List) d;
            
            for (int i = 0; i < l.size (); i++)
            {
                
                Map m = (Map) l.get (i);

                ret.add (this.createEditorFromData (m));
                
            }
            
        }

        return ret;

    }
    
    public List<EditorProject> findProjects (String title)
                                             throws Exception
    {
        
        Map parms = new HashMap ();
        parms.put ("title",
                   title);
        
        Object d = this.doGet (Service.projects,
                               null,
                               parms);        
        
        List<EditorProject> ret = new ArrayList ();
        
        if (d instanceof List)
        {
            
            List l = (List) d;
            
            for (int i = 0; i < l.size (); i++)
            {
                
                Map m = (Map) l.get (i);
                
                ret.add (this.createProjectFromData (m));
                
            }
            
        }

        return ret;
        
    }

    private EditorProject createProjectFromData (Map data)
    {
        
        EditorProject ep = new EditorProject ();
        
        ep.setId ((String) data.get (FieldNames.id));
        ep.setName ((String) data.get (FieldNames.name));
        ep.setDescription ((String) data.get (FieldNames.description));
        ep.setExpectations ((String) data.get (FieldNames.expectations));
        ep.setLastModified (new Date ((Long) data.get (FieldNames.lastModified)));

        return ep;        
        
    }
    
    private EditorEditor createEditorFromData (Map data)
    {
        
        EditorEditor e = new EditorEditor ();
        
        e.setId ((String) data.get (FieldNames.id));
        e.setName ((String) data.get (FieldNames.name));
        e.setAbout ((String) data.get (FieldNames.about));
        e.setLastModified (new Date ((Long) data.get (FieldNames.lastModified)));

        String avIm = (String) data.get (FieldNames.avatarImage);
        
        if (avIm != null)
        {
            
            e.setAvatar (new ByteArrayDataSource (Base64.decode (avIm)));
            e.setAvatarImageFileType ((String) data.get (FieldNames.avatarImageFileType));
            
        } else {
            
            e.setAvatar (new javax.activation.URLDataSource (Environment.getIconURL (Constants.NO_AVATAR_IMAGE_FILE_NAME,
                                                                                     -1)));
            
        }
        
        return e;        
        
    }

    public void deleteEditor ()
    {}
    
    public void updateEditor (String                             name,
                              String                             about,
                              File                               avatarImage,
                              Set<String>                        genres,
                              Set<EditorProject.WordCountLength> wordCountLengths)
                              throws                             Exception
    {
        
        if (this.acc == null)
        {
            
            throw new IllegalStateException ("Unable to update editor, no account available.");
            
        }
        
        EditorEditor ed = this.acc.getEditor ();
        
        if (ed == null)
        {
            
            throw new IllegalStateException ("Unable to update editor, no editor found.");
            
        }

        if (ed.getId () == null)
        {
            
            throw new IllegalStateException ("Have an editor but it doesn't have an id.");
            
        }
        
        Map data = this.getEditorData (name,
                                       about,
                                       avatarImage,
                                       genres,
                                       wordCountLengths);
                
        Object d = this.doPut (Service.editors,
                               ed.getId (),
                               data);        
        
        ed.setName (name);
        ed.setAbout (about);
        ed.setGenres (genres);
        ed.setWordCountLengths (wordCountLengths);

        // Save the editor away.
        this.saveEditorToLocal (avatarImage);                
        
    }
    
    public void createEditor (String                             name,
                              String                             about,
                              File                               avatarImage,
                              Set<String>                        genres,
                              Set<EditorProject.WordCountLength> wordCountLengths)
                              throws                             Exception
    {

        if (this.acc == null)
        {
            
            throw new IllegalStateException ("Unable to create editor, no account available.");
            
        }
        
        if (this.acc.getEditor () != null)
        {
            
            throw new IllegalStateException ("Unable to create editor, already have an editor.");
            
        }
        
        Map data = this.getEditorData (name,
                                       about,
                                       avatarImage,
                                       genres,
                                       wordCountLengths);
        
        Object d = this.doPost (Service.editors,
                                null,
                                data);        

        EditorEditor ed = new EditorEditor ();
        ed.setName (name);
        ed.setAbout (about);                                
        ed.setId (d.toString ());
        ed.setGenres (genres);
        ed.setWordCountLengths (wordCountLengths);
        
        this.acc.setEditor (ed);
        
        // Save the author away.
        this.saveEditorToLocal (avatarImage);
        
    }

    private void saveEditorToLocal (File   avatarImage)
                                    throws Exception
    {
                
        if (this.acc == null)
        {

            return;
            
        }

        EditorEditor ed = this.acc.getEditor ();
        
        if (ed == null)
        {
            
            return;
            
        }
        
        // Save the author away.
        File f = Environment.getEditorsEditorFile ();

        Element root = ed.getAsJDOMElement ();

        JDOMUtils.writeElementToFile (root,
                                      f,
                                      true);
        
        Environment.setUserProperty (Constants.QW_EDITORS_EDITOR_LAST_MODIFIED_PROPERTY_NAME,
                                     "" + ed.getLastModified ().getTime ());        
                                     
        // Save the avatar image file.
        if ((avatarImage != null)
            &&
            ((Environment.getEditorsEditorAvatarImageFile () == null)
             ||
             (avatarImage.getPath () != Environment.getEditorsEditorAvatarImageFile ().getPath ())
            )
           )
        {
            
            String ft = Utils.getFileType (avatarImage);
            
            if (ft != null)
            {
                
                // Save the file to the right location.
                IOUtils.copyFile (avatarImage,
                                  Environment.getEditorsEditorAvatarImageFile (ft),
                                  8192);            
    
                Environment.setUserProperty (Constants.EDITORS_EDITOR_AVATAR_IMAGE_FILE_TYPE_PROPERTY_NAME,
                                             ft);
                                             
            }
            
        }

        ed.setAvatar (new FileDataSource (Environment.getEditorsEditorAvatarImageFile ()));                                     
        
    }

    private Map getEditorData (String                             name,
                               String                             about,
                               File                               avatarImage,
                               Set<String>                        genres,
                               Set<EditorProject.WordCountLength> wordCountLengths)
                               throws                             Exception
    {
        
        Map data = new HashMap ();
        data.put (FieldNames.name,
                  name);
        data.put (FieldNames.about,
                  about);
        data.put (FieldNames.genres,
                  genres);
        data.put (FieldNames.wordCountLengths,
                  EditorProject.WordCountLength.getTypes (wordCountLengths));
        
        if ((avatarImage != null)
            &&
            (avatarImage.exists ())
           )
        {

            BufferedImage im = UIUtils.getScaledImage (avatarImage,
                                                       300);

            ByteArrayOutputStream bout = new ByteArrayOutputStream ();
            
            ImageIO.write (im,
                           Utils.getFileType (avatarImage),
                           bout);
            
            String s = Base64.encodeBytes (bout.toByteArray ());
            
            if (s.length () > MAX_IMAGE_SIZE)
            {
                
                throw new GeneralException ("Unable to convert image: " + avatarImage.getPath () + " to a 300x300 image that is " + MAX_IMAGE_SIZE + " or lower in length.");
                
            }
            
            data.put (FieldNames.avatarImage,
                      s);
            
            data.put (FieldNames.avatarImageFileType,
                      Utils.getFileType (avatarImage));
        }

        return data;
        
    }
    
    public void saveEditor (String                             name,
                            String                             about,
                            File                               avatarImage,
                            Set<String>                        genres,
                            Set<EditorProject.WordCountLength> wordCountLengths)
                            throws                             Exception
    {

        if (this.acc == null)
        {
            
            throw new IllegalStateException ("Unable to create author, no account available.");
            
        }

        EditorEditor ed = this.acc.getEditor ();
        
        if (ed != null)
        {
            
            // Update.
            this.updateEditor (name,
                               about,
                               avatarImage,
                               genres,
                               wordCountLengths);
            
        } else {
            
            this.createEditor (name,
                               about,
                               avatarImage,
                               genres,
                               wordCountLengths);
            
        }
        
    }
    
    public void saveAuthor (String name,
                            String about,
                            File   avatarImage)
                            throws Exception
    {
        
        if (this.acc == null)
        {
            
            throw new IllegalStateException ("Unable to create author, no account available.");
            
        }

        EditorAuthor a = this.acc.getAuthor ();
        
        if (a != null)
        {
            
            // Update.
            this.updateAuthor (name,
                               about,
                               avatarImage);
            
        } else {
            
            this.createAuthor (name,
                               about,
                               avatarImage);
            
        }
        
    }
    
    private Map getAuthorData (String name,
                               String about,
                               File   avatarImage)
                               throws Exception
    {
        
        Map data = new HashMap ();
        data.put (FieldNames.name,
                  name);
        data.put (FieldNames.about,
                  about);
        
        if (avatarImage != null)
        {

            BufferedImage im = UIUtils.getScaledImage (avatarImage,
                                                       300);

            ByteArrayOutputStream bout = new ByteArrayOutputStream ();
            
            ImageIO.write (im,
                           Utils.getFileType (avatarImage),
                           bout);
            
            String s = Base64.encodeBytes (bout.toByteArray ());
            
            if (s.length () > MAX_IMAGE_SIZE)
            {
                
                throw new GeneralException ("Unable to convert image: " + avatarImage.getPath () + " to a 300x300 image that is " + MAX_IMAGE_SIZE + " or lower in length.");
                
            }
            
            data.put (FieldNames.avatarImage,
                      s);
            
            data.put (FieldNames.avatarImageFileType,
                      Utils.getFileType (avatarImage));
        }

        return data;
        
    }
    
    private void createAuthor (String name,
                               String about,
                               File   avatarImage)
                               throws Exception
    {
        
        if (this.acc == null)
        {
            
            throw new IllegalStateException ("Unable to create author, no account available.");
            
        }
        
        if (this.acc.getAuthor () != null)
        {
            
            throw new IllegalStateException ("Unable to create author, already have an author.");
            
        }
        
        Map data = this.getAuthorData (name,
                                       about,
                                       avatarImage);
        
        Object d = this.doPost (Service.authors,
                                null,
                                data);        

        EditorAuthor au = new EditorAuthor ();
        au.setName (name);
        au.setAbout (about);                                
        au.setId (d.toString ());

        this.acc.setAuthor (au);
        
        // Save the author away.
        this.saveAuthorToLocal (avatarImage);
                
    }

    private void updateAuthor (String name,
                               String about,
                               File   avatarImage)
                               throws Exception
    {
        
        if (this.acc == null)
        {
            
            throw new IllegalStateException ("Unable to create author, no account available.");
            
        }
        
        EditorAuthor au = this.acc.getAuthor ();
        
        if (au == null)
        {
            
            throw new IllegalStateException ("Unable to update author, no author found.");
            
        }

        if (au.getId () == null)
        {
            
            throw new IllegalStateException ("Have an author but it doesn't have an id.");
            
        }
        
        Map data = this.getAuthorData (name,
                                       about,
                                       avatarImage);
                
        Object d = this.doPut (Service.authors,
                               au.getId (),
                               data);        
        
        au.setName (name);
        au.setAbout (about);
        
        // Save the author away.
        this.saveAuthorToLocal (avatarImage);        
        
    }
    
    private void saveAuthorToLocal (File  avatarImage)
                             throws       Exception
    {
                
        if (this.acc == null)
        {

            return;
            
        }

        EditorAuthor au = this.acc.getAuthor ();
        
        if (au == null)
        {
            
            return;
            
        }
        
        // Save the author away.
        File f = Environment.getEditorsAuthorFile ();

        Element root = au.getAsJDOMElement ();

        JDOMUtils.writeElementToFile (root,
                                      f,
                                      true);
        
        Environment.setUserProperty (Constants.QW_EDITORS_AUTHOR_LAST_MODIFIED_PROPERTY_NAME,
                                     "" + au.getLastModified ().getTime ());
        
        // Save the avatar image file.
        if ((avatarImage != null)
            &&
            ((Environment.getEditorsAuthorAvatarImageFile () == null)
             ||
             (avatarImage.getPath () != Environment.getEditorsAuthorAvatarImageFile ().getPath ())
            )
           )
        {
            
            String ft = Utils.getFileType (avatarImage);
            
            if (ft != null)
            {
                
                // Save the file to the right location.
                IOUtils.copyFile (avatarImage,
                                  Environment.getEditorsAuthorAvatarImageFile (ft),
                                  8192);            
    
                Environment.setUserProperty (Constants.EDITORS_AUTHOR_AVATAR_IMAGE_FILE_TYPE_PROPERTY_NAME,
                                             ft);
                                             
            }
            
        }
        
        au.setAvatar (new FileDataSource (Environment.getEditorsAuthorAvatarImageFile ())); 

    }
    
    private void loadAuthor ()
                             throws Exception
    {

        if (this.acc == null)
        {
            
            throw new IllegalStateException ("Unable to load author, no account available.");
            
        }
        
        if (this.acc.getAuthor () != null)
        {
            
            throw new IllegalStateException ("Unable to load author, already have an author.");
            
        }

        // Load the saved author info, if available.
        File f = Environment.getEditorsAuthorFile ();
        
        if (f.exists ())
        {
            
            try
            {
            
                Element root = JDOMUtils.getFileAsElement (f,
                                                           "gz");
                
                EditorAuthor au = new EditorAuthor (root);
                au.setAvatar (new FileDataSource (Environment.getEditorsAuthorAvatarImageFile ()));
                this.acc.setAuthor (au);

            } catch (Exception e) {
                
                Environment.logError ("Unable to load author file from: " +
                                      f,
                                      e);
                
            }
            
        }        
        
        Map data = null;
        
        String v = Environment.getProperty (Constants.QW_EDITORS_AUTHOR_LAST_MODIFIED_PROPERTY_NAME);
        
        if (v != null)
        {
                        
            if (this.acc.getAuthor () == null)
            {
                
                // Possibly the author file has been removed.
                // Get the author information again.
                this.loadAuthorFromServer ();
                                        
            } else {
            
                if (this.isModified (Service.authors,
                                     this.acc.getAuthor ().getId (),
                                     Long.parseLong (v)))
                {
                    
                    this.loadAuthorFromServer ();
                                    
                }

            }
            
        }
                                
        this.login ();
        
    }
    
    private void loadAuthorFromServer ()
                                       throws Exception
    {

        String authorId = null;
    
        if ((this.acc != null)
            &&
            (this.acc.getAuthor () != null)
           )
        {
            
            authorId = this.acc.getAuthor ().getId ();
            
        }

        authorId = null;
        Object d = this.doGet (Service.authors,
                               authorId,
                               null);        

        Map objD = null;
                               
        if (authorId == null)
        {
            
            // Might have got back our author in a list.
            if (d instanceof List)
            {
                
                List l = (List) d;
                
                if (l.size () > 0)
                {
                    
                    objD = (Map) l.get (0);
                    
                }
                
            }
            
        } else {
            
            objD = (Map) d;
            
        }
        
        if (objD != null)
        {                                  
        
            // We have the author information.
            EditorAuthor au = new EditorAuthor ();        
                                    
            Number lastModN = (Number) objD.get (FieldNames.lastModified);
            
            au.setId ((String) objD.get (FieldNames.id));
            au.setName ((String) objD.get (FieldNames.name));
            au.setAbout ((String) objD.get (FieldNames.about));
            au.setLastModified (new Date (lastModN.longValue ()));
            
            this.acc.setAuthor (au);
            
            // Save the file away.
            String av = (String) objD.get (FieldNames.avatarImage);
            
            if (av != null)
            {
                
                File f = Environment.getEditorsAuthorAvatarImageFile ((String) objD.get (FieldNames.avatarImageFileType));
                
                // Base64 decode, then write bytes to file.
                byte[] fbytes = Base64.decode (av);
                
                f = new File (f.getPath ());
                
                Utils.writeBytesToFile (f,
                                        fbytes);
                
            }
                                        
            this.saveAuthorToLocal (null);
        
        } else {
            
            // Just assume user has cleaned up or something has gone setting the property.
            Environment.setUserProperty (Constants.QW_EDITORS_AUTHOR_LAST_MODIFIED_PROPERTY_NAME,
                                         Long.MAX_VALUE + "");
            
        }
        
    }
        
    private void loadEditor ()
                             throws Exception
    {

        if (this.acc == null)
        {
            
            throw new IllegalStateException ("Unable to load editor, no account available.");
            
        }
        
        if (this.acc.getEditor () != null)
        {
            
            throw new IllegalStateException ("Unable to load editor, already have an editor.");
            
        }

        // Load the saved editor info, if available.
        File f = Environment.getEditorsEditorFile ();
        
        if (f.exists ())
        {
            
            Element root = JDOMUtils.getFileAsElement (f,
                                                       "gz");
            
            EditorEditor ed = new EditorEditor (root);
            
            ed.setAvatar (new FileDataSource (Environment.getEditorsEditorAvatarImageFile ()));
            
            this.acc.setEditor (ed);
            
        }        
        
        Map data = null;
        
        String v = Environment.getProperty (Constants.QW_EDITORS_EDITOR_LAST_MODIFIED_PROPERTY_NAME);
        
        if (v != null)
        {
                        
            if (this.acc.getEditor () == null)
            {
                
                // Possibly the author file has been removed.
                // Get the author information again.
                this.loadEditorFromServer ();
                                        
            } else {
            
                if (this.isModified (Service.editors,
                                     this.acc.getEditor ().getId (),
                                     Long.parseLong (v)))
                {
                    
                    this.loadEditorFromServer ();
                                    
                }

            }
            
        }
                                        
    }
    
    private void loadEditorFromServer ()
                                       throws Exception
    {

        String editorId = null;
    
        if ((this.acc != null)
            &&
            (this.acc.getEditor () != null)
           )
        {
            
            editorId = this.acc.getEditor ().getId ();
            
        }
        
        editorId = null;
        Object d = this.doGet (Service.editors,
                               editorId,
                               null);        

        Map objD = null;
                               
        if (editorId == null)
        {
            
            // Might have got back our author in a list.
            if (d instanceof List)
            {
                
                List l = (List) d;
                
                if (l.size () > 0)
                {
                    
                    objD = (Map) l.get (0);
                    
                }
                
            }
            
        } else {
            
            objD = (Map) d;
            
        }
        
        if (objD != null)
        {                                  
        
            // We have the author information.
            EditorEditor ed = new EditorEditor ();        
                        
            Number lastModN = (Number) objD.get (FieldNames.lastModified);
            
            ed.setId ((String) objD.get (FieldNames.id));
            ed.setName ((String) objD.get (FieldNames.name));
            ed.setAbout ((String) objD.get (FieldNames.about));
            ed.setLastModified (new Date (lastModN.longValue ()));
            
            this.acc.setEditor (ed);
            
            // Get the genres
            Object g = objD.get (FieldNames.genres);
            
            if (g != null)
            {
                
                ed.setGenres (new LinkedHashSet ((Collection<String>) g));

            }
            
            Object wcl = objD.get (FieldNames.wordCountLengths);
            
            if (wcl != null)
            {
                
                // Get the word count lengths
                ed.setWordCountLengths (EditorProject.WordCountLength.convert ((new LinkedHashSet ((Collection<String>) wcl))));
            
            }
            
            // Save the file away.
            String av = (String) objD.get (FieldNames.avatarImage);
            
            if (av != null)
            {
                
                File f = Environment.getEditorsEditorAvatarImageFile ((String) objD.get (FieldNames.avatarImageFileType));
                
                // Base64 decode, then write bytes to file.
                byte[] fbytes = Base64.decode (av);
                
                f = new File (f.getPath ());
                
                Utils.writeBytesToFile (f,
                                        fbytes);
                                
            }
                                        
            this.saveEditorToLocal (null);
        
        } else {
            
            // Just assume user has cleaned up or something has gone setting the property.
            Environment.setUserProperty (Constants.QW_EDITORS_EDITOR_LAST_MODIFIED_PROPERTY_NAME,
                                         Long.MAX_VALUE + "");
            
        }
        
    }

    public void deleteAccount ()
    {
        
        // Delete from server, then inform editors.
        
        // Need to deal with our project...
        
    }
    
    public void saveAccount (String email,
                             String password)
                             throws Exception
    {
        
        if (this.acc == null)
        {

            this.createAccount (email,
                                password);
        
        } else {

            this.updateAccount (email,
                                password);
            
        }
        
    }
    
    private void saveAccountToLocal (String password)
                                     throws Exception
    {
                                     
        // Save the account info.

        Environment.setUserProperty (Constants.QW_EDITORS_EMAIL_PROPERTY_NAME,
                                     this.acc.getEmail ());
        Environment.setUserProperty (Constants.QW_EDITORS_PASSWORD_PROPERTY_NAME,
                                     password);        
        
    }
    
    private void updateAccount (String email,
                                String password)
                                throws Exception
    {

        if (this.acc == null)
        {
            
            throw new IllegalStateException ("No account found.");
            
        }
        
        Map data = new HashMap ();
        data.put (FieldNames.email,
                  email);
        data.put (FieldNames.password,
                  password);
            
        Object d = this.doPut (Service.accounts,
                               null,
                               data);        
                    
        this.acc.setEmail (email);
                    
        this.saveAccountToLocal (password);
        
    }
    
    private void createAccount (String email,
                                String password)
                                throws Exception
    {
        
        if (this.acc != null)
        {
            
            throw new IllegalStateException ("Already have an account.");
            
        }
                
        Map data = new HashMap ();
        data.put (FieldNames.email,
                  email);
        data.put (FieldNames.password,
                  password);
            
        Object d = this.doPost (Service.accounts,
                                null,
                                data);        

        this.sessionId = d.toString ();
                    
        EditorAccount acc = new EditorAccount ();

        this.acc = acc;
        acc.setEmail (email);

        this.saveAccountToLocal (password);
        
    }
 
    public EditorAccount getAccount ()
    {
        
        return this.acc;
        
    }
    
    public void logout ()
    {
        
    }
    
    public void login ()
                       throws Exception
    {
        
        if (this.acc == null)
        {
            
            throw new GeneralException ("No account available");
            
        }
        
        Map data = new HashMap ();
        data.put (FieldNames.email,
                  this.acc.getEmail ());        
        data.put (FieldNames.password,
                  this.acc.getPassword ());
            
        Object d = this.doPost (Service.login,
                                null,
                                data);                
                
        this.sessionId = d.toString ();

        Environment.logDebugMessage ("Logged in, got session id: " + this.sessionId);
        
    }
    
}