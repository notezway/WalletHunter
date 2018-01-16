package net.wallethunter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class WalletKey {

    private static final char[] base58Chars =
            "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final int keyPrefix = 0x80;

    private final MessageDigest sha256;

    //array of unsigned bytes, 24 most significant bits are ignored
    private final int[] rawData;

    public WalletKey(int[] rawData) throws NoSuchAlgorithmException {
        this.rawData = rawData;
        sha256 = MessageDigest.getInstance("SHA-256");
    }

    public String getRawString() {
        StringBuilder result = new StringBuilder();
        for (int element : rawData) {
            result.append(Integer.toHexString(element));
        }
        return result.toString();
    }

    public String getBase58String() {
        int[] keyData = new int[rawData.length + 1];
        keyData[0] = keyPrefix;
        System.arraycopy(rawData, 0, keyData, 1, rawData.length);
        byte[] secret = toSignedBytes(keyData);
        byte[] dHash = getDoubleHash(secret);
        byte[] toBase58 = new byte[secret.length + 4];
        System.arraycopy(dHash, 0, toBase58, 0, 4);
        System.arraycopy(secret, 0, toBase58, 4, secret.length);
        return toBase58String(toBase58);
    }

    private String toBase58String(byte[] input) {
        // Count leading zeros.
        int zeros = 0;
        while (zeros < input.length && input[zeros] == 0) {
            ++zeros;
        }
        // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
        input = Arrays.copyOf(input, input.length); // since we modify it in-place
        char[] encoded = new char[input.length * 2]; // upper bound
        int outputStart = encoded.length;
        for (int inputStart = zeros; inputStart < input.length; ) {
            encoded[--outputStart] = base58Chars[divmod(input, inputStart, 256, 58)];
            if (input[inputStart] == 0) {
                ++inputStart; // optimization - skip leading zeros
            }
        }
        // Preserve exactly as many leading encoded zeros in output as there were leading zeros in input.
        while (outputStart < encoded.length && encoded[outputStart] == base58Chars[0]) {
            ++outputStart;
        }
        while (--zeros >= 0) {
            encoded[--outputStart] = base58Chars[0];
        }
        // Return encoded string (including encoded leading zeros).
        return new String(encoded, outputStart, encoded.length - outputStart);
    }

    private byte divmod(byte[] number, int firstDigit, int base, int divisor) {
        // this is just long division which accounts for the base of the input digits
        int remainder = 0;
        for (int i = firstDigit; i < number.length; i++) {
            int digit = (int) number[i] & 0xFF;
            int temp = remainder * base + digit;
            number[i] = (byte) (temp / divisor);
            remainder = temp % divisor;
        }
        return (byte) remainder;
    }

    private byte[] getDoubleHash(byte[] bytes) {
        return sha256.digest(sha256.digest(bytes));
    }

    private byte[] toSignedBytes(int[] unsignedBytes) {
        byte[] bytes = new byte[unsignedBytes.length];
        for(int i = 0; i < unsignedBytes.length; i++) {
            bytes[i] = (byte) unsignedBytes[i];
        }
        return bytes;
    }

    @Override
    public String toString() {
        return "{" + getRawString() + ", " + getBase58String() + "}";
    }
}
