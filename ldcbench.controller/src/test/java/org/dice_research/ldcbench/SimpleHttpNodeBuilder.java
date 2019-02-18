package org.dice_research.ldcbench;

import org.hobbit.sdk.docker.builders.AbstractDockersBuilder;
import org.hobbit.sdk.docker.builders.BothTypesDockersBuilder;

import static org.hobbit.core.Constants.*;
import static org.hobbit.sdk.Constants.*;
import static org.dice_research.ldcbench.ApiConstants.*;
import static org.dice_research.ldcbench.Constants.*;

public class SimpleHttpNodeBuilder extends BothTypesDockersBuilder {
    private static final String name = HTTPNODE_IMAGE_NAME;

    public SimpleHttpNodeBuilder(AbstractDockersBuilder builder) {
        super(builder);
    }

    @Override
    public void addEnvVars(AbstractDockersBuilder ret) {
        ret.addEnvironmentVariable(RABBIT_MQ_HOST_NAME_KEY, (String)System.getenv().get(RABBIT_MQ_HOST_NAME_KEY));
        ret.addEnvironmentVariable(HOBBIT_SESSION_ID_KEY, (String)System.getenv().get(HOBBIT_SESSION_ID_KEY));
        ret.addNetworks(new String[]{ HOBBIT_NETWORK_NAME });

        ret.addEnvironmentVariable(ENV_NODE_ID_KEY, (String)System.getenv().get(ENV_NODE_ID_KEY));
        ret.addEnvironmentVariable(ENV_BENCHMARK_EXCHANGE_KEY, (String)System.getenv().get(ENV_BENCHMARK_EXCHANGE_KEY));
        ret.addEnvironmentVariable(ENV_DATA_QUEUE_KEY, (String)System.getenv().get(ENV_DATA_QUEUE_KEY));
    }

    @Override
    public String getName() {
        return name;
    }

}
