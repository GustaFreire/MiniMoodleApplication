package br.usp.redes.moodle.client;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

import br.usp.redes.moodle.model.Aluno;
import br.usp.redes.moodle.model.Avaliacao;
import br.usp.redes.moodle.model.Professor;
import br.usp.redes.moodle.model.Prova;
import br.usp.redes.moodle.model.RespostaProva;
import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.system.Sistema;

/**
 * @author Gustavo Freire 
 * Classe auxiliar do Client, possui atributos e métodos que 
 * interagem diretamente com o servidor através do objeto socket
 */
@SuppressWarnings("unchecked")
public class ClientHelper {

	//constantes
	private static final int UDP_PORT = 12346;
	private static final int MAX_PACKET_SIZE = 1024;
	private static final String SERVER_HOST = "localhost";
	
	//atributos
	private Sistema sistema;
	private Usuario usuarioLogado;
	private Socket socket;
	private List<Prova> provasDisponiveis;
	private List<RespostaProva> respostasDisponiveis;
	private List<Avaliacao> notasDisponiveis;

	//construtor inicializa atributos
	public ClientHelper(Sistema sistema, Usuario usuarioLogado, Socket socket) {
		this.sistema = sistema;
		this.usuarioLogado = usuarioLogado;
		this.socket = socket;
		this.provasDisponiveis = new Vector<>();
		this.respostasDisponiveis = new Vector<>();
		this.notasDisponiveis = new Vector<>();
	}

