package org.dice_research.ldcbench.benchmark.eval.timer;

import org.hobbit.core.data.usage.ResourceUsageInformation;
import org.hobbit.core.components.utils.SystemResourceUsageRequester;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceUsageTimerTask extends TimerTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUsageTimerTask.class);

    protected List<Long> timestamps = new ArrayList<>();
    protected List<ResourceUsageInformation> values = new ArrayList<>();
    protected SystemResourceUsageRequester requester;

    public ResourceUsageTimerTask(SystemResourceUsageRequester requester) {
        this.requester = requester;
    }

    @Override
    public void run() {
        ResourceUsageInformation info = requester.getSystemResourceUsage();
        LOGGER.info("Resource usage: {}", info);
        if (info != null) {
            values.add(info);
            timestamps.add(System.currentTimeMillis());
        }
    }

    public List<Long> getTimestamps() {
        return timestamps;
    }

    public List<ResourceUsageInformation> getValues() {
        return values;
    }
}
