package com.samourai.whirlpool.cli.persistence.repository;

import com.samourai.whirlpool.cli.persistence.entity.UtxoConfigEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UtxoConfigRepository extends CrudRepository<UtxoConfigEntity, String> {

  UtxoConfigEntity getById(String id);
}
