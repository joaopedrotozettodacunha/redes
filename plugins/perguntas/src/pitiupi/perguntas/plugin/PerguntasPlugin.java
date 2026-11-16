package pitiupi.perguntas.plugin;

import pitiupi.api.plugin.PluginWorkspace;
import pitiupi.control.ConnectionManager;
import pitiupi.models.Messages.Message;
import pitiupi.models.Plugin;
import pitiupi.perguntas.GUI.PerguntasPanel;
import pitiupi.perguntas.net.PerguntasMessage;

import javax.swing.SwingUtilities;
import java.util.List;

public class PerguntasPlugin extends Plugin {

    private PerguntasPanel panel;

    @Override
    public String getName() {
        return "Perguntas";
    }

    @Override
    public String getDisplayName() {
        return "Perguntas e respostas";
    }

    @Override
    public String getAuthor() {
        return "Projeto Pitiupi";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getIdentifier() {
        return "Pitiupi.Perguntas";
    }

    @Override
    public void createPlugin(ConnectionManager connectionManager, PluginWorkspace workspace) {
        panel = new PerguntasPanel(connectionManager);
        bindShellWorkspaceContent(panel);
    }

    @Override
    public List<Class<? extends Message>> getMessages() {
        return List.of(PerguntasMessage.class);
    }

    @Override
    public void receiveMessage(Message message) {
        if (message instanceof PerguntasMessage perguntasMessage && panel != null) {
            SwingUtilities.invokeLater(() -> panel.receive(perguntasMessage));
        }
    }
}
