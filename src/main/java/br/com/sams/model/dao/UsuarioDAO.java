package br.com.sams.model.dao;

import br.com.sams.model.entity.Usuario;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class UsuarioDAO {

    private final Map<Integer, Usuario> usuarios = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(0);

    public void save(Usuario entity) {
        if (entity.getId() == null) {
            entity.setId(sequence.incrementAndGet());
        }
        usuarios.put(entity.getId(), entity);
    }

    public void delete(Integer id) {
        usuarios.remove(id);
    }

    public Usuario find(Integer id) {
        return usuarios.get(id);
    }

    public List<Usuario> findAll() {
        return usuarios.values().stream()
                .sorted(Comparator.comparing(Usuario::getId))
                .toList();
    }

    public Usuario findByEmail(String email) {
        return usuarios.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
}