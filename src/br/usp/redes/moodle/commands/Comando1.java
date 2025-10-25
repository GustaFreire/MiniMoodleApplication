package br.usp.redes.moodle.commands;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;

import br.usp.redes.moodle.model.Prova;
import br.usp.redes.moodle.model.Tipo;
import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.server.Server;

/**
 * @author Gustavo Freire 
 * Classe que representa o comando 1 (criação de prova para o professor, ou realização de prova pelo aluno)
 */
public class Comando1 extends Comando {

	// construtor
	public Comando1(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
		super(threadPool, usuario, servidor, socket);
	}

	// execucao do comando, dependendo do tipo de usuario encaminha para o método respectivo
	@Override
	public void execute() {
		String comando;
		if (this.usuario.getTipo().equals(Tipo.PROFESSOR)) {
			comando = "Digite o código da disciplina da prova:";
			criarProva(comando);
		} else {
			comando = "Comando recebido pelo servidor (realização de prova)";
			realizarProva(comando);
		}
	}

	// sinaliza para o professor que já pode criar a prova
	private void criarProva(String comando) {
		try {
			PrintStream out = new PrintStream(socket.getOutputStream());
			out.println(comando);
			out.println("criarProva");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// sinaliza para o aluno que já pode responder uma prova (manda as provas cadastradas)
	private void realizarProva(String comando) {
		try {
			PrintStream out = new PrintStream(socket.getOutputStream());
			out.println(comando);
			out.println("realizarProva");

			List<Prova> provas = servidor.getProvasDisponiveis();
			System.out.println("Enviando " + provas.size() + " provas para o aluno " + usuario.getNome());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(provas);
			oos.flush();
			byte[] provaAsBytes = baos.toByteArray();
			baos.flush();
			OutputStream os = socket.getOutputStream();
			os.write(provaAsBytes);
			os.flush();

			System.out.println("Mandou as provas para o aluno");
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}