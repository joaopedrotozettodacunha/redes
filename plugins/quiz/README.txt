INSTALACAO DO PLUGIN QUIZ

1. Copie a pasta 'quiz' deste pacote para a pasta 'plugins' do Pitiupi.
2. Abra plugins.registry.json e adicione este objeto dentro do array "plugins":
   {
     "id": "quiz",
     "dir": "plugins/quiz",
     "build": "ant",
     "jar": "dist/pitiupi.quiz.jar",
     "menuCategory": "Social"
   }
3. Na raiz do projeto, execute:
   scripts\pitiupi.cmd link --all
   scripts\pitiupi.cmd run --all

O prototipo usa as mensagens do Pitiupi por UDP multicast.
Crie uma sala, compartilhe o ID e os outros jogadores digitam esse ID e clicam Entrar.
O anfitriao envia a pergunta pelo botao correspondente.

LIMITACOES DESTA PRIMEIRA VERSAO:
- A pergunta de exemplo e fixa no codigo.
- O exemplo ainda nao implementa persistencia/placar completo.
- A resposta correta e o indice 1 (Python), definido no envio da pergunta.
- Os peers precisam estar na mesma rede com multicast UDP permitido.
