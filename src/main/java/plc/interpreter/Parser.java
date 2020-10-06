package plc.interpreter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Parser {

    private final TokenStream tokens;

    private Parser(String input) {
        tokens = new TokenStream(Lexer.lex(input));
    }

    public static Ast parse(String input) {
        return new Parser(input).parse();
    }

    private Ast parse() {
        List<Ast> sourceArgs = new ArrayList<Ast>();
        while (tokens.has(0)) {
            sourceArgs.add(parseAst());
        }
        return new Ast.Term("source", sourceArgs);
    }

    //where most of the work is, and that is what is being recursively called
    private Ast parseAst() {
        if (match("(") || match("[")) {
            String name = tokens.get(0).getLiteral();
            tokens.advance();
            List<Ast> args = new ArrayList<>();
            while (!match(")") && !match("]")) {
                if (peek(Token.Type.STRING)) {
                    String tempLiteral = tokens.get(0).getLiteral();
                    tempLiteral = tempLiteral.replaceAll("\\\\b", "\b");
                    tempLiteral = tempLiteral.replaceAll("\\\\n", "\n");
                    tempLiteral = tempLiteral.replaceAll("\\\\r", "\r");
                    tempLiteral = tempLiteral.replaceAll("\\\\t", "\t");
                    tempLiteral = tempLiteral.replaceAll("\\\\'", "\'");
                    tempLiteral = tempLiteral.replaceAll("\\\\\"", "\"");
                    tempLiteral = tempLiteral.replaceAll("\\\\\\\\", "\\");
                    args.add(new Ast.StringLiteral(tempLiteral));
                    tokens.advance();
                } else if (peek(Token.Type.NUMBER)) {
                    args.add(new Ast.NumberLiteral(new BigDecimal(tokens.get(0).getLiteral())));
                    tokens.advance();
                } else if (peek(Token.Type.IDENTIFIER)){
                    args.add(new Ast.Identifier(tokens.get(0).getLiteral()));
                    tokens.advance();
                } else if (peek("(") || peek("[")) {
                    args.add(parseAst());
                } else {
                    throw new ParseException("Invalid Character", 0);
                }
                if (!tokens.has(0)) {
                    throw new ParseException("Expected closing parenthesis or comma after argument.", tokens.get(-1).getIndex());
                }
            }
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
