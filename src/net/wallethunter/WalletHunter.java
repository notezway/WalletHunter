package net.wallethunter;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class WalletHunter {

    private static final int[] magicBytes = new int[] {
            0x01, 0x30, 0x82, 0x01, 0x13, 0x02, 0x01, 0x01, 0x04, 0x20
    };
    private static final int keyLength = 32;

    private final String path;
    private ProgressListener progressListener;

    public WalletHunter(String path) {
        this.path = path;
    }

    public WalletHunter setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    public List<WalletKey> findKeys() throws IOException, NoSuchAlgorithmException {
        File file = new File(path);
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        long fileLength = file.length();

        List<WalletKey> keys = new ArrayList<>();

        for(long passed = 0; passed < fileLength - 35; passed += 4) {
            bis.mark(magicBytes.length);
            if(tryDetectKey(bis) == magicBytes.length) {
                bis.mark(keyLength);
                keys.add(readKey(bis));
            } else {
                bis.reset();
                while(bis.skip(1) < 1);
            }
            if(progressListener != null && passed > 2<<16 && passed % 4096 == 0) {
                progressListener.updateProgress(((double)passed) / ((double)fileLength));
            }
        }
        bis.close();
        return keys;
    }

    private int tryDetectKey(BufferedInputStream bis) throws IOException {
        for(int i = 0; i < magicBytes.length; i++) {
            if(bis.read() != magicBytes[i]) {
                return i + 1;
            }
        }
        return magicBytes.length;
    }

    private WalletKey readKey(BufferedInputStream bis) throws IOException, NoSuchAlgorithmException {
        int[] keyData = new int[32];
        for(int i = 0; i < keyData.length; i++) {
            keyData[i] = bis.read();
        }
        return new WalletKey(keyData);
    }
}
