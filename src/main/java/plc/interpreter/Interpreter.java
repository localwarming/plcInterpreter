package plc.interpreter;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Interpreter {

    /**
     * The VOID constant represents a value that has no useful information. It
     * is used as the return value for functions which only perform side
     * effects, such as print, similar to Java.
     */
    public static final Object VOID = new Function<List<Ast>, Object>() {

        @Override
        public Object apply(List<Ast> args) {
            return VOID;
        }

    };

    public final PrintWriter out;
    public Scope scope;

    public Interpreter(PrintWriter out, Scope scope) {
        this.out = out;
        this.scope = scope;
        init();
    }

    /**
     * Delegates evaluation to the method for the specific instance of AST. This
     * is another approach to implementing the visitor pattern.
     */
    public Object eval(Ast ast) {
        if (ast instanceof Ast.Term) {
            return eval((Ast.Term) ast);
        } else if (ast instanceof Ast.Identifier) {
            return eval((Ast.Identifier) ast);
        } else if (ast instanceof Ast.NumberLiteral) {
            return eval((Ast.NumberLiteral) ast);
        } else if (ast instanceof Ast.StringLiteral) {
            return eval((Ast.StringLiteral) ast);
        } else {
            throw new AssertionError(ast.getClass());
        }
    }

    /**
     * Evaluations the Term ast, which returns the value resulting by calling
     * the function stored under the term's name in the current scope. You will
     * need to check that the type of the value is a {@link Function}, and cast
     * to the type {@code Function<List<Ast>, Object>}.
     */
    private Object eval(Ast.Term ast) {
        return requireType(Function.class, scope.lookup(ast.getName())).apply(ast.getArgs());
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Evaluates the Identifier ast, which returns the value stored under the
     * identifier's name in the current scope.
     */
    private Object eval(Ast.Identifier ast) {
        return scope.lookup(ast.getName());
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Evaluates the NumberLiteral ast, which returns the stored number value.
     */
    private BigDecimal eval(Ast.NumberLiteral ast) {
        return ast.getValue();
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Evaluates the StringLiteral ast, which returns the stored string value.
     */
    private String eval(Ast.StringLiteral ast) {
        return ast.getValue();
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Initializes the interpreter with fields and functions in the standard
     * library.
     */
    private void init() {
        scope.define("print", (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            evaluated.forEach(out::print);
            out.println();
            return VOID;
        });
        //TODO: Additional standard library functions

        //Math Functions

        //ADDITION
        scope.define("+",  (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream().map(a -> requireType(BigDecimal.class, eval(a))).collect(Collectors.toList());
            if(evaluated.isEmpty()){
                return BigDecimal.valueOf(0);
            }
            else {
                BigDecimal num = evaluated.get(0);
                for (int i = 1; i < evaluated.size(); i++) {
                    num = num.add(evaluated.get(i));
                }
                return num;
            }
        });

        //SUBTRACTION
        scope.define("-",  (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream().map(a -> requireType(BigDecimal.class, eval(a))).collect(Collectors.toList());
            if(evaluated.isEmpty()){
                throw new EvalException("Argumants to - cannot be empty");
            }
            else if(evaluated.size() == 1) {
                return evaluated.get(0).negate();
            }
            else{
                BigDecimal num = evaluated.get(0);
                for(int i = 1; i < evaluated.size(); i++) {
                    num = num.subtract(evaluated.get(i));
                }
                return num;
            }
        });

        //MULTIPLICATION
        scope.define("*",  (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream().map(a -> requireType(BigDecimal.class, eval(a))).collect(Collectors.toList());
            if(evaluated.isEmpty()){
                return BigDecimal.valueOf(1);
            }
            else {
                BigDecimal num = evaluated.get(0);
                for (int i = 1; i < evaluated.size(); i++) {
                    num = num.multiply(evaluated.get(i));
                }
                return num;
            }
        });

        //DIVISION
        scope.define("/",  (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> evaluated = args.stream().map(a -> requireType(BigDecimal.class, eval(a))).collect(Collectors.toList());
            if(evaluated.isEmpty()){
                throw new ArithmeticException("Divisor cannot be 0");
            }
            else if(evaluated.size() == 1) {
                //get inverse
                BigDecimal one = BigDecimal.valueOf(1);
                return one.divide(evaluated.get(0), RoundingMode.HALF_EVEN);
            }
            else{
                BigDecimal num = evaluated.get(0);
                for(int i = 1; i < evaluated.size(); i++) {
                    num = num.divide(evaluated.get(i), RoundingMode.HALF_EVEN);
                }
                return num;
            }
        });
    }

    /**
     * A helper function for type checking, taking in a type and an object and
     * throws an exception if the object does not have the required type.
     *
     * This function does a poor job of actually identifying where the issue
     * occurs - in a real interpreter, we would have a stacktrace to provide
     * that implementation. For now, this is the simple-but-not-ideal solution.
     */
    private static <T> T requireType(Class<T> type, Object value) {
        if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            throw new EvalException("Expected " + value + " to have type " + type.getSimpleName() + ".");
        }
    }

}
