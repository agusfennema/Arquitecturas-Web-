package ejemplo.dao;


import ejemplo.entity.Usuario;

import java.util.List;

public interface UsuarioDAO {
    Usuario findById(Long id);
    List<Usuario> findAll();
    void create(Usuario u);
    void update(Usuario u);
    void delete(Long id);
    void deleteAll();
}


