package plc.interpreter;

import java.util.List;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException}.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers, they're not necessary but their use will make the implementation a
 * lot easier. Regex isn't the most performant way to go but it gets the job
 * done, and the focus here is on the concept.
 */
public class Lexer {

    private final String input;
    private final CharStream chars = new CharStream();

    private Lexer(String input) {
        this.input = input;
    }

    /**
     * Lexes the input and returns the list of tokens.
     */
    public static List<Token> lex(String input) throws ParseException {
        return new Lexer(input).lex();
    }

    /**
     * Repeatedly lexes the next token using {@link #lexToken()} until the end
     * of the input is reached, returning the list of tokens lexed. This should
     * also handle skipping whitespace.
     */
    private List<Token> lex() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Lexes the next token. It may be helpful to have this call other methods,
     * such as {@code lexIdentifier()} or {@code lexNumber()}, based on the next
     * character(s).
     *
     * Additionally, here is an example of lexing a character literal (not used
     * in this assignment) using the peek/match methods below.
     *
     * <pre>
     * {@code
     *     private plc.interpreter.Token lexCharacter() {
     *         if (!match("\'")) {
     *             //your lexer should prevent this from happening, as it should
     *             //only try to lex a character literal if the next character
     *             //begins a character literal.
     *             throw new RuntimeException("Next character does not begin a character literal.");
     *         }
     *         if (!chars.has(0) || match("\'")) {
     *             throw new RuntimeException("Empty character literal.");
     *         } else if (match("\\")) {
     *             //lex escape characters...
     *         } else {
     *             chars.advance();
     *         }
     *         if (!match("\'")) {
     *             throw new RuntimeException("Unterminated character literal.");
     *         }
     *         return new plc.interpreter.Token(plc.interpreter.Token.Type.CHARACTER, chars.emit());
     *     }
     * }
     * </pre>
     */
    private Token lexToken() throws ParseException {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true for the sequence {@code 'a', 'b', 'c'}
     */
    private boolean peek(String... patterns) {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Returns true in the same way as peek, but also advances the CharStream to
     * if the characters matched.
     */
    private boolean match(String... patterns) {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * This is basically a sequence of characters. The index is used to maintain
     * where in the input string the lexer currently is, and the builder
     * accumulates characters into the literal value for the next token.
     */
    private final class CharStream {

        private int index = 0;
        private int length = 0;

        /**
         * Returns true if there is a character at index + offset.
         */
        public boolean has(int offset) {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Gets the character at index + offset.
         */
        public char get(int offset) {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Advances to the next character, incrementing the current index and
         * length of the literal being built.
         */
        public void advance() {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Resets the length to zero, skipping any consumed characters.
         */
        public void reset() {
            throw new UnsupportedOperationException(); //TODO
        }

        /**
         * Returns a token of the given type with the built literal. The index
         * of the token should be the <em>starting</em> index.
         */
        public Token emit(Token.Type type) {
            throw new UnsupportedOperationException(); //TODO
        }

    }

}
