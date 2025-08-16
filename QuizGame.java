import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

public class QuizGame {

    private static final String QUESTIONS_FILE = "questions.txt";
    private static final String RESULTS_FILE   = "results.txt";
    private static final int    MAX_QUESTIONS  = 10; // ask at most N questions per run (set <= total in file)

   
    static class Question {
        final String text;
        final List<String> options; 
        final char correct;    
        final int seconds;    

        Question(String text, List<String> options, char correct, int seconds) {
            this.text = text;
            this.options = options;
            this.correct = Character.toUpperCase(correct);
            this.seconds = seconds;
        }
    }

    static class QuizResult {
        final String player;
        final int attempted;
        final int correct;
        final int total;
        final LocalDateTime when = LocalDateTime.now();

        QuizResult(String player, int attempted, int correct, int total) {
            this.player = player;
            this.attempted = attempted;
            this.correct = correct;
            this.total = total;
        }

        String asLine() {
            return String.join("|",
                when.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                player,
                "attempted=" + attempted,
                "correct=" + correct,
                "total=" + total,
                "percent=" + String.format("%.2f", (correct * 100.0) / Math.max(1, total))
            );
        }
    }

    // FIle I/O
    static List<Question> loadQuestions(String path) throws IOException {
        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            writeSampleQuestions(p);
            System.out.println("No questions.txt found. A sample file has been created. Edit it and run again if you like.");
        }

        List<Question> list = new ArrayList<>();
        int lineNo = 0;
        for (String line : Files.readAllLines(p)) {
            lineNo++;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] parts = line.split("\\|");
            if (parts.length < 7) {
                System.out.println("Skipping malformed line " + lineNo + ": " + line);
                continue;
            }
            String qText = parts[0].trim();
            List<String> opts = Arrays.asList(parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim());
            char correct = parts[5].trim().isEmpty() ? 'A' : parts[5].trim().toUpperCase().charAt(0);
            int secs;
            try {
                secs = Integer.parseInt(parts[6].trim());
            } catch (NumberFormatException e) {
                secs = 20;
            }
            if (correct < 'A' || correct > 'D') correct = 'A';
            list.add(new Question(qText, opts, correct, secs));
        }
        return list;
    }

    static void writeSampleQuestions(Path p) throws IOException {
        List<String> sample = Arrays.asList(
            "# Format: question|A|B|C|D|CORRECT_OPTION|SECONDS",
            "# Example: What is 2+2?|2|3|4|5|C|15",
            "What is 2+2?|2|3|4|5|C|15",
            "Which keyword creates a subclass in Java?|extend|extends|inherit|implements|B|20",
            "Which collection does NOT allow duplicates?|ArrayList|LinkedList|HashSet|Vector|C|20",
            "Which is NOT a Java primitive type?|int|String|double|boolean|B|15",
            "Which package has the Scanner class?|java.io|java.util|java.lang|java.time|B|15",
            "Which method starts a Java app?|start()|main()|run()|init()|B|10",
            "Which OOP pillar is 'many forms'?|Abstraction|Encapsulation|Polymorphism|Inheritance|C|15",
            "Default value of uninitialized int field?|0|1|null|undefined|A|15",
            "Which interface for threads?|Runnable|Cloneable|Serializable|Comparable|A|15",
            "Which collection is LIFO?|Queue|Deque used as stack|PriorityQueue|TreeSet|B|15"
        );
        Files.write(p, sample, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    static void appendResult(String path, QuizResult result) {
        try {
            Files.write(Paths.get(path),
                Collections.singletonList(result.asLine()),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Warning: could not write results file: " + e.getMessage());
        }
    }

    // QUIZ ENGINE
    public static void main(String[] args) {
        System.out.println("=== Java Console Quiz Game ===");
        System.out.print("Enter your name: ");
        Scanner sc = new Scanner(System.in);
        String player = sc.nextLine().trim();
        if (player.isEmpty()) player = "Player";

        List<Question> questions;
        try {
            questions = loadQuestions(QUESTIONS_FILE);
        } catch (IOException e) {
            System.out.println("Failed to load questions: " + e.getMessage());
            return;
        }

        if (questions.isEmpty()) {
            System.out.println("No valid questions found. Please edit " + QUESTIONS_FILE);
            return;
        }

        // Shuffle and limit
        Collections.shuffle(questions);
        if (questions.size() > MAX_QUESTIONS) {
            questions = new ArrayList<>(questions.subList(0, MAX_QUESTIONS));
        }

        System.out.println("\nHello, " + player + "! You will be asked " + questions.size() + " questions.");
        System.out.println("Answer by typing A, B, C, or D within the time limit shown.\n");

        int total = questions.size();
        int attempted = 0;
        int correct = 0;

        // We'll create a new single-thread executor for each input to allow per-question timeout.
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            System.out.println("\nQ" + (i + 1) + ": " + q.text);
            System.out.println("A) " + q.options.get(0));
            System.out.println("B) " + q.options.get(1));
            System.out.println("C) " + q.options.get(2));
            System.out.println("D) " + q.options.get(3));
            System.out.println("(Time limit: " + q.seconds + "s)");
            System.out.print("Your answer (A/B/C/D): ");

            String ans = null;
            ExecutorService ex = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true); // don't block JVM exit
                return t;
            });

            try {
                Future<String> fut = ex.submit(() -> {
                    String line = sc.nextLine().trim();
                    return line;
                });
                try {
                    ans = fut.get(q.seconds, TimeUnit.SECONDS);
                } catch (TimeoutException te) {
                    System.out.println("\nTime's up! Moving on.");
                    fut.cancel(true);
                }
            } catch (Exception e) {
                System.out.println("Input error: " + e.getMessage());
            } finally {
                ex.shutdownNow();
            }

            if (ans != null && !ans.isEmpty()) {
                attempted++;
                char a = Character.toUpperCase(ans.charAt(0));
                if (a == 'A' || a == 'B' || a == 'C' || a == 'D') {
                    if (a == q.correct) {
                        System.out.println("✔ Correct!");
                        correct++;
                    } else {
                        System.out.println("✘ Wrong. Correct answer: " + q.correct);
                    }
                } else {
                    System.out.println("Invalid option. Correct answer: " + q.correct);
                }
            } else {
                System.out.println("No answer recorded. Correct answer: " + q.correct);
            }
        }

        double percent = (correct * 100.0) / Math.max(1, total);
        System.out.println("\n=== RESULT ===");
        System.out.println("Player   : " + player);
        System.out.println("Attempted: " + attempted + "/" + total);
        System.out.println("Correct  : " + correct);
        System.out.printf ("Percent  : %.2f%%\n", percent);
        System.out.println("Thanks for playing!");

        QuizResult r = new QuizResult(player, attempted, correct, total);
        appendResult(RESULTS_FILE, r);
        System.out.println("\nSaved to " + RESULTS_FILE);
    }
}
