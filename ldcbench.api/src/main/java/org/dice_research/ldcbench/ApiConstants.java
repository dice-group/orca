package org.dice_research.ldcbench;

public class ApiConstants {

    public static final String ENV_SDK_KEY = "HOBBIT_SDK";
    public static final String ENV_DOCKERIZED_KEY = "HOBBIT_SDK_DOCKERIZED";

    public static final String ENV_NODE_ID_KEY = "LDCBENCH_NODE_ID";
    public static final String ENV_NODE_URI_KEY = "LDCBENCH_NODE_URI";
    public static final String ENV_BENCHMARK_EXCHANGE_KEY = "LDCBENCH_BENCHMARK_EXCHANGE";
    public static final String ENV_DATA_QUEUE_KEY = "LDCBENCH_DATA_QUEUE";
    public static final String ENV_EVAL_DATA_QUEUE_KEY = "LDCBENCH_EVAL_DATA_QUEUE";
    public static final String ENV_SPARQL_ENDPOINT_KEY = "LDCBENCH_SPARQL_ENDPOINT";
    public static final String ENV_CRAWL_DELAY_KEY = "LDCBENCH_CRAWL_DELAY";
    public static final String ENV_DISALLOWED_RATIO_KEY = "LDCBENCH_DISALLOWED_RATIO";
    public static final String ENV_COMPRESSED_RATIO_KEY = "LDCBENCH_COMPRESSED_RATIO";
    public static final String ENV_NUMBER_OF_GRAPHS_KEY = "LDCBENCH_DATAGENERATOR_NUMBER_OF_GRAPHS";

    public static final String ENV_SEED_KEY = "LDCBENCH_DATAGENERATOR_SEED";
    public static final String ENV_COMPONENT_COUNT_KEY = "LDCBENCH_COMPONENT_COUNT";
    public static final String ENV_COMPONENT_ID_KEY = "LDCBENCH_COMPONENT_ID";

    public static final String ENV_HTTP_PORT_KEY = "HTTP_PORT";

    public static final String SPARQL_IMAGE = "openlink/virtuoso-opensource-7:7.2.6-r2-gf5e32aa";
    public static final String SPARQL_USER= "dba";
    public static final String SPARQL_PASSWORD = "123dice";

    public static final byte CRAWLING_STARTED_SIGNAL = 126;
    public static final byte CRAWLING_FINISHED_SIGNAL = 127;
    public static final byte DATAGENERATOR_READY_SIGNAL = 125;
    public static final byte NODE_START_SIGNAL = -3;
    public static final byte NODE_ACK_SIGNAL = -4;
    public static final byte NODE_INIT_SIGNAL = -1;
    public static final byte NODE_READY_SIGNAL = 124;
    public static final byte NODE_RESULTS_SIGNAL = -5;
    public static final byte NODE_URI_TEMPLATE = -2;
    
    public static final String FILE_ENDING_GRAPH=".graph";
    public static final String FILE_ENDING_HTML_TAR_GZ=".html.tar.gz";
    public static final String FILE_ENDING_TTL_TAR_GZ=".ttl.tar.gz";

}
