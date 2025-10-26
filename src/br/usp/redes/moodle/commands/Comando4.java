package br.usp.redes.moodle.commands;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import br.usp.redes.moodle.model.Usuario;
import br.usp.redes.moodle.server.Server;

/**
 * @author Gustavo Freire 
 * Classe que representa o comando 4 (verificação de status do servidor via UDP)
*/
public class Comando4 extends Comando {

	// construtor
    public Comando4(ExecutorService threadPool, Usuario usuario, Server servidor, Socket socket) {
        super(threadPool, usuario, servidor, socket);
    }

    @Override
    public void execute() { // execucao do comando, apenas sinaliza para cliente iniciar o procedimento UDP
    	try {
			PrintStream out = new PrintStream(socket.getOutputStream());
			// Confirmação via TCP
			out.println("Confirmação do comando 4 - VERIFICAR STATUS (UDP)");
			// Comando para o cliente iniciar a lógica UDP
			out.println("verificarStatus"); 
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
}