package br.usp.redes.moodle.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gustavo Freire 
 * Classe abstrata que representa uma resposta de determinada prova, com as questões e respostas, 
 * além da prova a qual a resposta faz parte, e o identificador (cpf) do aluno que respondeu 
 */
public class RespostaProva implements Serializable {

    private static final long serialVersionUID = 1L;

    private Prova prova;
    private String cpfAluno;
    private Map<Questao, Resposta> respostas;

    public RespostaProva(Prova prova, String cpfAluno) {
        this.prova = prova;
        this.cpfAluno = cpfAluno;
        this.respostas = new HashMap<>();
    }

    public void adicionarResposta(Resposta resposta) {
        this.respostas.put(resposta.getQuestao(), resposta);
    }

    public Prova getProva() {
        return prova;
    }

    public String getCpfAluno() {
        return cpfAluno;
    }

    public Map<Questao, Resposta> getRespostas() {
        return respostas;
    }
}