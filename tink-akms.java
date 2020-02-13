package us.syh.april;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Base64;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import com.google.crypto.tink.integration.android.AndroidKeysetManager;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MainActivity extends AppCompatActivity {
    
    EditText editText_stext, editText_ctext;
    Button button_e, button_d;
    private static final String PREF_FILE_NAME = "hello_world_pref";
    private static final String TINK_KEYSET_NAME = "hello_world_keyset";
    private static final String MASTER_KEY_URI = "android-keystore://hello_world_master_key";
    public Aead aead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //aead crypto init
        try {
            AeadConfig.register();
            aead = getOrGenerateNewKeysetHandle().getPrimitive(Aead.class);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        initView();
        initOnClick();
    }

    private void initView() {
        editText_stext = findViewById(R.id.editText_stext);
        editText_ctext = findViewById(R.id.editText_ctext);
        button_e = findViewById(R.id.button_e);
        button_d = findViewById(R.id.button_d);
    }

    private void initOnClick() {
        button_e.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 3. Use the primitive to encrypt a plaintext,
                    byte[] ciphertext = aead.encrypt(editText_stext.getText().toString().getBytes(), "11111".getBytes());
                    editText_ctext.setText(base64Encode(ciphertext));
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        button_d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 3. Use the primitive to encrypt a plaintext,
                    byte[] decrypted = aead.decrypt(base64Decode(editText_ctext.getText().toString()), "11111".getBytes());
                    editText_stext.setText(new String(decrypted));
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private KeysetHandle getOrGenerateNewKeysetHandle() throws IOException, GeneralSecurityException {
        return new AndroidKeysetManager.Builder()
                .withSharedPref(getApplicationContext(), TINK_KEYSET_NAME, PREF_FILE_NAME)
                .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()
                .getKeysetHandle();
    }

    private static String base64Encode(final byte[] input) {
        return Base64.encodeToString(input, Base64.DEFAULT);
    }

    private static byte[] base64Decode(String input) {
        return Base64.decode(input, Base64.DEFAULT);
    }
}
