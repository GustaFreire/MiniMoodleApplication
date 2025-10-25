package br.usp.redes.moodle.thread;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.server.Server;

/**
 * @author Gustavo Freire 
 * Classe que representa a thread inicial criada pelo servidor, 
 * que encaminha cada cliente para login ou cadastro (atua do lado do servidor é criada para cada cliente)
 */
public class ThreadLoginCadastro implements Runnable {

	// atributos
	private Socket socket;
	private Server server;

	// construtor
	public ThreadLoginCadastro(Socket socket, Server server) {
		this.socket = socket;
		this.server = server;
	}

	@Override
	public void run() { //execucao da thread
		try {
			//recebe a opção digitada (1 = login, 2 = cadastro) e sinaliza
			Scanner in = new Scanner(socket.getInputStream());
			int opcao = in.nextInt();
			System.out.println("Opcao recebida: " + opcao);
			in.nextLine();
			
			//se opção é login, envia lista de usuários cadastrados para o cliente
			if (opcao == 1) {
				enviaListaDeUsuariosCadastrados(socket);
			} 
			//se opção é cadastro, envia lista de usuários cadastrados para o cliente, pega o novo usuário criado e manda a lista de novo
			else if (opcao == 2) { 
				enviaListaDeUsuariosCadastrados(socket);
				obtemUsuarioCadastrado(socket);
				enviaListaDeUsuariosCadastrados(socket);
			}
			
			try {
				//captura cpf do usuário enviado pelo cliente e faz tratamento para evitar bug
				String cpfUsuarioLogado = in.nextLine();
				if (cpfUsuarioLogado.startsWith("y")) {
					cpfUsuarioLogado = cpfUsuarioLogado.substring(1);
				}
				
				/*busca usuário pelo cpf, se nao encontrar ou já estiver logado sinaliza login: failed, 
				se encontrar e não estiver logado sinaliza login: sucess*/
				Usuario user = buscaUsuarioPorCpf(cpfUsuarioLogado);
				if (user == null) {
					System.out.println("Usuario informado nao existe.");
					enviaMensagemClienteLogin("failed", socket);
				} else {
					if (!user.getLogado()) {
						user.setLogado(true);
						enviaMensagemClienteLogin("success", socket);
						this.server.adicionarUsuarioOnline();
						System.out.println("Usuários Online: " + this.server.getContadorUsuariosOnline());
						//se chegou nesse ponto, monta e executa a thread que recebe os comandos do cliente
						ThreadDispatcher dispatcher = new ThreadDispatcher(this.server.getThreadPool(), socket, user, server);
						this.server.getThreadPool().execute(dispatcher);
					} else {
						System.out.println("Usuario informado " + user.getNome() + " ja esta logado no sistema.");
						enviaMensagemClienteLogin("failed", socket);
					}
				}
			} catch (NoSuchElementException e) {}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}

	// envia texto para o cliente referente ao login (sucess ou failed)
	private void enviaMensagemClienteLogin(String mensagem, Socket socket) throws Exception {
		PrintStream out = new PrintStream(socket.getOutputStream());
		out.println(mensagem);
		out.flush();
	}
	
	// auxiliar que varre a lista de usuários e busca pelo CPF
	private Usuario buscaUsuarioPorCpf(String cpfUsuarioLogado) {
		for (Usuario user: server.getUsuarios()) {
			if (user.getCpf().equals(cpfUsuarioLogado)) {
				return user;
			}
		}
		return null;
	}

	// envia para o cliente a lista de usuários cadastrados no servidor
	private void enviaListaDeUsuariosCadastrados(Socket socket) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(this.server.getUsuarios());
		oos.flush();
		byte[] usuariosAsBytes = baos.toByteArray();
		OutputStream os = socket.getOutputStream();
		os.write(usuariosAsBytes);
		os.flush();
	}
	
	// obtém o novo usuário cadastrado e armazena na lista de usuários cadastrados, e sinaliza
	private void obtemUsuarioCadastrado(Socket socket) throws Exception {
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		Usuario usuario = (Usuario) in.readObject();
		this.server.getUsuarios().add(usuario);
		System.out.println("Novo usuario cadastrado, lista atual de usuarios: " + this.server.getUsuarios().size());
	}
}