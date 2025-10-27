package com.ch220048.controlgastospersonales

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnRegister: MaterialButton
    private lateinit var btnGoogleSignIn: MaterialButton

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Log.w("LoginActivity", "Google sign in failed", e)
            Toast.makeText(this, "Error al iniciar sesión con Google: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            irAMainActivity()
            return
        }

        configurarGoogleSignIn()
        inicializarVistas()
        animarCard()
        configurarListeners()
    }

    private fun configurarGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun inicializarVistas() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
    }

    private fun animarCard() {
        val cardLogin = findViewById<CardView>(R.id.cardLogin)
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        cardLogin.startAnimation(animation)
    }

    private fun configurarListeners() {
        btnLogin.setOnClickListener {
            iniciarSesion()
        }

        btnRegister.setOnClickListener {
            registrarUsuario()
        }

        btnGoogleSignIn.setOnClickListener {
            iniciarSesionConGoogle()
        }
    }

    private fun iniciarSesion() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Ingresa tu correo"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Ingresa tu contraseña"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            etPassword.requestFocus()
            return
        }

        btnLogin.isEnabled = false
        btnLogin.text = "Iniciando sesión..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                btnLogin.isEnabled = true
                btnLogin.text = "Iniciar Sesión"

                if (task.isSuccessful) {
                    Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show()
                    irAMainActivity()
                } else {
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun registrarUsuario() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Ingresa tu correo"
            etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Ingresa tu contraseña"
            etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            etPassword.requestFocus()
            return
        }

        btnRegister.isEnabled = false
        btnRegister.text = "Creando cuenta..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                btnRegister.isEnabled = true
                btnRegister.text = "Crear Cuenta Nueva"

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "¡Cuenta creada exitosamente!",
                        Toast.LENGTH_SHORT
                    ).show()
                    irAMainActivity()
                } else {
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun iniciarSesionConGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "¡Bienvenido ${account?.displayName}!", Toast.LENGTH_SHORT).show()
                    irAMainActivity()
                } else {
                    Toast.makeText(
                        this,
                        "Error de autenticación: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun irAMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}