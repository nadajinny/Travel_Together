package com.example.myapplication // 패키지 이름 정의

import android.os.Bundle // Bundle 클래스를 가져와 상태 저장/복원
import android.view.Menu // 메뉴 관련 클래스를 가져옴
import com.google.android.material.snackbar.Snackbar // 스낵바 UI 컴포넌트를 사용
import com.google.android.material.navigation.NavigationView // 네비게이션 드로어를 위한 클래스
import androidx.navigation.findNavController // NavController로 프래그먼트를 관리
import androidx.navigation.ui.AppBarConfiguration // AppBar 설정 클래스
import androidx.navigation.ui.navigateUp // 네비게이션 뒤로가기 처리
import androidx.navigation.ui.setupActionBarWithNavController // AppBar와 NavController 연결
import androidx.navigation.ui.setupWithNavController // NavigationView와 NavController 연결
import androidx.drawerlayout.widget.DrawerLayout // DrawerLayout 클래스
import androidx.appcompat.app.AppCompatActivity // 메인 액티비티의 부모 클래스
import androidx.appcompat.widget.Toolbar
import com.example.myapplication.data.model.SocketManager
import com.example.myapplication.databinding.ActivityMainBinding // 데이터 바인딩 클래스
import com.example.myapplication.databinding.LoginMainBinding

class MainActivity : AppCompatActivity() { // 메인 액티비티 정의

    private lateinit var appBarConfiguration: AppBarConfiguration // AppBar 설정을 위한 변수
    private lateinit var binding: ActivityMainBinding // 레이아웃과 연결하기 위한 바인딩 변수

    override fun onCreate(savedInstanceState: Bundle?) { // 액티비티 생성 시 호출
        super.onCreate(savedInstanceState) // 부모 클래스의 onCreate 호출

        binding = ActivityMainBinding.inflate(layoutInflater) // 데이터 바인딩 초기화
        setContentView(binding.root) // 바인딩된 루트 뷰를 화면에 표시

        setSupportActionBar(binding.appBarMain.toolbar) // 툴바를 액션바로 설정


        val drawerLayout: DrawerLayout = binding.drawerLayout // DrawerLayout 바인딩
        val navView: NavigationView = binding.navView // NavigationView 바인딩
        val navController = findNavController(R.id.nav_host_fragment_content_main) // NavController 가져오기
        val bottomNavView = binding.appBarMain.bottomNavigationView
        val toolbar : Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = " "
        supportActionBar?.setDisplayShowTitleEnabled(false)
        // AppBarConfiguration에 최상위 메뉴 ID 설정
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_trip, R.id.nav_message // 최상위 프래그먼트 ID
            ), drawerLayout // DrawerLayout과 연결
        )
        // NavController와 ActionBar 연결
        setupActionBarWithNavController(navController, appBarConfiguration)
        // NavigationView와 NavController 연결
        navView.setupWithNavController(navController)
        bottomNavView.setupWithNavController(navController)
        // NavController의 DestinationChangedListener 추가
        navController.addOnDestinationChangedListener { _, destfination, _ ->
            // 다른 경우 기본 AppBar로 설정
            setupDefaultAppBar()
            onCreateSocket()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { // 옵션 메뉴 생성
        menuInflater.inflate(R.menu.main, menu) // main 메뉴를 툴바에 추가
        return true
    }

    override fun onSupportNavigateUp(): Boolean { // 네비게이션 뒤로 가기 처리
        val navController = findNavController(R.id.nav_host_fragment_content_main) // NavController 가져오기
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp() // 뒤로 가기 동작
    }

    private fun onCreateSocket() { // 옵션 메뉴 생성
        SocketManager.initializeSocket()
        SocketManager.connect()
    }

    // 기본 AppBar로 복구
    private fun setupDefaultAppBar() {
        setSupportActionBar(binding.appBarMain.toolbar) // 기본 AppBar 설정
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // 뒤로가기 버튼 활성화
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED) // 드로어 활성화
    }

}
