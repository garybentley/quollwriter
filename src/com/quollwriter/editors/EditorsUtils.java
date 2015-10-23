package com.quollwriter.editors;

import java.io.*;

import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.image.*;
import javax.imageio.*;

import java.math.*;
import java.util.*;
import java.security.*;

import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import org.bouncycastle.bcpg.*;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.bc.*;
import org.bouncycastle.crypto.generators.*;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.openpgp.operator.bc.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.events.*;
import com.quollwriter.ui.components.QPopup;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.editors.ui.*;
import com.quollwriter.data.editors.*;

public class EditorsUtils
{
                         
    public static BufferedImage getImageFromBase64EncodedString (String s)
                                                          throws Exception
    {
        
        if (s == null)
        {
            
            return null;
                 
        }
        
        // Decode.
        byte[] bytes = com.quollwriter.Base64.decode (s);		
		
		return UIUtils.getImage (bytes);
        
    }
    
    public static String getImageAsBase64EncodedString (BufferedImage im)
                                                 throws Exception
    {
        
        if (im == null)
        {
            
            return null;
            
        }
		
		byte[] bytes = UIUtils.getImageBytes (im);
		        
        return com.quollwriter.Base64.encodeBytes (bytes);
        
    }

                 
	public static PGPKeyPair generateKeyPair ()
									   throws Exception
	{
		
        RSAKeyPairGenerator generator = new RSAKeyPairGenerator ();
        
        generator.init (new RSAKeyGenerationParameters (new BigInteger ("10001", 16),//publicExponent
                                                        SecureRandom.getInstance("SHA1PRNG"),//prng
                                                        2048,//strength
                                                        100));
                
        return new BcPGPKeyPair (PublicKeyAlgorithmTags.RSA_GENERAL,
                                 generator.generateKeyPair (),
                                 new java.util.Date ());
				 
	}				 
	
	public static byte[] getPGPPublicKeyByteEncoded (PGPPublicKey k)
	{
		
		RSAPublicBCPGKey pubKey = (RSAPublicBCPGKey) k.getPublicKeyPacket ().getKey ();
		
		return pubKey.getEncoded ();
		
	}
	
	public static String getPGPPublicKeyBase64Encoded (PGPPublicKey k)
	{
		
        RSAPublicBCPGKey pubKey = (RSAPublicBCPGKey) k.getPublicKeyPacket ().getKey ();

		return com.quollwriter.Base64.encodeBytes (pubKey.getEncoded ());
		
	}
	
    public static PGPPublicKey convertToPGPPublicKey (byte[] bytes)
                                               throws Exception
    {
        
        if (bytes == null)
        {
            
            return null;
            
        }
        
        ByteArrayInputStream bin = new ByteArrayInputStream (bytes);

        RSAPublicBCPGKey pk = new RSAPublicBCPGKey (new BCPGInputStream (bin));

        PublicKeyPacket pkp = new PublicKeyPacket (PublicKeyAlgorithmTags.RSA_GENERAL,
                                                   new java.util.Date (),
                                                   pk);

        return new PGPPublicKey (pkp,
                                 new BcKeyFingerprintCalculator ());
        
        
    }
    
    public static String bytesToHex (byte[] bytes)
    {
        
        StringBuilder sb = new StringBuilder();
    
        for (byte b : bytes)
        {
        
            sb.append(String.format("%02X", b));
    
        }        
    
        return sb.toString ().toLowerCase ();
        
    }
 
    public static byte[] encrypt (String message,
                                  // Your private key, used for signature generation
                                  PGPPrivateKey privateKey,
                                  // Their public key
                                  PGPPublicKey publicKey)
                           throws Exception
    {

        int BUFFER_SIZE = 8192; // should always be power of 2

        ByteArrayOutputStream bout = new ByteArrayOutputStream ();
        
        ArmoredOutputStream outputStream = new ArmoredOutputStream(bout);

        // Init encrypted data generator
        BcPGPDataEncryptorBuilder encB = new BcPGPDataEncryptorBuilder (SymmetricKeyAlgorithmTags.AES_256);
        encB.setWithIntegrityPacket (true);
        
        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator (encB);
                
        encryptedDataGenerator.addMethod (new BcPublicKeyKeyEncryptionMethodGenerator (publicKey));
        
        OutputStream encryptedOut = encryptedDataGenerator.open (outputStream,
                                                                 new byte[BUFFER_SIZE]);

        // Init compression
        PGPCompressedDataGenerator compressedDataGenerator = new PGPCompressedDataGenerator (CompressionAlgorithmTags.ZIP);
        OutputStream compressedOut = compressedDataGenerator.open (encryptedOut);

        BcPGPContentSignerBuilder b = new BcPGPContentSignerBuilder (PublicKeyAlgorithmTags.RSA_GENERAL,
                                                                     HashAlgorithmTags.SHA512);
        
        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator (b);
        
        signatureGenerator.init (PGPSignature.BINARY_DOCUMENT,
                                 privateKey);
        
        PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
        spGen.setSignerUserID (false,
                               "user");
        
        signatureGenerator.setHashedSubpackets (spGen.generate ());

        signatureGenerator.generateOnePassVersion (false).encode (compressedOut);

        // Create the Literal Data generator output stream
        PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator ();

        OutputStream literalOut = literalDataGenerator.open (compressedOut,
                                                             PGPLiteralDataGenerator.UTF8,
                                                             "message",
                                                             new Date (),
                                                             new byte[BUFFER_SIZE]);

        byte[] messBytes = message.getBytes ();

        literalOut.write (messBytes,
                          0,
                          messBytes.length);
        signatureGenerator.update (messBytes,
                                   0,
                                   messBytes.length);

        literalOut.close ();
        literalDataGenerator.close ();
        
        signatureGenerator.generate ().encode (compressedOut);
        
        compressedOut.close();
        
        compressedDataGenerator.close();
        
        encryptedOut.close();
        
        encryptedDataGenerator.close();

        outputStream.close();        
        
        bout.flush ();
        bout.close ();
        
        return bout.toByteArray ();
        
    }
 
