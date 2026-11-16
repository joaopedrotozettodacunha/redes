package pitiupi.quiz.net;

import pitiupi.models.Messages.Message;
import java.util.List;
import java.util.Map;

public class QuizMessage extends Message {
    private static final long serialVersionUID = 1L;

    private final String action;
    private final String roomId;
    private final String sender;
    private final String question;
    private final List<String> options;
    private final int correctIndex;
    private final int answerIndex;
    private final int questionNumber;
    private final Map<String, Integer> scores;

    public QuizMessage(String action, String roomId, String sender,
                       String question, List<String> options,
                       int correctIndex, int answerIndex, int questionNumber,
                       Map<String, Integer> scores) {
        this.action = action;
        this.roomId = roomId;
        this.sender = sender;
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
        this.answerIndex = answerIndex;
        this.questionNumber = questionNumber;
        this.scores = scores;
    }

    public String getAction() { return action; }
    public String getRoomId() { return roomId; }
    public String getSender() { return sender; }
    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public int getCorrectIndex() { return correctIndex; }
    public int getAnswerIndex() { return answerIndex; }
    public int getQuestionNumber() { return questionNumber; }
    public Map<String, Integer> getScores() { return scores; }
}
