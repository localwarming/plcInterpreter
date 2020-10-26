package plc.interpreter;

import javax.jws.Oneway;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                throw new EvalException("Arguments to - cannot be empty");
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
            } else if(evaluated.size() == 1) {
                //get inverse
                BigDecimal one = BigDecimal.valueOf(1);
                return one.divide(evaluated.get(0), RoundingMode.HALF_EVEN);
            } else{
                BigDecimal num = evaluated.get(0);
                for(int i = 1; i < evaluated.size(); i++) {
                    num = num.divide(evaluated.get(i), RoundingMode.HALF_EVEN);
                }
                return num;
            }
        });

        //IF STATEMENT EXAMPLE
        scope.define("if",  (Function<List<Ast>, Object>) args -> {
            if (args.size() != 3) {
                throw new EvalException("if requires 3 args");
            }
            if (requireType(Boolean.class, eval(args.get(0)))) {
                return eval(args.get(1));
            }
            else{
                return eval(args.get(2));
            }
        });

        //Comparison & Equality Functions

        //TRUE
        scope.define("true", true);

        //FALSE
        scope.define("false", false);

        //EQUALS?
        scope.define("equals?",  (Function<List<Ast>, Object>) args -> {
            List<Object> evaluated = args.stream().map(this::eval).collect(Collectors.toList());
            if (evaluated.size() != 2)
                throw new EvalException("equals compares 2 args");
            return Objects.deepEquals(evaluated.get(0), evaluated.get(1));
        });

        //NOT
        scope.define("not",  (Function<List<Ast>, Object>) args -> {
            if (args.size() != 1)
                throw new EvalException("not requires a single arg");
            return !requireType(Boolean.class, eval(args.get(0)));
        });

        //AND
        scope.define("and",  (Function<List<Ast>, Object>) args -> {
            List<Boolean> argList = args.stream().map(a -> requireType(Boolean.class, eval(a))).collect(Collectors.toList());
            for(Boolean arg : argList)
                if (!arg) return false;
            return true;
        });

        //OR
        scope.define("or",  (Function<List<Ast>, Object>) args -> {
            List<Boolean> argList = args.stream().map(a -> requireType(Boolean.class, eval(a))).collect(Collectors.toList());
            for(Boolean arg : argList)
                if (arg) return true;
            return false;
        });

        //<, <=, >, >=

        scope.define("<", (Function<List<Ast>, Object>) args -> {
            List<Object> argList = args.stream().map(a -> eval(a)).collect(Collectors.toList());
            for (int i = 0; i < argList.size() - 1; i++) {
                if (argList.get(i+1).getClass() != argList.get(i+1).getClass()) return false;
                if (((Comparable<Object>) argList.get(i)).compareTo(argList.get(i+1)) >= 0) return false;
            }
            return true;
        });

        scope.define("<=", (Function<List<Ast>, Object>) args -> {
            List<Object> argList = args.stream().map(a -> eval(a)).collect(Collectors.toList());
            for (int i = 0; i < argList.size() - 1; i++) {
                if (argList.get(i+1).getClass() != argList.get(i+1).getClass()) return false;
                if (((Comparable<Object>) argList.get(i)).compareTo(argList.get(i+1)) > 0) return false;
            }
            return true;
        });

        scope.define(">", (Function<List<Ast>, Object>) args -> {
            List<Object> argList = args.stream().map(a -> eval(a)).collect(Collectors.toList());
            for (int i = 0; i < argList.size() - 1; i++) {
                if (argList.get(i+1).getClass() != argList.get(i+1).getClass()) return false;
                if (((Comparable<Object>) argList.get(i)).compareTo(argList.get(i+1)) <= 0) return false;
            }
            return true;
        });

        scope.define(">=", (Function<List<Ast>, Object>) args -> {
            List<Object> argList = args.stream().map(a -> eval(a)).collect(Collectors.toList());
            for (int i = 0; i < argList.size() - 1; i++) {
                if (argList.get(i+1).getClass() != argList.get(i+1).getClass()) return false;
                if (((Comparable<Object>) argList.get(i)).compareTo(argList.get(i+1)) < 0) return false;
            }
            return true;
        });

        //Sequence Functions


        //LIST
        scope.define("list",  (Function<List<Ast>, Object>) args ->
                new LinkedList(args.stream().map(a ->eval(a)).collect(Collectors.toList())));

        //RANGE
        scope.define("range",  (Function<List<Ast>, Object>) args -> {
            List<BigDecimal> argList = args.stream().map(a -> requireType(BigDecimal.class, eval(a))).collect(Collectors.toList());

            if (argList.size() != 2)
                throw new EvalException("range requires 2 arguments, " + argList.size() + " provided");
            if (argList.get(0).remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0 ||
                    argList.get(1).remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0)
                throw new EvalException("range requires integer arguments");
            if (argList.get(0).intValue() > argList.get(1).intValue())
                throw new EvalException("second range arg must be greater than first");

            IntStream stream = IntStream.range(argList.get(0).intValue(), argList.get(1).intValue());
            return stream.boxed().collect(Collectors.toCollection(LinkedList::new));
        });

        //DEFINE
        scope.define("define",  (Function<List<Ast>, Object>) args -> {
            if (args.size() != 2) throw new EvalException("define requires 2 arguments");
            if (eval(args.get(0)).getClass() == Ast.Identifier.class) {
                scope.define(requireType(Ast.Identifier.class, args.get(0)).getName(), eval(args.get(0)));
            } else if (eval(args.get(0)).getClass() == Ast.Term.class) {
                Ast.Term term = requireType(Ast.Term.class, eval(args.get(0)));
                List funcArgs = term.getArgs();
                scope.define(term.getName(),  (Function<List<Ast>, Object>) callingArgs -> {
                    scope = new Scope(scope);
                    if (funcArgs.size() != callingArgs.size())
                        throw new EvalException("argument count does not match definition");
                    for (int i = 0; i < funcArgs.size(); i++)
                        scope.define(requireType(Ast.Identifier.class, funcArgs.get(i)).getName(), eval(callingArgs.get(i)));
                    eval(args.get(1));
                    scope = scope.getParent();
                    return VOID;
                });
            } else {
                throw new EvalException("define has incorrect argument type");
            }
            return VOID;
        });


        //SET
        scope.define("set",  (Function<List<Ast>, Object>) args -> {
            if (args.size() != 2) throw new EvalException("set requires 2 arguments");
            scope.set(requireType(Ast.Identifier.class, args.get(0)).getName(), eval(args.get(0)));
            return VOID;
        });


        //State Functions

        //DO
        scope.define("do",  (Function<List<Ast>, Object>) args -> {
            if (args.isEmpty()) return VOID;
            Scope subScope = new Scope(scope);
            scope = subScope;
            Object lastArg = null;
            for (Ast arg : args) lastArg = eval(arg);
            scope = scope.getParent();
            return lastArg;
        });

        //WHILE
        scope.define("while",  (Function<List<Ast>, Object>) args -> {
            if (args.size() != 2)
                throw new EvalException("while requires 2 arguments");
            scope = new Scope(scope);
            while (requireType(Boolean.class, eval(args.get(0)))) {
                eval(args.get(1));
            }
            scope = scope.getParent();
            return VOID;
        });

        //FOR
        scope.define("for",  (Function<List<Ast>, Object>) args -> {
            if (args.size() != 2)
                throw new EvalException("for requires 2 arguments");
            scope = new Scope(scope);
            Ast.Term term = requireType(Ast.Term.class, eval(args.get(0)));
            LinkedList forList = requireType(LinkedList.class, term.getArgs().get(0));
            scope.define(term.getName(), forList.get(0));
            for (Object item : forList) {
                scope.set(term.getName(), item);
                eval(args.get(2));
            }
            scope = scope.getParent();
            return VOID;
        });

        //Control Flow Functions
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
