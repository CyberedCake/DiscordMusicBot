package net.cybercake.discordmusicbot.utilities;

public class StringUtils {

    /**
     * [A-Za-z0-9 ]
     * (space included)
     */
    public static String removeNonAlphanumericSpaceCharacters(String string) {
        char[] doctoredString = string.toCharArray();
        for(int index = 0; index < string.length(); index++) {
            int character = doctoredString[index];
            if (Character.isSpaceChar(character) || Character.isDigit(character) || Character.isLetter(character))
                continue;
            doctoredString[index] = Character.MIN_VALUE;
        }
        return new String(doctoredString);
    }

}
