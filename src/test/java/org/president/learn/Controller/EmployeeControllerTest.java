package org.president.learn.Controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.president.learn.Entity.Employee;
import org.president.learn.Exception.DuplicateEmailException;
import org.president.learn.Exception.EmployeeNotFoundException;
import org.president.learn.Service.EmployeeService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmployeeControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private EmployeeService service;

    @InjectMocks
    private EmployeeController controller;

    @BeforeEach
    void setUp() {
        var factory = new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean();
        factory.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new org.president.learn.Exception.GlobalExceptionHandler())
                .setValidator(factory)
                .build();
    }

    @Test
    void getAll_shouldReturnList() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                employee(1L, "Diop", "Moussa", "moussa@email.com"),
                employee(2L, "Sow", "Aminata", "aminata@email.com")
        ));

        mockMvc.perform(get("/api/employes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nom").value("Diop"))
                .andExpect(jsonPath("$[1].nom").value("Sow"));
    }

    @Test
    void getById_shouldReturnEmployee() throws Exception {
        when(service.findById(1L)).thenReturn(employee(1L, "Diop", "Moussa", "moussa@email.com"));

        mockMvc.perform(get("/api/employes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Diop"))
                .andExpect(jsonPath("$.email").value("moussa@email.com"));
    }

    @Test
    void getById_shouldReturn404() throws Exception {
        when(service.findById(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/api/employes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_shouldReturn201() throws Exception {
        Employee input = employee(null, "Fall", "Fatou", "fatou@email.com");
        Employee saved = employee(1L, "Fall", "Fatou", "fatou@email.com");
        when(service.save(any(Employee.class))).thenReturn(saved);

        mockMvc.perform(post("/api/employes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_shouldReturn400_whenInvalid() throws Exception {
        mockMvc.perform(post("/api/employes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void update_shouldReturnUpdated() throws Exception {
        Employee input = employee(null, "Diop", "Moussa", "moussa.new@email.com");
        Employee updated = employee(1L, "Diop", "Moussa", "moussa.new@email.com");
        when(service.update(eq(1L), any(Employee.class))).thenReturn(updated);

        mockMvc.perform(put("/api/employes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("moussa.new@email.com"));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/employes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void create_shouldReturn409_whenDuplicateEmail() throws Exception {
        Employee input = employee(null, "Fall", "Fatou", "fatou@email.com");
        when(service.save(any(Employee.class))).thenThrow(new DuplicateEmailException("fatou@email.com"));

        mockMvc.perform(post("/api/employes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(input)))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_shouldReturn404() throws Exception {
        doThrow(new EmployeeNotFoundException(99L)).when(service).delete(99L);

        mockMvc.perform(delete("/api/employes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void search_shouldReturnResults() throws Exception {
        when(service.searchByNom("Diop")).thenReturn(List.of(employee(1L, "Diop", "Moussa", "moussa@email.com")));

        mockMvc.perform(get("/api/employes/search?nom=Diop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nom").value("Diop"));
    }

    private Employee employee(Long id, String nom, String prenom, String email) {
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
}
