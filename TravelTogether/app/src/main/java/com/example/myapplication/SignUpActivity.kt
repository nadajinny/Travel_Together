package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivitySignupBinding
import com.example.myapplication.repository.UserRepository
import com.google.firebase.FirebaseNetworkException

class SignUpActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener { //singup 버튼 동작
            Log.d("SignUpActivity", "SignUp 버튼 클릭됨") //log에 버튼 클릭 동작 확인
            val userId = binding.etUserId.text.toString() //사용자 Id 입력받기
            val phoneNumber = binding.etPhoneNum.text.toString() //사용자 전화번호 받기
            val email = binding.etEmail.text.toString() //get email
            val password = binding.etPassword.text.toString() //get password
            val confirmPassword = binding.etConfirmPassword.text.toString() //checking password

            if (userId.isNotEmpty() && phoneNumber.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) { //password check
                    userRepository.signUp(context = this, userId = userId, phoneNumber = phoneNumber, email = email, password = password,
                        onSuccess = {
                            Log.d("SignUpActivity", "회원가입 성공!") //log에 회원가입 성공 출력
                            Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                            finish() // Login 화면으로 돌아가기
                        },
                        onFailure = { e ->
                            if(e is FirebaseNetworkException){
                                Log.e("SignUpActivity", "네트워크 오류 발생: ${e.message}") //log에 에러 메시지 출력
                                Toast.makeText(this, "네트워크 오류 발생. 인터넷 연결을 확인하세요.", Toast.LENGTH_SHORT).show()
                            }
                            e.printStackTrace() // 디버깅을 위한 출력
                            Log.d("SignUpActivity", "회원가입 실패: ${e.message}")
                            Toast.makeText(this, "회원가입 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else { //비밀번호 일치 X
                    Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                }
            } else { //필드 비어있음
                Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
