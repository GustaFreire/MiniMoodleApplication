package br.usp.redes.moodle;

import br.usp.redes.moodle.server.Server;

/**
 * @author Gustavo Freire
 * Classe principal, por meio dela o servidor é inicializado.
 */
public class Main {

	public static void main(String[] args) throws Exception {
		Server servidor = new Server();
		servidor.start();
	}
}