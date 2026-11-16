package pitiupi.quiz.GUI;

import pitiupi.control.ConnectionManager;
import pitiupi.quiz.net.QuizMessage;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class QuizPanel extends JPanel {
    private final ConnectionManager connectionManager;
    private final JTextField roomField = new JTextField(12);
    private final JTextArea log = new JTextArea();
    private final JPanel answersPanel = new JPanel(new GridLayout(0, 1, 5, 5));
    private final JLabel roomLabel = new JLabel("Nenhuma sala conectada");
    private final JLabel questionLabel = new JLabel("Aguardando uma pergunta...");
    private String roomId;
    private boolean host;
    private int currentQuestion = -1;

    public QuizPanel(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createButton = new JButton("Criar sala");
        JButton joinButton = new JButton("Entrar");
        JButton questionButton = new JButton("Enviar pergunta (host)");
        top.add(createButton);
        top.add(new JLabel("ID da sala:"));
        top.add(roomField);
        top.add(joinButton);
        top.add(questionButton);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(5, 5));
        center.add(roomLabel, BorderLayout.NORTH);
        questionLabel.setFont(questionLabel.getFont().deriveFont(Font.BOLD, 16f));
        center.add(questionLabel, BorderLayout.CENTER);
        center.add(answersPanel, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        log.setEditable(false);
        log.setRows(8);
        add(new JScrollPane(log), BorderLayout.SOUTH);

        createButton.addActionListener(e -> createRoom());
        joinButton.addActionListener(e -> joinRoom());
        questionButton.addActionListener(e -> sendQuestion());
    }

    private String userName() {
        String name = connectionManager.getUserName();
        return name == null || name.isBlank() ? "Jogador" : name;
    }

    private void createRoom() {
        roomId = UUID.randomUUID().toString().substring(0, 8);
        host = true;
        currentQuestion = -1;
        roomField.setText(roomId);
        roomLabel.setText("Você é o anfitrião. Sala: " + roomId);
        append("Sala criada. Compartilhe o ID com os outros jogadores.");
        broadcast(new QuizMessage("ROOM", roomId, userName(), null, null,
                -1, -1, -1, null));
    }

    private void joinRoom() {
        String requestedId = roomField.getText().trim();
        if (requestedId.isEmpty()) {
            append("Digite o ID da sala.");
            return;
        }
        roomId = requestedId;
        host = false;
        roomLabel.setText("Conectado à sala: " + roomId);
        broadcast(new QuizMessage("JOIN", roomId, userName(), null, null,
                -1, -1, -1, null));
        append("Solicitação de entrada enviada. Aguarde o anfitrião.");
    }

    private void sendQuestion() {
        if (!host || roomId == null) {
            append("Somente o anfitrião pode enviar perguntas.");
            return;
        }
        currentQuestion++;
        List<String> options = List.of(
                "Java", "Python", "HTML", "SQL"
        );
        QuizMessage message = new QuizMessage(
                "QUESTION", roomId, userName(),
                "Qual linguagem é conhecida por sua sintaxe simples e legibilidade?",
                options, 1, -1, currentQuestion, null
        );
        broadcast(message);
        showQuestion(message);
        append("Pergunta " + (currentQuestion + 1) + " enviada.");
    }

    private void sendAnswer(int answerIndex) {
        if (roomId == null || currentQuestion < 0) return;
        broadcast(new QuizMessage("ANSWER", roomId, userName(), null, null,
                -1, answerIndex, currentQuestion, null));
        for (Component component : answersPanel.getComponents()) {
            if (component instanceof JButton) component.setEnabled(false);
        }
        append("Resposta enviada.");
    }

    private void broadcast(QuizMessage message) {
        boolean sent = connectionManager.sendUdpMulticast(message);
        if (!sent) append("Não foi possível enviar a mensagem pela rede.");
    }

    public void receive(QuizMessage message) {
        if (message == null) return;

        if ("ROOM".equals(message.getAction())) {
            append("Sala anunciada por " + message.getSender()
                    + ". ID: " + message.getRoomId());
            return;
        }

        if (roomId == null || !roomId.equals(message.getRoomId())) return;

        switch (message.getAction()) {
            case "JOIN" -> {
                if (host) append(message.getSender() + " entrou na sala.");
            }
            case "QUESTION" -> showQuestion(message);
            case "ANSWER" -> {
                if (host && message.getQuestionNumber() == currentQuestion) {
                    boolean correct = message.getAnswerIndex() == 1;
                    append(message.getSender() + " respondeu "
                            + (correct ? "corretamente." : "incorretamente."));
                    // Protótipo: pontuação local do anfitrião exibida no log.
                    if (correct) append("Ponto para " + message.getSender() + ".");
                }
            }
            default -> { }
        }
    }

    private void showQuestion(QuizMessage message) {
        currentQuestion = message.getQuestionNumber();
        questionLabel.setText("<html>" + message.getQuestion() + "</html>");
        answersPanel.removeAll();
        List<String> options = message.getOptions();
        if (options != null) {
            for (int i = 0; i < options.size(); i++) {
                final int index = i;
                JButton button = new JButton(options.get(i));
                button.addActionListener(e -> sendAnswer(index));
                answersPanel.add(button);
            }
        }
        answersPanel.revalidate();
        answersPanel.repaint();
        append("Nova pergunta recebida.");
    }

    private void append(String text) {
        log.append(text + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }
}
