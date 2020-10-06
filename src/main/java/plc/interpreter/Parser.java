package plc.interpreter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    private Parser(String input) {
        tokens = new TokenStream(Lexer.lex(input));
    }

    /**
     * Parses the input and returns the AST
     */
    public static Ast parse(String input) {
        return new Parser(input).parse();
    }

    /**
     * Repeatedly parses a list of ASTs, returning the list as arguments of an
     * {@link Ast.Term} with the identifier {@code "source"}.
     */

    private Ast parse() {
        List<Ast> sourceArgs = new ArrayList<Ast>();
        while (tokens.has(0)) {
            sourceArgs.add(parseAst());
        }
        return new Ast.Term("source", sourceArgs);
    }


    //where most of the work is, and that is what is being recursively called
    private Ast parseAst() {
        if (match("(")) {
            String name = tokens.get(0).getLiteral();
            tokens.advance();
            List<Ast> args = new ArrayList<>();
            while (!match(")")) {
                if (peek(Token.Type.STRING)) {
                    args.add(new Ast.StringLiteral(tokens.get(0).getLiteral()));
                } else if (peek(Token.Type.NUMBER)) {
                    args.add(new Ast.NumberLiteral(new BigDecimal(tokens.get(0).getLiteral())));
                } else if (peek(Token.Type.IDENTIFIER)){
                    args.add(new Ast.Identifier(tokens.get(0).getLiteral()));
                } else if (peek("(")) {
                    args.add(parseAst());
                }
                tokens.advance();
                if (!tokens.has(0)) {
                    throw new ParseException("Expected closing parenthesis or comma after argument.", tokens.get(-1).getIndex());
                }
            }
            tokens.advance();
            return new Ast.Term(name, args);
        }
        throw new ParseException("Expected starting (", 0);
    }


    private boolean peek(Object... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            if (!tokens.has(i) || (!patterns[i].equals(tokens.get(i).getType()) && !patterns[i].equals(tokens.get(i).getLiteral()))) {
                return false;
            }
        }
        return true;
    }

    private boolean match(Object... patterns) {
        for (Object pattern : patterns) {
            if (!peek(pattern)) {
                return false;
            }
        }
        tokens.advance(patterns.length);
        return true;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        public boolean has(int offset) {
            return (index + offset) < tokens.size();
        }

        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        public void advance() {
            index++;
        }

        public void advance(int offset) {
            index += offset;
        }

    }

}
