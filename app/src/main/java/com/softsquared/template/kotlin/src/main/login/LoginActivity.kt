package com.softsquared.template.kotlin.src.main.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.softsquared.template.kotlin.config.BaseActivity
import com.softsquared.template.kotlin.databinding.ActivityLoginBinding

class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {
    lateinit var kakaoCallback: (OAuthToken?, Throwable?) -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 카카오톡으로 로그인 정보 확인


        binding.kakaoLoginButton.setOnClickListener {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e("TAG", "로그인 실패", error)
                } else if (token != null) {
                    Log.i("TAG", "로그인 성공 ${token.accessToken}")
                    startActivity(Intent(this, RegisterLogin::class.java))
                    finish()
                }
            }

            setKakaoCallback()
            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Log.e("TAG", "카카오톡으로 로그인 실패", error)

                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = kakaoCallback)
                    } else if (token != null) {
                        Log.i("TAG", "카카오톡으로 로그인 성공 ${token.accessToken}")
                        startActivity(Intent(this, RegisterLogin::class.java))
                        finish()
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this, callback = kakaoCallback)
            }
        }
    }

    fun setKakaoCallback() {
        kakaoCallback = { token, error ->
            if (error != null) {
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Log.d("[카카오로그인]", "접근이 거부 됨(동의 취소)")
                    }
                    error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        Log.d("[카카오로그인]", "유효하지 않은 앱")
                    }
                    error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        Log.d("[카카오로그인]", "인증 수단이 유효하지 않아 인증할 수 없는 상태")
                    }
                    error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        Log.d("[카카오로그인]", "요청 파라미터 오류")
                    }
                    error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        Log.d("[카카오로그인]", "유효하지 않은 scope ID")
                    }
                    error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        Log.d("[카카오로그인]", "설정이 올바르지 않음(android key hash)")
                    }
                    error.toString() == AuthErrorCause.ServerError.toString() -> {
                        Log.d("[카카오로그인]", "서버 내부 에러")
                    }
                    error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        Log.d("[카카오로그인]", "앱이 요청 권한이 없음")
                    }
                    else -> { // Unknown
                        Log.d("[카카오로그인]", "기타 에러")
                    }
                }
            } else if (token != null) {
                Log.d("[카카오로그인]", "로그인에 성공하였습니다.\n${token.accessToken}")
                // tokenInfo , error _로 rename
                UserApiClient.instance.accessTokenInfo { _, _ ->
                    UserApiClient.instance.me { user, _ ->
                        binding.nickname.text = "닉네임: ${user?.kakaoAccount?.profile?.nickname}"
                    }
                }
                startActivity(Intent(this, RegisterLogin::class.java))
                finish()
            } else {
                Log.d("카카오로그인", "토큰==null error==null")
            }
        }

    }
}
//            //로그아웃
//            UserApiClient.instance.logout { error ->
//                if (error != null) {
//                    Log.d("카카오","카카오 로그아웃 실패")
//                }else {
//                    Log.d("카카오","카카오 로그아웃 성공!")
//                }
//            }
//            //회원탈퇴
//            UserApiClient.instance.unlink { error ->
//                if (error != null) {
//                    Log.d("카카오로그인","회원 탈퇴 실패")
//                }else {
//                    Log.d("카카오로그인","회원 탈퇴 성공")
//                }
//            }
//        }