package com.example.backend.bootstrapdata;

import com.example.backend.talent.model.entity.Talent;
import com.example.backend.talent.repository.TalentRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class TalentDataLoader implements CommandLineRunner {

    public static final int SIZE = 20;
    private final TalentRepository talentRepository;
    private final Faker faker;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        for (int i = 0; i < SIZE; i++) {
            talentRepository.save(generateOneTalent());
        }
    }

    private Talent generateOneTalent() {
        String lastname = faker.name().lastName();
        String firstname = faker.name().firstName();
        String email = firstname.toLowerCase() + "." + lastname.toLowerCase() + "@gmail.com";
        String location = faker.address().country() + ", " + faker.address().cityName();
        String password = "1234567890";

        return Talent.builder()
                .lastname(lastname)
                .firstname(firstname)
                .avatar(faker.avatar().image())
                .banner(faker.internet().image())
                .email(email)
                .password(passwordEncoder.encode(password))
                .birthday(LocalDate.now())
                .aboutMe(faker.lebowski().quote())
                .location(location)
                .skills(generateSkills())
                .build();
    }

    private Set<String> generateSkills() {
        Set<String> skills = new HashSet<>();
        int size = faker.random().nextInt(3) + 3;

        for (int i = 0; i < size; i++)
            skills.add(faker.job().keySkills());

        return skills;
    }
}