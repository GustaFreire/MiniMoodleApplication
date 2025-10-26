package br.usp.redes.moodle.thread;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import br.usp.redes.moodle.client.ClientHelper;
import br.usp.redes.moodle.model.Aluno;
import br.usp.redes.moodle.model.Avaliacao;
import br.usp.redes.moodle.model.Professor;
import br.usp.redes.moodle.model.Prova;
import br.usp.redes.moodle.model.RespostaProva;

/**
 * @author Gustavo Freire 
 * Classe auxiliar do Client, possui atributos e métodos que 
 * realiza montagem e execução das threads do cliente
 */
public class ThreadBuilder {

	//atributos da classe, para controle de execução de threads
	private static AtomicBoolean isThreadCriaProvaRodando;
	private static AtomicBoolean isThreadFazProvaRodando;
	private static AtomicBoolean isThreadCorrigeProvaRodando;
	private static AtomicBoolean isThreadExibeNotasRodando;
	private static AtomicBoolean isThreadVerificaStatusRodando;

	//helper do cliente e scanner
	private ClientHelper helper;
	private Scanner sc;

	// construtor inicializa atributos
	public ThreadBuilder(ClientHelper helper, Scanner sc) {
		this.helper = helper;
		this.sc = sc;
		isThreadCriaProvaRodando = new AtomicBoolean(false);
		isThreadFazProvaRodando = new AtomicBoolean(false);
		isThreadCorrigeProvaRodando = new AtomicBoolean(false);
		isThreadExibeNotasRodando = new AtomicBoolean(false);
		isThreadVerificaStatusRodando = new AtomicBoolean(false);
	}

