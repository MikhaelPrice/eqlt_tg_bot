package com.realestate.repos;

import com.realestate.entity.EqtRealEstates;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EqtRealEstatesRepo extends CrudRepository<EqtRealEstates, Long> {
    @Query(value = "SELECT id FROM EqtRealEstates WHERE willingness = :willingness AND type = :type AND price > :price1 AND price < :price2")
    List<Long> findRealEstatesIds(@Param("type") String type,
                                  @Param("willingness") String willingness,
                                  @Param("price1") String price1,
                                  @Param("price2") String price2);
}
