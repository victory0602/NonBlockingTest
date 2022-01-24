package com.nonBlocking.dto;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "USER")
@Entity
@Builder
public class UserDto extends BaseDto implements Serializable {
    @Id
    @Column(name = "ID")
    // 사용자 ID
    private String id;

    // 이름
    @Column(name = "NAME")
    private String name;

    // 주민등록번호
    @Column(name = "REGNO")
    private String regNo;

    // 비밀번호
    @Column(name = "PASSWORD")
    private String password;
}
