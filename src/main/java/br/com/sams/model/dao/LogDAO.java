package br.com.sams.model.dao;

import br.com.sams.model.entity.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;

import static java.util.Objects.requireNonNull;

@ApplicationScoped
public class LogDAO {

    private final EntityManager em;

    public LogDAO(EntityManager em) {
        this.em = requireNonNull(em, "em is required");
    }

    public void save(Log entity) {
        em.persist(entity);
    }

    public List<Log> listAll() {
        return em.createQuery("FROM Log l ORDER BY l.id DESC", Log.class)
                .getResultList();
    }
}
