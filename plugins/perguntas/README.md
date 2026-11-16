# Perguntas e respostas

Este plugin funciona como um quiz compartilhado:

1. Qualquer participante cria uma pergunta, preenche quatro alternativas e marca a correta.
2. A pergunta aparece para os outros participantes, que escolhem uma alternativa e enviam a resposta.
3. Quem publicou a pergunta valida as respostas. Cada acerto vale 1 ponto e o placar é atualizado em todos os computadores.

A alternativa correta não é enviada junto com a pergunta; o computador de quem a publicou divulga o resultado depois que cada participante responde. Por isso, essa pessoa precisa continuar conectada. O placar vale durante a sessão e não é sincronizado para quem entrar depois.

As mensagens são enviadas por UDP multicast. Para jogar, todos precisam estar na mesma sala/rede Pitiupi, com a mesma porta configurada e multicast permitido pela rede e pelo firewall.

O script `scripts/pitiupi.cmd run --all` ou `scripts/pitiupi.sh run --all` compila e instala este plugin a partir do catálogo do projeto.
