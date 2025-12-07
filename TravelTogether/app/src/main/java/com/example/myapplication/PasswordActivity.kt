package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class PasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //email로 비밀번호 변경 메일 발송
        binding.btnSendResetEmail.setOnClickListener {
            val email = binding.etEmail.text.toString() //email get

            if(email.isNotEmpty()){
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener {task ->
                        if(task.isSuccessful){ //전송 성공 콜백
                            Toast.makeText(this, "비밀번호 재설정 이메일이 전송되었습니다.", Toast.LENGTH_SHORT).show()
                        }else{ //전송 실패 콜백
                            Toast.makeText(this, "이메일 전송 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }
            }else{ //이메일란 비어있음
                Toast.makeText(this, "이메일 입력", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLogin.setOnClickListener{
            finish() //login 화면으로
        }
    }
}