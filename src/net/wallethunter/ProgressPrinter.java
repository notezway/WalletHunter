package net.wallethunter;

public class ProgressPrinter implements ProgressListener {

    private long lastTime;

    @Override
    public void updateProgress(double progress) {
        long time = System.currentTimeMillis();

        if(time > lastTime + 999) {
            lastTime = time;
            System.out.println((int)(progress * 100) + "%");
        }
    }
}
