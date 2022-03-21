package com.samourai.whirlpool.cli.persistence.repository;

import com.samourai.whirlpool.cli.persistence.entity.Boltzmann;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoltzmannRepository extends CrudRepository<Boltzmann, String> {

  Boltzmann getByTxid(String txid);
}
