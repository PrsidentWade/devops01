package org.president.learn.Controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.president.learn.Entity.Employee;
import org.president.learn.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import org.president.learn.Exception.DuplicateEmailException;
import org.president.learn.Exception.EmployeeNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
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
    void getAll_shouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                createEmployee(1L, "Diop", "Moussa", "moussa@email.com"),
                createEmployee(2L, "Sow", "Aminata", "aminata@email.com")
        ));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nom").value("Diop"))
                .andExpect(jsonPath("$[1].nom").value("Sow"));
    }

    @Test
    void getById_shouldReturnEmployee() throws Exception {
        when(service.findById(1L)).thenReturn(createEmployee(1L, "Diop", "Moussa", "moussa@email.com"));

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Diop"))
                .andExpect(jsonPath("$.email").value("moussa@email.com"));
    }

    @Test
    void getById_shouldReturn404() throws Exception {
        when(service.findById(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        Employee employee = createEmployee(null, "Fall", "Fatou", "fatou@email.com");
        Employee saved = createEmployee(1L, "Fall", "Fatou", "fatou@email.com");

        when(service.save(any(Employee.class))).thenReturn(saved);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Fall"));
    }

    @Test
    void create_shouldReturn400_whenInvalid() throws Exception {
        Employee invalid = new Employee();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturnUpdated() throws Exception {
        Employee employee = createEmployee(null, "Fall", "Fatou", "fatou@email.com");
        Employee updated = createEmployee(1L, "Fall", "Fatou", "fatou.new@email.com");

        when(service.update(eq(1L), any(Employee.class))).thenReturn(updated);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("fatou.new@email.com"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void create_shouldReturn409_whenDuplicateEmail() throws Exception {
        Employee employee = createEmployee(null, "Fall", "Fatou", "fatou@email.com");

        when(service.save(any(Employee.class))).thenThrow(new DuplicateEmailException("fatou@email.com"));

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employee)))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_shouldReturn404() throws Exception {
        doThrow(new EmployeeNotFoundException(99L)).when(service).delete(99L);

        mockMvc.perform(delete("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_shouldReturnResults() throws Exception {
        when(service.searchByNom("Diop")).thenReturn(List.of(
                createEmployee(1L, "Diop", "Moussa", "moussa@email.com")
        ));

        mockMvc.perform(get("/api/employees/search?nom=Diop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nom").value("Diop"));
    }
}
