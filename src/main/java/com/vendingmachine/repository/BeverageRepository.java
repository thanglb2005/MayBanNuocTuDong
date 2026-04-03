package com.vendingmachine.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BeverageRepository extends JpaRepository<Beverage, String> {
}
