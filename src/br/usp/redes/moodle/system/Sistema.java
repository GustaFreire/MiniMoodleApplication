package br.usp.redes.moodle.system;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

import br.usp.redes.moodle.model.Aluno;
import br.usp.redes.moodle.model.Avaliacao;
import br.usp.redes.moodle.model.Disciplina;
import br.usp.redes.moodle.model.Professor;
import br.usp.redes.moodle.model.Prova;
import br.usp.redes.moodle.model.Questao;
import br.usp.redes.moodle.model.QuestaoDeMultiplaEscolha;
import br.usp.redes.moodle.model.QuestaoDissertativa;
import br.usp.redes.moodle.model.Resposta;
import br.usp.redes.moodle.model.RespostaProva;
import br.usp.redes.moodle.model.Tipo;
import br.usp.redes.moodle.model.Usuario;

/**
 * @author Gustavo Freire 
 * Classe que representa o sistema, possui as funcionalidades que o cliente vai interagir
 */
public class Sistema {

	// imprime frase de boas-vindas
	public void welcome() {
		System.out.println("--------------------------------------------------------------------------------------");
		System.out.println("Bem-vindo ao Mini Moodle! (command line version)");
		System.out.println("Para obter acesso as opções, é necessário estar logado no sistema");
		System.out.println("Caso não possua cadastro, entre na opção de cadastro.");
		System.out.println("--------------------------------------------------------------------------------------");
	}

	// imprime e captura a opção desejada
	public int escolherOpcao(Scanner sc) {
		System.out.println("Escolha uma opção:");
		System.out.println("1 - Logar");
		System.out.println("2 - Cadastrar");
		System.out.println("3 - Sair");
		int opcao = sc.nextInt();
		sc.nextLine();

		if (opcao == 1) {
			return 1;
		} else if (opcao == 2) {
			return 2;
		} else if (opcao == 3) {
			return 3;
		}
		return -1;
	}

	// faz login do usuário
	public Usuario logar(List<Usuario> usuarios, Scanner sc) {
		System.out.println("-------------------------------LOGIN-------------------------------");

		System.out.println("Digite o email:");
		String email = sc.nextLine();

		System.out.println("Digite a senha:");
		String senha = sc.nextLine();

		for (Usuario user : usuarios) {
			if (user.getEmail().equals(email) && user.getSenha().equals(senha)) {
				return user;
			}
		}

		System.out.println("Email ou senha inexistentes! Tente novamente.");
		System.exit(0);
		return null;
	}

	// faz cadastro do usuário
	public Usuario cadastrar(List<Usuario> usuarios, Scanner sc) {
		System.out.println("Digite o nome do usuario que deseja cadastrar:");
		String nome = sc.nextLine();

		System.out.println("Digite o email do usuario que deseja cadastrar:");
		String email = sc.nextLine();

		System.out.println("Digite o cpf do usuario que deseja cadastrar:");
		String cpf = sc.nextLine();

		System.out.println("Digite a senha do usuario que deseja cadastrar:");
		String senha = sc.nextLine();

		System.out.println("Digite a opção correspondente ao tipo de usuario:");
		System.out.println("1 - Aluno");
		System.out.println("2 - Professor");

		int opcao = sc.nextInt();
		sc.nextLine();
		Tipo tipo = null;

		if (!usuarioExiste(cpf, usuarios)) {
			Usuario usuario = null;
			if (opcao == 1) {
				tipo = Tipo.ALUNO;
				usuario = new Aluno(nome, email, cpf, senha, tipo);
			} else if (opcao == 2) {
				tipo = Tipo.PROFESSOR;
				usuario = new Professor(nome, email, cpf, senha, tipo);
			} else {
				System.out.println("Opção inválida! Tente novamente");
				System.exit(0);
			}

			System.out.println("Usuario cadastrado com sucesso!");
			return usuario;
		} else {
			System.out.println("Usuario com cpf ja cadastrado no sistema!");
			System.exit(0);
			return null;
		}
	}

	// imprime primeiro menu
	public void menu1Nivel(Usuario usuario) {
		System.out.println("--------------------------------------------------------------------------------------");
		System.out.println("Bem-vindo ao Mini-moodle, " + usuario.getNome() + "!!");
		System.out.println("Escolha o que deseja fazer (digite o número da opção):");
		apresentaOpcoes(usuario.getTipo());
		System.out.println("3 - Logout");
		System.out.println("--------------------------------------------------------------------------------------");
		System.out.println("Opção:");
	}

