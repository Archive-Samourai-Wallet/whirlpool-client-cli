package com.samourai.whirlpool.cli.persistence.repository;

import com.samourai.whirlpool.cli.persistence.entity.LogEntity;
import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends CrudRepository<LogEntity, String> {

  Collection<LogEntity> findAllByOrderByCreatedDesc();
}
