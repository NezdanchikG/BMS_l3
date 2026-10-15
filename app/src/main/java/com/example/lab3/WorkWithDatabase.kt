package com.example.lab3

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel
import java.util.*

class WorkWithDatabase : AppCompatActivity(), View.OnClickListener {

    private lateinit var dbHelper: DBHelper
    private lateinit var showRes: TextView
    private lateinit var showLog: TextView
    private lateinit var showPas: TextView
    private lateinit var showNote: TextView
    private lateinit var eresource: EditText
    private lateinit var elogin: EditText
    private lateinit var epassword: EditText
    private lateinit var enotes: EditText
    private lateinit var btnDel: Button
    private lateinit var btnAdd: Button
    private lateinit var btnGet: Button
    private lateinit var btnIm: Button
    private lateinit var btnEx: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.work_with_database2)

        val arguments = intent.extras
        val password = arguments?.get("password").toString()
        dbHelper = DBHelper(this, password)
        showRes = findViewById(R.id.ShowRes)
        showLog = findViewById(R.id.ShowLog)
        showPas = findViewById(R.id.ShowPass)
        showNote = findViewById(R.id.ShowNot)

        eresource = findViewById(R.id.editResource)
        elogin = findViewById(R.id.editLogin)
        epassword = findViewById(R.id.editPassword)
        enotes = findViewById(R.id.editNotes)

        btnAdd = findViewById(R.id.buttonAdd)
        btnDel = findViewById(R.id.buttonDel)
        btnGet = findViewById(R.id.buttonGet)
        btnIm = findViewById(R.id.buttonImport)
        btnEx = findViewById(R.id.buttonExport)

        btnAdd.setOnClickListener(this)
        btnDel.setOnClickListener(this)
        btnGet.setOnClickListener(this)
        btnIm.setOnClickListener(this)
        btnEx.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonDel -> {
                dbHelper.deleteAll()
                showRes.text = ""
                showLog.text = ""
                showPas.text = ""
                showNote.text = ""
            }
            R.id.buttonAdd -> {
                val resource = eresource.text.toString()
                val login = elogin.text.toString()
                val password = epassword.text.toString()
                val notes = enotes.text.toString()

                val data = Data(resource, login, password, notes)
                dbHelper.addOne(data)
                eresource.setText("")
                elogin.setText("")
                epassword.setText("")
                enotes.setText("")
            }
            R.id.buttonGet -> {
                showRes.text = ""
                showLog.text = ""
                showPas.text = ""
                showNote.text = ""
                val list: LinkedList<Data> = dbHelper.getAll()
                var textRes = ""
                var textLog = ""
                var textPas = ""
                var textNote = ""

                for (d in list) {
                    textRes += "${d.resource}\n"
                    textLog += "${d.login}\n"
                    textPas += "${d.password}\n"
                    textNote += "${d.notes}\n"
                }
                showRes.text = textRes
                showLog.text = textLog
                showPas.text = textPas
                showNote.text = textNote
            }
            R.id.buttonExport -> exportDatabase()
            R.id.buttonImport -> importDatabase()
        }
    }

    private fun exportDatabase() {
        try {
            val sd: File = Environment.getExternalStorageDirectory()

            if (sd.canWrite()) {
                val currentDBPath: String = getDatabasePath("example.db").absolutePath
                val backupDBPath = "example_backup.db"
                val currentDB = File(currentDBPath)
                val backupDB = File(sd, backupDBPath)

                if (currentDB.exists()) {
                    Log.d("ExportDatabase", "Current DB path: $currentDBPath")
                    Log.d("ExportDatabase", "Backup DB path: $backupDBPath")

                    val src: FileChannel = FileInputStream(currentDB).channel
                    val dst: FileChannel = FileOutputStream(backupDB).channel
                    dst.transferFrom(src, 0, src.size())
                    src.close()
                    dst.close()
                    Toast.makeText(this, "База данных успешно экспортирована", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ExportDatabase", "Current DB does not exist")
                }
            } else {
                Log.e("ExportDatabase", "Cannot write to external storage")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ExportDatabase", "Error exporting database: ${e.message}")
            Toast.makeText(this, "Ошибка экспорта базы данных", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importDatabase() {
        try {
            val sd: File = Environment.getExternalStorageDirectory()
            val data: File = Environment.getDataDirectory()

            if (sd.canWrite()) {
                val currentDBPath: String = getDatabasePath("example.db").absolutePath
                val backupDBPath = "example_backup.db"
                val currentDB = File(currentDBPath)
                val backupDB = File(sd, backupDBPath)

                if (backupDB.exists()) {
                    Log.d("ImportDatabase", "Current DB path: $currentDBPath")
                    Log.d("ImportDatabase", "Backup DB path: $backupDBPath")

                    val src: FileChannel = FileInputStream(backupDB).channel
                    val dst: FileChannel = FileOutputStream(currentDB).channel
                    dst.transferFrom(src, 0, src.size())
                    src.close()
                    dst.close()
                    Toast.makeText(this, "База данных успешно импортирована", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ImportDatabase", "Backup DB does not exist")
                }
            } else {
                Log.e("ImportDatabase", "Cannot write to external storage")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ImportDatabase", "Error importing database: ${e.message}")
            Toast.makeText(this, "Ошибка импорта базы данных", Toast.LENGTH_SHORT).show()
        }
    }
}