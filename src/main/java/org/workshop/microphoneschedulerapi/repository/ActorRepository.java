package org.workshop.microphoneschedulerapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.workshop.microphoneschedulerapi.domain.entity.Actor;
import org.workshop.microphoneschedulerapi.domain.entity.User;

@Repository
public interface ActorRepository extends JpaRepository<Actor, Integer> {

    boolean existsByUser(User user);
}
