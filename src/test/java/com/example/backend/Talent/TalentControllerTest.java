package com.example.backend.Talent;

import com.example.backend.jwt.JwtTokenProvider;
import com.example.backend.pagination.PageWithMetadata;
import com.example.backend.payload.AuthResponse;
import com.example.backend.talent.controller.TalentController;
import com.example.backend.talent.model.entity.Talent;
import com.example.backend.talent.model.request.TalentEdit;
import com.example.backend.talent.model.request.TalentLogin;
import com.example.backend.talent.model.request.TalentRegistration;
import com.example.backend.talent.model.response.TalentGeneralInfo;
import com.example.backend.talent.model.response.TalentOwnProfile;
import com.example.backend.talent.model.response.TalentProfile;
import com.example.backend.talent.service.TalentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(TalentController.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TalentControllerTest {

    @MockBean
    TalentService talentService;
    @MockBean
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    MockMvc mockMvc;
    private Talent talent;
    @Autowired
    private ObjectMapper objectMapper;

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

    }

    @Test
    @Order(1)
    @DisplayName("[US-1] - Get all talents successfully")
    void getAllTalentsSuccessfully() throws Exception {
        List<TalentGeneralInfo> talentGeneralInfos = Arrays.asList(
                TalentGeneralInfo.builder()
                        .id(talent.getId())
                        .lastname(talent.getLastname())
                        .firstname(talent.getFirstname())
                        .skills(talent.getSkills()).build(),
                TalentGeneralInfo.builder()
                        .id(2L)
                        .lastname("John")
                        .firstname("Doe")
                        .skills(Set.of("Java", "Spring")).build()
        );

        given(talentService.getAllTalents(0, 9))
                .willReturn(new PageWithMetadata<>(talentGeneralInfos, 1));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents")
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").exists());
    }

    @Test
    @Order(2)
    @DisplayName("[US-2] - Get talent profile successfully")
    void getTalentProfileSuccessfully() throws Exception {
        given(talentService.getTalentProfileById(talent.getId()))
                .willReturn(new TalentProfile());

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{id}", talent.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").doesNotExist());
    }

    @Test
    @Order(3)
    @DisplayName("[US-2] - Get own profile successfully")
    void getOwnProfileSuccessfully() throws Exception {
        given(talentService.getTalentProfileById(talent.getId()))
                .willReturn(new TalentOwnProfile(talent.getEmail(), LocalDate.now()));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.get("/api/v1/talents/{id}", talent.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").exists());
    }


    @Test
    @Order(4)
    @DisplayName("[US-3] - Register new Talent successfully")
    void registerNewTalentSuccessfully() throws Exception {
        TalentRegistration registrationRequest = generateRegistrationRequest();

        String jwtToken = "token";

        given(jwtTokenProvider.generateJwtToken(any(Talent.class)))
                .willReturn(jwtToken);

        when(talentService.addTalent(any(TalentRegistration.class)))
                .thenReturn(new AuthResponse(jwtToken));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/talents")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)));

        response
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt_token").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt_token").value(jwtToken));
    }


    @Test
    @Order(5)
    @DisplayName("[US-3] - Log in successfully")
    void loginSuccessfully() throws Exception {
        TalentLogin loginRequest = new TalentLogin(talent.getEmail(), talent.getPassword());

        String jwtToken = "token";

        given(jwtTokenProvider.generateJwtToken(any(Talent.class)))
                .willReturn(jwtToken);

        given(talentService.login(any(TalentLogin.class)))
                .willReturn(new AuthResponse(jwtToken));

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.post("/api/v1/talents/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)));
        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt_token").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.jwt_token").value(jwtToken))
                .andReturn().getResponse().getContentAsString();
    }


    @Test
    @Order(6)
    @DisplayName("[US-3] - Edit own profile successfully")
    void editOwnProfileSuccessfully() throws Exception {
        TalentEdit editRequest = TalentEdit.builder()
                .lastname("John")
                .firstname("Doe")
                .skills(Set.of("Java", "Spring"))
                .build();

        TalentOwnProfile expectedDto = new TalentOwnProfile();
        expectedDto.setLastname(editRequest.getLastname());
        expectedDto.setFirstname(editRequest.getFirstname());
        expectedDto.setEmail(talent.getEmail());
        expectedDto.setBirthday(talent.getBirthday());
        expectedDto.setSkills(editRequest.getSkills());

        given(talentService.updateTalent(anyLong(), any(TalentEdit.class)))
                .willReturn(expectedDto);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{id}", talent.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest)));
        response
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").exists())
                .andReturn().getResponse().getContentAsString();
    }



    @Test
    @Order(7)
    @DisplayName("[US-3] - Fail editing own profile")
    void failEditingOwnProfile() throws Exception {
        TalentEdit editRequest = TalentEdit.builder()
                .lastname("John")
                .firstname("Doe")
                .build();

        given(talentService.updateTalent(anyLong(), any(TalentEdit.class)))
                .willThrow(NullPointerException.class);

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.patch("/api/v1/talents/{id}", talent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(editRequest))
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    @DisplayName("[US-4] - Delete own profile successfully")
    void deleteOwnProfileSuccessfully() throws Exception {
        willDoNothing().given(talentService).deleteTalent(talent.getId());

        ResultActions response = mockMvc
                .perform(MockMvcRequestBuilders.delete("/api/v1/talents/{id}", talent.getId())
                        .accept(MediaType.APPLICATION_JSON));

        response
                .andDo(print())
                .andExpect(status().isNoContent());
    }


    private TalentRegistration generateRegistrationRequest() {
        TalentRegistration registrationRequest = new TalentRegistration();

        registrationRequest.setLastname(talent.getLastname());
        registrationRequest.setFirstname(talent.getFirstname());
        registrationRequest.setEmail(talent.getEmail());
        registrationRequest.setPassword(talent.getPassword());
        registrationRequest.setSkills(talent.getSkills());

        return registrationRequest;
    }
}
