package br.usp.redes.moodle.model;

import java.io.Serializable;

/**
 * @author Gustavo Freire 
 * Classe abstrata que representa um usuario genérico (aluno ou professor), com todas as informações necessárias
 */
public abstract class Usuario implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String nome;
	private String email;
	private String cpf;
	private String senha;
	private Tipo tipo;
	private boolean logado;
	
	public Usuario(String nome, String email, String cpf, String senha, Tipo tipo) {
		this.nome = nome;
		this.email = email;
		this.cpf = cpf;
		this.senha = senha;
		this.tipo = tipo;
		this.logado = false;
	}

	public String getNome() {
		return nome;
	}

	public String getEmail() {
		return email;
	}

	public String getCpf() {
		return cpf;
	}

	public String getSenha() {
		return senha;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public boolean getLogado() {
		return logado;
	}
	
	public void setLogado(boolean logado) {
		this.logado = logado;
	}
}