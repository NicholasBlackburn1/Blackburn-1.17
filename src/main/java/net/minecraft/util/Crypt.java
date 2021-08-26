package net.minecraft.util;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypt
{
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final int SYMMETRIC_BITS = 128;
    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final int ASYMMETRIC_BITS = 1024;
    private static final String BYTE_ENCODING = "ISO_8859_1";
    private static final String HASH_ALGORITHM = "SHA-1";

    public static SecretKey generateSecretKey() throws CryptException
    {
        try
        {
            KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
            keygenerator.init(128);
            return keygenerator.generateKey();
        }
        catch (Exception exception)
        {
            throw new CryptException(exception);
        }
    }

    public static KeyPair generateKeyPair() throws CryptException
    {
        try
        {
            KeyPairGenerator keypairgenerator = KeyPairGenerator.getInstance("RSA");
            keypairgenerator.initialize(1024);
            return keypairgenerator.generateKeyPair();
        }
        catch (Exception exception)
        {
            throw new CryptException(exception);
        }
    }

    public static byte[] digestData(String pServerId, PublicKey pPublicKey, SecretKey pSecretKey) throws CryptException
    {
        try
        {
            return m_13602_(pServerId.getBytes("ISO_8859_1"), pSecretKey.getEncoded(), pPublicKey.getEncoded());
        }
        catch (Exception exception)
        {
            throw new CryptException(exception);
        }
    }

    private static byte[] m_13602_(byte[]... p_13603_) throws Exception
    {
        MessageDigest messagedigest = MessageDigest.getInstance("SHA-1");

        for (byte[] abyte : p_13603_)
        {
            messagedigest.update(abyte);
        }

        return messagedigest.digest();
    }

    public static PublicKey m_13600_(byte[] p_13601_) throws CryptException
    {
        try
        {
            EncodedKeySpec encodedkeyspec = new X509EncodedKeySpec(p_13601_);
            KeyFactory keyfactory = KeyFactory.getInstance("RSA");
            return keyfactory.generatePublic(encodedkeyspec);
        }
        catch (Exception exception)
        {
            throw new CryptException(exception);
        }
    }

    public static SecretKey m_13597_(PrivateKey p_13598_, byte[] p_13599_) throws CryptException
    {
        byte[] abyte = m_13605_(p_13598_, p_13599_);

        try
        {
            return new SecretKeySpec(abyte, "AES");
        }
        catch (Exception exception)
        {
            throw new CryptException(exception);
        }
    }

    public static byte[] m_13594_(Key p_13595_, byte[] p_13596_) throws CryptException
    {
        return m_13586_(1, p_13595_, p_13596_);
    }

    public static byte[] m_13605_(Key p_13606_, byte[] p_13607_) throws CryptException
    {
        return m_13586_(2, p_13606_, p_13607_);
    }

    private static byte[] m_13586_(int p_13587_, Key p_13588_, byte[] p_13589_) throws CryptException
    {
        try
        {
            return setupCipher(p_13587_, p_13588_.getAlgorithm(), p_13588_).doFinal(p_13589_);
        }
        catch (Exception exception)
        {
            throw new CryptException(exception);
        }
    }

    private static Cipher setupCipher(int pOpMode, String pTransformation, Key pKey) throws Exception
    {
        Cipher cipher = Cipher.getInstance(pTransformation);
        cipher.init(pOpMode, pKey);
        return cipher;
    }

    public static Cipher getCipher(int pOpMode, Key pKey) throws CryptException
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(pOpMode, pKey, new IvParameterSpec(pKey.getEncoded()));
            return cipher;
        }
        catch (Exception exception)
        {
            throw new CryptException(exception);
        }
    }
}
