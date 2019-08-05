/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifpb.edu.com.br.main;

import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Cliente
 */
public class NewMain {

    private static Semaphore sem = new Semaphore(1);
    private static int op = 0;
    private static boolean continua = true;
    private static Lock lock = new ReentrantLock();
    private static Condition condition = lock.newCondition();

    public static void main(String[] args) throws SQLException, InterruptedException {

        Controlador controlador = new Controlador();
        Runnable scan = new Runnable() {

            @Override
            public void run() {
                Scanner in = new Scanner(System.in);

                System.out.println("0 para pausar ou 1 para continuar");

                while (true) {
                    op = in.nextInt();
                    System.out.println(op);
                    lock.lock();
                    try {
                        if (op == 0) {
                            continua = false;
                        } else if (op == 1) {
                            continua = true;
                            condition.signalAll();
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
        };

        new Thread(scan).start();

        while (continua) {

            while (true) {
                Thread salvar = new Thread(controlador.salvar);
                Thread atualizar = new Thread(controlador.atualizar);
                Thread deletar = new Thread(controlador.deletar);
                System.out.println("ok!");
                lock.lock();
                try {
                    while (!continua) {
                        condition.await();
                    }
                } finally {
                    lock.unlock();
                }
                try {
                    sem.acquire();
                    salvar.start();
                    sem.release();
                    atualizar.start();
                    deletar.start();

                } catch (InterruptedException ex) {
                }

            }
        }

    }
}
