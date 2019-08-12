package ifpb.edu.com.br.main;

import ifpb.edu.com.br.usuario.Usuario;
import ifpb.edu.com.br.usuario.UsuarioService;

import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Controlador {

    private static ArrayBlockingQueue<Integer> bufferdelete;
    private static ArrayBlockingQueue<Integer> bufferatualizar;

    final long tempo = System.currentTimeMillis();
    private static Semaphore sem = new Semaphore(1);
    private UsuarioService us = new UsuarioService();

    Controlador() {
        bufferdelete = new ArrayBlockingQueue<Integer>(3);
        bufferatualizar = new ArrayBlockingQueue<Integer>(3);
    }

    Runnable salvar = new Runnable() {

        @Override
        public void run() {
            try {
                sem.acquire();
                Usuario u = new Usuario(us.IdUsuario() + 1, "teste");
                us.salvar(u);
               
               System.out.println("save: " + u.toString());
                bufferatualizar.put(u.getId());
                sem.release();
            } catch (SQLException ex) {
                Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
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
               // System.out.println("atualizou: " + id);
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
               // System.out.println("deleted: " + id);
                if (id >= 100) {
                    long tempofinal = System.currentTimeMillis() - tempo;
                 //   System.out.println("Tempo final: " + tempofinal);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(Controlador.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };
}
