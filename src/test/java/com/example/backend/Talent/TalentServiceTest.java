package com.example.backend.Talent;

import com.example.backend.jwt.JwtTokenProvider;
import com.example.backend.mapper.TalentMapper;
import com.example.backend.pagination.PageWithMetadata;
import com.example.backend.payload.AuthResponse;
import com.example.backend.talent.exception.TalentExistsException;
import com.example.backend.talent.exception.TalentNotFoundException;
import com.example.backend.talent.model.entity.Talent;
import com.example.backend.talent.model.request.TalentEdit;
import com.example.backend.talent.model.request.TalentLogin;
import com.example.backend.talent.model.request.TalentRegistration;
import com.example.backend.talent.model.response.TalentGeneralInfo;
import com.example.backend.talent.repository.TalentRepository;
import com.example.backend.talent.service.TalentService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
@ExtendWith({MockitoExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TalentServiceTest {

    @Mock
    private TalentRepository talentRepository;

    @Mock
    private TalentMapper talentMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @InjectMocks
    private TalentService talentService;

    private Talent talent;

    private static final Long nonExistentTalentId = 1000L;

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
    void getAllTalentsSuccessfully() {
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

        Page<Talent> talentsPage = new PageImpl<>(talents);

        List<TalentGeneralInfo> talentGeneralInfos = Arrays.asList(
                TalentGeneralInfo.builder()
                        .id(talent.getId())
                        .lastname(talent.getLastname())
                        .firstname(talent.getFirstname())
                        .skills(talent.getSkills())
                        .build(),
                TalentGeneralInfo.builder()
                        .id(2L)
                        .lastname("John")
                        .firstname("Doe")
                        .skills(Set.of("Java", "Spring"))
                        .build()
        );

        when(talentMapper.toTalentGeneralInfos(anyList())).thenReturn(talentGeneralInfos);

        when(talentRepository.findAllByOrderByIdDesc(any(PageRequest.class))).thenReturn(talentsPage);

        PageWithMetadata<TalentGeneralInfo> result = talentService.getAllTalents(0, 9);

        verify(talentRepository, times(1)).findAllByOrderByIdDesc(PageRequest.of(0, 9));

        verify(talentMapper, times(1)).toTalentGeneralInfos(talents);

        assertThat(result.getContent()).isEqualTo(talentGeneralInfos);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(talentGeneralInfos.get(0).getId());
    }


    @Test
    @Order(4)
    @DisplayName("[US-2] - Fail get talent profile because talent does not exist")
    void failGettingTalentProfileWhichDoesNotExist() {

        when(talentRepository.findById(nonExistentTalentId))
                .thenThrow(new TalentNotFoundException("Talent was not found"));

        assertThrows(TalentNotFoundException.class, () -> talentService.getTalentProfileById(nonExistentTalentId));
    }

    @Test
    @Order(5)
    @DisplayName("[US-3] - Register new Talent successfully")
    void registerNewTalentSuccessfully() {

        when(talentRepository.save(any()))
                .thenReturn(talent);

        AuthResponse authResponse = talentService.addTalent(generateRegistrationRequest());

        assertThat(authResponse).isNotNull();
    }

    @Test
    @Order(6)
    @DisplayName("[US-3] - Register new Talent with earlier occupied email")
    void registerNewTalentWithEarlierOccupiedEmail() {

        String exceptionMessage = "The talent has already exists with email [" + talent.getEmail() + "]";

        when(talentRepository.save(any()))
                .thenThrow(new TalentExistsException(exceptionMessage));

        assertThrows(TalentExistsException.class, () -> talentService.addTalent(generateRegistrationRequest()));
    }

    @Test
    @Order(7)
    @DisplayName("[US-3] - Register new Talent and forget input some data")
    void registerNewTalentAndForgetInputSomeData() {
        TalentRegistration registrationRequest = generateRegistrationRequest();
        registrationRequest.setFirstname(null);

        when(talentRepository.save(any()))
                .thenThrow(new MockitoException(""));

        assertThrows(MockitoException.class, () -> talentService.addTalent(registrationRequest));
    }

    @Test
    @Order(8)
    @DisplayName("[US-3] - Log in successfully")
    void loginSuccessfully() {
        securitySetUp();

        TalentLogin loginRequest = new TalentLogin(talent.getEmail(), "12345");

        when(talentRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(talent));

        when(passwordEncoder.matches(loginRequest.getPassword(), talent.getPassword())).thenReturn(true);

        AuthResponse loggedInUser = talentService.login(loginRequest);

        verify(talentRepository, times(1)).findByEmail(loginRequest.getEmail());

        assertThat(loggedInUser).isNotNull();
    }

    @Test
    @Order(9)
    @DisplayName("[US-3] - Fail attempt of log in")
    void failLoginWithBadCredentials() {
        securitySetUp();

        TalentLogin loginRequestWithBadPassword =
                new TalentLogin(talent.getEmail(), "another_password");

        when(talentRepository.findByEmail(loginRequestWithBadPassword.getEmail())).thenReturn(Optional.of(talent));

        when(passwordEncoder.matches(loginRequestWithBadPassword.getPassword(), talent.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> talentService.login(loginRequestWithBadPassword));

        TalentLogin loginRequestWithBadEmail =
                new TalentLogin("mark.gimonov@gmail.com", "12345");

        when(talentRepository.findByEmail(loginRequestWithBadEmail.getEmail())).thenReturn(Optional.empty());

        assertThrows(TalentNotFoundException.class, () -> talentService.login(loginRequestWithBadEmail));
    }





    @Test
    @Order(12)
    @DisplayName("[US-3] - Fail editing own profile")
    void failEditingOwnProfile() {
        securitySetUp();

        willReturnOwnProfile();

        TalentEdit editRequest = TalentEdit.builder()
                .lastname("Himonov")
                .firstname("Mark")
                .build();

        assertThrows(NullPointerException.class, () -> talentService.updateTalent(talent.getId(), editRequest));
    }




    @Test
    @Order(15)
    @DisplayName("[US-4] - Delete non-existent profile")
    void deleteNonExistentProfile() {
        when(talentRepository.findById(nonExistentTalentId))
                .thenThrow(new TalentNotFoundException("Talent was not found"));

        assertThrows(TalentNotFoundException.class, () -> talentService.deleteTalent(nonExistentTalentId));
    }


    private void securitySetUp() {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        assertThat(securityContext.getAuthentication()).isEqualTo(authentication);
    }

    private void willReturnOwnProfile() {
        given(talentRepository.findById(talent.getId()))
                .willReturn(Optional.of(talent));

    }

    private void willReturnProfile() {
        given(talentRepository.findById(talent.getId()))
                .willReturn(Optional.of(talent));

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
