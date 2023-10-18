package com.real_estate.eqlt.repos;

import com.real_estate.eqlt.domain.RealEstates;
import org.springframework.data.repository.CrudRepository;

public interface RealEstateRepo extends CrudRepository<RealEstates, Long> {

}
