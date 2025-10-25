package br.usp.redes.moodle.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.usp.redes.moodle.model.Avaliacao;
import br.usp.redes.moodle.model.Prova;
import br.usp.redes.moodle.model.RespostaProva;
import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.thread.ThreadLoginCadastro;
import br.usp.redes.moodle.thread.ThreadProducer;

/**
 * @author Gustavo Freire
 * Classe principal, por meio dela o servidor é criado (executado na classe Main.java).
 */
public class Server {

	// atributos
	private ServerSocket servidor;
	private ExecutorService threadPool;
	private List<Usuario> usuarios;
	private List<Prova> provasDisponiveis;
	private List<RespostaProva> respostasDisponiveis;
	private List<Avaliacao> correcoesDisponiveis;
	private Integer contadorUsuariosOnline;

	// construtor inicializa atributos
	public Server() throws IOException {
		System.out.println("------------------------ Iniciando Servidor ------------------------");
		this.servidor = new ServerSocket(12345);
		this.threadPool = Executors.newCachedThreadPool(new ThreadProducer());
		this.usuarios = new Vector<>();
		this.provasDisponiveis = new Vector<>();
		this.respostasDisponiveis = new Vector<>();
		this.correcoesDisponiveis = new Vector<>();
		this.contadorUsuariosOnline = 0;
	}

	// inicia o servidor (roda sem parar)
	public void start() throws Exception {
		while (true) {
			try {
				//aceita nova conexão com cliente, e monta/cria thread de login/cadastro
				Socket socket = this.servidor.accept();
				System.out.println("Aceitando novo cliente na porta " + socket.getPort());
				ThreadLoginCadastro threadInicial = new ThreadLoginCadastro(socket, this);
				this.threadPool.execute(threadInicial);
				
			} catch (SocketException e) {
				System.out.println("SocketException");
			}
		}
	}

	//métodos de adição nas listas de provas, respostas, correções e usuários
	
	public void adicionarProvaCadastrada(Prova prova) {
		this.provasDisponiveis.add(prova);
	}
	
	public void adicionarRespostaCadastrada(RespostaProva respostaProva) {
		this.respostasDisponiveis.add(respostaProva);
	}
	
	public void adicionarCorrecaoCadastrada(Avaliacao provaCorrigida) {
		this.correcoesDisponiveis.add(provaCorrigida);
	}
	
	//manipula variavel de usuários online
	
	public void adicionarUsuarioOnline() {
		this.contadorUsuariosOnline++;
	}
	
	public void logoutUsuario() {
		this.contadorUsuariosOnline--;
	}
	
	public Integer getContadorUsuariosOnline() {
		return contadorUsuariosOnline;
	}
	
	//getters
	
	public ExecutorService getThreadPool() {
		return threadPool;
	}
	
	public List<Usuario> getUsuarios() {
		return usuarios;
	}
	
	public List<Prova> getProvasDisponiveis() {
		return provasDisponiveis;
	}
	
	public List<RespostaProva> getRespostasDisponiveis() {
		return respostasDisponiveis;
	}
	
	public List<Avaliacao> getCorrecoesDisponiveis() {
		return correcoesDisponiveis;
	}
}