package br.usp.redes.moodle.commands;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import br.usp.redes.moodle.model.Avaliacao;
import br.usp.redes.moodle.model.Professor;
import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.server.Server;

/**
 * @author Gustavo Freire 
 * Classe que representa o comando de armazenar correção de prova
 */
public class ComandoGuardaCorrecao extends Comando {

	// construtor
	public ComandoGuardaCorrecao(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
		super(threadPool, usuario, servidor, socket);
	}

	@Override
	public void execute() { // execucao do comando, apenas armazena a prova corrigida pelo professor e sinaliza
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			Avaliacao provaCorrigida = (Avaliacao) in.readObject();
			Professor professor = (Professor) usuario;
			professor.adicionarProvaCorrigida(provaCorrigida);
			this.usuario = professor;
			servidor.adicionarCorrecaoCadastrada(provaCorrigida);
			System.out.println("Prova do aluno CPF: " + provaCorrigida.getRespostaProva().getCpfAluno()
					+ " corrigida pelo Professor CPF: "
					+ provaCorrigida.getRespostaProva().getProva().getCpfProfessorCriador());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}