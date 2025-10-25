package br.usp.redes.moodle.model;

import java.util.List;
import java.util.Vector;

/**
 * @author Gustavo Freire 
 * Classe que representa um usuário do tipo aluno, possui as respostas de prova que o aluno cadastrou 
 */
public class Aluno extends Usuario {
	private static final long serialVersionUID = 1L;

	private List<RespostaProva> minhasRespostas;
	
	public Aluno(String nome, String email, String cpf, String senha, Tipo tipo) {
		super(nome, email, cpf, senha, tipo);
		this.minhasRespostas = new Vector<>();
	}
	
	public void adicionarRespostaProva(RespostaProva respostaProva) {
        this.minhasRespostas.add(respostaProva);
    }
	
	public List<RespostaProva> getMinhasRespostas() {
		return minhasRespostas;
	}
}