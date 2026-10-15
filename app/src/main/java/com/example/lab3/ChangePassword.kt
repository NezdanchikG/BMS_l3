package com.example.lab3

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ChangePassword : AppCompatActivity() {

    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.changepassword)

        oldPasswordEditText = findViewById(R.id.oldPasswordEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        changePasswordButton = findViewById(R.id.changePasswordButton)

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        changePasswordButton.setOnClickListener(View.OnClickListener {
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()
            val savedPassword = sharedPreferences.getString("password", "")

            if (oldPassword == savedPassword) {
                // Старый пароль верен, меняем на новый
                val editor = sharedPreferences.edit()
                editor.putString("password", newPassword)
                editor.apply()

                Toast.makeText(this@ChangePassword, "Пароль изменен", Toast.LENGTH_SHORT).show()

                val dbDecrypt = DBHelper(this@ChangePassword, oldPassword)
                dbDecrypt.decryptAllData()
                dbDecrypt.close()

                val dbEncrypt = DBHelper(this@ChangePassword, newPassword)
                dbEncrypt.encryptAllData()
                dbEncrypt.close()

                finish()
            } else {
                Toast.makeText(this@ChangePassword, "Неправильный старый пароль. Правильный пароль: $savedPassword", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
