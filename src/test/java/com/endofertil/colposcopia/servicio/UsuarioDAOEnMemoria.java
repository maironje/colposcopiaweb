package com.endofertil.colposcopia.servicio;

import com.endofertil.colposcopia.dao.UsuarioDAO;
import com.endofertil.colposcopia.modelo.Usuario;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DAO falso para pruebas: guarda los usuarios en un mapa en memoria,
 * así {@link AutenticacionServicio} se prueba sin MySQL.
 */
class UsuarioDAOEnMemoria extends UsuarioDAO {

    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();
    private int siguienteId = 1;

    /** Agrega un usuario directamente, como si ya existiera en la base de datos. */
    Usuario agregar(String nombre, String passwordHash, String rol, boolean activo) {
        Usuario u = new Usuario();
        u.setIdUsuario(siguienteId++);
        u.setUsuario(nombre);
        u.setPasswordHash(passwordHash);
        u.setRol(rol);
        u.setActivo(activo);
        usuarios.put(nombre, u);
        return u;
    }

    @Override
    public Usuario buscarPorUsuario(String usuario) {
        return usuarios.get(usuario);
    }

    @Override
    public int insertar(Usuario u) {
        u.setIdUsuario(siguienteId++);
        usuarios.put(u.getUsuario(), u);
        return u.getIdUsuario();
    }

    @Override
    public boolean cambiarClave(int id, String hash) {
        for (Usuario u : usuarios.values()) {
            if (u.getIdUsuario() == id) {
                u.setPasswordHash(hash);
                return true;
            }
        }
        return false;
    }
}
