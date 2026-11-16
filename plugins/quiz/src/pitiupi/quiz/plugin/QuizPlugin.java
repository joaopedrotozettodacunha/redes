package pitiupi.quiz.plugin;

import pitiupi.api.plugin.PluginWorkspace;
import pitiupi.control.ConnectionManager;
import pitiupi.models.Messages.Message;
import pitiupi.models.Plugin;
import pitiupi.quiz.GUI.QuizPanel;
import pitiupi.quiz.net.QuizMessage;

import javax.swing.SwingUtilities;
import java.util.List;

public class QuizPlugin extends Plugin {
    private QuizPanel panel;

    @Override
    public String getName() { return "Quiz"; }

    @Override
    public String getDisplayName() { return "Quiz multiplayer"; }

    @Override
    public String getAuthor() { return "Projeto acadêmico"; }

    @Override
    public String getVersion() { return "0.1"; }

    @Override
    public String getIdentifier() { return "Pitiupi.Quiz"; }

    @Override
    public void createPlugin(ConnectionManager connectionManager, PluginWorkspace workspace) {
        panel = new QuizPanel(connectionManager);
        bindShellWorkspaceContent(panel);
    }

    @Override
    public List<Class<? extends Message>> getMessages() {
        return List.of(QuizMessage.class);
    }

    @Override
    public void receiveMessage(Message message) {
        if (message instanceof QuizMessage quizMessage && panel != null) {
            SwingUtilities.invokeLater(() -> panel.receive(quizMessage));
        }
    }
}
