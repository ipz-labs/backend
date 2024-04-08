package com.example.backend.talent.controller;


import com.example.backend.pagination.PageWithMetadata;
import com.example.backend.talent.model.request.TalentEdit;
import com.example.backend.talent.model.request.TalentLogin;
import com.example.backend.talent.model.request.TalentRegistration;
import com.example.backend.talent.model.response.TalentGeneralInfo;
import com.example.backend.talent.model.response.TalentOwnProfile;
import com.example.backend.talent.model.response.TalentProfile;
import com.example.backend.talent.service.TalentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/talents")
public class TalentController {
    private final TalentService talentService;
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageWithMetadata<TalentGeneralInfo> getAllTalents(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "9") int size){
        return talentService.getAllTalents(page, size);
    }
    @GetMapping("/export")
    public ResponseEntity<String> exportUsersToFile() {
        talentService.exportUsersToFile();
        return ResponseEntity.ok("Users exported successfully.");
    }
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TalentProfile getTalentProfile(@PathVariable Long id){return talentService.getTalentProfileById(id);}

    @PostMapping
    public ResponseEntity<?> registerTalent(@Valid @RequestBody TalentRegistration talent){
        var response = talentService.addTalent(talent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody TalentLogin loginRequest){
        var response = talentService.login(loginRequest);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TalentOwnProfile updateTalent(@PathVariable Long id,
                                         @Valid @RequestBody TalentEdit updatedTalent){
        return talentService.updateTalent(id, updatedTalent);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTalent(@PathVariable Long id) {
        talentService.deleteTalent(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
