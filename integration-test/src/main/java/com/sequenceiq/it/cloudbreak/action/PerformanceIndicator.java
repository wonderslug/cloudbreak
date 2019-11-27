package com.sequenceiq.it.cloudbreak.action;

public class PerformanceIndicator {
    private long start;

    private long duration;

    public PerformanceIndicator(long start, long duration) {
        this.start = start;
        this.duration = duration;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