	/**
	 * Método responsável por criar a thread que envia os comandos do cliente para o servidor
	 * @return a thread envio de comandos montada
	 */
	public Thread montaThreadEnviaComando() {
		return new Thread(() -> {
			try {
				PrintStream out = new PrintStream(helper.getSocket().getOutputStream());
				
				/*além de enviar comandos, faz também o controle das execuções das threads:
				criação de prova, realização de prova, correção de prova e verificação de notas*/
				while (sc.hasNextLine()) {
					while (isThreadCriaProvaRodando.get()) {
						Thread threadCriaProva = montaThreadCriaProva();
						threadCriaProva.start();
						threadCriaProva.join();
					}
					while (isThreadFazProvaRodando.get()) {
						Thread threadFazProva = montaThreadFazProva();
						threadFazProva.start();
						threadFazProva.join();
					}
					while (isThreadCorrigeProvaRodando.get()) {
						Thread threadCorrigeProva = montaThreadCorrigeProva();
						threadCorrigeProva.start();
						threadCorrigeProva.join();
					}
					while (isThreadExibeNotasRodando.get()) {
						Thread threadMostraNotas = montaThreadMostraNotas();
						threadMostraNotas.start();
						threadMostraNotas.join();
					}
					while (isThreadVerificaStatusRodando.get()) {
						Thread threadVerificaStatus = montaThreadVerificaStatus();
						threadVerificaStatus.start();
						threadVerificaStatus.join();
					}
					String linha = sc.nextLine();
					out.println(linha);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Método responsável por criar a thread que recebe as respostas do servidor
	 * @return a thread recebimento de respostas montada
	 */
	public Thread montaThreadRecebeResposta() {
		return new Thread(() -> {
			try {
				Scanner in = new Scanner(helper.getSocket().getInputStream());
				while (in.hasNextLine()) {
					String linha = in.nextLine();
					
					/*além de receber comandos, manipula os atributos booleanos 
					 * quando recebe determinado comando do servidor*/
					if (linha.equals("logout")) { //sai do sistema
						helper.getSistema().sair();
					} else if (linha.equals("criarProva")) { //seta variavel para thread de criacao de provas iniciar
						isThreadCriaProvaRodando.set(true);
					} else if (linha.equals("realizarProva")) { //seta variavel para thread de realizacao de provas iniciar
						boolean temProvas = helper.recebeProvasServidor();
						if (temProvas) {
							isThreadFazProvaRodando.set(true);
						}
					} else if (linha.equals("corrigirProva")) { //seta variavel para thread de correcao de provas iniciar
						boolean temRespostas = helper.recebeRespostasServidor();
						if (temRespostas) {
							isThreadCorrigeProvaRodando.set(true);
						}
					} else if (linha.equals("exibirNotas")) { //seta variavel para thread de recebimento de notas iniciar
						boolean temNotas = helper.recebeNotasServidor();
						if (temNotas) {
							isThreadExibeNotasRodando.set(true);
						}
					} else if (linha.equals("verificarStatus")) { //comando para status UDP
						isThreadVerificaStatusRodando.set(true); //
					} else if (linha.equals("Comando Desconhecido")) { //servidor sinalizou que comando enviado é desconhecido
						System.out.println("Comando Inválido: digite uma das opções do menu!");
						helper.getSistema().menu2Nivel(helper.getUsuarioLogado());
					} else {
						System.out.println(linha);
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Método responsável por criar a thread que faz a criação de prova
	 * @return a thread criadora de provas
	 */
	private Thread montaThreadCriaProva() {
		return new Thread(() -> {
			try {
				Prova prova = helper.getSistema().criarProva((Professor) helper.getUsuarioLogado(), sc);
				helper.enviaMsgProvaCriada();
				helper.enviarProvaCadastrada(prova);
				helper.getSistema().menu2Nivel(helper.getUsuarioLogado());
				isThreadCriaProvaRodando.set(false);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Método responsável por criar a thread que faz a realização de prova
	 * @return a thread realizadora de provas
	 */
	private Thread montaThreadFazProva() {
		return new Thread(() -> {
			try {
				Aluno aluno = (Aluno) helper.getUsuarioLogado();
				RespostaProva respostaProva = helper.getSistema().realizarProva(helper.getProvasDisponiveis(), sc, aluno);
				aluno.adicionarRespostaProva(respostaProva);
				helper.setUsuarioLogado(aluno);
				helper.enviaMsgRespostaCriada();
				helper.enviarRespostaCadastrada(respostaProva);
				helper.getSistema().menu2Nivel(helper.getUsuarioLogado());
				isThreadFazProvaRodando.set(false);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Método responsável por criar a thread que faz a correção de prova
	 * @return a thread corretora de provas
	 */
	private Thread montaThreadCorrigeProva() {
		return new Thread(() -> {
			try {
				Professor professor = (Professor) helper.getUsuarioLogado();
				Avaliacao avaliacaoProva = helper.getSistema().avaliarProva(helper.getRespostasDisponiveis(), sc, professor);
				professor.adicionarProvaCorrigida(avaliacaoProva);
				helper.setUsuarioLogado(professor);
				helper.enviaMsgAvaliacaoCriada();
				helper.enviarAvaliacaoCadastrada(avaliacaoProva);
				helper.getSistema().menu2Nivel(helper.getUsuarioLogado());
				isThreadCorrigeProvaRodando.set(false);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	/**
	 * Método responsável por criar a thread que devolve as notas de prova
	 * @return a thread buscadora de notas
	 */
	private Thread montaThreadMostraNotas() {
		return new Thread(() -> {
			try {
				helper.getSistema().exibirNotas(helper.getNotasDisponiveis(), sc, (Aluno) helper.getUsuarioLogado());
				helper.getSistema().menu2Nivel(helper.getUsuarioLogado());
				isThreadExibeNotasRodando.set(false);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	/**
	 * Método responsável por criar a thread que verifica o status do servidor via UDP.
	 * @return a thread verificadora de status
	*/
	private Thread montaThreadVerificaStatus() {
		return new Thread(() -> {
			try {
				helper.verificarStatusServidorUDP(); // Chama a função UDP do helper
				helper.getSistema().menu2Nivel(helper.getUsuarioLogado()); // Volta para o menu
				isThreadVerificaStatusRodando.set(false);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
}