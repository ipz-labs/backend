package com.example.backend.talent.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

import static jakarta.persistence.FetchType.EAGER;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Talent implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    private String lastname;
    private String firstname;
    private String avatar;
    private String banner;
    @ElementCollection(fetch = EAGER)
    private Set<String> skills;
    private String location;
    private String email;
    private String password;
    @JsonIgnore
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private String aboutMe;

}