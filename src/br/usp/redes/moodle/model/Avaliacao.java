package br.usp.redes.moodle.model;

import java.io.Serializable;

/**
 * @author Gustavo Freire 
 * Classe que representa uma avaliação de prova, possui a resposta da prova e a nota final
 */
public class Avaliacao implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private RespostaProva respostaProva;
    private double nota;

    public Avaliacao(RespostaProva respostaProva) {
        this.respostaProva = respostaProva;
    }

    public RespostaProva getRespostaProva() {
        return respostaProva;
    }

    public double getNota() {
        return nota;
    }

    public void setNota(double nota) {
        this.nota = nota;
    }
}