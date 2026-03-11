package com.example.EyeCareHubDB.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.EyeCareHubDB.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c WHERE LOWER(c.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    java.util.List<Customer> searchByName(@Param("name") String name);

    default Optional<Customer> findByAccountId(Long accountId) {
        return findById(accountId);
    }

    default boolean existsByAccountId(Long accountId) {
        return existsById(accountId);
    }
}
