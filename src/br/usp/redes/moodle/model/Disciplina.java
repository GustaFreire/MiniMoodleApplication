package br.usp.redes.moodle.model;

import java.io.Serializable;

/**
 * @author Gustavo Freire 
 * Classe que representa uma disciplina, utilizada no momento de criação da prova
 */
public class Disciplina implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String nome;
    private String codigo;

    public Disciplina(String nome, String codigo) {
        this.nome = nome;
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getCodigo() {
        return codigo;
    }
}