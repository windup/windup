package diva.spring;

import org.springframework.data.jpa.repository.JpaRepository;

public class BaseService<E> {

    JpaRepository<E, Long> repository;
    
    public BaseService(JpaRepository<E, Long> repository) {
        this.repository = repository;
    }

}
