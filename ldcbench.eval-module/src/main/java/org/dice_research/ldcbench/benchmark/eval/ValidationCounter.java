package org.dice_research.ldcbench.benchmark.eval;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ValidationCounter implements Consumer<Boolean>{

    protected AtomicInteger positives = new AtomicInteger();
    protected AtomicInteger count = new AtomicInteger();
    
    @Override
    public void accept(Boolean t) {
        count.incrementAndGet();
        if(t) {
            positives.incrementAndGet();
        }
    }
    
    public ValidationResult getValidationResult() {
        return new ValidationResult(count.get(), positives.get());
    }
}
