package com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.commandlistener.core;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.AbstractSul;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.core.SulWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractOutput;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.commandlistener.core.config.CommandListenerConfig;
import com.github.protocolfuzzing.protocolstatefuzzer.entrypoints.commandlistener.core.config.CommandListenerEnabler;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.CleanupTasks;
import de.learnlib.sul.SUL;
import net.automatalib.alphabet.Alphabet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class CommandListener {
    private static final Logger LOGGER = LogManager.getLogger(CommandListener.class);

    private CleanupTasks cleanup;
    private SUL<AbstractInput, AbstractOutput> sul;
    private Map<String, AbstractInput> nameInputMap;
    private ServerSocket serverSocket;
    private SocketWrapper wrapper;
    private CommandListenerConfig config;

    private static final String CMD_RESET = "reset";
    private static final String CMD_EXIT = "exit";
    private static final String CMD_QUERY = "query";
    private static final String RESET_OK = "resetok";

    public CommandListener(CommandListenerEnabler enabler, AlphabetBuilder alphabetBuilder,
            SulBuilder sulBuilder, SulWrapper sulWrapper) {
        config = enabler.getCommandListenerConfig();
        cleanup = new CleanupTasks();
        AbstractSul abstractSul = sulBuilder.build(enabler.getSulConfig(), cleanup);
        sul = sulWrapper.wrap(abstractSul).getWrappedSul();
        Alphabet<AbstractInput> alphabet = alphabetBuilder.build(enabler.getLearnerConfig());
        nameInputMap = new LinkedHashMap<>();
        alphabet.forEach(i -> nameInputMap.put(i.getName(), i));

        cleanup.submit(new Runnable() {
            @Override
            public void run() {
                if (wrapper != null) {
                    wrapper.close();
                }
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failure closing server socket", e);
                }
            }
        });
    }

    public void run() {
        try {
            try {
                serverSocket = new ServerSocket(config.getListenerPort());
                serverSocket.setReuseAddress(true);
                serverSocket.setSoTimeout(config.getTimeout());
            } catch (IOException e) {
                throw new RuntimeException("Could not create server socket", e);
            }
            doRun();
        } catch (IOException e) {
            throw new RuntimeException("Exception encountered", e);
        }
    }

    private void doRun() throws IOException {
        Socket socket = newSocket();
        InputCommand inputCmd;
        State prevState = null;
        wrapper = new SocketWrapper(socket);
        State state = State.SUT_OFF;
        while (state != State.TERMINATE) {
            prevState = state;
            LOGGER.info("Current state: {}", state.name());
            Command command = null;
            if (state != State.CONN_CLOSED) {
                LOGGER.info("Listening for commands");
                command = readCommand();
                if (command == null) {
                    state = State.CONN_CLOSED;
                }
            }
            switch(state) {
            case SUT_OFF:
                switch(command.name) {
                case CMD_RESET:
                    wrapper.writeOutput(RESET_OK);
                    state = State.SUT_OFF;
                    break;
                case CMD_EXIT:
                    state = State.TERMINATE;
                    break;
                case CMD_QUERY:
                    state = State.READ_QUERY;
                    break;
                default:
                    inputCmd = parseAsInputCommand(command);
                    sul.pre();
                    executeInputCommand(inputCmd);
                    state = State.SUT_ON;
                    break;
                }
                break;

            case SUT_ON:
                switch(command.name) {
                case CMD_RESET:
                    sul.post();
                    wrapper.writeOutput(RESET_OK);
                    state = State.SUT_OFF;
                    break;
                case CMD_EXIT:
                    sul.post();
                    state = State.TERMINATE;
                    break;
                case CMD_QUERY:
                    sul.post();
                    state = State.READ_QUERY;
                    break;
                default:
                    inputCmd = parseAsInputCommand(command);
                    executeInputCommand(inputCmd);
                    state = State.SUT_ON;
                    break;
                }
                break;

            case READ_QUERY:
                List<InputCommand> commands = new ArrayList<>();
                while (command != null && command.name.length() > 0) {
                    InputCommand input = parseAsInputCommand(command);
                    commands.add(input);
                    command = readCommand();
                }

                sul.pre();
                for (InputCommand inputCommand : commands) {
                    executeInputCommand(inputCommand);
                }
                sul.post();
                state = State.SUT_OFF;
                break;

            case CONN_CLOSED:
                if (prevState == State.SUT_ON) {
                    sul.post();
                }
                wrapper.close();
                if (config.isContinuous()) {
                    socket = newSocket();
                    wrapper = new SocketWrapper(socket);
                    state = State.SUT_OFF;
                } else {
                    state = State.TERMINATE;
                }
                break;

            case TERMINATE:
                break;
            }
        }

        terminate();
    }

    private Socket newSocket() {
        try {
            LOGGER.info("Listening for connections at address {}", serverSocket.getLocalSocketAddress());
            Socket socket = serverSocket.accept();
            return socket;
        } catch (SocketTimeoutException  e) {
            LOGGER.info("Listener socket timed out");
            terminate();
        } catch (IOException e) {
            LOGGER.error("Unknown exception: {}", e.getMessage());
            terminate();
        }
        return null;
    }

    private void terminate() {
        LOGGER.info("Terminating...");
        cleanup.execute();
    }

    private Command readCommand() {
        String inputString = wrapper.readInput();
        if (inputString != null)
            return parseCommand(inputString);
        return null;
    }

    private void executeInputCommand(InputCommand command) {
        AbstractOutput output = sul.step(command.input);
        writeOutput(output);
    }

    private InputCommand parseAsInputCommand(Command command) {
        AbstractInput input = nameInputMap.get(command.name);
        if (input == null) {
            LOGGER.error("Could not extract the input symbol from the command: {} ", command.name);
            terminate();
        }
        return new InputCommand(input);
    }

    private Command parseCommand(String inputString) {
        if (inputString.length() == 0) {
            return null;
        }
        return new Command(inputString);
    }

    private void writeOutput(AbstractOutput output) {
        wrapper.writeOutput(output.toString());
    }

    static class InputCommand {
        private final AbstractInput input;
        public InputCommand(AbstractInput input) {
            this.input = input;
        }
    }

    static class Command {
        private final String name;
        public Command(String name) {
            this.name = name;
        }
    }

    static enum State {
        SUT_OFF,
        SUT_ON,
        READ_QUERY,
        CONN_CLOSED,
        TERMINATE
    }
}
