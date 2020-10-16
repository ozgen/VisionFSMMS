package com.shoppingmall.smms.Helpers;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.shoppingmall.smms.RunnableArg;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class FingerPrintHelper extends BiometricHelperBase {

    private static final String KEY_NAME = "yourKey";
    private Cipher cipher;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private TextView textView;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public FingerPrintHelper(AppCompatActivity _context) {
        super(_context);
        keyguardManager = (KeyguardManager) this.context.getSystemService(Context.KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager) this.context.getSystemService(Context.FINGERPRINT_SERVICE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public HWSTATUS getBiometricHardwareStatus() {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.USE_FINGERPRINT) == PackageManager.PERMISSION_GRANTED) {
            if (fingerprintManager.isHardwareDetected()) {
                if (keyguardManager.isKeyguardSecure()) {
                    return fingerprintManager.hasEnrolledFingerprints() ? HWSTATUS.ENROLLED : HWSTATUS.NONE_ENROLLED;
                } else {
                    return HWSTATUS.UNSETLOCKSCREEN;
                }
            } else {
                return HWSTATUS.UNAVAILABLE;
            }
        } else {
            return HWSTATUS.PERMISSIONDENIED;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public Boolean checkBiometricHardware() {
        switch (this.getBiometricHardwareStatus()) {
            case ENROLLED:
                return true;
            case NONE_ENROLLED:
                return null;
            default:
                return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void authUser(RunnableArg<Boolean> runnableArg) {
        try {
            generateKey();
        } catch (FingerprintException e) {
            e.printStackTrace();
        }

        if (initCipher()) {
            cryptoObject = new FingerprintManager.CryptoObject(cipher);

            FingerprintHandler helper = new FingerprintHandler(this.context, runnableArg);
            helper.startAuth(fingerprintManager, cryptoObject);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateKey() throws FingerprintException {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            //Generate the key//
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Initialize an empty KeyStore//
            keyStore.load(null);

            //Initialize the KeyGenerator//
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            //Generate the key//
            keyGenerator.generateKey();

        } catch (KeyStoreException |
                NoSuchAlgorithmException |
                NoSuchProviderException |
                InvalidAlgorithmParameterException |
                CertificateException |
                IOException exc) {
            exc.printStackTrace();
            throw new FingerprintException(exc);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean initCipher() {
        try {
            //Obtain a cipher instance and configure it with the properties required for fingerprint authentication//
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/" +
                            KeyProperties.BLOCK_MODE_CBC + "/" +
                            KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //Return true if the cipher has been initialized successfully//
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {

            //Return false if cipher initialization failed//
            return false;
        } catch (KeyStoreException | CertificateException |
                UnrecoverableKeyException | IOException |
                NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    private enum HWSTATUS {
        PERMISSIONDENIED,
        UNAVAILABLE,
        NONE_ENROLLED,
        UNSETLOCKSCREEN,
        ENROLLED
    }

    private class FingerprintException extends Exception {
        public FingerprintException(Exception e) {
            super(e);
        }
    }
}

@TargetApi(Build.VERSION_CODES.M)
class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    // You should use the CancellationSignal method whenever your app can no longer process user input, for example when your app goes
    // into the background. If you don’t use this method, then other apps will be unable to access the touch sensor, including the lockscreen!//

    private CancellationSignal cancellationSignal;
    private Context context;
    private RunnableArg<Boolean> runnableArg;
    private Integer wrongEntryCount = 0;

    public FingerprintHandler(Context mContext, RunnableArg<Boolean> _runnableArg) {
        context = mContext;
        runnableArg = runnableArg;
        wrongEntryCount = 0;
    }

    //Implement the startAuth method, which is responsible for starting the fingerprint authentication process//
    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    //onAuthenticationError is called when a fatal error has occurred. It provides the error code and error message as its parameters//
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        runnableArg.run(false);
        cancellationSignal.cancel();
    }

    @Override
    //onAuthenticationFailed is called when the fingerprint doesn’t match with any of the fingerprints registered on the device//
    public void onAuthenticationFailed() {
        if (++wrongEntryCount == 3) {
            onAuthenticationError(0, "");
        }
    }

    @Override
    //onAuthenticationHelp is called when a non-fatal error has occurred. This method provides additional information about the error,
    //so to provide the user with as much feedback as possible I’m incorporating this information into my toast//
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Toast.makeText(context, "İpucu: \n" + helpString, Toast.LENGTH_LONG).show();
    }

    @Override
    //onAuthenticationSucceeded is called when a fingerprint has been successfully matched to one of the fingerprints stored on the user’s device//
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        runnableArg.run(true);
    }

}
