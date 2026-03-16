package com.example.EyeCareHubDB.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByCustomerId(Long customerId);
    
    Optional<Address> findByCustomerIdAndIsDefaultShipTrue(Long customerId);
    
    @Query("SELECT a FROM Address a WHERE a.customer.id = :customerId AND a.isDefaultShip = true")
    Optional<Address> findDefaultAddressByCustomerId(@Param("customerId") Long customerId);
}
