package org.rundellse.squashleague.persistence;

import org.rundellse.squashleague.model.Season;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SeasonRepository extends CrudRepository<Season, Long> {

    @Query("FROM Season s WHERE s.startDate <= :date AND :date < s.endDate ORDER BY s.endDate DESC")
    Season findSeasonForDate(@Param("date") LocalDateTime date);

    Season findFirstByOrderByEndDateDesc();

}
