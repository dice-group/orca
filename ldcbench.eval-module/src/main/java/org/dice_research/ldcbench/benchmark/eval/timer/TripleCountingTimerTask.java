package org.dice_research.ldcbench.benchmark.eval.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class TripleCountingTimerTask extends TimerTask {
    
    protected List<Long> timestamps = new ArrayList<>();
    protected List<Long> tripleCounts = new ArrayList<>();
    protected TripleCounter counter;

    public TripleCountingTimerTask(TripleCounter counter) {
        this.counter = counter;
    }

    @Override
    public void run() {
        tripleCounts.add(counter.countTriples());
        timestamps.add(System.currentTimeMillis());
    }

    public List<Long> getTimestamps() {
        return timestamps;
    }
    
    public List<Long> getTripleCounts() {
        return tripleCounts;
    }
}
