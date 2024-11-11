package com.mogun.tomorrowhouse

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mogun.tomorrowhouse.databinding.FragmentAuthBinding

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private lateinit var binding: FragmentAuthBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAuthBinding.bind(view)

        setupSignUpButton()
        setupSignInOutButton()
    }

    override fun onStart() {
        super.onStart()

        if (Firebase.auth.currentUser == null) {
            initViewsToSignOutState()
        } else {
            initViewsToSignInState()
        }
    }

    private fun setupSignUpButton() {
        binding.signUpButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(binding.root, "이메일 또는 비밀번호를 입력해주세요.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Snackbar.make(binding.root, "회원가입에 성공하였습니다.", Snackbar.LENGTH_SHORT).show()
                        initViewsToSignInState()
                    } else {
                        Snackbar.make(binding.root, "회원가입에 실패했습니다.", Snackbar.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun setupSignInOutButton() {
        binding.signInOutButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (Firebase.auth.currentUser == null) {
                //  로그인
                if (email.isEmpty() || password.isEmpty()) {
                    Snackbar.make(binding.root, "이메일 또는 비밀번호를 입력해주세요.", Snackbar.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }

                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            initViewsToSignInState()
                        } else {
                            Snackbar.make(
                                binding.root,
                                "로그인에 실패했습니다. 이메일 또는 패스워드를 확인해주세요.",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                //  로그아웃
                Firebase.auth.signOut()
                initViewsToSignOutState()
            }
        }
    }

    // 로그인 상태
    private fun initViewsToSignInState() {
        binding.emailEditText.setText(Firebase.auth.currentUser?.email)
        binding.emailEditText.isEnabled = false
        binding.passwordEditText.isVisible = false
        binding.signInOutButton.text = getString(R.string.SignOut)
        binding.signUpButton.isEnabled = false
    }

    // 로그아웃 상태
    private fun initViewsToSignOutState() {
        binding.emailEditText.text.clear()
        binding.emailEditText.isEnabled = true
        binding.passwordEditText.text.clear()
        binding.passwordEditText.isVisible = true
        binding.signInOutButton.text = getString(R.string.SignIn)
        binding.signUpButton.isEnabled = true
    }
}