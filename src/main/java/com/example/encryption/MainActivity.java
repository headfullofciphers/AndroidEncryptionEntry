package com.example.encryption;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import android.util.Base64;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final String ALGORITHM = "AES";
    private static final String CIPHER = "AES/ECB/PKCS5Padding";
    private static final String SHA2 = "SHA-256";

    EditText etPlain, etPass, etEnc;
    TextView tvKey, tvEncHex, tvOp;
    Button btEnc, btDec;
    String output;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etPlain = (EditText) findViewById(R.id.etPlain);
        etPass = (EditText) findViewById(R.id.etPass);
        etEnc = (EditText) findViewById(R.id.etEnc);

        btEnc = (Button) findViewById(R.id.btEnc);
        btDec = (Button) findViewById(R.id.btDec);

        tvKey = (TextView) findViewById(R.id.tvKey);
        tvEncHex = (TextView) findViewById(R.id.tvEncHex);
        tvOp = (TextView) findViewById(R.id.tvOp);

        btEnc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    tvEncHex.setBackgroundColor(R.color.colorPrimary);
                    String plain = etPlain.getText().toString();

                    String enc_pass = etPass.getText().toString();
                    SecretKeySpec encKey = generateKey(enc_pass);

                    output = encrypt(plain, encKey);

                    etEnc.setText(output);
                    tvOp.setText("Last operation: Encryption");
                    etPlain.getText().clear();
                    etPass.getText().clear();
                } catch (Exception e) {
                    Log.d("Encryption Error", e.getMessage());
                    Toast.makeText(getApplicationContext(), "Encryption failed.", Toast.LENGTH_LONG);
                    tvEncHex.setText("Encryption failed - result can be unexpected.");
                    tvEncHex.setBackgroundColor(Color.RED);
                }
            }
        });

        btDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    tvEncHex.setBackgroundColor(R.color.colorPrimary);
                    String enc_text = etEnc.getText().toString();
                    String dec_pass = etPass.getText().toString();
                    SecretKeySpec decKey = generateKey(dec_pass);

                    output = decrypt(enc_text, decKey);
                    etPlain.setText(output);
                    etEnc.getText().clear();
                    etPass.getText().clear();
                    tvOp.setText("Last operation: Decryption");
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Decryption failed.", Toast.LENGTH_LONG);
                    Log.d("Decryption Error", e.getMessage());
                    tvEncHex.setText("Decryption failed - result can be unreadable. Try different password");
                    tvEncHex.setBackgroundColor(Color.RED);
                }

                etPlain.setText(output);
            }
        });

    }

    private String encrypt(String value, SecretKeySpec key) throws Exception {
        tvKey.setText("Enc Key= " + bytesToHex(key.getEncoded(), true));
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(value.getBytes("Latin1"));
        tvEncHex.setText("B64 Hex= " + Base64.encodeToString(encrypted, android.util.Base64.DEFAULT));
        String ciphertext = bytesToHex(encrypted, false);
        return ciphertext;
    }

    private String decrypt(String encrypted, SecretKeySpec key) throws Exception {
        tvKey.setText("Dec Key= " + bytesToHex(key.getEncoded(), true));
        tvEncHex.setText("");
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] enc_bytes = hexStringToByteArray(encrypted);
        byte[] original = cipher.doFinal(enc_bytes);
        return new String(original);
    }

    private SecretKeySpec generateKey(String pass) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        final MessageDigest digest = MessageDigest.getInstance(SHA2);
        byte[] bytes = pass.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);
        return secretKeySpec;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static String bytesToHex(byte[] bytes, boolean spaces) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            if (spaces) {
                sb.append(String.format("%02X ", b));
            } else {
                sb.append(String.format("%02X", b));
            }
        }
        return sb.toString();
    }

}