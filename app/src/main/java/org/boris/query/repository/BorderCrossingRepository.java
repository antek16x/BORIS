package org.boris.query.repository;

import org.boris.query.entity.BorderCrossing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BorderCrossingRepository extends JpaRepository<BorderCrossing, Long> {
}
