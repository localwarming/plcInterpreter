package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains JUnit tests for {@link Regex}. Tests declarations for steps 1 & 2
 * are provided, you must add your own for step 3.
 *
 * To run tests, either click the run icon on the left margin or execute the
 * gradle test task, which can be done by clicking the Gradle tab in the right
 * sidebar and navigating to Tasks > verification > test Regex(double click to run).
 */
public class RegexTests {

    /**
     * This is a parameterized test for the {@link Regex#EMAIL} regex. The
     * {@link ParameterizedTest} annotation defines this method as a
     * parameterized test, and {@link MethodSource} tells JUnit to look for the
     * static method {@link #testEmailRegex()}.
     *
     * For personal preference, I include a test name as the first parameter
     * which describes what that test should be testing - this is visible in
     * IntelliJ when running the tests.
     */
    @ParameterizedTest
    @MethodSource
    public void testEmailRegex(String test, String input, boolean success) {
        test(input, Regex.EMAIL, success);
    }

    /**
     * This is the factory method providing test cases for the parameterized
     * test above - note that it is static, takes no arguments, and has the same
     * name as the test. The {@link Arguments} object contains the arguments for
     * each test to be passed to the function above
     */
    public static Stream<Arguments> testEmailRegex() {
        return Stream.of(
                Arguments.of("Alphanumeric", "thelegend27@gmail.com", true),
                Arguments.of("Valid Symbols", "symbols._-@gmail.com", true),
                Arguments.of("Upper Case Alphanumeric", "THELENGEND27@gmail.com", true),
                Arguments.of("Upper Case Email Service", "THELENGEND27@GMAIL.com", true),
                Arguments.of("UF Domain", "otherdomain@ufl.edu", true),
                Arguments.of("Missing Domain Dot", "missingdot@gmailcom", false),
                Arguments.of("Extra At Symbol", "extraat@@gmail.com", false),
                Arguments.of("Extra Domain Dot", "extradot@gmail..com", false),
                Arguments.of("Long com", "differentcom.@gmail.comm", false),
                Arguments.of("Symbols", "symbols#$%@gmail.com", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testFileNamesRegex(String test, String input, boolean success) {
        //this one is different as we're also testing the file name capture
        Matcher matcher = test(input, Regex.FILE_NAMES, success);
        if (success) {
            Assertions.assertEquals(input.substring(0, input.indexOf(".")), matcher.group("name"));
        }
    }

    public static Stream<Arguments> testFileNamesRegex() {
        return Stream.of(
                Arguments.of("Java File", "Regex.tar.java", true),
                Arguments.of("Java Class", "RegexTests.class", true),
                Arguments.of("Gz Class", "RegexTests.class.gz", true),
                Arguments.of("Zip Class", "RegexTests.class.zip", true),
                Arguments.of("Sub-Name Class", "Regex.Tests.class", true),
                Arguments.of("Directory", "directory", false),
                Arguments.of("No type", "directory.", false),
                Arguments.of("Start with .", ".file.class", false),
                Arguments.of("Dot in class", "file.cl.ass", false),
                Arguments.of("Whitespace", "file. class", false),
                Arguments.of("Python File", "scrippy.py", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                Arguments.of("14 Characters", "thishas14chars", true),
                Arguments.of("10 Characters", "i<3pancakes!", true),
                Arguments.of("16 Characters", "ld84&8bk12s^yg9*", true),
                Arguments.of("18 Characters", "kd;keicng*031232((", true),
                Arguments.of("20 Characters", "5284jfa923#$%&vba:I8", true),
                Arguments.of("0 Characters", "", false),
                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("15 Characters", "i<3pancakes!!", false),
                Arguments.of("11 Characters", "wec&y4ei(a", false),
                Arguments.of("22 Characters", "5284jfa923#$%&vba:I833", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegerListRegex(String test, String input, boolean success) {
        test(input, Regex.INTEGER_LIST, success);
    }

    public static Stream<Arguments> testIntegerListRegex() {
        return Stream.of(
                Arguments.of("Empty List", "[]", true),
                Arguments.of("Single Element", "[1]", true),
                Arguments.of("Multiple Elements no spaces", "[1,2,3]", true),
                Arguments.of("Multiple Elements spaces", "[1, 2, 3]", true),
                Arguments.of("Multiple Elements", "[1,2,3]", true),
                Arguments.of("Missing Brackets", "1,2,3", false),
                Arguments.of("One Bracket start", "[1,2,3", false),
                Arguments.of("One Bracket end", "1,2,3]", false),
                Arguments.of("Extra spaces", "[1,  2,3]", false),
                Arguments.of("Wrong space", "[1, 2, 3 ]", false),
                Arguments.of("Missing Commas", "[1 2 3]", false),
                Arguments.of("Trailing Comma", "[1,2,3,]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIdentifierRegex(String test, String input, boolean success) {
        test(input, Regex.IDENTIFIER, success);
    }

    public static Stream<Arguments> testIdentifierRegex() {
        return Stream.of(
                Arguments.of("All letters", "foo", true),
                Arguments.of("All characters", "<=>", true),
                Arguments.of("Letters with mixed chars", "one+!two", true),
                Arguments.of("Starting chars", "-99", true),
                Arguments.of("Two periods", "..", true),
                Arguments.of("Start with numbers", "145", false),
                Arguments.of("Has commas", "some,string", false),
                Arguments.of("Has quotes", "this\"quote", false),
                Arguments.of("Has space", "this quote", false),
                Arguments.of("Just period", ".", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testNumberRegex(String test, String input, boolean success) {
        test(input, Regex.NUMBER, success);
    }

    public static Stream<Arguments> testNumberRegex() {
        return Stream.of(
                Arguments.of("Unsigned int", "1234", true),
                Arguments.of("Negative int", "-1234", true),
                Arguments.of("Positive int", "+1234", true),
                Arguments.of("Unsigned Floating Point", "12.34", true),
                Arguments.of("Negative Floating Point", "-12.34", true),
                Arguments.of("Positive Floating Point", "+12.34", true),
                Arguments.of("Random positive", "423+123", false),
                Arguments.of("Random negative", "423-123", false),
                Arguments.of("Start with letter", "a123", false),
                Arguments.of("Start with char", "<123", false),
                Arguments.of("Start with decimal", ".123", false),
                Arguments.of("Random letter", "12a3", false),
                Arguments.of("Random char", "12!3", false),
                Arguments.of("Has space", "423 123", false),
                Arguments.of("Two decimals", "12.34.56", false),
                Arguments.of("Trailing period", "423123.", false),
                Arguments.of("Whitespace", "42 3123", false),
                Arguments.of("Just period", ".", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testStringRegex(String test, String input, boolean success) {
        test(input, Regex.STRING, success);
    }

    public static Stream<Arguments> testStringRegex() {
        return Stream.of(
                Arguments.of("String", "\"test\"", true),
                Arguments.of("Empty", "\"\"", true),
                Arguments.of("Unescaped '", "\"test ' quote '\"", true),
                Arguments.of("Escaped \\b", "\"test with \\b escaped\"", true),
                Arguments.of("Escaped \\r", "\"test with \\r escaped\"", true),
                Arguments.of("Escaped \\n", "\"test with \\n escaped\"", true),
                Arguments.of("Escaped \\t", "\"test with \\t escaped\"", true),
                Arguments.of("Escaped \\'", "\"test with \\' escaped\"", true),
                Arguments.of("Escaped \\\"", "\"test with \\\" escaped\"", true),
                Arguments.of("Escaped \\\\", "\"test with \\\\ escaped\"", true),
                Arguments.of("Characters", "\"!@#$%^&*()_+1234567890[]{}|`~./<>?\"", true),
                Arguments.of("Missing quote", "\"test quote", false),
                Arguments.of("Unescaped \"", "\"test \" quote \"", false),
                Arguments.of("Unescaped \\", "\"test \\ quote \"", false),
                Arguments.of("Invalid \\", "\"test \\e quote \"", false),
                Arguments.of("No start quote", "test \"quote\"", false)
        );
    }

    /**
     * Asserts that the input matches the given pattern and returns the matcher
     * for additional assertions.
     */
    private static Matcher test(String input, Pattern pattern, boolean success) {
        Matcher matcher = pattern.matcher(input);
        Assertions.assertEquals(success, matcher.matches());
        return matcher;
    }

}
