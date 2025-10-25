package br.usp.redes.moodle.exception;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * @author Gustavo Freire 
 * Classe que pega as exceptions não capturadas que determinada thread lançar
 */
public class ExceptionHandler implements UncaughtExceptionHandler {
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		System.out.println("Exceção na thread " + thread.getName() + ": " + ex.getMessage());
	}
}