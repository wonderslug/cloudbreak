package com.sequenceiq.redbeams.flow.stack.termination.event;

import com.sequenceiq.freeipa.flow.stack.StackEvent;

public class TerminationEvent extends StackEvent {

    private final Boolean forced;

    public TerminationEvent(String selector, Long stackId, Boolean forced) {
        super(selector, stackId, null);
        this.forced = forced;
    }

    public Boolean getForced() {
        return forced;
    }
}
