package ifpb.edu.com.br.usuario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Cliente
 */
public class UsuarioService {

    public int IdUsuario;

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager
                .getConnection(
                        "jdbc:postgresql://localhost/sd",
                        "postgres", 
                        "secret"
                );
        return connection;
    }

    public void salvar(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuario (id,nome) VALUES (?,?)";
        Connection con = getConnection();
        PreparedStatement stm = con.prepareStatement(sql);
        stm.setInt(1, u.getId());
        stm.setString(2, u.getNome());
        stm.executeUpdate();
        stm.close();
        con.close();
    }

    public void atualizar(int id) throws SQLException {

        String sql = "UPDATE usuario SET nome = ? WHERE id = ?";
        Connection con = getConnection();
        PreparedStatement stm = con.prepareStatement(sql);
        stm.setString(1, "nome atualizado" + id);
        stm.setInt(2, id);
        stm.executeUpdate();
        stm.close();
        con.close();
    }

    public void deletar(int id) throws SQLException {

        String sql = "DELETE FROM usuario WHERE id = ?";
        Connection con = getConnection();
        PreparedStatement stm = con.prepareStatement(sql);
        stm.setInt(1, id);
        stm.execute();
        stm.close();
        con.close();
    }

    public int IdUsuario() throws SQLException {

        String sql = "SELECT id FROM usuario ORDER BY id DESC LIMIT 1";
        Connection con = getConnection();
        PreparedStatement stm = con.prepareStatement(sql);
        ResultSet rs = stm.executeQuery();
        int id = 0;
        con.close();
        while (rs.next()) {
            id = rs.getInt("id");
        }
        con.close();
        return id;
        
        
    }
}