	// imprime segundo menu
	public void menu2Nivel(Usuario usuario) {
		System.out.println("--------------------------------------------------------------------------------------");
		System.out.println("Escolha o que deseja fazer (digite o número da opção):");
		apresentaOpcoes(usuario.getTipo());
		System.out.println("3 - Logout");
		System.out.println("--------------------------------------------------------------------------------------");
		System.out.println("Opção:");
	}

	// exibe opções dependendo do tipo de usuario
	public void apresentaOpcoes(Tipo tipo) {
		if (tipo.equals(Tipo.ALUNO)) {
			System.out.println("1 - Realizar Prova");
			System.out.println("2 - Ver Notas");
		} else if (tipo.equals(Tipo.PROFESSOR)) {
			System.out.println("1 - Criar Prova");
			System.out.println("2 - Corrigir Prova");
		}
	}

	// faz cadastro do prova
	public Prova criarProva(Professor professor, Scanner sc) {
		String codigoDisciplina = sc.nextLine();
		System.out.println("Digite o nome da disciplina no qual a prova será criada:");
		String nomeDisciplina = sc.nextLine();
		System.out.println("Digite o nome da prova que será criada:");
		String nomeProva = sc.nextLine();

		Prova prova = new Prova(new Disciplina(nomeDisciplina, codigoDisciplina), nomeProva, professor.getCpf());

		System.out.println("Digite o número de questões da prova:");
		int numQuestoes = sc.nextInt();
		sc.nextLine();

		for (int i = 0; i < numQuestoes; i++) {
			Questao questao = null;
			System.out.println("Digite o enunciado da questão número " + (i + 1));
			String enunciado = sc.nextLine();

			System.out.println(
					"Digite 1 para criar uma questão dissertativa, ou 2 para criar uma questão de múltipla escolha:");
			int opcao = sc.nextInt();
			sc.nextLine();

			if (opcao == 1) {
				questao = new QuestaoDissertativa(enunciado);
			} else if (opcao == 2) {
				System.out.println("Digite a quantidade de alternativas da questão:");
				int quantidade = sc.nextInt();
				sc.nextLine();

				List<String> alternativas = new Vector<>();

				for (int j = 0; j < quantidade; j++) {
					System.out.println("Digite a alterativa número " + (j + 1));
					String alternativa = sc.nextLine();
					alternativas.add(alternativa);
				}
				questao = new QuestaoDeMultiplaEscolha(enunciado, alternativas);
			} else {
				System.out.println("Opção inválida, tente novamente");
				System.exit(0);
			}

			prova.adicionarQuestao(questao);
		}

		System.out.println("Prova criada com sucesso!");
		return prova;
	}

	// faz cadastro do resposta de prova
	public RespostaProva realizarProva(List<Prova> provasDisponiveis, Scanner sc, Aluno usuario) {
		System.out.println("Escolha a prova que deseja realizar (digite o número correspondente a prova):");
		System.out.println("--------------------------------------------");

		for (int i = 1; i <= provasDisponiveis.size(); i++) {
			Prova prova = provasDisponiveis.get(i - 1);
			System.out.println("Prova " + i + ": " + prova.getNome());
			System.out.println("Disciplina: " + prova.getDisciplina().getNome());
			System.out.println("Quantidade de questões: " + prova.getQuestoes().size());
			System.out.println("**************************************************");
		}

		System.out.println("--------------------------------------------");

		System.out.println("Opção:");
		int opcao = sc.nextInt();
		sc.nextLine();

		Prova provaEscolhida = null;

		if (opcao <= provasDisponiveis.size()) {
			provaEscolhida = provasDisponiveis.get(opcao - 1);
			System.out.println("Prova Escolhida: " + provaEscolhida.getNome());
		} else {
			System.out.println("Opcao invalida, tente novamente.");
			System.exit(0);
		}

		System.out.println("Agora responda as perguntas (a medida que os enunciados vão aparecendo):");
		RespostaProva respostaProva = new RespostaProva(provaEscolhida, usuario.getCpf());
		int cont = 1;
		List<Questao> questoes = provaEscolhida.getQuestoes();

		for (int i = 0; i < questoes.size(); i++) {
			Questao questao = questoes.get(i);
			System.out.println("Enunciado questão " + cont + ": " + questao.getEnunciado());

			if (questao instanceof QuestaoDeMultiplaEscolha) {
				QuestaoDeMultiplaEscolha questaoMultipla = (QuestaoDeMultiplaEscolha) questao;
				List<String> alternativas = questaoMultipla.getOpcoes();
				System.out.println("Alternativas:");

				char letra = 'a';
				for (String alternativa : alternativas) {
					System.out.println(letra + ") " + alternativa);
					letra++;
				}
			}

			System.out.println(
					"Digite a resposta (se for dissertativa insira o texto, se for alternativa apenas a opção)");
			System.out.println("Resposta:");
			String respostaStr = sc.nextLine();
			Resposta resposta = new Resposta(questao, respostaStr);
			respostaProva.adicionarResposta(resposta);
			cont++;
		}

		System.out.println("Prova respondida com sucesso, aguarde a nota do professor.");
		return respostaProva;
	}

