/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ifpb.edu.com.br.main;

import ifpb.edu.com.br.usuario.Usuario;
import ifpb.edu.com.br.usuario.UsuarioService;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static UsuarioService us = new UsuarioService();
    private static ArrayBlockingQueue<Integer> bufferdelete = new ArrayBlockingQueue<Integer>(3);
    private static ArrayBlockingQueue<Integer> bufferatualizar = new ArrayBlockingQueue<Integer>(3);

    ;

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

                    if (op == 0) {
                        continua = false;
                    } else if (op == 1) {
                        continua = true;
                        condition.signalAll();
                    }

                    lock.unlock();
                }
            }

        };

        new Thread(scan).start();

        while (true) {

            lock.lock();

            try {
                while (!continua) {

                    condition.await();
                }
            }  finally {
                lock.unlock();

            }

            Runnable salvar;
            salvar = new Runnable() {

                @Override
                public void run() {
                    try {
                        sem.acquire();
                        Usuario u = new Usuario(us.IdUsuario() + 1, "teste");
                        us.salvar(u);

                        System.out.println("save: " + u.toString());
                        bufferatualizar.put(u.getId());
                        sem.release();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SQLException ex) {
                        Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            };

            Runnable atualizar = new Runnable() {

                @Override
                public void run() {
                    try {
                        int id = bufferatualizar.take();
                        us.atualizar(id);
                        bufferdelete.put(id);
                         System.out.println("atualizou: " + id);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SQLException ex) {
                        Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            Runnable deletar = new Runnable() {

                @Override
                public void run() {
                    try {
                        int id = bufferdelete.take();
                        us.deletar(id);
                         System.out.println("deleted: " + id);
                        // if (id >= 100) {
                        //long tempofinal = System.currentTimeMillis() - tempo;
                        //   System.out.println("Tempo final: " + tempofinal);
                        //  }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SQLException ex) {
                        Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            Thread inserir = new Thread(salvar);
            Thread update = new Thread(atualizar);
            Thread del = new Thread(deletar);

            sem.acquire();
            inserir.start();
            sem.release();
            update.start();
            del.start();

            

    }

}

}

