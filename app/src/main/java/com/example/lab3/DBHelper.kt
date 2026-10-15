package com.example.lab3

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Base64
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.*

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DBHelper(context: Context?, secret_key: String) : SQLiteOpenHelper(context, "example.db", null, 1),
    AutoCloseable {

    private val MY_TABLE = "MY_TABLE"
    private val COLUMN_RESOURCE = "COLUMN_RESOURCE"
    private val COLUMN_LOGIN = "COLUMN_LOGIN"
    private val COLUMN_PASSWORD = "COLUMN_PASSWORD"
    private val COLUMN_NOTES = "COLUMN_NOTES"
    private val SECRET_KEY: String = secret_key
    private val INIT_VECTOR = "BaseVectorCipher"
    private val SALT = "MAGICNUMBER"

    private val encryptCipher: Cipher
    private val decryptCipher: Cipher

    init {
        encryptCipher = initCipher(Cipher.ENCRYPT_MODE)
        decryptCipher = initCipher(Cipher.DECRYPT_MODE)
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE $MY_TABLE (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_RESOURCE TEXT, "
                + "$COLUMN_LOGIN TEXT, "
                + "$COLUMN_PASSWORD TEXT, "
                + "$COLUMN_NOTES TEXT)")
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Обновление базы данных, если необходимо
    }

    override fun close() {
        // Освобождение ресурсов
    }

    fun deleteAll() {
        val db = writableDatabase
        db.delete(MY_TABLE, null, null)
        db.close()
    }

    fun addOne(data: Data) {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(COLUMN_RESOURCE, encryptData(data.resource))
        cv.put(COLUMN_LOGIN, encryptData(data.login))
        cv.put(COLUMN_PASSWORD, encryptData(data.password))
        cv.put(COLUMN_NOTES, encryptData(data.notes))
        db.insert(MY_TABLE, null, cv)
        db.close()
    }

    fun getAll(): LinkedList<Data> {
        val list = LinkedList<Data>()
        val db = writableDatabase
        val cursor: Cursor = db.query(MY_TABLE, null, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val iRes = cursor.getColumnIndex(COLUMN_RESOURCE)
                val iLogin = cursor.getColumnIndex(COLUMN_LOGIN)
                val iPassword = cursor.getColumnIndex(COLUMN_PASSWORD)
                val iNotes = cursor.getColumnIndex(COLUMN_NOTES)

                // Проверка на корректность индексов столбцов
                if (iRes != -1 && iLogin != -1 && iPassword != -1 && iNotes != -1) {
                    val data = Data(
                        decryptData(cursor.getString(iRes)),
                        decryptData(cursor.getString(iLogin)),
                        decryptData(cursor.getString(iPassword)),
                        decryptData(cursor.getString(iNotes))
                    )
                    list.add(data)
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    private fun encryptData(data: String): String {
        return try {
            val encryptedBytes: ByteArray = encryptCipher.doFinal(data.toByteArray())
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            data
        }
    }

    private fun decryptData(data: String): String {
        return try {
            val encryptedBytes: ByteArray = Base64.decode(data, Base64.DEFAULT)
            val decryptedBytes: ByteArray = decryptCipher.doFinal(encryptedBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            data
        }
    }

    private fun initCipher(mode: Int): Cipher {
        return try {
            val factory: SecretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec: KeySpec = PBEKeySpec(SECRET_KEY.toCharArray(), SALT.toByteArray(), 65536, 64) // 64 бита для DES
            val tmp: SecretKey = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "DES") // Изменено на DES
            val iv = IvParameterSpec(INIT_VECTOR.toByteArray().copyOf(8)) // DES использует 8-байтовый IV
            val cipher: Cipher = Cipher.getInstance("DES/CTR/PKCS5Padding") // Изменено на DES
            cipher.init(mode, secretKey, iv)
            cipher
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to initialize cipher", e)
        }
    }

    fun decryptAllData() {
        try {
            val db = writableDatabase
            val cv = ContentValues()

            val cursor: Cursor = db.query(MY_TABLE, null, null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val rowId: Int = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                    val iRes = cursor.getColumnIndex(COLUMN_RESOURCE)
                    val iLogin = cursor.getColumnIndex(COLUMN_LOGIN)
                    val iPassword = cursor.getColumnIndex(COLUMN_PASSWORD)
                    val iNotes = cursor.getColumnIndex(COLUMN_NOTES)

                    // Проверка на корректность индексов столбцов
                    if (iRes != -1 && iLogin != -1 && iPassword != -1 && iNotes != -1) {
                        val decryptedResource: String = decryptData(cursor.getString(iRes))
                        val decryptedLogin: String = decryptData(cursor.getString(iLogin))
                        val decryptedPassword: String = decryptData(cursor.getString(iPassword))
                        val decryptedNotes: String = decryptData(cursor.getString(iNotes))

                        cv.put(COLUMN_RESOURCE, decryptedResource)
                        cv.put(COLUMN_LOGIN, decryptedLogin)
                        cv.put(COLUMN_PASSWORD, decryptedPassword)
                        cv.put(COLUMN_NOTES, decryptedNotes)

                        db.update(MY_TABLE, cv, BaseColumns._ID + "=?", arrayOf(rowId.toString()))
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun encryptAllData() {
        val db = writableDatabase
        val cv = ContentValues()

        val cursor: Cursor = db.query(MY_TABLE, null, null, null, null, null, null, null)
        if (cursor.moveToFirst()) {
            do {
                val rowId: Int = cursor.getInt(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                val iRes = cursor.getColumnIndex(COLUMN_RESOURCE)
                val iLogin = cursor.getColumnIndex(COLUMN_LOGIN)
                val iPassword = cursor.getColumnIndex(COLUMN_PASSWORD)
                val iNotes = cursor.getColumnIndex(COLUMN_NOTES)

                // Проверка на корректность индексов столбцов
                if (iRes != -1 && iLogin != -1 && iPassword != -1 && iNotes != -1) {
                    val encryptedResource: String = encryptData(cursor.getString(iRes))
                    val encryptedLogin: String = encryptData(cursor.getString(iLogin))
                    val encryptedPassword: String = encryptData(cursor.getString(iPassword))
                    val encryptedNotes: String = encryptData(cursor.getString(iNotes))

                    cv.put(COLUMN_RESOURCE, encryptedResource)
                    cv.put(COLUMN_LOGIN, encryptedLogin)
                    cv.put(COLUMN_PASSWORD, encryptedPassword)
                    cv.put(COLUMN_NOTES, encryptedNotes)

                    db.update(MY_TABLE, cv, BaseColumns._ID + "=?", arrayOf(rowId.toString()))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
    }
}