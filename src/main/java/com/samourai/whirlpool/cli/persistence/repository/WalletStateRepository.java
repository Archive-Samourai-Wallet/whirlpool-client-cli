package com.samourai.whirlpool.cli.persistence.repository;

import com.samourai.whirlpool.cli.persistence.entity.WalletStateEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletStateRepository extends CrudRepository<WalletStateEntity, String> {

  WalletStateEntity getById(String id);
}
