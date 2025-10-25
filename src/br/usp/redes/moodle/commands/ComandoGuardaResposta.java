package br.usp.redes.moodle.commands;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import br.usp.redes.moodle.model.Aluno;
import br.usp.redes.moodle.model.RespostaProva;
import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.server.Server;

/**
 * @author Gustavo Freire 
 * Classe que representa o comando de armazenar resposta de prova
 */
public class ComandoGuardaResposta extends Comando {

	// construtor
	public ComandoGuardaResposta(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
		super(threadPool, usuario, servidor, socket);
	}

	@Override
	public void execute() { // execucao do comando, apenas armazena a resposta de prova criada pelo aluno e sinaliza
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			RespostaProva respostaProva = (RespostaProva) in.readObject();
			Aluno aluno = (Aluno) usuario;
			aluno.adicionarRespostaProva(respostaProva);
			this.usuario = aluno;
			servidor.adicionarRespostaCadastrada(respostaProva);
			System.out.println("Resposta do aluno CPF: " + respostaProva.getCpfAluno() + " da prova: "
					+ respostaProva.getProva().getNome() + " adicionada no servidor");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}