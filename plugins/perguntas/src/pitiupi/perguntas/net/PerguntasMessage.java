package pitiupi.perguntas.net;

import pitiupi.models.Messages.Message;

import java.util.UUID;

public class PerguntasMessage extends Message {

    private static final long serialVersionUID = 1L;

    public enum Type {
        QUESTION,
        ANSWER,
        RESULT
    }

    private Type type;
    private UUID eventId;
    private UUID questionId;
    private UUID answerEventId;
    private String senderId;
    private String senderName;
    private String playerId;
    private String playerName;
    private String text;
    private String[] options;
    private int selectedOption;
    private int correctOption;
    private int pointsAwarded;

    public PerguntasMessage() {
    }

    private PerguntasMessage(
            Type type,
            UUID eventId,
            UUID questionId,
            UUID answerEventId,
            String senderId,
            String senderName,
            String playerId,
            String playerName,
            String text,
            String[] options,
            int selectedOption,
            int correctOption,
            int pointsAwarded
    ) {
        this.type = type;
        this.eventId = eventId;
        this.questionId = questionId;
        this.answerEventId = answerEventId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.playerId = playerId;
        this.playerName = playerName;
        this.text = text;
        this.options = options == null ? null : options.clone();
        this.selectedOption = selectedOption;
        this.correctOption = correctOption;
        this.pointsAwarded = pointsAwarded;
    }

    public static PerguntasMessage question(
            UUID questionId,
            String hostId,
            String hostName,
            String text,
            String[] options
    ) {
        return new PerguntasMessage(
                Type.QUESTION, questionId, questionId, null, hostId, hostName, null, null,
                text, options, -1, -1, 0
        );
    }

    public static PerguntasMessage answer(
            UUID eventId,
            UUID questionId,
            String playerId,
            String playerName,
            int selectedOption
    ) {
        return new PerguntasMessage(
                Type.ANSWER, eventId, questionId, null, playerId, playerName, null, null,
                null, null, selectedOption, -1, 0
        );
    }

    public static PerguntasMessage result(
            UUID eventId,
            UUID answerEventId,
            UUID questionId,
            String hostId,
            String hostName,
            String playerId,
            String playerName,
            int selectedOption,
            int correctOption,
            int pointsAwarded
    ) {
        return new PerguntasMessage(
                Type.RESULT, eventId, questionId, answerEventId, hostId, hostName,
                playerId, playerName, null, null, selectedOption, correctOption, pointsAwarded
        );
    }

    public Type getType() {
        return type;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public UUID getAnswerEventId() {
        return answerEventId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getText() {
        return text;
    }

    public String[] getOptions() {
        return options == null ? null : options.clone();
    }

    public int getSelectedOption() {
        return selectedOption;
    }

    public int getCorrectOption() {
        return correctOption;
    }

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }
}
