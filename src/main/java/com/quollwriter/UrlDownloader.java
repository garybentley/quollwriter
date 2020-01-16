package com.quollwriter;

import java.net.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;

public class UrlDownloader
{

    private URL url = null;
    private DownloadListener list = null;
    private File toFile = null;
    private boolean stop = false;
    private boolean inProgress = false;

    private Consumer<Exception> onError = null;
    private BiConsumer<Integer, Integer> onProgress = null;
    private Runnable onStop = null;
    private Consumer<Path> onComplete = null;

    private UrlDownloader (Builder b)
    {

        if (b.url == null)
        {

            throw new IllegalArgumentException ("Url must be provided.");

        }

        this.url = b.url;
        this.onError = b.onError;
        this.onProgress = b.onProgress;
        this.onComplete = b.onComplete;
        this.onStop = b.onStop;

        if (b.saveTo != null)
        {

            this.toFile = b.saveTo.toFile ();

        } else {

            try
            {

                this.toFile = File.createTempFile (UUID.randomUUID ().toString (),
                                                   null);

            } catch (Exception e) {

                Environment.logError ("Unable to download language files, cant create temp file",
                                      e);

                if (this.onError != null)
                {

                    this.onError.accept (e);

                }

                return;

            }

            this.toFile.deleteOnExit ();

        }

    }

    public UrlDownloader (URL              url,
                          File             toFile,
                          DownloadListener l)
    {

        this.url = url;
        this.toFile = toFile;
        this.list = l;

    }

    public boolean isInProgress ()
    {

        return this.inProgress;

    }

    public void start ()
    {

        this.stop = false;

        if (this.inProgress)
        {

            return;

        }

        this.inProgress = true;

        final UrlDownloader _this = this;

        new Thread (() ->
        {

            InputStream in = null;
            OutputStream out = null;

            try
            {

                URLConnection conn = _this.url.openConnection ();

                int length = conn.getContentLength ();

                in = new BufferedInputStream (conn.getInputStream ());
                out = new BufferedOutputStream (new FileOutputStream (this.toFile));

                byte[] buf = new byte[8192];

                int bRead = -1;

                int total = 0;

                while ((bRead = in.read (buf)) != -1)
                {

                    if (this.stop)
                    {

                        in.close ();
                        out.flush ();
                        out.close ();

                        if (this.onStop != null)
                        {

                            this.onStop.run ();

                        }

                        return;

                    }

                    total += bRead;

                    out.write (buf,
                               0,
                               bRead);

                    if (this.onProgress != null)
                    {

                        this.onProgress.accept (total, length);

                    }

                    if (this.list != null)
                    {

                        this.list.progress (total,
                                             length);

                    }

                }

                in.close ();
                out.flush ();
                out.close ();

                if (this.onComplete != null)
                {

                    this.onComplete.accept (this.toFile.toPath ());

                }

                if (this.list != null)
                {

                    this.list.finished (length);

                }

            } catch (Exception e) {

                if (this.onError != null)
                {

                    this.onError.accept (e);

                }

                if (this.list != null)
                {

                    this.list.handleError (e);

                }

            } finally {

                this.inProgress = false;

                try
                {

                    if (in != null)
                    {

                        in.close ();

                    }

                    if (out != null)
                    {

                        out.flush ();
                        out.close ();

                    }

                } catch (Exception e) {}

            }

        }).start ();

    }

    public void stop ()
    {

        this.stop = true;

    }

    public static UrlDownloader.Builder builder ()
    {

        return new Builder ();

    }

    public static class Builder
    {

        private BiConsumer<Integer, Integer> onProgress = null;
        private Consumer<Exception> onError = null;
        private Runnable onStop = null;
        private Consumer<Path> onComplete = null;
        private URL url = null;
        private Path saveTo = null;

        private Builder ()
        {

        }

        public UrlDownloader build ()
        {

            return new UrlDownloader (this);

        }

        public Builder onProgress (BiConsumer<Integer, Integer> on)
        {

            this.onProgress =on;
            return this;

        }

        public Builder onError (Consumer<Exception> on)
        {

            this.onError = on;
            return this;

        }

        public Builder onStop (Runnable r)
        {

            this.onStop = r;
            return this;

        }

        public Builder onComplete (Consumer<Path> on)
        {

            this.onComplete = on;
            return this;

        }

        public Builder url (URL u)
        {

            this.url = u;
            return this;

        }

        public Builder saveTo (Path f)
        {

            this.saveTo = f;
            return this;

        }

    }

}
