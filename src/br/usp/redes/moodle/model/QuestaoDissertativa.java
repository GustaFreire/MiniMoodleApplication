package br.usp.redes.moodle.model;

/**
 * @author Gustavo Freire 
 * Classe abstrata que representa uma questão de prova do tipo dissertativa
 */
public class QuestaoDissertativa extends Questao {
	private static final long serialVersionUID = 1L;

	public QuestaoDissertativa(String enunciado) {
        super(enunciado);
    }
}