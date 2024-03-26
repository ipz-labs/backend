package com.example.backend.mapper;


import com.example.backend.talent.model.entity.Talent;
import com.example.backend.talent.model.response.TalentGeneralInfo;
import com.example.backend.talent.model.response.TalentOwnProfile;
import com.example.backend.talent.model.response.TalentProfile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TalentMapper {
    List<TalentGeneralInfo> toTalentGeneralInfos(List<Talent> talents);
    TalentProfile toTalentProfile(Talent talent);

    TalentOwnProfile toTalentOwnProfile(Talent talent);

}