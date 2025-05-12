package org.rundellse.squashleague.persistence;

import org.rundellse.squashleague.model.SquashMatch;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SquashMatchRepository extends CrudRepository<SquashMatch, Long> {
}
