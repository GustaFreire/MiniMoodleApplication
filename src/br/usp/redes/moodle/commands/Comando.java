package br.usp.redes.moodle.commands;

import java.net.Socket;
import java.util.concurrent.ExecutorService;

import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.server.Server;
import br.usp.redes.moodle.system.Sistema;

/**
 * @author Gustavo Freire 
 * Classe abstrata que representa os atributos que um comando precisa ter
 * - pool de threads para executar o comando
 * - objeto servidor para manipular informações
 * - objeto socket para interagir com cliente
 */
public abstract class Comando implements Command {

	//atributos visíveis para as filhas
	protected final static Sistema sistema = new Sistema();
    protected ExecutorService threadPool;
    protected Usuario usuario;
    protected Server servidor;
    protected Socket socket;
    
  //construtor inicializa atributos
    public Comando(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
        this.threadPool = threadPool;
        this.usuario = usuario;
		this.servidor = servidor;
		this.socket = socket;
    }
}