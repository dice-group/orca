package org.dice_research.ldcbench;

public class ApiConstants {

    public static final String ENV_NODE_ID_KEY = "LDCBENCH_NODE_ID";
    public static final String ENV_BENCHMARK_EXCHANGE_KEY = "LDCBENCH_BENCHMARK_EXCHANGE";
    public static final String ENV_DATA_QUEUE_KEY = "LDCBENCH_DATA_QUEUE";
    public static final String ENV_EVAL_DATA_QUEUE_KEY = "LDCBENCH_EVAL_DATA_QUEUE";
    public static final String ENV_SPARQL_ENDPOINT_KEY = "LDCBENCH_SPARQL_ENDPOINT";

    public static final String ENV_HTTP_PORT_KEY = "HTTP_PORT";

    public static final byte CRAWLING_STARTED_SIGNAL = 126;
    public static final byte CRAWLING_FINISHED_SIGNAL = 127;
    public static final byte DATAGENERATOR_READY_SIGNAL = 125;
    public static final byte NODE_READY_SIGNAL = 124;

}
