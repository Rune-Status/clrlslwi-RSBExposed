package ne.frs.bots;

import lombok.RequiredArgsConstructor;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.security.Key;

/**
 * This class allows for easy transcoding of dreambot credentials.
 *
 * @author Demmonic
 */
@RequiredArgsConstructor
public class DreamBotCredentialTranscoder {

    private final String userSpecificKey;

    /**
     * Encodes a credential line.
     *
     * @param line The line to encode.
     * @return The encoded line.
     * @throws Exception If encoding the line fails.
     */
    public String encode(final String line) throws Exception {
        Key key = generateKey();
        Cipher instance = Cipher.getInstance("AES");
        instance.init(Cipher.ENCRYPT_MODE, key);
        return new BASE64Encoder().encode(instance.doFinal(line.getBytes()));
    }

    /**
     * Decodes a credential line.
     *
     * @param line The line to decode.
     * @return The decoded line.
     * @throws Exception If decoding the line fails.
     */
    public String decode(String line) throws Exception {
        Key key = generateKey();
        Cipher instance = Cipher.getInstance("AES");
        instance.init(Cipher.DECRYPT_MODE, key);
        return new String(instance.doFinal(new BASE64Decoder().decodeBuffer(line)));
    }

    /**
     * Generates a {@link Key key} that can be used for encrypting and decrypting
     * credential lines.
     *
     * @return The generated key.
     * @throws Exception If generating the key fails.
     */
    private Key generateKey() throws Exception {
        byte[] key = (userSpecificKey + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAA").substring(0, 16).getBytes();
        return new SecretKeySpec(key, "AES");
    }

    /**
     * Retrieves a key that's specific to the machine this program
     * is running within the context of.
     *
     * @return The user specific key.
     * @throws Exception If retrieves the key fails.
     */
    private static String getUserSpecificKey() throws Exception {
        return InetAddress.getLocalHost().getHostAddress();
    }

    /**
     * Creates a new transcoder that has a user-specific key relative
     * to the machine that is running this application.
     *
     * @return The newly created transcoder.
     * @throws Exception If retrieving the key fails.
     */
    public static DreamBotCredentialTranscoder machineBased() throws Exception {
        return new DreamBotCredentialTranscoder(getUserSpecificKey());
    }

}
