package io.github.protocolfuzzing.protocolstatefuzzer.e2e.mealy;

import io.github.protocolfuzzing.protocolstatefuzzer.e2e.Scenario;
import io.github.protocolfuzzing.protocolstatefuzzer.e2e.ScenarioCli;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The commands of the Mealy scenarios besides {@link ScenarioCli#LEARN} and
 * {@link ScenarioCli#TEST}: the scenarios' {@value #PROBE} and
 * {@value #IDENTIFY} args files, and the commands running no endpoint, which
 * are common to all scenarios, in {@value #COMMON_ARGS}.
 */
public final class MealyCli {

    public static final String PROBE = "args/probe";
    public static final String IDENTIFY = "args/identify";

    /** The directory of the learned models of each protocol and role, and their ADGs. */
    public static final String FINGERPRINT_DIR = "fingerprint.dir";

    /** The classpath directory of the args files common to all scenarios. */
    public static final String COMMON_ARGS = MealyScenarioRun.SCENARIOS + "/common-args";

    private MealyCli() {}

    /**
     * Builds a {@code diff-test} command.
     *
     * @param  firstModel   the first model
     * @param  firstName    the name of the first model
     * @param  secondModel  the second model
     * @param  secondName   the name of the second model
     * @param  alphabetFile the alphabet of both models
     *
     * @return              the command-line arguments
     */
    public static String[] diffTest(Path firstModel, String firstName, Path secondModel, String secondName,
        Path alphabetFile) {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("model.a", firstModel.toString());
        properties.put("name.a", firstName);
        properties.put("model.b", secondModel.toString());
        properties.put("name.b", secondName);
        properties.put("alphabet", alphabetFile.toString());
        return ScenarioCli.command(commonArgs("diff-test"), properties);
    }

    /**
     * Builds a {@code fingerprint} command.
     *
     * @param  modelsDir the directory with one subdirectory per model
     * @param  adgFile   the file to write the ADG to
     *
     * @return           the command-line arguments
     */
    public static String[] fingerprint(Path modelsDir, Path adgFile) {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("models.dir", modelsDir.toString());
        properties.put("adg.file", adgFile.toString());
        return ScenarioCli.command(commonArgs("fingerprint"), properties);
    }

    private static Path commonArgs(String name) {
        return Scenario.classpathDir(COMMON_ARGS).resolve(name);
    }
}
