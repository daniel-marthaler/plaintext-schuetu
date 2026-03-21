package ch.plaintext.schuetu.service.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * ID Generator
 */
@Slf4j
public class IDGeneratorContainer {

    private static int pointer1 = -0;
    private static int pointer2 = -1;

    private static String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public static synchronized String getNext() {

        IDGeneratorContainer.pointer2++;

        if (IDGeneratorContainer.pointer2 >= IDGeneratorContainer.letters.length) {
            IDGeneratorContainer.pointer1++;
            IDGeneratorContainer.pointer2 = 0;
        }

        if (IDGeneratorContainer.pointer1 >= letters.length) {
            IDGeneratorContainer.pointer1 = 0;
            log.warn("!!! achtung, id's fangen wieder von vorne an, limite wurde erreicht");
        }

        return IDGeneratorContainer.letters[IDGeneratorContainer.pointer1] + IDGeneratorContainer.letters[IDGeneratorContainer.pointer2];

    }

}
