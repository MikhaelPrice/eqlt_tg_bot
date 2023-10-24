package com.real_estate.eqlt.repos;

import com.real_estate.eqlt.domain.EqtUsers;
import org.springframework.data.repository.CrudRepository;

public interface EqtUserRepo extends CrudRepository<EqtUsers, Long> {
}
