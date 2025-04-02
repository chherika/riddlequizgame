import java.util.*;
import java.io.*;

class Test {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<String, String> users = new HashMap<>();
    private static final Map<String, Long> completionTimes = new HashMap<>();
    private static final String USERS_FILE = "users.txt";
    private static final String COMPLETION_TIMES_FILE = "completion_times.txt";
    private static final String QUESTIONS_FILE = "questions.txt";
    private static final List<Question> questions = new ArrayList<>();
    private static final String FILE_PATH = "usernamepassword.csv";
    public static void main(String[] args) {
        loadUsers();
        loadCompletionTimes();
        loadQuestions();
        showWelcomeScreen();
    }

    // Starting screen
    private static void showWelcomeScreen() {
        System.out.println("--------------***--------------\n" +
                " Welcome to Riddle Quiz Game!!!\n"+
                "--------------***--------------");
        System.out.println("Test your wits with challenging riddles across different difficulty levels.\n"+
                "---------------***--------------");

        boolean isLoggedIn = false;
        String loggedInUser= null;
        while (!isLoggedIn) {
            System.out.println("Do you have an account yet? yes/no ");
            String choice =scanner.nextLine().trim().toLowerCase();

            if (choice.equalsIgnoreCase("yes")) {
                isLoggedIn=checkLoginUsernamePassword();
                if (isLoggedIn) {
                   loggedInUser= enteredUsername;
                   showMainMenu(loggedInUser);
                }else{
                    System.out.println("Login failed. Try again.");
                }
            } else if (choice.equalsIgnoreCase("no")) {
                boolean signUpSuccess=signUp();
                if (signUpSuccess){
                    System.out.println("Please login to play the game");
                }
            } else {
                System.out.println("Invalid choice. Please enter 'yes' or 'no'");
            }
        }
    }
    private static void showMainMenu(String username) {
        while (true){
            System.out.println("---***---\n"+"Main Menu\n"+ "---***---\n"+
                    "Enter 'a' to edit your profile \n" +
                    "Enter 'l' to see the leaderboard \n" +
                    "Enter 'p' to start the game \n" +
                    "Enter 'q' to quit");
            System.out.println("enter your choice:");
            try {
                String choice=scanner.nextLine().trim().toLowerCase();
                switch(choice) {
                    case "a":
                        editProfile(username);
                        break;
                    case "l":
                        showLeaderboard();
                        break;
                    case "p":
                        playQuiz(username);
                        break;
                    case "q":
                        System.out.println("Exiting program. Goodbye, "+username+"!");
                        showWelcomeScreen();
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e){
                System.out.println("Invalid choice. Please try again.");
            }

        }
    }
    private static String enteredUsername="";
    private static boolean checkLoginUsernamePassword(){
        System.out.print("Enter username: ");
        String enteredUsername = scanner.nextLine();
        System.out.print("Enter password: ");
        String enteredPassword = scanner.nextLine();
        try { Scanner fileScanner=new Scanner(new File(FILE_PATH));
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String username = parts[0];
                    String password = parts[1];
                    if (enteredUsername.equals(username) && enteredPassword.equals(password)) {
                        System.out.println("Login successful.");
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("User login file not found.");
        }
        return false;
    }
    private static boolean signUp(){
        Scanner input = new Scanner(System.in);
        System.out.print("***Sign-Up Menu***\n");
        System.out.print("Enter a username: ");
        String username = input.nextLine();

        if (usernameAlreadyExists(username)) {
            System.out.println("Username already exists. Try another one.");
            return false;
        }
        System.out.print("Enter a password: ");
        String password = input.nextLine().trim();
        saveUser(username, password);
        System.out.print("Sign-Up successful!");
        return true;
    }
    private static boolean usernameAlreadyExists(String username) {
        try (Scanner fileScanner=new Scanner(new File(FILE_PATH))) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].trim().equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading username file."+e.getMessage());
        }
        return false;
    }
    private static void saveUser(String username, String password) {
        try(FileWriter writer=new FileWriter(FILE_PATH, true)){
            writer.append(username).append(",").append(password).append("\n");
        } catch (IOException e) {
            System.out.println("Error writing username file."+e.getMessage());
        }
    }

