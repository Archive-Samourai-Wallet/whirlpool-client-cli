package com.samourai.whirlpool.cli.persistence.repository;

import com.samourai.whirlpool.cli.persistence.entity.BoltzmannEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoltzmannRepository extends CrudRepository<BoltzmannEntity, String> {

  BoltzmannEntity getByTxid(String txid);
}
