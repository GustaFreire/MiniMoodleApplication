package br.usp.redes.moodle.client;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.system.Sistema;
import br.usp.redes.moodle.thread.ThreadBuilder;

/**
 * @author Gustavo Freire 
 * Classe principal, cada execução representa um cliente diferente interagindo com o servidor. 
 * Obs: obrigatório executar a classe Main antes dessa.
 */
public class Client {

	//atributos da classe que serão utilizados
	private static Scanner sc = new Scanner(System.in);
	private static Sistema sistema = new Sistema();
	private static Usuario usuarioLogado = null;
	private static List<Usuario> usuarios = new Vector<>();

	public static void main(String[] args) throws Exception {
		System.out.println("Estabelecendo conexão com o servidor...");
		// se conecta com o servidor (que está rodando obrigatoriamente na porta 12345
		// da máquina local)
		Socket socket = new Socket("localhost", 12345);
		System.out.println("Conexão Estabelecida!");

		// instanciando classes auxiliares que são utilizadas pelo cliente
		ClientHelper helper = new ClientHelper(sistema, usuarioLogado, socket);
		ThreadBuilder montadorDeThreads = new ThreadBuilder(helper, sc);

		sistema.welcome(); // exibe mensagem de boas-vindas ao cliente

		// pegando a opcao do cliente e mandando para o servidor
		int opcao = sistema.escolherOpcao(sc);
		PrintStream out = new PrintStream(socket.getOutputStream());
		out.println(opcao);

		// encaminha o cliente a depender da opcao escolhida
		int retorno = encaminhaCliente(opcao, socket);

		// se veio do cadastro (retorno == 2) encaminha para o login
		if (retorno == 2) {
			getListaDeUsuariosCadastrados(socket);
			usuarioLogado = sistema.logar(usuarios, sc);
		}

		// envia cpf do usuario logado para o servidor, e valida se o login aconteceu
		// com sucesso
		enviaCpfUsuarioLogado(socket);
		boolean logou = getRetornoLogin(socket);

		// caso login sucesso = apresenta o menu e inicializa as threads do cliente
		if (logou) {
			System.out.println("Login efetuado com sucesso!");
			sistema.menu1Nivel(usuarioLogado);
			helper.setUsuarioLogado(usuarioLogado);
			inicializaThreadsCliente(montadorDeThreads);
		} else {
			System.out.println("Erro: ja existe um usuario com essas credenciais logado!");
		}

		// se chegar nesse ponto, fecha conexao com servidor e encerra
		System.out.println("Encerrando...");
		socket.close();
	}

	/**
	 * Método responsável por capturar opção digitada pelo cliente e encaminhar
	 * 
	 * @param opcao:  opcao digitada (1 = login, 2 = cadastro e 3 = sair)
	 * @param socket: objeto que representa a conexao com servidor
	 * @return 1 se logou, 2 se cadastrou ou 3 se saiu
	 */
	private static int encaminhaCliente(int opcao, Socket socket) throws Exception {
		if (opcao == 1) {
			usuarioLogado = sistema.logar(getListaDeUsuariosCadastrados(socket), sc);
			return 1; // logou
		} else if (opcao == 2) {
			Usuario usuario = sistema.cadastrar(getListaDeUsuariosCadastrados(socket), sc);
			enviaUsuarioCadastrado(usuario, socket);
			usuarios.add(usuario);
			return 2; // cadastrou
		} else if (opcao == 3) {
			sistema.sair();
			return 3; // saiu
		} else {
			System.out.println("Opção inválida! Tente novamente");
			System.exit(0);
			return 0; // invalida
		}
	}

	/*
	 * Método que apenas lê mensagem de retorno do login enviada pelo servidor,
	 * onde: msgRetornoLogin = sucess significa logou com sucesso, caso contrário
	 * servidor mandou mensagem failed, ou seja, nã logou com sucesso.
	 */
	@SuppressWarnings("resource")
	private static boolean getRetornoLogin(Socket socket) throws Exception {
		Scanner in = new Scanner(socket.getInputStream());
		String msgRetornoLogin = in.nextLine();
		if (msgRetornoLogin.equals("success")) {
			return true;
		}
		return false;
	}

	// Método que apenas lê a lista de usuários cadastrados enviada pelo servidor.
	@SuppressWarnings("unchecked")
	private static List<Usuario> getListaDeUsuariosCadastrados(Socket socket) throws Exception {
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		List<Usuario> usuarios = (List<Usuario>) in.readObject();
		return usuarios;
	}

	// Método que apenas escreve o usuário recém-cadastrado para o servidor.
	private static void enviaUsuarioCadastrado(Usuario usuario, Socket socket) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(usuario);
		oos.flush();
		byte[] usuarioAsBytes = baos.toByteArray();
		OutputStream os = socket.getOutputStream();
		os.write(usuarioAsBytes);
		os.flush();
	}

	// Método que apenas escreve o cpf do usuário logado para o servidor.
	private static void enviaCpfUsuarioLogado(Socket socket) throws Exception {
		PrintStream out = new PrintStream(socket.getOutputStream());
		out.println(usuarioLogado.getCpf());
		out.flush();
	}

	// Método que cria as threads de envio de comandos e recebimento de respostas, e as inicializa.
	private static void inicializaThreadsCliente(ThreadBuilder montadorDeThreads) throws Exception {
		Thread threadEnviaComando = montadorDeThreads.montaThreadEnviaComando();
		Thread threadRecebeResposta = montadorDeThreads.montaThreadRecebeResposta();
		threadRecebeResposta.start();
		threadEnviaComando.start();
		threadEnviaComando.join();
	}
}