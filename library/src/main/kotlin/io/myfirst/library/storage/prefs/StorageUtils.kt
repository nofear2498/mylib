package io.myfirst.library.storage.prefs

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class StorageUtils {

    class Preferences(context: Context, name: String, secureKey: String, boolean: Boolean){
        private val sharedPrefs: SecurePreferences =
            SecurePreferences(
                context,
                name,
                secureKey,
                boolean
            )


        fun setOnConversionDataSuccess(name: String, flag: String) {
            sharedPrefs.put(name, flag)
        }

        fun getOnConversionDataSuccess(name: String): String {
            return sharedPrefs.getString(name).toString()
        }

        fun setOnDeepLinkDataSuccess(name: String, flag: String){
            sharedPrefs.put(name, flag)
        }

        fun getOnDeepLinkDataSuccess(name: String): String{
            return sharedPrefs.getString(name).toString()
        }


    }

    class SecurePreferences(
        context: Context,
        preferenceName: String?,
        secureKey: String,
        encryptKeys: Boolean
    ) {
        class SecurePreferencesException(e: Throwable?) :
            RuntimeException(e)

        /**
         * настройка шифрования
         */
        //шиврофка ключей
        private var encryptKeys = false
        private var writer: Cipher? = null
        private var reader: Cipher? = null
        private var keyWriter: Cipher? = null
        private var preferences: SharedPreferences? = null

        //ексепшены
        @Throws(
            UnsupportedEncodingException::class,
            NoSuchAlgorithmException::class,
            InvalidKeyException::class,
            InvalidAlgorithmParameterException::class
        )
        //инициализируем протоколы шифрования
        protected fun initCiphers(secureKey: String) {
            val ivSpec = iv
            val secretKey = getSecretKey(secureKey)
            writer!!.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            reader!!.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            keyWriter!!.init(Cipher.ENCRYPT_MODE, secretKey)
        }

        //трансформация ключа в байткод
        protected val iv: IvParameterSpec
            protected get() {
                val iv = ByteArray(writer!!.blockSize)
                System.arraycopy(
                    "fldsjfodasjifudslfjdsaofshaufihadsf".toByteArray(),
                    0,
                    iv,
                    0,
                    writer!!.blockSize
                )
                return IvParameterSpec(iv)
            }

        @Throws(
            UnsupportedEncodingException::class,
            NoSuchAlgorithmException::class
        )
        //получаем секретный ключ
        protected fun getSecretKey(key: String): SecretKeySpec {
            val keyBytes = createKeyBytes(key)
            return SecretKeySpec(
                keyBytes,
                TRANSFORMATION
            )
        }

        @Throws(
            UnsupportedEncodingException::class,
            NoSuchAlgorithmException::class
        )
        //хеш в зашифрованный байт код
        protected fun createKeyBytes(key: String): ByteArray {
            val md =
                MessageDigest.getInstance(SECRET_KEY_HASH_TRANSFORMATION)
            md.reset()
            return md.digest(key.toByteArray(charset(CHARSET)))
        }

        //сеттим преф
        fun put(key: String, value: String?) {
            if (value == null) {
                preferences!!.edit().remove(toKey(key)).commit()
            } else {
                putValue(toKey(key), value)
            }
        }


        @Throws(SecurePreferencesException::class)
        fun getString(key: String): String? {
            if (preferences!!.contains(toKey(key))) {
                val securedEncodedValue = preferences!!.getString(toKey(key), "")
                return decrypt(securedEncodedValue)
            }
            return null
        }


        private fun toKey(key: String): String {
            return if (encryptKeys) encrypt(key, keyWriter) else key
        }

        @Throws(SecurePreferencesException::class)
        private fun putValue(key: String, value: String) {
            val secureValueEncoded = encrypt(value, writer)
            preferences!!.edit().putString(key, secureValueEncoded).commit()
        }

        //шифруем
        @Throws(SecurePreferencesException::class)
        protected fun encrypt(value: String, writer: Cipher?): String {
            val secureValue: ByteArray
            secureValue = try {
                convert(
                    writer,
                    value.toByteArray(charset(CHARSET))
                )
            } catch (e: UnsupportedEncodingException) {
                throw SecurePreferencesException(
                    e
                )
            }
            return Base64.encodeToString(secureValue, Base64.NO_WRAP)
        }

        //расшифровка
        protected fun decrypt(securedEncodedValue: String?): String {
            val securedValue =
                Base64.decode(securedEncodedValue, Base64.NO_WRAP)
            val value =
                convert(
                    reader,
                    securedValue
                )
            return try {
                String(value, Charset.forName(CHARSET))
            } catch (e: UnsupportedEncodingException) {
                throw SecurePreferencesException(
                    e
                )
            }
        }

        //объекты трансформации и константы
        companion object {
            private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
            private const val KEY_TRANSFORMATION = "AES/ECB/PKCS5Padding"
            private const val SECRET_KEY_HASH_TRANSFORMATION = "SHA-256"
            private const val CHARSET = "UTF-8"

            @Throws(SecurePreferencesException::class)
            private fun convert(cipher: Cipher?, bs: ByteArray): ByteArray {
                return try {
                    cipher!!.doFinal(bs)
                } catch (e: Exception) {
                    throw SecurePreferencesException(
                        e
                    )
                }
            }
        }

        //инициализируем всё необходимое
        init {
            try {
                writer =
                    Cipher.getInstance(TRANSFORMATION)
                reader =
                    Cipher.getInstance(TRANSFORMATION)
                keyWriter =
                    Cipher.getInstance(KEY_TRANSFORMATION)
                initCiphers(secureKey)
                preferences =
                    context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
                this.encryptKeys = encryptKeys
            } catch (e: GeneralSecurityException) {
                throw SecurePreferencesException(
                    e
                )
            } catch (e: UnsupportedEncodingException) {
                throw SecurePreferencesException(
                    e
                )
            }
        }
    }

}