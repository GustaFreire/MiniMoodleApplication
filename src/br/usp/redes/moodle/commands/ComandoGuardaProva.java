package br.usp.redes.moodle.commands;

import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import br.usp.redes.moodle.model.Prova;
import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.server.Server;

/**
 * @author Gustavo Freire 
 * Classe que representa o comando de armazenar prova
 */
public class ComandoGuardaProva extends Comando {

	// construtor
	public ComandoGuardaProva(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
        super(threadPool, usuario, servidor, socket);
    }

	@Override
    public void execute() { // execucao do comando, apenas armazena a prova criada pelo professor e sinaliza
    	try {
    		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
    		Prova prova = (Prova) in.readObject();
			servidor.adicionarProvaCadastrada(prova);
			System.out.println("Prova de codigo " + prova.getCodigo() + " adicionada no servidor");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}