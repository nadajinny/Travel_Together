package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.repository.UserRepository
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private  val userRepository = UserRepository()
    private lateinit var binding: ActivityLoginBinding
    internal lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            preferences = getSharedPreferences("USERSIGN", Context.MODE_PRIVATE)
            val editor = preferences.edit()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                //email로 id조회
                userRepository.fetchUserIdByEmail(email = email,
                    onSuccess = {userId->
                        if(userId!=null){
                            //JSON 형태로 SharedPreferences에 저장
                            val userInfoJson = JSONObject().apply {
                                put("email", email)
                                put("userId", userId)
                            }.toString()
                            editor.putString(email, userInfoJson) //email을 키로 저장
                            editor.putString("currentUser", email) //현재 로그인한 사용자 email 저장
                            editor.apply()

                            //로그인 처리
                            userRepository.signIn(email=email, password=password,
                                onSuccess = {
                                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this,MainActivity::class.java))
                                    finish()
                                },
                                onFailure = { e ->
                                    Toast.makeText(this, "이메일/비밀번호가 틀립니다.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else{
                            Toast.makeText(this,"사용자를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = {e->
                        Toast.makeText(this,"오류가 발생했습니다. 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java)) // 회원가입 화면으로 이동
        }

        binding.btnEmail.setOnClickListener{
            startActivity(Intent(this, EmailActivity::class.java)) //email확인 화면으로 이동
        }

        binding.btnPw.setOnClickListener{
            startActivity(Intent(this, PasswordActivity::class.java)) //password 재설정 화면으로 이동
        }
    }
}