    // Edit profile method
    private static void editProfile(String currentUsername) {
        System.out.print("***Edit profile***\n");
        System.out.print("1. Change Username\n");
        System.out.print("2. Change Password\n");
        System.out.print("3. Back to Main Menu\n");
        System.out.print("Enter your choice:");
        try{
            int choice=scanner.nextInt();
            scanner.nextLine();
            switch(choice) {
                case 1:
                    changeUsername(currentUsername);
                    break;
                case 2:
                    changePassword(currentUsername);
                    break;
                case 3:
                    System.out.println("Back to Main Menu");
                    showWelcomeScreen();
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (InputMismatchException e){
            System.out.println("Invalid choice. Please try again.");
            scanner.nextLine();
        }

    }
    private static void changeUsername(String currentUsername) {
        System.out.print("\nEnter new username: ");
        String newUsername = scanner.nextLine();

        if (usernameAlreadyExists(newUsername)) {
            System.out.println("Username already exists. Please choose another one.");
            return;
        }

        // Update all records with the new username
        String password = users.get(currentUsername);
        long time = completionTimes.getOrDefault(currentUsername, Long.MAX_VALUE);

        // Remove old entries
        users.remove(currentUsername);
        completionTimes.remove(currentUsername);

        // Add new entries
        users.put(newUsername, password);
        completionTimes.put(newUsername, time);

        saveUsers();
        saveCompletionTimes();

        System.out.println("Username changed successfully to: " + newUsername);

        // Return to welcome screen with new username
        System.out.println("Please login again with your new username.");
        showWelcomeScreen();
    }

    // Change password method
    private static void changePassword(String username) {
        System.out.print("\nEnter current password: ");
        String currentPassword = scanner.nextLine();

        if (!users.get(username).equals(currentPassword)) {
            System.out.println("Incorrect current password.");
            return;
        }

        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();

        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return;
        }

        users.put(username, newPassword);
        saveUsers();
        System.out.println("Password changed successfully!");
    }

    // Quiz method
    private static void playQuiz(String username) {
        if (questions.isEmpty()) {
            System.out.println("No questions available.");
            return;
        }   

        // Group questions by difficulty
        Map<String, List<Question>> questionsByDifficulty = new HashMap<>();
        questionsByDifficulty.put("Easy", new ArrayList<>());
        questionsByDifficulty.put("Medium", new ArrayList<>());
        questionsByDifficulty.put("Hard", new ArrayList<>());

        for (Question q : questions) {
            questionsByDifficulty.get(q.getDifficulty()).add(q);
        }

        long startTime = System.currentTimeMillis();

        for (String difficulty : new String[]{"Easy", "Medium", "Hard"}) {
            List<Question> difficultyQuestions = questionsByDifficulty.get(difficulty);
            if (difficultyQuestions.isEmpty()) continue;
        
            System.out.println("\n\nStarting " + difficulty + " difficulty questions!");
            
            for (Question q : difficultyQuestions) {
                boolean answeredCorrectly = false;
                while (!answeredCorrectly) {
                    System.out.println("\n" + q.getNumber() + ". (" + q.getDifficulty() + ") " + q.getText());
                    char optionChar = 'A';
                    for (String option : q.getOptions()) {
                        System.out.println(optionChar + ". " + option);
                        optionChar++;
                    }
                    System.out.print("Your answer: ");
                    char answer;
                    try {
                        answer = Character.toUpperCase(scanner.next().charAt(0));
                        scanner.nextLine();
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please enter a letter (A, B, C, etc.).");
                        scanner.nextLine();
                        continue;
                    }

                    if (answer == q.getCorrectAnswer()) {
                        System.out.println("Correct!");
                        answeredCorrectly = true;
                    } else {
                        System.out.println("Wrong! Try again.");
                    }
                }
            }
            
            System.out.println("\nCongratulations on completing the " + difficulty + " difficulty!");
        }

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;
        String formattedTime = formatDuration(durationMillis);
        System.out.println("\n\nQuiz Over!");
        System.out.println("Total time taken: " + formattedTime);

        // Update completion times if this is the fastest completion
        if (!completionTimes.containsKey(username) || durationMillis < completionTimes.get(username)) {
            completionTimes.put(username, durationMillis);
            saveCompletionTimes();
        }
    }

    private static String formatDuration(long durationMillis) {
        long seconds = (durationMillis / 1000) % 60;
        long minutes = (durationMillis / (1000 * 60)) % 60;
        long hours = (durationMillis / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Leaderboard method
    private static void showLeaderboard() {
        while (true) {
            System.out.println("\n\n                                Leaderboard                              ");
            completionTimes.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> {
                    String username = entry.getKey();
                    long time = entry.getValue();
                    System.out.println(username + " - Fastest time: " + 
                        (time == Long.MAX_VALUE ? "N/A" : formatDuration(time)));
                });
    
            System.out.println("\n1. Back to Main Menu");
            System.out.print("Choose an option: ");
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice == 1) {
                    return;
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number (1).");
                scanner.nextLine();
            }
        }
    }

    // Loading user data
    private static void loadUsers() {
        try (Scanner fileScanner = new Scanner(new File(USERS_FILE))) {
            while (fileScanner.hasNextLine()) {
                String[] data = fileScanner.nextLine().split(",");
                if (data.length >= 2) {
                    users.put(data[0], data[1]);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No previous user data found. Starting fresh.");
        }
    }

    // Load completion times data
    private static void loadCompletionTimes() {
        try (Scanner fileScanner = new Scanner(new File(COMPLETION_TIMES_FILE))) {
            while (fileScanner.hasNextLine()) {
                String[] data = fileScanner.nextLine().split(",");
                if (data.length >= 2) {
                    try {
                        completionTimes.put(data[0], Long.parseLong(data[1]));
                    } catch (NumberFormatException e) {
                        completionTimes.put(data[0], Long.MAX_VALUE);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No previous completion times data found. Starting fresh.");
        }
    }

    // Load questions from file
    private static void loadQuestions() {
        try (Scanner fileScanner = new Scanner(new File(QUESTIONS_FILE))) {
            int questionNumber = 1;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                // Split on commas that are not inside quotes
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                // Trim each part and remove surrounding quotes if present
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim().replaceAll("^\"|\"$", "");
                }
                
                // Question files need difficulty, text, 3 options, and answer
                if (parts.length >= 6) {
                    String difficulty = parts[0];
                    String text = parts[1];
                    String[] options = Arrays.copyOfRange(parts, 2, parts.length - 1);
                    char correctAnswer = parts[parts.length - 1].trim().charAt(0);
                    questions.add(new Question(questionNumber++, difficulty, text, options, correctAnswer));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No questions found. Please add questions to " + QUESTIONS_FILE);
        }
    }

    // Save user data method
    private static void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            users.forEach((user, pass) -> writer.println(user + "," + pass));
        } catch (IOException e) {
            System.out.println("Error saving users.");
        }
    }

    // Save completion times data method
    private static void saveCompletionTimes() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(COMPLETION_TIMES_FILE))) {
            completionTimes.forEach((user, time) -> 
                writer.println(user + "," + (time == Long.MAX_VALUE ? "" : time)));
        } catch (IOException e) {
            System.out.println("Error saving completion times.");
        }
    }
}

class Question {
    private final int number;
    private final String difficulty;
    private final String text;
    private final String[] options;
    private final char correctAnswer;

    public Question(int number, String difficulty, String text, String[] options, char correctAnswer) {
        this.number = number;
        this.difficulty = difficulty;
        this.text = text;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public int getNumber() { return number; }
    public String getDifficulty() { return difficulty; }
    public String getText() { return text; }
    public String[] getOptions() { return options; }
    public char getCorrectAnswer() { return correctAnswer; }
}
