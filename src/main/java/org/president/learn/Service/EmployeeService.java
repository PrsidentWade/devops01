package org.president.learn.Service;

import org.president.learn.Entity.Employee;
import org.president.learn.Exception.DuplicateEmailException;
import org.president.learn.Exception.EmployeeNotFoundException;
import org.president.learn.Repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> findAll() {
        return repository.findAll();
    }

    public Employee findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    public Employee save(Employee employee) {
        if (repository.existsByEmail(employee.getEmail())) {
            throw new DuplicateEmailException(employee.getEmail());
        }
        return repository.save(employee);
    }

    public Employee update(Long id, Employee employee) {
        Employee existing = findById(id);
        if (!existing.getEmail().equals(employee.getEmail())
                && repository.existsByEmail(employee.getEmail())) {
            throw new DuplicateEmailException(employee.getEmail());
        }
        existing.setNom(employee.getNom());
        existing.setPrenom(employee.getPrenom());
        existing.setEmail(employee.getEmail());
        existing.setPoste(employee.getPoste());
        existing.setSalaire(employee.getSalaire());
        existing.setDateEmbauche(employee.getDateEmbauche());
        return repository.save(existing);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        repository.deleteById(id);
    }

    public List<Employee> searchByNom(String nom) {
        return repository.findByNomContainingIgnoreCase(nom);
    }
}
