package com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.fingerprint.core.io;

import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet.AlphabetBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.components.learner.statistics.MealyMachineWrapper;
import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.OutputBuilder;
import com.github.protocolfuzzing.protocolstatefuzzer.statefuzzer.sulidentifier.core.IdentifierAlphabetStore;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.MealyIOProcessor;
import com.github.protocolfuzzing.protocolstatefuzzer.utils.ModelFactory;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.automaton.transducer.MealyMachine;
import net.automatalib.exception.FormatException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class for Parsing model files for fingerprint
 *
 * @param <I> the input type of the models to be parsed
 */
public class FingerprintParser<I> {
    private static final Logger LOGGER = LogManager.getLogger();

    /** The alphabet builder */
    protected AlphabetBuilder<I> alphabetBuilder;

    /** The string output builder */
    private final OutputBuilder<String> stringOutputBuilder;

    /**
     * Create a new instanse of a Fingerprint Parser with the given alhpabet
     * builder and a custom sting output builder
     *
     * @param alphabetBuilder the alphabet builder
     */
    public FingerprintParser(AlphabetBuilder<I> alphabetBuilder) {
        this.stringOutputBuilder = new OutputBuilder<String>() {
            @Override
            public String buildOutputExact(String name) {
                return name;
            }
        };

        this.alphabetBuilder = alphabetBuilder;
    }

    /**
     * Returns a list of all the Mealy Machines, with their alphabets, that were contained in the
     * specified directory
     *
     * @param  dir         a directory with Mealy Machine models. Models should be saved in subfolders
     *                         with their designated names. Each subfolder should contain a "learnedModel.dot" and
     *                         "alphabet.xml" file
     * @param  modelNames  output parameter to store the names of the models as written in the subdirectories
     *
     * @return             the list of all constructed Mealy Machines with their alphabets as strings
     *                         {@link MealyMachineWrapper}
     *
     * @throws IOException if cannot create a subfolder stream to sort them
     */
    public List<MealyMachineWrapper<String, String>> loadDirectory(String dir, List<String> modelNames)
        throws IOException {
        Path dirPath = Paths.get(dir);
        List<Path> subfolders = sortedSubfolders(dirPath);
        List<MealyMachineWrapper<String, String>> result = new ArrayList<>();
        for (Path subfolder: subfolders) {
            if (subfolder == null || !new File(subfolder.toString()).exists())
                continue;
            Path model = subfolder.resolve("learnedModel.dot");
            Path alphabet = subfolder.resolve("alphabet.xml");

            Alphabet<String> tempAlphabet;

            Path fileNamePath = subfolder.getFileName();
            String subfolderName = fileNamePath != null
                ? fileNamePath.toString()
                : subfolder.toString();

            if (new File(model.toString()).exists()) {
                try {
                    if (new File(alphabet.toString()).exists()) {
                        tempAlphabet = alphabetBuilder.toStringAlphabet(
                            alphabetBuilder.build(new IdentifierAlphabetStore(alphabet.toString())));
                    } else {
                        LOGGER.info("Folder {} does not contain an alphabet, default alphabet will be used",
                            subfolderName);
                        tempAlphabet = alphabetBuilder.toStringAlphabet(
                            alphabetBuilder.build(new IdentifierAlphabetStore(null)));
                    }

                    MealyIOProcessor<String, String> processor = new MealyIOProcessor<>(tempAlphabet,
                        stringOutputBuilder);
                    MealyMachine<?, String, ?, String> hyp = loadModel(model.toString(), processor);
                    result.add(new MealyMachineWrapper<>(hyp, tempAlphabet));

                    modelNames.add(subfolderName);
                }
                catch (IOException | FormatException e) {
                    LOGGER.error("Failed to load model {}, error was {}", subfolderName,
                        e.getMessage());
                }
                catch (Exception e) {
                    LOGGER.error("Error while loading model {}, error: {}", subfolderName,
                        e.getMessage());
                }

            } else {
                LOGGER.info("Folder {} does not contain a model, moving to next", subfolderName);

            }

        }

        return result;

    }

    /**
     * Returns the subfolders of a directory sorted by name
     *
     * @param  dir         the directory
     *
     * @return             the list of subdirectory paths, sorted by name
     *
     * @throws IOException if cannot create a directory stream
     */
    private static List<Path> sortedSubfolders(Path dir) throws IOException {
        List<Path> subfolders = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
            for (Path p: ds) {
                if (Files.isDirectory(p))
                    subfolders.add(p);
            }
        }
        subfolders.sort(Comparator.comparing(p -> p.getFileName().toString()));
        return subfolders;
    }

    /**
     * Loads a Mealy machine model from the given DOT file path.
     *
     * @param  <S>             the state type of the loaded model
     * @param  path            the path to the DOT file
     * @param  processor       the processor for inputs and outputs
     *
     * @return                 the loaded Mealy machine model
     *
     * @throws IOException     if an error occurs while reading the DOT file
     * @throws FormatException if the DOT file has an invalid format
     */
    private <S> MealyMachine<S, String, ?, String> loadModel(String path, MealyIOProcessor<String, String> processor)
        throws IOException, FormatException {
        @SuppressWarnings("unchecked")
        MealyMachine<S, String, ?, String> model = (MealyMachine<S, String, ?, String>) ModelFactory
            .buildProtocolModel(path, processor);
        return model;
    }

}
