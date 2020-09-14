package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Standard JUnit5 parameterized tests. See the RegexTests file from part 1 for
 * more information.
 */
final class LexerTests {

    @ParameterizedTest
    @MethodSource
    void testIdentifier(String input, boolean success) {
        test(input, Token.Type.IDENTIFIER, success);
    }

    private static Stream<Arguments> testIdentifier() {
        return Stream.of(
                Arguments.of("getName", true),
                Arguments.of("is-empty?", true),
                Arguments.of("<=>", true),
                Arguments.of("42=life", false),
                Arguments.of("why,are,there,commas,", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testNumber(String input, boolean success) {
        test(input, Token.Type.NUMBER, success);
    }

    private static Stream<Arguments> testNumber() {
        return Stream.of(
                Arguments.of("1", true),
                Arguments.of("-1.0", true),
                Arguments.of("007.000", true),
                Arguments.of("1.", false),
                Arguments.of(".5", false),
                Arguments.of("+-10", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testString(String input, boolean success) {
        test(input, Token.Type.STRING, success);
    }

    private static Stream<Arguments> testString() {
        return Stream.of(
                Arguments.of("\"\"", true),
                Arguments.of("\"abc\"", true),
                Arguments.of("\"Hello,\nWorld\"", true),
                Arguments.of("\"unterminated", false),
                Arguments.of("\"invalid escape \\uXYZ\"", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testOperator(String input, boolean success) {
        test(input, Token.Type.OPERATOR, success);
    }

    private static Stream<Arguments> testOperator() {
        return Stream.of(
                Arguments.of("(", true),
                Arguments.of("#", true),
                Arguments.of(" ", false),
                Arguments.of("\t", false)
        );
    }

    @Test
    void testExample1() {
        String input = "(+ 1 -2.0)";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "(", 0),
                new Token(Token.Type.IDENTIFIER, "+", 1),
                new Token(Token.Type.NUMBER, "1", 3),
                new Token(Token.Type.NUMBER, "-2.0", 5),
                new Token(Token.Type.OPERATOR, ")", 9)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testExample2() {
        String input = "(print \"Hello, World!\")";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "(", 0),
                new Token(Token.Type.IDENTIFIER, "print", 1),
                new Token(Token.Type.STRING, "\"Hello, World!\"", 7),
                new Token(Token.Type.OPERATOR, ")", 22)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testExample3() {
        String input = "(let [x 10] (assert-equals? x 10))";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "(", 0),
                new Token(Token.Type.IDENTIFIER, "let", 1),
                new Token(Token.Type.OPERATOR, "[", 5),
                new Token(Token.Type.IDENTIFIER, "x", 6),
                new Token(Token.Type.NUMBER, "10", 8),
                new Token(Token.Type.OPERATOR, "]", 10),
                new Token(Token.Type.OPERATOR, "(", 12),
                new Token(Token.Type.IDENTIFIER, "assert-equals?", 13),
                new Token(Token.Type.IDENTIFIER, "x", 28),
                new Token(Token.Type.NUMBER, "10", 30),
                new Token(Token.Type.OPERATOR, ")", 32),
                new Token(Token.Type.OPERATOR, ")", 33)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    /**
     * Tests that the input lexes to the (single) expected token if successful,
     * else throws a {@link ParseException} otherwise.
     */
    private static void test(String input, Token.Type expected, boolean success) {
        if (success) {
            Assertions.assertIterableEquals(Arrays.asList(new Token(expected, input, 0)), Lexer.lex(input));
        } else {
            Assertions.assertThrows(ParseException.class, () -> {
                List<Token> tokens = Lexer.lex(input);
                if (tokens.size() != 1) {
                    throw new ParseException("Expected a single token.", 0);
                }
            });
        }
    }

}
