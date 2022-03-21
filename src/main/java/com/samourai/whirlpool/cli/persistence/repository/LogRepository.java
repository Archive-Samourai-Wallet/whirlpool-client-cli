package com.samourai.whirlpool.cli.persistence.repository;

import com.samourai.whirlpool.cli.persistence.entity.Log;
import java.util.Collection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends CrudRepository<Log, String> {

  Collection<Log> findAllByOrderByCreatedDesc();
}