	// faz cadastro do correção de prova
	public Avaliacao avaliarProva(List<RespostaProva> respostasDisponiveis, Scanner sc, Professor usuarioLogado) {
		System.out.println("Escolha a resposta que deseja avaliar (digite o número correspondente a resposta):");
		System.out.println("--------------------------------------------");

		for (int i = 1; i <= respostasDisponiveis.size(); i++) {
			RespostaProva resposta = respostasDisponiveis.get(i - 1);
			System.out.println("Resposta " + i + ":");
			System.out.println("CPF Aluno: " + resposta.getCpfAluno());
			System.out.println("Prova: " + resposta.getProva().getNome());
			System.out.println("**************************************************");
		}

		System.out.println("--------------------------------------------");
		System.out.println("Opção:");
		int opcao = sc.nextInt();
		sc.nextLine();

		RespostaProva respostaEscolhida = null;

		if (opcao <= respostasDisponiveis.size()) {
			respostaEscolhida = respostasDisponiveis.get(opcao - 1);
			System.out.println("Resposta Escolhida: número " + opcao);
		} else {
			System.out.println("Opcao invalida, tente novamente.");
			System.exit(0);
		}

		System.out.println("Agora atribua uma nota para cada resposta (a medida que vão aparecendo):");
		Avaliacao avaliacao = new Avaliacao(respostaEscolhida);
		int cont = 1;
		double notaFinal = 0.0;

		Map<Questao, Resposta> respostas = respostaEscolhida.getRespostas();

		for (Map.Entry<Questao, Resposta> entry : respostas.entrySet()) {
			Questao questao = entry.getKey();
			Resposta resposta = entry.getValue();

			System.out.println("--------------------------------------------------");
			System.out.println("Questão " + cont + ": " + questao.getEnunciado());

			if (questao instanceof QuestaoDeMultiplaEscolha) {
				QuestaoDeMultiplaEscolha questaoMultipla = (QuestaoDeMultiplaEscolha) questao;
				List<String> opcoes = questaoMultipla.getOpcoes();
				char c = 'a';
				for (String alternativa : opcoes) {
					System.out.println(c + " - " + alternativa);
					c++;
				}
			}

			System.out.println("Resposta: " + resposta.getResposta());
			System.out.println("--------------------------------------------------");

			System.out.println("Nota:");
			double notaDouble = Double.parseDouble(sc.nextLine());
			notaFinal += notaDouble;
			cont++;
		}

		avaliacao.setNota(notaFinal);
		System.out.println("Prova Corrigida! Nota: " + avaliacao.getNota());
		return avaliacao;
	}

	// imprime as notas do usuário
	public void exibirNotas(List<Avaliacao> notasDisponiveis, Scanner sc, Aluno usuarioLogado) {
		System.out.println("----------------------------Lista de Notas Lançadas----------------------------");
		for (Avaliacao nota : notasDisponiveis) {
			System.out.println("Prova: " + nota.getRespostaProva().getProva().getNome());
			System.out.println("Nota: " + nota.getNota());
			System.out.println("------------------------------------------------------------------------");
		}

		System.out.println("Obs: em caso de dúvidas, converse com seu professor.");
	}

	// sai do sistema
	public void sair() {
		System.out.println("Obrigado por utilizar o mini moodle...");
		System.exit(0);
	}

	// método usado internamente que ve se usuário existe
	private boolean usuarioExiste(String cpf, List<Usuario> usuarios) {
		for (Usuario user : usuarios) {
			if (user.getCpf().equals(cpf)) {
				return true;
			}
		}
		return false;
	}
}