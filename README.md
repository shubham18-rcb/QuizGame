# QuizGame
Java Console Quiz Game

A pure Java console-based Quiz Game that lets users test their knowledge with multiple-choice questions (MCQs). The project is simple, lightweight, and requires no external database—all questions and results are stored in text files.

Features

File-based Storage

Reads questions from questions.txt

Saves player scores in results.txt

MCQ Quiz System

Each question has 4 options (A, B, C, D)

Randomized question order

Per-question Timer

Each question has a countdown timer (customizable per question)

If the player doesn’t answer in time, the question is marked wrong

Automatic Result Calculation

Shows total questions, attempted, correct answers, and percentage score

Stores results with timestamp and player name

Easy Customization

Just edit questions.txt to add/remove questions

Format:

Question|OptionA|OptionB|OptionC|OptionD|CorrectOption|Seconds

File Structure
.
├── QuizGame.java      # Main Java source file
├── questions.txt      # Stores all quiz questions
└── results.txt        # Stores player results (auto-generated)

Example questions.txt
What is 2+2?|2|3|4|5|C|15
Which keyword creates a subclass in Java?|extend|extends|inherit|implements|B|20
Which collection does NOT allow duplicates?|ArrayList|LinkedList|HashSet|Vector|C|20

How to Run

Clone or download this repo.

Compile the Java file:

javac QuizGame.java


Run the program:

java QuizGame
Enter your name, answer questions, and view results at the end.

Sample Output
=== Java Console Quiz Game ===
Enter your name: Shubham

Hello, Shubham! You will be asked 5 questions.
Answer by typing A, B, C, or D within the time limit.

Q1: What is 2+2?
A) 2
B) 3
C) 4
D) 5
(Time limit: 15s)
Your answer (A/B/C/D): C
✔ Correct!

=== RESULT ===
Player   : Shubham
Attempted: 5/5
Correct  : 4
Percent  : 80.00%
Saved to results.txt

Concepts Used

OOP (Classes & Objects) → Question, QuizResult

File Handling → reading/writing questions and results

Collections (ArrayList, List, etc.) → storing questions

Multithreading & Concurrency → per-question countdown timer

Exception Handling → input validation and file errors

Possible Enhancements

Add difficulty levels (Easy/Medium/Hard)

Negative marking for wrong answers

Categories (Java, DBMS, General Knowledge, etc.)

Leaderboard with top scores

GUI version using JavaFX

Why this Project?

This project is a great fit for academic submission (4th year / mini-projects) because it:

Demonstrates core Java skills (OOP, Collections, File I/O, Threads).

Is portable (runs on any machine with Java).

Is scalable (can later integrate DB or GUI).
