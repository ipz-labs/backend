package com.example.backend.Talent;

import com.example.backend.pagination.PageWithMetadata;
import com.example.backend.talent.model.entity.Talent;

import com.example.backend.talent.model.response.TalentGeneralInfo;
import com.example.backend.talent.repository.TalentRepository;
import com.example.backend.talent.service.TalentService;
import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.List;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TalentServiceTestWithoutMocks {

    @Autowired
    private TalentRepository talentRepository;

    @Autowired
    private TalentService talentService;

    private Talent talent;

    @BeforeEach
    public void setUp() {
        talent = Talent.builder()
                .id(1L)
                .lastname("John")
                .firstname("Doe")
                .email("john.doe@gmail.com")
                .password("1234567890")
                .skills(Set.of("Java", "Spring"))
                .build();
        talentRepository.save(talent); // Save a talent to the database for testing
    }

    @Test
    @Order(1)
    @DisplayName("[US-1] - Get all talents successfully")
    void getAllTalentsSuccessfully() {
        // Create a list of talents
        List<Talent> talents = Arrays.asList(
                talent,
                Talent.builder()
                        .id(2L)
                        .lastname("John")
                        .firstname("Doe")
                        .email("john.doe@gmail.com")
                        .password("123")
                        .skills(Set.of("Java", "Spring"))
                        .build()
        );

        // Create a Page object containing the list of talents
        Page<Talent> talentsPage = new PageImpl<>(talents);

        // Invoke the actual service method to get all talents
        PageWithMetadata<TalentGeneralInfo> result = talentService.getAllTalents(0, 9);

        // Assert that the returned result matches the expected content and metadata
        assertThat(result.getContent()).hasSize(9); // Assuming the page size is 9, there are only 2 talents
        assertThat(result.getTotalPages()).isEqualTo(3); // Assuming all talents fit on one page
        assertThat(result.getContent().get(0).getId()).isEqualTo(20L); // Assuming the first talent has ID 1
    }
}