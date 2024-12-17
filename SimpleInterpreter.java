import java.util.*;
import java.util.regex.*;

public class SimpleInterpreter {

    static class Interpreter {
        private final Map<String, Integer> variables = new HashMap<>();

        public void executeProgram(String program) {
            String[] lines = program.split("\\n");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;

                if (!isValidAssignment(line)) {
                    System.out.println("error");
                    return;
                }

                if (!processAssignment(line)) {
                    System.out.println("error");
                    return;
                }
            }

            
            variables.forEach((key, value) -> System.out.println(key + " = " + value));
        }

        private boolean isValidAssignment(String line) {
            
            String assignmentRegex = "[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*.+;";
            return Pattern.matches(assignmentRegex, line);
        }

        private boolean processAssignment(String line) {
            try {
                String[] parts = line.split("\\s*=\\s*", 2);
                String identifier = parts[0].trim();
                String expression = parts[1].trim().replace(";", "");

                if (!isValidIdentifier(identifier)) return false;

                int value = evaluateExpression(expression);
                variables.put(identifier, value);
                return true;

            } catch (Exception e) {
                return false;
            }
        }

        private boolean isValidIdentifier(String identifier) {
            return Pattern.matches("[a-zA-Z_][a-zA-Z0-9_]*", identifier);
        }

        private int evaluateExpression(String expression) {
            return new ExpressionEvaluator(variables).evaluate(expression);
        }
    }

    static class ExpressionEvaluator {
        private final Map<String, Integer> variables;
        private int pos = -1, ch;
        private String input;

        public ExpressionEvaluator(Map<String, Integer> variables) {
            this.variables = variables;
        }

        public int evaluate(String input) {
            this.input = input;
            pos = -1;
            ch = -1;
            nextChar();
            int value = parseExpression();
            if (pos < input.length()) throw new RuntimeException("Unexpected: " + (char) ch);
            return value;
        }

        private void nextChar() {
            ch = (++pos < input.length()) ? input.charAt(pos) : -1;
        }

        private boolean eat(int charToEat) {
            while (ch == ' ') nextChar();
            if (ch == charToEat) {
                nextChar();
                return true;
            }
            return false;
        }

        private int parseExpression() {
            int value = parseTerm();
            for (;;) {
                if (eat('+')) value += parseTerm();
                else if (eat('-')) value -= parseTerm();
                else return value;
            }
        }

        private int parseTerm() {
            int value = parseFactor();
            for (;;) {
                if (eat('*')) value *= parseFactor();
                else return value;
            }
        }

        private int parseFactor() {
            int sign = 1;

           
            while (eat('+') || eat('-')) {
                if (ch == '-') {
                    sign = -sign; 
                }
            }

            int value;
            int startPos = this.pos;

            if (eat('(')) { 
                value = parseExpression();
                if (!eat(')')) throw new RuntimeException("Missing closing parenthesis");
            } else if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') { // Handle literals or variables
                while ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_')
                    nextChar();
                String token = input.substring(startPos, this.pos);
                if (Character.isDigit(token.charAt(0))) {
                    if (token.matches("0\\d+")) throw new RuntimeException("Invalid number format");
                    value = Integer.parseInt(token);
                } else { 
                    if (!variables.containsKey(token)) throw new RuntimeException("Uninitialized variable: " + token);
                    value = variables.get(token);
                }
            } else {
                throw new RuntimeException("Unexpected: " + (char) ch);
            }

            return sign * value; 
        }

        
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder program = new StringBuilder();

        System.out.println("Enter your program (type 'END' to finish):");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equals("END")) break;
            program.append(line).append("\n");
        }

        new Interpreter().executeProgram(program.toString());
    }
}
