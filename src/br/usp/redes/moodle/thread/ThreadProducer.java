package br.usp.redes.moodle.thread;

import java.util.concurrent.ThreadFactory;

import br.usp.redes.moodle.exception.ExceptionHandler;

/**
 * @author Gustavo Freire 
 * Classe utilizada na criação de uma nova thread (atribui para a thread um nome e número)
 */
public class ThreadProducer implements ThreadFactory {
    private static int numero = 1;

    @Override
    public Thread newThread(Runnable tarefa) {
        Thread thread = new Thread(tarefa, "Thread Servidor Tarefas " + numero);
        numero++;
        thread.setUncaughtExceptionHandler(new ExceptionHandler());
        return thread;
    }
}