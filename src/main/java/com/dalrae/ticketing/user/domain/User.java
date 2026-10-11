package com.dalrae.ticketing.user.domain;

import com.dalrae.ticketing.global.Role;
import com.dalrae.ticketing.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String password;

    @Size(max = 50)
    private String name;

    @Size(max = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    protected User() {
    }

    @Builder
    private User(String email, String password, String name, String phone) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = Role.USER;
    }

}
