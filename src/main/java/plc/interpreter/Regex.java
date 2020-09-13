package plc.interpreter;

import java.util.regex.Pattern;

/**
 * Contains {@link Pattern} constants, which are compiled regular expressions.
 * See the assignment page for resources on regex's as needed.
 */
public class Regex {

    public static final Pattern
            EMAIL = Pattern.compile("[A-Za-z0-9._-]+@[A-Za-z0-9-]*\\.[a-z]{2,3}"),
            FILE_NAMES = Pattern.compile("(?<name>[^\\.]*).*(\\.java|\\.class)"),
            EVEN_STRINGS = Pattern.compile(""),
            INTEGER_LIST = Pattern.compile(""),
            IDENTIFIER = Pattern.compile("[^0-9][A-Za-z0-9_\\+\\-\\*/:\\.!?<>=]*[A-Za-z\\+\\-\\*/:.!?<>=]+[A-Za-z0-9\\+\\-\\*/:.!?<>=]*"),
            NUMBER = Pattern.compile("[\\+\\-]?([0-9]|[0-9]+\\.?[0-9]+)"),
            STRING = Pattern.compile("\"[^\\\\\"]*(\\\\[bnrt'\"\\\\][^\\\\\"]*)*\"");

}
