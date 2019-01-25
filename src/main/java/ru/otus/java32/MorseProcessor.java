package ru.otus.java32;//import jdk.internal.jline.internal.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class MorseProcessor {
    private static final String MORSECODES_CSV = "morsecodes.csv";
    private Map<Integer, String> morseCodes;

    private static Logger logger = Logger.getLogger(MorseProcessor.class.getSimpleName());

    MorseProcessor() {
        try {
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(MORSECODES_CSV)) {
            Stream<String> lines = (new BufferedReader(new InputStreamReader(inputStream))).lines();
            morseCodes = lines
                    .map(line -> line.split(","))
                    .collect(Collectors.toMap(pair -> (int) Character.toUpperCase(pair[0].charAt(0)), pair -> pair[1]));
        }
    }

    Map<Integer, String> getMorseCodes() {
        return Collections.unmodifiableMap(morseCodes);
    }

    String textToMorse(String text) {
        List<String> codes = text
                .toUpperCase()
                .chars()
                .mapToObj((c) -> {
                    String code = morseCodes.get(c);
                    return code != null ? code : " ";
                })
                .collect(Collectors.toList());
        // remove repeated spaces and return
        return IntStream
                .range(0, codes.size())
                .filter(i -> (
                                (i > 0 && i < codes.size() - 1
                                        && !(codes.get(i + 1).equals(" ") && codes.get(i).equals(" ")))
                                        || (i == codes.size() - 1) && !codes.get(i).equals(" ")
                                        || (i == 0) && !codes.get(i).equals(" ")
                        )
                )
                .mapToObj(codes::get).collect(Collectors.joining("|"));
    }
}
