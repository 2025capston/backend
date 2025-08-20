package com.capston.matching_app.dto.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDTO {
    @NotBlank @Size(max=100)
    private String name;

    @NotBlank @Email @Size(max=100)
    private String email;

    @NotBlank @Size(min=8, max=72) //BCrypt salt 고려 시 72자 권장
    private String password;

    @NotBlank @Size(min=8, max=72)
    private String passwordConfirm; //저장용 아님. 검증용 입력만

    @NotBlank @Size(max=20)
    private String phoneNumber;

    //서버에서 바로 두 비밀번호 일치 검증
    @AssertTrue(message = "비밀번호가 일치하지 않습니다.")
    public boolean isPasswordMatching(){
        if(password == null || passwordConfirm==null) return false;
        return password.equals(passwordConfirm);
    }
}

