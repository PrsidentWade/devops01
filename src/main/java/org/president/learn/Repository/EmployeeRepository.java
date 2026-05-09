package org.president.learn.Repository;

import org.president.learn.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByNomContainingIgnoreCase(String nom);

    List<Employee> findByPosteContainingIgnoreCase(String poste);

    boolean existsByEmail(String email);
}
