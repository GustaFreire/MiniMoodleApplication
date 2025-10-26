package br.usp.redes.moodle.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
	
	// CONSTANTE PARA PORTAS
	private static final int UDP_PORT = 12346;
	private static final int TCP_PORT = 12345;
	
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
		this.servidor = new ServerSocket(TCP_PORT);
		this.threadPool = Executors.newCachedThreadPool(new ThreadProducer());
		this.usuarios = new Vector<>();
		this.provasDisponiveis = new Vector<>();
		this.respostasDisponiveis = new Vector<>();
		this.correcoesDisponiveis = new Vector<>();
		this.contadorUsuariosOnline = 0;
		
		// Inicializa o listener UDP em uma nova thread
		new Thread(new UDPStatusListener(this)).start();
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
	
	/**
	 * Classe interna para ouvir requisições UDP na porta 12346 e responder com o status do servidor.
	*/
	private static class UDPStatusListener implements Runnable {
		private Server server;
		private static final int MAX_PACKET_SIZE = 1024;
		
		public UDPStatusListener(Server server) { //
			this.server = server;
		}

		@Override
		public void run() {
			try (DatagramSocket socket = new DatagramSocket(UDP_PORT)) {
				System.out.println("Servidor UDP iniciado na porta " + UDP_PORT + " para checagem de status.");
				byte[] receiveData = new byte[MAX_PACKET_SIZE];
				
				while (true) {
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					socket.receive(receivePacket); //Bloqueia até receber um pacote.
					
					// Prepara a resposta, incluindo o contador de usuários online e o total de provas.
					String statusMessage = "STATUS_ONLINE:" + server.getContadorUsuariosOnline() + 
					                       "|PROVAS_DISPONIVEIS:" + server.getProvasDisponiveis().size();
					
					byte[] sendData = statusMessage.getBytes();
					InetAddress IPAddress = receivePacket.getAddress();
					int port = receivePacket.getPort();
					
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
					socket.send(sendPacket); // Envia a resposta.
					
					System.out.println("UDP: Recebido pedido de status de " + IPAddress.getHostAddress() + 
					                   ":" + port + ". Respondido com: " + statusMessage);
				}
				
			} catch(Exception e) {
				System.err.println("Erro no listener UDP: " + e.getMessage());
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}