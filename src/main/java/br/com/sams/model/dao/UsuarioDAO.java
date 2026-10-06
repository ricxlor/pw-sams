package br.com.sams.model.dao;

import br.com.sams.model.entity.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;

import static java.util.Objects.requireNonNull;

@ApplicationScoped
public class UsuarioDAO {

    private final EntityManager em;

    public UsuarioDAO(EntityManager em) {
        this.em = requireNonNull(em, "em is required");
    }

    public void save(Usuario entity) {
        if (entity.getId() == null) {
            em.persist(entity);
        } else {
            em.merge(entity);
        }
    }

    public void delete(Integer id) {
        Usuario usuario = em.find(Usuario.class, id);
        if (usuario != null) {
            em.remove(usuario);
        }
    }

    public Usuario find(Integer id) {
        return em.find(Usuario.class, id);
    }

    public List<Usuario> findAll() {
        return em.createQuery("FROM Usuario u ORDER BY u.id", Usuario.class)
                .getResultList();
    }

    public Usuario findByEmail(String email) {
        return em.createQuery("FROM Usuario u WHERE u.email = :email", Usuario.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}