	/**
	 * Método responsável por enviar uma requisição UDP ao servidor para verificar o status.
	*/
	public void verificarStatusServidorUDP() {
		try (DatagramSocket clientSocket = new DatagramSocket()) {
			InetAddress IPAddress = InetAddress.getByName(SERVER_HOST);
			
			String statusRequest = "STATUS_REQUEST"; // Mensagem simples de requisição.
			byte[] sendData = statusRequest.getBytes();
			
			// Envia o pacote UDP.
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, UDP_PORT);
			clientSocket.send(sendPacket);
			
			System.out.println("Requisição de status (UDP) enviada para o servidor na porta " + UDP_PORT);
			
			// Prepara para receber a resposta.
			byte[] receiveData = new byte[MAX_PACKET_SIZE];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			
			clientSocket.setSoTimeout(3000); // 3 segundos de timeout para não bloquear indefinidamente
			
			// Recebe o pacote UDP de resposta.
			clientSocket.receive(receivePacket);
			
			// Processa a resposta.
			String response = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
			processarRespostaStatus(response);
			
		} catch (java.net.SocketTimeoutException e) {
			System.out.println("Timeout: O servidor não respondeu à requisição de status UDP."); //
		} catch (Exception e) {
			System.err.println("Erro na comunicação UDP: " + e.getMessage()); //
		}
	}
	
	/**
	 * Processa a string de resposta recebida via UDP.
	*/
	private void processarRespostaStatus(String response) {
		System.out.println("------------------------- STATUS DO SERVIDOR (UDP) -------------------------");
		try {
			// Espera um formato como: STATUS_ONLINE:X|PROVAS_DISPONIVEIS:Y
			String[] parts = response.split("\\|");
			int onlineUsers = 0;
			int availableExams = 0;
			
			for (String part : parts) { //
				if (part.startsWith("STATUS_ONLINE:")) {
					onlineUsers = Integer.parseInt(part.substring("STATUS_ONLINE:".length()));
				} else if (part.startsWith("PROVAS_DISPONIVEIS:")) {
					availableExams = Integer.parseInt(part.substring("PROVAS_DISPONIVEIS:".length()));
				}
			}
			
			System.out.println("-> Usuários Online: " + onlineUsers);
			System.out.println("-> Provas Cadastradas (Total): " + availableExams);
			System.out.println("--------------------------------------------------------------------------");
		} catch (Exception e) {
			System.out.println("Erro ao processar a resposta do servidor: " + response);
		}
	}

	/**
	 * Método responsável por capturar as provas disponíveis no servidor, e disponibilizar para o Aluno
	 * 
	 * @return booleano indicando se recebeu provas ou não (pois pode não ter provas para o Aluno logado realizar)
	 */
	public boolean recebeProvasServidor() throws Exception {
		//lê a lista de todas as provas que foram cadastradas pelos professores no servidor
		System.out.println("Recebendo provas do servidor");
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		List<Prova> provas = (List<Prova>) in.readObject();
		provasDisponiveis = provas;

		if (provasDisponiveis.size() == 0) { //não tem nenhuma prova no servidor no momento
			System.out.println("Não há provas disponíveis para realizar, converse com seu professor!");
			sistema.menu2Nivel(usuarioLogado);
			return false;
		} else { //veio provas, agora filtra apenas as que o Aluno nao resolveu ainda
			
			Aluno aluno = (Aluno) usuarioLogado;
			//listas de provas filtradas e respostas de prova do aluno 
			List<Prova> provasVisiveisAoAluno = new Vector<>(); 
			List<RespostaProva> minhasRespostas = aluno.getMinhasRespostas();
			
			for (Prova prova : provasDisponiveis) { //para cada prova cadastrada no servidor
				boolean podeDisponibilizarProva = true;
				if (minhasRespostas.size() > 0) { //se existem respostas de provas para o aluno logado
					
					for (RespostaProva respostaProva : minhasRespostas) { //para cada resposta de prova do aluno logado
						
						//se o nome da prova referente a essa resposta for igual o nome da prova atual, 
						//o Aluno ja respondeu essa prova, não disponibiliza a prova atual para ele
						if (respostaProva.getProva().getNome().equals(prova.getNome())) {
							podeDisponibilizarProva = false;
							break;
						}
					}
					if (podeDisponibilizarProva) { //chegou nesse ponto: prova pode ser disponibilizada
						provasVisiveisAoAluno.add(prova);
					}
				} else { //chegou nesse ponto: aluno ainda não tem respostas de prova, ou seja, prova pode ser disponibilizada
					provasVisiveisAoAluno.add(prova);
				}
			}

			//após sair do loop das provas, checa se a lista resultante está vazia ou nao, e retorna que tem provas (ou não).
			if (provasVisiveisAoAluno.size() == 0) {
				System.out.println("Não há provas disponíveis para você realizar :(");
				sistema.menu2Nivel(usuarioLogado);
				return false;
			} else {
				provasDisponiveis = provasVisiveisAoAluno;
				return true;
			}
		}
	}

	/**
	 * Método responsável por capturar as respostas de provas disponíveis no servidor, e disponibilizar para o Professor
	 * 
	 * @return booleano indicando se recebeu respostas ou não (pois pode não ter respostas de provas para o Professor logado corrigir)
	 */
	public boolean recebeRespostasServidor() throws Exception {
		//lê a lista de todas as respostas de provas que foram cadastradas pelos alunos no servidor
		System.out.println("Recebendo respostas do servidor");
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		List<RespostaProva> respostas = (List<RespostaProva>) in.readObject();
		respostasDisponiveis = respostas;

		if (respostasDisponiveis.size() == 0) { //não tem nenhuma resposta de prova no servidor no momento
			System.out.println("Não há respostas disponíveis para avaliar, converse com os alunos!");
			sistema.menu2Nivel(usuarioLogado);
			return false;
		} else { //veio respostas de provas, agora filtra apenas as que o Professor nao corrigiu ainda
			
			Professor professor = (Professor) usuarioLogado;
			//listas de respostas de provas filtradas e provas corrigidas do professor
			List<RespostaProva> respostasVisiveisAoProfessor = new Vector<>();
			List<Avaliacao> minhasProvasCorrigidas = professor.getMinhasProvasCorrigidas();
			
			for (RespostaProva resposta : respostasDisponiveis) { //para cada resposta de prova cadastrada no servidor
				boolean podeDisponibilizarProvaCorrigida = true;
				if (minhasProvasCorrigidas.size() > 0) { //se existem provas corrigidas para o professor logado
					
					for (Avaliacao minhaProvaCorrigida : minhasProvasCorrigidas) { //para cada prova corrigida do professor logado

						//checa três condições para que a prova corrigida seja a correcao da resposta de prova atual:
						//cpfAlunoIgual = o cpf do aluno da prova corrigida é o mesmo cpf da resposta de prova
						//cpfProfessorIgual = o cpf do professor da prova é o mesmo cpf da resposta de prova
						//nomeProvaIgual = nome da prova respondida é o mesmo nome da prova corrigida
						
						boolean cpfAlunoIgual = minhaProvaCorrigida.getRespostaProva().getCpfAluno()
								.equals(resposta.getCpfAluno());

						boolean cpfProfessorIgual = minhaProvaCorrigida.getRespostaProva().getProva()
								.getCpfProfessorCriador().equals(resposta.getProva().getCpfProfessorCriador());

						boolean nomeProvaIgual = minhaProvaCorrigida.getRespostaProva().getProva().getNome()
								.equals(resposta.getProva().getNome());

						if (cpfAlunoIgual && cpfProfessorIgual && nomeProvaIgual) {
							podeDisponibilizarProvaCorrigida = false;
						}
					}
					if (podeDisponibilizarProvaCorrigida) { //chegou nesse ponto: resposta de prova pode ser disponibilizada
						respostasVisiveisAoProfessor.add(resposta);
					}
				} else { //chegou nesse ponto: professor ainda não tem provas corrigidas, ou seja, resposta de prova pode ser disponibilizada
					respostasVisiveisAoProfessor.add(resposta);
				}
			}
			
			//após sair do loop das respostas de prova, checa se a lista resultante está vazia ou nao, e retorna que tem respostas (ou não).
			if (respostasVisiveisAoProfessor.size() == 0) {
				System.out.println("Não há respostas disponíveis para você corrigir :(");
				sistema.menu2Nivel(usuarioLogado);
				return false;
			} else {
				respostasDisponiveis = respostasVisiveisAoProfessor;
				return true;
			}
		}
	}

	/**
	 * Método responsável por capturar as notas de provas disponíveis no servidor, e disponibilizar para o Aluno
	 * 
	 * @return booleano indicando se recebeu notas ou não (pois pode não ter notas de provas para o Aluno logado ver)
	 */
	public boolean recebeNotasServidor() throws Exception {
		//lê a lista de todas as notas de provas que foram cadastradas pelos professores no servidor
		System.out.println("Recebendo notas do servidor");
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		List<Avaliacao> notas = (List<Avaliacao>) in.readObject();
		notasDisponiveis = notas;

		if (notasDisponiveis.size() == 0) { //não tem nenhuma nota de prova no servidor no momento
			System.out.println("Não há notas disponíveis para exibir, converse com seu professor!");
			sistema.menu2Nivel(usuarioLogado);
			return false;
		} else { //veio notas de provas, agora filtra apenas as notas do Aluno logado
			
			List<Avaliacao> notasVisiveisAoAluno = new Vector<>(); //listas de notas de provas filtradas
			
			for (Avaliacao avaliacao : notasDisponiveis) { //para cada nota de prova cadastrada no servidor
				
				//cpf do aluno da prova corrigida for o mesmo do cpf do aluno logado: a nota é dele
				if (avaliacao.getRespostaProva().getCpfAluno().equals(usuarioLogado.getCpf())) {
					notasVisiveisAoAluno.add(avaliacao);
				}
			}
			
			//após sair do loop das notas de prova, checa se a lista resultante está vazia ou nao, e retorna que tem notas (ou não).
			if (notasVisiveisAoAluno.size() == 0) {
				System.out.println("Ainda não há notas lançadas para suas provas!");
				sistema.menu2Nivel(usuarioLogado);
				return false;
			} else {
				notasDisponiveis = notasVisiveisAoAluno;
				return true;
			}
		}
	}

	// Método que apenas escreve a prova criada para o servidor.
	public void enviarProvaCadastrada(Prova prova) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(prova);
		oos.flush();
		byte[] provaAsBytes = baos.toByteArray();
		baos.flush();
		OutputStream os = socket.getOutputStream();
		os.write(provaAsBytes);
		os.flush();
	}

	// Método que apenas escreve a resposta de prova criada para o servidor.
	public void enviarRespostaCadastrada(RespostaProva respostaProva) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(respostaProva);
		oos.flush();
		byte[] provaAsBytes = baos.toByteArray();
		baos.flush();
		OutputStream os = socket.getOutputStream();
		os.write(provaAsBytes);
		os.flush();
	}

	// Método que apenas escreve a prova corrigida para o servidor.
	public void enviarAvaliacaoCadastrada(Avaliacao provaCorrigida) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(provaCorrigida);
		oos.flush();
		byte[] correcaoAsBytes = baos.toByteArray();
		baos.flush();
		OutputStream os = socket.getOutputStream();
		os.write(correcaoAsBytes);
		os.flush();
	}

	// Manda mensagem para o servidor sinalizando que já pode guardar a prova cadastrada
	public void enviaMsgProvaCriada() throws Exception {
		PrintStream out = new PrintStream(socket.getOutputStream());
		out.println("Armazenar Prova Criada");
		out.flush();
	}

	// Manda mensagem para o servidor sinalizando que já pode guardar a resposta de prova cadastrada
	public void enviaMsgRespostaCriada() throws Exception {
		PrintStream out = new PrintStream(socket.getOutputStream());
		out.println("Armazenar Resposta Criada");
		out.flush();
	}

	// Manda mensagem para o servidor sinalizando que já pode guardar a correção de prova cadastrada
	public void enviaMsgAvaliacaoCriada() throws Exception {
		PrintStream out = new PrintStream(socket.getOutputStream());
		out.println("Armazenar Correcao Criada");
		out.flush();
	}

	//getters e setters
	
	public Socket getSocket() {
		return socket;
	}

	public Sistema getSistema() {
		return sistema;
	}

	public Usuario getUsuarioLogado() {
		return usuarioLogado;
	}

	public void setUsuarioLogado(Usuario usuarioLogado) {
		this.usuarioLogado = usuarioLogado;
	}

	public List<Prova> getProvasDisponiveis() {
		return provasDisponiveis;
	}

	public List<RespostaProva> getRespostasDisponiveis() {
		return respostasDisponiveis;
	}

	public List<Avaliacao> getNotasDisponiveis() {
		return notasDisponiveis;
	}
}