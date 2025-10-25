package br.usp.redes.moodle.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.server.Server;

/**
 * @author Gustavo Freire 
 * Classe que representa o comando 3 (logout de usuario)
 */
public class Comando3 extends Comando {

	// construtor
    public Comando3(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
        super(threadPool, usuario, servidor, socket);
    }

    @Override
    public void execute() { // execucao do comando, apenas sinaliza para cliente e em seguida faz logout
    	try {
			PrintStream out = new PrintStream(socket.getOutputStream());
			out.println("Confirmação do comando 3 - LOGOUT");
			System.out.println("Deslogando usuario: " + this.usuario.getNome());
			this.servidor.logoutUsuario();
			this.usuario.setLogado(false);
			System.out.println("Usuários Online: " + this.servidor.getContadorUsuariosOnline());
			out.println("logout");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
}