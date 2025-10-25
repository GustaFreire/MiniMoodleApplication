package br.usp.redes.moodle.model;

import java.io.Serializable;

/**
 * @author Gustavo Freire 
 * Classe abstrata que representa uma questão de prova, possui enunciado
 */
public abstract  class Questao implements Serializable {

	private static final long serialVersionUID = 1L;
	private String enunciado;

    public Questao(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getEnunciado() {
        return enunciado;
    }
}