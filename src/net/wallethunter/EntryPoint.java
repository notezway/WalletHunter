package net.wallethunter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class EntryPoint {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

        System.out.println(new WalletHunter(args[0])
                .setProgressListener(new ProgressPrinter()).findKeys());
    }
}
