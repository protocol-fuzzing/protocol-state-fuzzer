package com.github.protocolfuzzing.protocolstatefuzzer.components.learner.alphabet;

import com.github.protocolfuzzing.protocolstatefuzzer.components.sul.mapper.abstractsymbols.AbstractInput;
import net.automatalib.words.Alphabet;

import java.io.InputStream;
import java.io.OutputStream;

public interface AlphabetSerializer {
    Alphabet<AbstractInput> read(InputStream alphabetStream) throws AlphabetSerializerException;
    void write(OutputStream alphabetStream, Alphabet<AbstractInput> alphabet) throws AlphabetSerializerException;
    String getAlphabetFileExtension();
}
