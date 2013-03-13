package com.quollwriter;

import java.io.*;

import java.net.*;

public class ObjectRefURLStreamHandlerFactory implements URLStreamHandlerFactory
{

    public URLStreamHandler createURLStreamHandler (String protocol)
    {

        if (protocol.equals ("file"))
        {

            return new URLStreamHandler ()
            {

                public URLConnection openConnection (final URL url)
                                              throws IOException
                {

                    return new URLConnection (url)
                    {

                        public void connect ()
                                      throws IOException
                        {

                        }

                        public InputStream getInputStream ()
                                                    throws IOException
                        {

                            try
                            {

                                return new FileInputStream (new File (url.toURI ()));

                            } catch (URISyntaxException e)
                            {

                                throw new IOException ("Unable to construct file for url: " +
                                                       url,
                                                       e);

                            }

                        }

                        public OutputStream getOutputStream ()
                                                      throws IOException
                        {

                            try
                            {

                                return new FileOutputStream (new File (url.toURI ()));

                            } catch (URISyntaxException e)
                            {

                                throw new IOException ("Unable to construct file for url: " +
                                                       url,
                                                       e);

                            }

                        }

                    };

                }

            };

        }

        if (protocol.equals (Constants.OBJECTREF_PROTOCOL))
        {

            return new URLStreamHandler ()
            {

                public URLConnection openConnection (URL url)
                                              throws IOException
                {

                    throw new IOException ("Not supported for the: " +
                                           Constants.OBJECTREF_PROTOCOL +
                                           " protocol.");

                }

            };

        }

        if (protocol.equals (Constants.HELP_PROTOCOL))
        {

            return new URLStreamHandler ()
            {

                public URLConnection openConnection (URL url)
                                              throws IOException
                {

                    throw new IOException ("Not supported for the: " +
                                           Constants.HELP_PROTOCOL +
                                           " protocol.");

                }

            };

        }

        if (protocol.equals (Constants.QUOLLWRITER_PROTOCOL))
        {

            return new URLStreamHandler ()
            {

                public URLConnection openConnection (URL url)
                                              throws IOException
                {

                    throw new IOException ("Not supported for the: " +
                                           Constants.QUOLLWRITER_PROTOCOL +
                                           " protocol.");

                }

            };

        }
        
        return null;

    }

}
