package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityEmailBinding
import com.example.myapplication.repository.UserRepository

class EmailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailBinding
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //id, 전화번호로 email 확인
        binding.btnCheckEmail.setOnClickListener {
            val userId = binding.etUserId.text.toString()
            val phoneNumber = binding.etPhoneNumber.text.toString()

            if(userId.isNotEmpty() && phoneNumber.isNotEmpty()){
                userRepository.getEmailByUserId(userId = userId, phoneNumber = phoneNumber,
                    onSuccess = {email ->
                        if(email!=null){ //사용자 있음
                            binding.tvEmailResult.text = "email : $email"
                        }else{ //사용자 없음
                            binding.tvEmailResult.text = "사용자 정보를 찾을 수 없습니다."
                        }
                    },
                    onFailure = { exception ->
                        Toast.makeText(this,"오류가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                        //binding.tvEmailResult.text = "에러 발생: ${exception.message}"
                    })
            }else{ //id or 전화번호 비어있음
                Toast.makeText(this, "ID, 전화번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        //login 버튼 동작
        binding.btnLogin.setOnClickListener{
            finish() //login 화면으로
        }
    }
}