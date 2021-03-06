package chainUtil;

import core.blockchain.Block;
import core.blockchain.BlockBody;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class ChainUtil {

    private static ChainUtil chainUtil;
    private static final Logger log = LoggerFactory.getLogger(ChainUtil.class);

    //change to private after changes
    public ChainUtil() {}

    public static ChainUtil getInstance() {
        if (chainUtil == null) {
            chainUtil = new ChainUtil();
        }
        return chainUtil;
    }

    public static String digitalSignature(String data) {
        Signature dsa = null;
        String signature = null;
        try {
            dsa = Signature.getInstance("SHA1withDSA", "SUN");
            dsa.initSign(KeyGenerator.getInstance().getPrivateKey());
            byte[] byteArray = data.getBytes();
            dsa.update(byteArray);
            signature = bytesToHex(dsa.sign());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return signature;
    }

    public static boolean signatureVerification(String publicKey, String signature, String data) {
        log.info("verifying signature: {}", publicKey);
        return verify(KeyGenerator.getInstance().getPublicKey(publicKey),hexStringToByteArray(signature),data);
    }

    public static byte[] sign(PrivateKey privateKey,String data) throws SignatureException {
        //sign the data
        Signature dsa = null;
        try {
            dsa = Signature.getInstance("SHA1withDSA", "SUN");
            dsa.initSign(privateKey);
            byte[] byteArray = data.getBytes();
            dsa.update(byteArray);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return dsa.sign();
    }

    public static boolean verify(PublicKey publicKey, byte[] signature, String data) {
        Signature sig = null;
        boolean verification = false;
        try {
            sig = Signature.getInstance("SHA1withDSA", "SUN");
            sig.initVerify(publicKey);
            sig.update(data.getBytes(),0,data.getBytes().length);
            verification = sig.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return verification;
    }

//    public publicKeyEncryption() {
//
//    }

    public static byte[] getHashByteArray(String data) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return digest.digest(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String getHash(String data) {
        return bytesToHex(getHashByteArray(data));
    }

    public String getBlockHash(Block block) {
        JSONObject jsonBlock = new JSONObject(block);
        return getHash((jsonBlock.toString()));
    }

    public static String getBlockHash(BlockBody blockBody) {
        JSONObject jsonBlock = new JSONObject(blockBody);
        return getHash((jsonBlock.toString()));
    }


    //functionaly changed
    public String getBlockChainHash(LinkedList<Block> blockchain) {
        String blockChainString = "";
        for(Block block: blockchain) {
            blockChainString += new JSONObject(block).toString();
        }
        return getHash(blockChainString);
    }

    //functionaly changed
    public String getBlockchainAsJsonString(LinkedList<Block> blockchain) {
        JSONObject jsonBlockchain = new JSONObject();
        for(int i = 0; i < blockchain.size(); i++) {
            jsonBlockchain.put(String.valueOf(i), new JSONObject(blockchain.get(i)).toString());
        }

        return jsonBlockchain.toString();
    }

    public boolean verifyUser(String peerID, String publicKey) {
        if(peerID.equals(publicKey.substring(0,40))) {
            return true;
        }
        return false;
    }



    public static Timestamp convertStringToTimestamp(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd.hh.mm.ss");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new java.sql.Timestamp(parsedDate.getTime());
    }

    public static Timestamp convertStringToTimestamp2(String time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new java.sql.Timestamp(parsedDate.getTime());
    }

    public static String getNodeIdUsingPk(String publicKey) {
        return publicKey.substring(publicKey.length()-40);
    }

}