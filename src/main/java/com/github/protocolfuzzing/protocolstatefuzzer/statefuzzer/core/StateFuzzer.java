package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.core;

public interface StateFuzzer {
    String LEARNED_MODEL_FILENAME = "learnedModel.dot";
    String STATISTICS_FILENAME = "statistics.txt";
    String MAPPER_CONNECTION_CONFIG_FILENAME = "mapper_connection.config";
    String ALPHABET_FILENAME_NO_EXTENSION = "alphabet";
    String ERROR_FILENAME = "error.msg";
    String LEARNING_STATE_FILENAME = "state.log";

    void startFuzzing();

}
