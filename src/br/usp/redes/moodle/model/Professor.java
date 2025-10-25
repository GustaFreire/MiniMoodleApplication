package br.usp.redes.moodle.model;

import java.util.List;
import java.util.Vector;

/**
 * @author Gustavo Freire 
 * Classe que representa um usuário do tipo professor, possui as correções de prova que o professor cadastrou 
 */
public class Professor  extends Usuario{

	private static final long serialVersionUID = 1L;

	private List<Avaliacao> minhasProvasCorrigidas;
	
	public Professor(String nome, String email, String cpf, String senha, Tipo tipo) {
		super(nome, email, cpf, senha, tipo);
		this.minhasProvasCorrigidas = new Vector<>();
	}
	
	public void adicionarProvaCorrigida(Avaliacao provaCorrigida) {
        this.minhasProvasCorrigidas.add(provaCorrigida);
    }
	
	public List<Avaliacao> getMinhasProvasCorrigidas() {
		return minhasProvasCorrigidas;
	}
}