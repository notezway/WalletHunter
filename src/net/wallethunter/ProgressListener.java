package net.wallethunter;

public interface ProgressListener {

    //from 0 to 1
    void updateProgress(double progress);
}
