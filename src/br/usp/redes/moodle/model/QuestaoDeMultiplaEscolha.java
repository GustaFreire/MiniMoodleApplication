package br.usp.redes.moodle.model;

import java.util.List;

/**
 * @author Gustavo Freire 
 * Classe abstrata que representa uma questão de prova do tipo múltipla escolha, 
 * possui as alternativas da questão
 */
public class QuestaoDeMultiplaEscolha extends Questao {

	private static final long serialVersionUID = 1L;
	
	private List<String> opcoes;

    public QuestaoDeMultiplaEscolha(String enunciado, List<String> opcoes) {
        super(enunciado);
        this.opcoes = opcoes;
    }

    public List<String> getOpcoes() {
        return opcoes;
    }	
}