	public static byte[] decrypt (byte[]        message,
                                  // Your private key
                                  PGPPrivateKey privateKey,
                                  // Their public key
                                  PGPPublicKey  publicKey)
                           throws Exception
    {
    
        ByteArrayOutputStream bout = new ByteArrayOutputStream ();
    
        InputStream in = new ByteArrayInputStream (message);
        
        in = org.bouncycastle.openpgp.PGPUtil.getDecoderStream (in);

        PGPObjectFactory pgpF = new BcPGPObjectFactory (in);
        PGPEncryptedDataList enc;

        Object o = pgpF.nextObject();
        //
        // the first object might be a PGP marker packet.
        //
        if (o instanceof  PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        } else {
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }

        //
        // find the secret key
        //
        Iterator<PGPPublicKeyEncryptedData> it = enc.getEncryptedDataObjects ();
        //PGPPrivateKey sKey = null;
        PGPPublicKeyEncryptedData pbe = null;

        while (it.hasNext ())
        {
            
            pbe = it.next();
        
        }

        BcPublicKeyDataDecryptorFactory bkf = new BcPublicKeyDataDecryptorFactory (privateKey);
        
        InputStream clear = pbe.getDataStream (bkf);

        PGPObjectFactory plainFact = new BcPGPObjectFactory(clear);

        Object messObj = null;
        
        while (true)
        {
            
            messObj = plainFact.nextObject ();

            if (messObj == null)
            {
                
                break;
                
            }            
                        
            if (messObj instanceof  PGPCompressedData)
            {

                PGPCompressedData cData = (PGPCompressedData) messObj;
                PGPObjectFactory pgpFact = new BcPGPObjectFactory (cData.getDataStream ());
    
                PGPOnePassSignature osig = null;
                PGPSignature sig = null;
    
                while (true)
                {
        
                    Object messObj2 = pgpFact.nextObject ();

                    if (messObj2 == null)
                    {
                        
                        break;
                        
                    }
                        
                    if (messObj2 instanceof  PGPLiteralData)
                    {
                    
                        PGPLiteralData ld = (PGPLiteralData) messObj2;
            
                        InputStream unc = ld.getInputStream ();
                        int ch;
            
                        while ((ch = unc.read()) >= 0)
                        {
                        
                            if (osig != null)
                            {
                                
                                osig.update ((byte) ch);
                                
                            }
                        
                            bout.write(ch);
                        
                        }
                
                    }
                    
                    if (messObj2 instanceof PGPOnePassSignatureList)
                    {
                        
                        PGPOnePassSignatureList slist = (PGPOnePassSignatureList) messObj2;
                        
                        for (int i = 0; i < slist.size (); i++)
                        {
                            
                            osig = slist.get (i);
                            
                            osig.init (new BcPGPContentVerifierBuilderProvider (),
                                       publicKey);
                            
                        }
                        
                    }
                    
                    if (messObj2 instanceof PGPSignatureList)
                    {
                        
                        PGPSignatureList slist = (PGPSignatureList) messObj2;
                        
                        for (int i = 0; i < slist.size (); i++)
                        {
                            
                            sig = slist.get (i);

                        }
                        
                    }

                }
                
                if ((osig != null)
                    &&
                    (sig != null)
                   )
                {
                    
                    if (!osig.verify (sig))
                    {
                        
                        throw new GeneralException ("Unable to verify one pass signature");
                        
                    }
                    
                }
                
            }
                    
        }

        if (pbe.isIntegrityProtected())
        {
        
            if (!pbe.verify())
            {
            
            	throw new GeneralException("Message failed integrity check");
            
            }
        
        }        
        
        bout.flush ();
        bout.close ();
        
        return bout.toByteArray ();
        
    }
    
}