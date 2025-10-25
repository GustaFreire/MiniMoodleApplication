package br.usp.redes.moodle.commands;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;

import br.usp.redes.moodle.model.Avaliacao;
import br.usp.redes.moodle.model.RespostaProva;
import br.usp.redes.moodle.model.Tipo;
import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.server.Server;

/**
 * @author Gustavo Freire 
 * Classe que representa o comando 2 (correção de prova para o professor, ou verificação de nota pelo aluno)
 */
public class Comando2 extends Comando {

	// construtor
	public Comando2(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
		super(threadPool, usuario, servidor, socket);
	}

	// execucao do comando, dependendo do tipo de usuario encaminha para o método respectivo
	@Override
	public void execute() {
		String comando;
		if (this.usuario.getTipo().equals(Tipo.PROFESSOR)) {
			comando = "Comando recebido pelo servidor (correção de prova)";
			corrigirProva(comando);
		} else {
			comando = "Comando recebido pelo servidor (verificação de nota)";
			verNota(comando);
		}
	}

	// sinaliza para o professor que já pode corrigir uma prova (manda as respostas cadastradas)
	private void corrigirProva(String comando) {
		try {
			PrintStream out = new PrintStream(socket.getOutputStream());
			out.println(comando);
			out.println("corrigirProva");

			List<RespostaProva> respostas = servidor.getRespostasDisponiveis();
			System.out.println("Enviando " + respostas.size() + " respostas para o professor " + usuario.getNome());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(respostas);
			oos.flush();
			byte[] provaAsBytes = baos.toByteArray();
			baos.flush();
			OutputStream os = socket.getOutputStream();
			os.write(provaAsBytes);
			os.flush();

			System.out.println("Mandou as respostas para o professor");

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// sinaliza para o aluno que já pode ver as notas (manda as notas cadastradas)
	private void verNota(String comando) {
		try {
			PrintStream out = new PrintStream(socket.getOutputStream());
			out.println(comando);
			out.println("exibirNotas");

			List<Avaliacao> notas = servidor.getCorrecoesDisponiveis();
			System.out.println("Enviando " + notas.size() + " notas para o aluno " + usuario.getNome());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(notas);
			oos.flush();
			byte[] provaAsBytes = baos.toByteArray();
			baos.flush();
			OutputStream os = socket.getOutputStream();
			os.write(provaAsBytes);
			os.flush();

			System.out.println("Mandou as notas para o aluno");

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}