package com.example.lab3

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Environment
import android.net.Uri

class MainActivity : AppCompatActivity() {

    private lateinit var passwordEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var changePassButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Запрос разрешений на чтение и запись во внешнее хранилище
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                10
            )
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                10
            )
        }

        // Запрос разрешения MANAGE_EXTERNAL_STORAGE для Android 11 и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:${applicationContext.packageName}")
                startActivity(intent)
            }
        }

        passwordEditText = findViewById(R.id.passwordEditText)
        submitButton = findViewById(R.id.submitButton)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        submitButton.setOnClickListener {
            val enteredPassword = passwordEditText.text.toString()
            val savedPassword = sharedPreferences.getString("password", "") ?: ""

            if (savedPassword.isEmpty()) {
                // Первый вход пользователя, установление пароля
                val editor = sharedPreferences.edit()
                editor.putString("password", enteredPassword)
                editor.apply()
            } else if (enteredPassword != savedPassword) {
                // Неправильный пароль
                Toast.makeText(this, "Неправильный пароль", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Пароль верный или установлен, открываем WorkWithDatabase
            val intent = Intent(this, WorkWithDatabase::class.java)
            intent.putExtra("password", savedPassword)
            startActivity(intent)
            passwordEditText.setText("")
        }

        changePassButton = findViewById(R.id.changePassButton)

        changePassButton.setOnClickListener {
            val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)
            passwordEditText.setText("")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            10 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Разрешения предоставлены, можно продолжать работу
                } else {
                    // Разрешения не предоставлены, показываем сообщение пользователю
                    Toast.makeText(this, "Разрешения на доступ к хранилищу не предоставлены", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}