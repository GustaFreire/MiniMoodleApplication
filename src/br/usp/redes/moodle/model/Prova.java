package br.usp.redes.moodle.model;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

/**
 * @author Gustavo Freire 
 * Classe que representa uma prova, com todas as informações necessárias
 */
public class Prova implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static int nextCodigo = 1;
	
	private Integer codigo;
	private String nome;
	private Disciplina disciplina;
	private List<Questao> questoes;
	private String cpfProfessorCriador;

    public Prova(Disciplina disciplina, String nome, String cpfProfessorCriador) {
    	this.codigo = nextCodigo;
    	Prova.nextCodigo = nextCodigo + 1;
    	this.disciplina = disciplina;
    	this.nome = nome;
    	this.cpfProfessorCriador = cpfProfessorCriador;
        this.questoes = new Vector<>();
    }

    public void adicionarQuestao(Questao questao) {
        this.questoes.add(questao);
    }

    public Integer getCodigo() {
		return codigo;
	}
    
    public Disciplina getDisciplina() {
        return disciplina;
    }
    
    public String getNome() {
		return nome;
	}
    
    public List<Questao> getQuestoes() {
        return questoes;
    }
    
     public String getCpfProfessorCriador() {
		return cpfProfessorCriador;
	}
}