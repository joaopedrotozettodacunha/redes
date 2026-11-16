package pitiupi.perguntas.GUI;

import pitiupi.control.ConnectionManager;
import pitiupi.perguntas.net.PerguntasMessage;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PerguntasPanel extends JPanel {

    private static final String[] OPTION_LABELS = {"A", "B", "C", "D"};

    private final ConnectionManager connectionManager;
    private final String participantId = UUID.randomUUID().toString();
    private final JTextField questionInput = new JTextField(48);
    private final JTextField[] optionInputs = {
            new JTextField(20), new JTextField(20), new JTextField(20), new JTextField(20)
    };
    private final JComboBox<String> correctOption = new JComboBox<>(OPTION_LABELS);
    private final JTextArea activeQuestion = new JTextArea(2, 40);
    private final JPanel answerOptions = new JPanel(new GridLayout(0, 1, 4, 4));
    private final ButtonGroup answerGroup = new ButtonGroup();
    private final JRadioButton[] answerButtons = new JRadioButton[OPTION_LABELS.length];
    private final JButton submitAnswer = new JButton("Enviar resposta");
    private final JLabel status = new JLabel("Crie uma pergunta ou aguarde outra pessoa.");
    private final JTextArea activityLog = new JTextArea(4, 60);
    private final DefaultTableModel scoreModel = new DefaultTableModel(
            new Object[]{"Participante", "Pontos"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final Map<UUID, QuestionState> questions = new HashMap<>();
    private final Map<UUID, List<PerguntasMessage>> pendingAnswers = new HashMap<>();
    private final Map<UUID, List<PerguntasMessage>> pendingResults = new HashMap<>();
    private final Map<String, ScoreEntry> scores = new HashMap<>();
    private final Set<UUID> receivedEvents = new HashSet<>();
    private final Set<UUID> locallyAnsweredQuestions = new HashSet<>();
    private final Set<UUID> scoredAnswers = new HashSet<>();
    private UUID activeQuestionId;

    public PerguntasPanel(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout(4, 4));
        top.add(new JLabel(
                "Quiz da sala: publique uma pergunta com 4 alternativas. Cada acerto vale 1 ponto."
        ), BorderLayout.NORTH);
        top.add(createQuestionEditor(), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(createActiveQuestionPanel(), BorderLayout.CENTER);
        center.add(createScorePanel(), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        activityLog.setEditable(false);
        activityLog.setLineWrap(true);
        activityLog.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(activityLog);
        logScroll.setBorder(BorderFactory.createTitledBorder("Atividade"));
        add(logScroll, BorderLayout.SOUTH);

        submitAnswer.addActionListener(event -> publishAnswer());
    }

    public void receive(PerguntasMessage message) {
        if (!isValid(message) || !receivedEvents.add(message.getEventId())) {
            return;
        }
        switch (message.getType()) {
            case QUESTION -> receiveQuestion(message);
            case ANSWER -> receiveAnswer(message);
            case RESULT -> receiveResult(message);
        }
    }

    private JPanel createQuestionEditor() {
        JPanel editor = new JPanel(new BorderLayout(6, 6));
        editor.setBorder(BorderFactory.createTitledBorder("Criar pergunta"));
        editor.add(questionInput, BorderLayout.NORTH);

        JPanel choices = new JPanel(new GridLayout(0, 2, 6, 4));
        for (int i = 0; i < optionInputs.length; i++) {
            choices.add(new JLabel("Alternativa " + OPTION_LABELS[i] + ":"));
            choices.add(optionInputs[i]);
        }
        editor.add(choices, BorderLayout.CENTER);

        JButton publish = new JButton("Publicar pergunta");
        publish.addActionListener(event -> publishQuestion());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(new JLabel("Resposta correta:"));
        footer.add(correctOption);
        footer.add(publish);
        editor.add(footer, BorderLayout.SOUTH);
        return editor;
    }

    private JPanel createActiveQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Pergunta atual"));

        activeQuestion.setEditable(false);
        activeQuestion.setLineWrap(true);
        activeQuestion.setWrapStyleWord(true);
        activeQuestion.setOpaque(false);
        activeQuestion.setText("Aguardando uma pergunta...");
        panel.add(activeQuestion, BorderLayout.NORTH);
        panel.add(answerOptions, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(6, 6));
        footer.add(status, BorderLayout.CENTER);
        footer.add(submitAnswer, BorderLayout.EAST);
        panel.add(footer, BorderLayout.SOUTH);
        submitAnswer.setEnabled(false);
        return panel;
    }

    private JPanel createScorePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Placar da sessão"));
        JTable table = new JTable(scoreModel);
        table.setFillsViewportHeight(true);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.setPreferredSize(new java.awt.Dimension(230, 0));
        return panel;
    }

    private void publishQuestion() {
        String text = questionInput.getText().trim();
        String[] options = Arrays.stream(optionInputs)
                .map(field -> field.getText().trim())
                .toArray(String[]::new);
        if (text.isEmpty() || Arrays.stream(options).anyMatch(String::isEmpty)) {
            status.setText("Preencha a pergunta e as quatro alternativas.");
            return;
        }

        UUID questionId = UUID.randomUUID();
        QuestionState question = new QuestionState(
                questionId,
                participantId,
                currentUserName(),
                text,
                options,
                correctOption.getSelectedIndex()
        );
        questions.put(questionId, question);
        PerguntasMessage message = PerguntasMessage.question(
                questionId, participantId, question.hostName, text, options
        );
        if (!send(message)) {
            questions.remove(questionId);
            return;
        }
        receive(message);
        questionInput.setText("");
        for (JTextField optionInput : optionInputs) {
            optionInput.setText("");
        }
        appendActivity("Você publicou uma pergunta.");
    }

    private void publishAnswer() {
        QuestionState question = questions.get(activeQuestionId);
        if (question == null || question.hostId.equals(participantId)) {
            status.setText("A pessoa que publicou a pergunta não pode respondê-la.");
            return;
        }
        if (locallyAnsweredQuestions.contains(question.id)) {
            status.setText("Você já respondeu a esta pergunta.");
            return;
        }
        int selected = selectedAnswerIndex();
        if (selected < 0) {
            status.setText("Selecione uma alternativa.");
            return;
        }

        PerguntasMessage message = PerguntasMessage.answer(
                UUID.randomUUID(), question.id, participantId, currentUserName(), selected
        );
        if (!send(message)) {
            return;
        }
        locallyAnsweredQuestions.add(question.id);
        receive(message);
        refreshActiveQuestion();
        status.setText("Resposta enviada; aguardando o resultado.");
    }

    private boolean send(PerguntasMessage message) {
        if (connectionManager.sendUdpMulticast(message)) {
            return true;
        }
        status.setText("Não foi possível enviar pela rede. Confira a porta e a conexão.");
        return false;
    }

    private void receiveQuestion(PerguntasMessage message) {
        QuestionState question = questions.get(message.getQuestionId());
        if (question == null) {
            question = new QuestionState(
                    message.getQuestionId(),
                    message.getSenderId(),
                    message.getSenderName(),
                    message.getText(),
                    message.getOptions(),
                    null
            );
            questions.put(question.id, question);
        } else if (!question.hostId.equals(message.getSenderId())) {
            return;
        }

        activeQuestionId = question.id;
        refreshActiveQuestion();
        appendActivity("Pergunta de " + question.hostName + ": " + question.text);

        List<PerguntasMessage> waitingAnswers = pendingAnswers.remove(question.id);
        if (waitingAnswers != null) {
            waitingAnswers.forEach(this::receiveAnswer);
        }
        List<PerguntasMessage> waitingResults = pendingResults.remove(question.id);
        if (waitingResults != null) {
            waitingResults.forEach(this::receiveResult);
        }
    }

    private void receiveAnswer(PerguntasMessage message) {
        QuestionState question = questions.get(message.getQuestionId());
        if (question == null) {
            pendingAnswers.computeIfAbsent(message.getQuestionId(), id -> new ArrayList<>()).add(message);
            return;
        }
        ensureScoreEntry(message.getSenderId(), message.getSenderName());
        if (!question.hostId.equals(participantId) || question.correctOption == null
                || !question.answeredPlayers.add(message.getSenderId())) {
            return;
        }

        int points = message.getSelectedOption() == question.correctOption ? 1 : 0;
        PerguntasMessage result = PerguntasMessage.result(
                UUID.randomUUID(),
                message.getEventId(),
                question.id,
                participantId,
                currentUserName(),
                message.getSenderId(),
                message.getSenderName(),
                message.getSelectedOption(),
                question.correctOption,
                points
        );
        if (!send(result)) {
            appendActivity("Não foi possível divulgar o resultado para " + message.getSenderName() + ".");
        }
        receive(result);
    }

    private void receiveResult(PerguntasMessage message) {
        QuestionState question = questions.get(message.getQuestionId());
        if (question == null) {
            pendingResults.computeIfAbsent(message.getQuestionId(), id -> new ArrayList<>()).add(message);
            return;
        }
        if (!question.hostId.equals(message.getSenderId())
                || (question.correctOption != null
                        && question.correctOption != message.getCorrectOption())
                || !scoredAnswers.add(message.getAnswerEventId())) {
            return;
        }

        boolean correct = message.getSelectedOption() == message.getCorrectOption();
        int expectedPoints = correct ? 1 : 0;
        if (message.getPointsAwarded() != expectedPoints) {
            scoredAnswers.remove(message.getAnswerEventId());
            return;
        }

        ScoreEntry previous = scores.get(message.getPlayerId());
        int total = (previous == null ? 0 : previous.points) + message.getPointsAwarded();
        scores.put(message.getPlayerId(), new ScoreEntry(message.getPlayerName(), total));
        refreshScoreboard();

        if (message.getPlayerId().equals(participantId)) {
            locallyAnsweredQuestions.add(question.id);
            status.setText(correct
                    ? "Correto! Você ganhou 1 ponto."
                    : "Resposta incorreta. A alternativa correta era "
                            + OPTION_LABELS[message.getCorrectOption()] + ".");
            refreshActiveQuestion();
        }
        appendActivity(message.getPlayerName() + (correct
                ? " acertou e ganhou 1 ponto."
                : " errou. A resposta correta era " + OPTION_LABELS[message.getCorrectOption()] + "."));
    }

    private void refreshActiveQuestion() {
        QuestionState question = questions.get(activeQuestionId);
        answerOptions.removeAll();
        answerGroup.clearSelection();
        Arrays.fill(answerButtons, null);
        if (question == null) {
            activeQuestion.setText("Aguardando uma pergunta...");
            submitAnswer.setEnabled(false);
            answerOptions.revalidate();
            answerOptions.repaint();
            return;
        }

        activeQuestion.setText(question.text + "\nPergunta de " + question.hostName);
        boolean canAnswer = !question.hostId.equals(participantId)
                && !locallyAnsweredQuestions.contains(question.id);
        for (int i = 0; i < question.options.length; i++) {
            JRadioButton button = new JRadioButton(
                    OPTION_LABELS[i] + ") " + question.options[i]
            );
            button.setEnabled(canAnswer);
            answerButtons[i] = button;
            answerGroup.add(button);
            answerOptions.add(button);
        }
        submitAnswer.setEnabled(canAnswer);
        if (!canAnswer && !question.hostId.equals(participantId)) {
            status.setText("Você já respondeu a esta pergunta.");
        }
        answerOptions.revalidate();
        answerOptions.repaint();
    }

    private int selectedAnswerIndex() {
        for (int i = 0; i < answerButtons.length; i++) {
            if (answerButtons[i] != null && answerButtons[i].isSelected()) {
                return i;
            }
        }
        return -1;
    }

    private void ensureScoreEntry(String playerId, String playerName) {
        scores.putIfAbsent(playerId, new ScoreEntry(playerName, 0));
        refreshScoreboard();
    }

    private void refreshScoreboard() {
        scoreModel.setRowCount(0);
        scores.values().stream()
                .sorted(Comparator.comparingInt((ScoreEntry entry) -> entry.points).reversed()
                        .thenComparing(entry -> entry.name, String.CASE_INSENSITIVE_ORDER))
                .forEach(entry -> scoreModel.addRow(new Object[]{entry.name, entry.points}));
    }

    private void appendActivity(String text) {
        activityLog.append(text + "\n");
        activityLog.setCaretPosition(activityLog.getDocument().getLength());
    }

    private String currentUserName() {
        String name = connectionManager.getUserName();
        return name == null || name.isBlank() ? "Jogador" : name;
    }

    private static boolean isValid(PerguntasMessage message) {
        if (message == null || message.getType() == null || message.getEventId() == null
                || message.getQuestionId() == null || isBlank(message.getSenderId())
                || isBlank(message.getSenderName())) {
            return false;
        }
        return switch (message.getType()) {
            case QUESTION -> !isBlank(message.getText())
                    && message.getOptions() != null
                    && message.getOptions().length == OPTION_LABELS.length
                    && Arrays.stream(message.getOptions()).noneMatch(PerguntasPanel::isBlank);
            case ANSWER -> message.getSelectedOption() >= 0
                    && message.getSelectedOption() < OPTION_LABELS.length;
            case RESULT -> message.getAnswerEventId() != null
                    && !isBlank(message.getPlayerId())
                    && !isBlank(message.getPlayerName())
                    && message.getSelectedOption() >= 0
                    && message.getSelectedOption() < OPTION_LABELS.length
                    && message.getCorrectOption() >= 0
                    && message.getCorrectOption() < OPTION_LABELS.length
                    && (message.getPointsAwarded() == 0 || message.getPointsAwarded() == 1);
        };
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static final class QuestionState {
        private final UUID id;
        private final String hostId;
        private final String hostName;
        private final String text;
        private final String[] options;
        private final Integer correctOption;
        private final Set<String> answeredPlayers = new HashSet<>();

        private QuestionState(
                UUID id,
                String hostId,
                String hostName,
                String text,
                String[] options,
                Integer correctOption
        ) {
            this.id = id;
            this.hostId = hostId;
            this.hostName = hostName;
            this.text = text;
            this.options = options.clone();
            this.correctOption = correctOption;
        }
    }

    private record ScoreEntry(String name, int points) {
    }
}
