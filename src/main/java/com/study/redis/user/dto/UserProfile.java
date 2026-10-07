package com.study.redis.user.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="user_profile")
@Getter
@NoArgsConstructor
public class UserProfile {

    @Id
    private Long userId;

    private String name;
    private String email;
    private String tier;

    public void changeProfile(String name, String email, String tier) {
        this.name = name;
        this.email = email;
        this.tier = tier;
    }
}
