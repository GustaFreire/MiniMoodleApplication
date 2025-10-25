package br.usp.redes.moodle.model;

import java.io.Serializable;

/**
 * @author Gustavo Freire 
 * Classe abstrata que representa uma resposta de determinada questão
 */
public class Resposta implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Questao questao;
    private String resposta;

    public Resposta(Questao questao, String resposta) {
        this.questao = questao;
        this.resposta = resposta;
    }

    public Questao getQuestao() {
        return questao;
    }
    
    public String getResposta() {
		return resposta;
	}
}