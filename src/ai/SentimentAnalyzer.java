package ai;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SentimentAnalyzer {

    private static final List<String> POSITIVE_WORDS = Arrays.asList("good", "great", "excellent", "happy", "fast", "smooth", "easy", "satisfied");
    private static final List<String> NEGATIVE_WORDS = Arrays.asList("bad", "terrible", "slow", "hard", "confusing", "angry", "dissatisfied", "poor");

    public void analyzeVoterSentiment(Scanner scanner) {
        System.out.println("\n--- Post-Vote Feedback ---");
        System.out.println("Please answer 3 quick questions about your experience.");
        
        System.out.print("1. How was the registration process? ");
        String ans1 = scanner.nextLine().toLowerCase();
        
        System.out.print("2. Was finding your candidate easy? ");
        String ans2 = scanner.nextLine().toLowerCase();
        
        System.out.print("3. Overall, how would you rate the voting system? ");
        String ans3 = scanner.nextLine().toLowerCase();

        String combinedResponses = ans1 + " " + ans2 + " " + ans3;
        
        int score = 0;
        for (String word : combinedResponses.split("\\s+")) {
            word = word.replaceAll("[^a-zA-Z]", "");
            if (POSITIVE_WORDS.contains(word)) score++;
            if (NEGATIVE_WORDS.contains(word)) score--;
        }

        System.out.print("[AI Sentiment Analysis] Tagged Voter as: ");
        if (score > 0) {
            System.out.println("SATISFIED 😊");
        } else if (score < 0) {
            System.out.println("DISSATISFIED 😞");
        } else {
            System.out.println("NEUTRAL 😐");
        }
    }
}
