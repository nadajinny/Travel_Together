package com.example.myapplication.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.myapplication.MainActivity
import com.example.myapplication.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository (
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun signUp( //signup함수
        context: Context,
        userId : String, //사용자 지정 id
        phoneNumber: String, //전화번호
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password) //이메일, 비밀번호로 회원가입
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user //유저 정보 불러오기, 객체 생성
                    if(user==null){ //유저객체 비어있음
                        Log.e("UserRepository", "FirebaseUser is null after sign-up")
                        onFailure(Exception("FirebaseUser is null"))
                        onFailure.invoke(Exception("User is null")) //문제 발생 시 실패 콜백
                    }
                    saveUserToFirestore(context = context, user = user, userId = userId, phoneNumber = phoneNumber, email = email, onSuccess =onSuccess, onFailure = onFailure)
                } else {
                    onFailure(task.exception ?: Exception("Unknown error")) // firebase 에러 메시지 로그로 출력
                    task.exception?.printStackTrace() // 디버깅을 위한 출력
                }
            }
    }

    //firestore에 user 데이터 저장
    private fun saveUserToFirestore(context: Context, user: FirebaseUser?, userId: String, phoneNumber: String, email: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit){
        if(user == null){ //null일시
            onFailure(Exception("User is null")) //오류
            return
        }

        val userData = User(userId = userId, phoneNumber = phoneNumber, email = email) //userdata 객체

        firestore.collection("users")
            .document(user.uid) //firebase Authentication에서 생성된 UID를 사용. 사용자 지정 id X
            .set(userData) //데이터 저장
            .addOnSuccessListener {
                Toast.makeText(context, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                goToMainActivity(context, user) //메인화면으로
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Firestore 저장 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                onFailure(exception)
            }
    }

    fun goToMainActivity(context: Context, user: FirebaseUser?) { //메인화면 이동
        if (user != null) {
            val intent = Intent(context, MainActivity::class.java) //메인 연결
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent) //이동
        }
    }

    fun signIn( //signin 함수
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password) //이메일, 비밀번호로 singin
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception ?: Exception("Unknown error")) // firebase 에러 메시지 로그로 출력
                    task.exception?.printStackTrace() // 디버깅을 위한 출력
                }
            }
    }

    fun signOut() { //signOUt
        auth.signOut()
    }

    fun getEmailByUserId(userId: String, phoneNumber: String, onSuccess: (String?) -> Unit, onFailure: (Exception) -> Unit){
        firestore.collection("users")
            .whereEqualTo("userId", userId)
            .whereEqualTo("phoneNumber", phoneNumber)//query 메서드 -> 조건 검색
            .get() //firestore 값 갖고오기
            .addOnSuccessListener { querySnapshot -> //성공 콜백
                if(!querySnapshot.isEmpty) {
                    val email = querySnapshot.documents.firstOrNull()?.getString("email")
                    onSuccess(email)
                }else{
                    onSuccess(null) //해당 이메일 없음
                }
            }
            .addOnFailureListener{exception ->
                onFailure(exception)
            }
    }

    // Firestore에서 email에 대응하는 userId 가져오기
    fun fetchUserIdByEmail(email: String, onSuccess: (String?) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // 첫 번째 문서에서 userId 가져오기
                    val userId = querySnapshot.documents.firstOrNull()?.getString("userId")
                    onSuccess(userId) // 성공 시 userId 반환
                } else {
                    onSuccess(null) // 데이터 없음
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

}