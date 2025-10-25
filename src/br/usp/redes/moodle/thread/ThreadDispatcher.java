package br.usp.redes.moodle.thread;

import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

import br.usp.redes.moodle.commands.Command;
import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.commands.Comando1;
import br.usp.redes.moodle.commands.Comando2;
import br.usp.redes.moodle.commands.Comando3;
import br.usp.redes.moodle.commands.ComandoGuardaCorrecao;
import br.usp.redes.moodle.commands.ComandoGuardaProva;
import br.usp.redes.moodle.commands.ComandoGuardaResposta;
import br.usp.redes.moodle.server.Server;

/**
 * @author Gustavo Freire 
 * Classe que distribui as tarefas do cliente (comandos)	
 */
public class ThreadDispatcher implements Runnable {

	// atributos
	private Socket socket;
	private ExecutorService threadPool;
	private Usuario usuario;
	private Server servidor;

	// construtor
	public ThreadDispatcher(ExecutorService threadPool, Socket socket, Usuario usuario, Server servidor) {
		this.threadPool = threadPool;
		this.socket = socket;
		this.usuario = usuario;
		this.servidor = servidor;
	}

	@Override
	public void run() { //execucao da thread
		try {
			System.out.println("Capturando comandos do cliente: " + this.usuario.getNome());
			Scanner in = new Scanner(socket.getInputStream());
			PrintStream out = new PrintStream(socket.getOutputStream());
			
			//recebe os comandos do cliente 
			while (in.hasNextLine()) {
				//faz tratamento para evitar bug
				String comando = in.nextLine();
				if (comando.startsWith("y")) {
					comando = comando.substring(1);
				}
				System.out.println("Comando recebido: " + comando);
				
				//encaminha para o comando respectivo
				if (comando.equals("1")) {
					executarComando1(threadPool, usuario, servidor, socket);
				} else if (comando.equals("2")) {
					executarComando2(threadPool, usuario, servidor, socket);
				} else if (comando.equals("3")) {
					executarComando3(threadPool, usuario, servidor, socket);
				} else if (comando.equals("Armazenar Prova Criada")) {
					excutarComandoGuardaProva(threadPool, usuario, servidor, socket);
				} else if (comando.equals("Armazenar Resposta Criada")) {
					excutarComandoGuardaResposta(threadPool, usuario, servidor, socket);
				} else if (comando.equals("Armazenar Correcao Criada")) {
					excutarComandoGuardaCorrecao(threadPool, usuario, servidor, socket);
				} else {
					out.println("Comando Desconhecido");
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	//métodos usados internamento para instanciar commando e executa-los
	
	private void executarComando1(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
		Command command = new Comando1(threadPool, usuario, servidor, socket);
		command.execute();
	}

	private void executarComando2(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
		Command command = new Comando2(threadPool, usuario, servidor, socket);
		command.execute();
	}
	
	private void executarComando3(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
		Command command = new Comando3(threadPool, usuario, servidor, socket);
		command.execute();
	}
	
	private void excutarComandoGuardaProva(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
		Command command = new ComandoGuardaProva(threadPool, usuario, servidor, socket);
		command.execute();
	}
	
	private void excutarComandoGuardaResposta(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
		Command command = new ComandoGuardaResposta(threadPool, usuario, servidor, socket);
		command.execute();
	}
	
	private void excutarComandoGuardaCorrecao(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
		Command command = new ComandoGuardaCorrecao(threadPool, usuario, servidor, socket);
		command.execute();
	}
}