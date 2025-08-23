package com.dua3.utility.crypt;

import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.HexFormat;

public class BlindSigningClient {

    private final RSAPublicKey serverPub;
    private final BigInteger n, e;
    private final int k;
    private final SecureRandom rnd = new SecureRandom();
    private final PrivateKey clientPriv;

    public BlindSigningClient(RSAPublicKey serverPub, PrivateKey clientPriv) {
        this.serverPub = serverPub;
        this.n = serverPub.getModulus();
        this.e = serverPub.getPublicExponent();
        this.k = (n.bitLength() + 7) / 8;
        this.clientPriv = clientPriv;
    }

    public byte[] blindSign(byte[] message, URI endpoint) throws Exception {
        // 1) EMSA-PKCS1-v1_5 encoding (SHA-256)
        byte[] em = emsaPkcs1v15Sha256(message, k);
        BigInteger m = new BigInteger(1, em);

        // 2) Blind
        BigInteger r = randomCoprime(n);
        BigInteger mBlinded = m.multiply(r.modPow(e, n)).mod(n);

        // 3) Sign blinded message with client key
        byte[] clientSig = signClient(mBlinded.toByteArray(), clientPriv);

        // 4) Send to server
        String json = String.format("{\"m_blinded_hex\":\"%s\",\"client_sig_hex\":\"%s\"}",
                mBlinded.toString(16), HexFormat.of().formatHex(clientSig));
        HttpRequest req = HttpRequest.newBuilder()
                .uri(endpoint)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
        String sBlindedHex = resp.body().replaceAll(".*\"s_blinded_hex\"\\s*:\\s*\"([0-9a-fA-F]+)\".*", "$1");

        // 5) Unblind
        BigInteger sBlinded = new BigInteger(sBlindedHex, 16);
        BigInteger rInv = r.modInverse(n);
        BigInteger s = sBlinded.multiply(rInv).mod(n);

        // 6) Verify locally
        BigInteger v = s.modPow(e, n);
        if (!MessageDigest.isEqual(toFixedLen(v, k), em))
            throw new SignatureException("Signature verification failed");

        return toFixedLen(s, k);
    }

    private BigInteger randomCoprime(BigInteger n) {
        BigInteger r;
        do {
            r = new BigInteger(n.bitLength() - 1, rnd);
        } while (!r.gcd(n).equals(BigInteger.ONE) || r.signum() <= 0);
        return r;
    }

    private static byte[] signClient(byte[] data, PrivateKey priv) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(priv);
        sig.update(data);
        return sig.sign();
    }

    private static byte[] emsaPkcs1v15Sha256(byte[] message, int k) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] h = md.digest(message);
        byte[] prefix = HexFormat.of().parseHex("3031300d060960864801650304020105000420");
        int tLen = prefix.length + h.length;
        if (k < tLen + 11) throw new IllegalArgumentException("k too small");
        byte[] em = new byte[k];
        int psLen = k - tLen - 3;
        em[0] = 0x00; em[1] = 0x01;
        for (int i = 0; i < psLen; i++) em[2 + i] = (byte) 0xFF;
        em[2 + psLen] = 0x00;
        System.arraycopy(prefix, 0, em, 3 + psLen, prefix.length);
        System.arraycopy(h, 0, em, 3 + psLen + prefix.length, h.length);
        return em;
    }

    private static byte[] toFixedLen(BigInteger x, int len) {
        byte[] tmp = x.toByteArray();
        if (tmp.length == len) return tmp;
        if (tmp.length == len + 1 && tmp[0] == 0) {
            byte[] out = new byte[len]; System.arraycopy(tmp, 1, out, 0, len); return out;
        }
        byte[] out = new byte[len];
        System.arraycopy(tmp, Math.max(0, tmp.length - len), out, len - Math.min(len, tmp.length), Math.min(len, tmp.length));
        return out;
    }

    // Utility to load RSA public key from PEM
    public static RSAPublicKey loadRsaPublicKeyFromPem(String pem) throws Exception {
        String clean = pem.replaceAll("-----\\w+ PUBLIC KEY-----", "").replaceAll("\\s", "");
        byte[] bytes = java.util.Base64.getDecoder().decode(clean);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(bytes));
    }

    // Utility to load RSA private key from PEM (PKCS#8)
    public static PrivateKey loadRsaPrivateKeyFromPem(String pem) throws Exception {
        String clean = pem.replaceAll("-----\\w+ PRIVATE KEY-----", "").replaceAll("\\s", "");
        byte[] bytes = java.util.Base64.getDecoder().decode(clean);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }
}