package plc.interpreter;

import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

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
public final class Lexer {

    final CharStream chars;

    Lexer(String input) {
        chars = new CharStream(input);
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
    List<Token> lex() throws ParseException {
        List<Token> tokens = new ArrayList<Token>();
        while (chars.has(0)) {
            if (!peek("\\S")) {
                chars.advance();
                chars.reset();
            } else {
                tokens.add(lexToken());
            }
        }
        return tokens;
    }

    Token lexToken() throws ParseException {
        if (peek("\"")) {
            return lexString();
        } else if (peek("[+-]","[0-9]") | peek("[0-9]")) {
            return lexNumber();
        } else if (peek("[A-Za-z_+\\-*/:!?<>=]")){
            return lexIdentifier();
        } else {
            chars.advance();
            return chars.emit(Token.Type.OPERATOR);
        }
    }

    Token lexIdentifier() {
        if (!match("[A-Za-z_+\\-*/:!?<>=]")) {
            throw new ParseException("Identifier does not begin with identifier character.", chars.index);
        }
        while (peek("[A-Za-z0-9_+\\-*/.:!?<>=]")) {
            if (match("\\.")) {
                if (!peek("[A-Za-z0-9_+\\-*/.:!?<>=]")) {
                    throw new ParseException("Trailing period in identifier.", chars.index);
                }
            } else if (!match("[A-Za-z0-9_+\\-*/.:!?<>=]")) {
                throw new ParseException("Identifier contains invalid character.", chars.index);
            }
        }
        return chars.emit(Token.Type.IDENTIFIER);
    }

    Token lexNumber() {
        boolean decimalLexed = false;
        match("[+-]");
        while (peek("[0-9]|\\.")) {
            if (match("\\.")) {
                if (decimalLexed) {
                    throw new ParseException("Multiple decimal points in number.", chars.index);
                }
                decimalLexed = true;
                if (!peek("[0-9]")) {
                    throw new ParseException("Trailing decimal point in number.", chars.index);
                }
            } else if (!match("[0-9]")) {
                throw new ParseException("Number contains non-number.", chars.index);
            }
        }
        return chars.emit(Token.Type.NUMBER);
    }

    Token lexString() throws ParseException {
        if (!match("\"")) {
            throw new ParseException("String does not begin with a string literal.", chars.index);
        }
        while (!peek("\"") & chars.has(0)) {
            if (match("\\\\")) {
                if (!match("[bnrt\'\"\\\\]")) {
                    throw new ParseException("Invalid escaped character in string literal.", chars.index);
                }
            }
            else {
                chars.advance();
            }
        }
        if (!match("\"")) {
            throw new ParseException("Unterminated string literal.", chars.index);
        }
        return chars.emit(Token.Type.STRING);
    }

    boolean peek(String... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (!chars.has(i)) return false;
            Pattern pattern = Pattern.compile(patterns[i]);
            Matcher matcher = pattern.matcher(Character.toString(chars.get(i)));
            if (!matcher.matches()) return false;
        }
        return true;
    }

    boolean match(String... patterns) {
        for (String pattern : patterns) {
            if (!peek(pattern)) {
                return false;
            }
        }
        chars.advance(patterns.length);
        return true;
    }

    static final class CharStream {

        final String input;
        int index = 0;
        int length = 0;

        CharStream(String input) {
            this.input = input;
        }

        boolean has(int offset) {
            return input.length() > index + offset;
        }

        char get(int offset) {
            return input.charAt(index + offset);
        }

        void advance() {
            index++;
            length++;
        }

        void advance(int offset) {
            index += offset;
            length += offset;
        }

        void reset() {
            length = 0;
        }

        Token emit(Token.Type type) {
            Token token = new Token(type, input.substring(index - length, index), index - length);
            reset();
            return token;
        }
    }
}
