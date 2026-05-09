package org.president.learn.Service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.president.learn.Entity.Employee;
import org.president.learn.Exception.DuplicateEmailException;
import org.president.learn.Exception.EmployeeNotFoundException;
import org.president.learn.Repository.EmployeeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository repository;

    @InjectMocks
    private EmployeeService service;

    private Employee createEmployee(Long id, String nom, String prenom, String email) {
        Employee e = new Employee();
        e.setId(id);
        e.setNom(nom);
        e.setPrenom(prenom);
        e.setEmail(email);
        e.setPoste("Developpeur");
        e.setSalaire(50000);
        e.setDateEmbauche(LocalDate.of(2023, 1, 15));
        return e;
    }

    @Test
    void findAll_shouldReturnAllEmployees() {
        when(repository.findAll()).thenReturn(List.of(
                createEmployee(1L, "Diop", "Moussa", "moussa@email.com"),
                createEmployee(2L, "Sow", "Aminata", "aminata@email.com")
        ));

        List<Employee> result = service.findAll();

        assertEquals(2, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void findById_shouldReturnEmployee() {
        when(repository.findById(1L)).thenReturn(Optional.of(createEmployee(1L, "Diop", "Moussa", "moussa@email.com")));

        Employee result = service.findById(1L);

        assertNotNull(result);
        assertEquals("Diop", result.getNom());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void save_shouldCreateEmployee() {
        Employee employee = createEmployee(null, "Fall", "Fatou", "fatou@email.com");
        when(repository.existsByEmail("fatou@email.com")).thenReturn(false);
        when(repository.save(any(Employee.class))).thenReturn(createEmployee(1L, "Fall", "Fatou", "fatou@email.com"));

        Employee result = service.save(employee);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void save_shouldThrowWhenDuplicateEmail() {
        Employee employee = createEmployee(null, "Fall", "Fatou", "fatou@email.com");
        when(repository.existsByEmail("fatou@email.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> service.save(employee));
        verify(repository, never()).save(any());
    }

    @Test
    void delete_shouldRemoveEmployee() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void delete_shouldThrowWhenNotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(EmployeeNotFoundException.class, () -> service.delete(99L));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void update_shouldModifyEmployee() {
        Employee existing = createEmployee(1L, "Diop", "Moussa", "moussa@email.com");
        Employee updates = createEmployee(null, "Diop", "Moussa", "moussa.new@email.com");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmail("moussa.new@email.com")).thenReturn(false);
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee result = service.update(1L, updates);

        assertEquals("moussa.new@email.com", result.getEmail());
    }

    @Test
    void update_shouldThrowWhenDuplicateEmail() {
        Employee existing = createEmployee(1L, "Diop", "Moussa", "moussa@email.com");
        Employee updates = createEmployee(null, "Sow", "Aminata", "aminata@email.com");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByEmail("aminata@email.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> service.update(1L, updates));
    }
}
