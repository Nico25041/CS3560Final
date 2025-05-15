Rubik’s Cube Puzzle Simulator

Team Members





Ramsey Foster (Developer)



Nico Escobedo (Developer)



Luca Ficano (Developer)

Project Description

A 3D Rubik’s Cube simulator built with Java, JOGL, and JavaFX, inspired by Chess.com’s random puzzles. Players solve randomized cube scrambles at Beginner (2 moves), Intermediate (15 moves), or Hard (25 moves) difficulty levels using keyboard controls. Designed for puzzle enthusiasts and students exploring 3D graphics and OOP principles.

How to Run





Prerequisites:





JDK 19



Maven



JavaFX SDK 24.0.1



JOGL (included via Maven)



IntelliJ IDEA (recommended)



Setup:





Clone the repository: git clone <repo-url>



Download JavaFX SDK from openjfx.io and unzip.



Build and Run:





Run mvn clean install in the project root.



Set main class to org.example.RubiksCube in IntelliJ.



Click Run or use mvn javafx:run.



Troubleshooting:





Ensure JAVA_HOME points to JDK 19: echo %JAVA_HOME% (Windows).



If errors occur, try File > Invalidate Caches / Restart in IntelliJ.

Features Implemented





Interactive 3D Rubik’s Cube with 6-color faces (Yellow, White, Red, Orange, Green, Blue).



Keyboard controls: u/U (Up), d/D (Down), r/R (Right), l/L (Left), f/F (Front), b/B (Back), m/M (M layer), n/N (E layer), k/K (S layer).



Randomized scrambles for three difficulty levels: Beginner (2 moves), Intermediate (15 moves), Hard (25 moves).



Solve detection with “SOLVED!!!” message (red text, semi-transparent background).



JavaFX UI with home screen, difficulty selection, and back button to restart.



Smooth 60 FPS rendering with JOGL, supporting mouse-dragged camera rotation.

Future Work





Add move counter and solve timer for performance tracking.



Display on-screen control guide for easier learning.



Implement save/load for scramble states.



Add rotation animations for smoother visuals.



Support larger cubes (e.g., 4x4).

Known Issues





Back button may leave a stale JavaFX window open on some systems.



“SOLVED!!!” message transparency varies by system graphics settings.



No validation for invalid JavaFX SDK paths, which may cause startup errors.
