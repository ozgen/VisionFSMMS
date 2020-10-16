package com.shoppingmall.smms.Helpers;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.shoppingmall.smms.R;
import com.shoppingmall.smms.RunnableArg;

import java.util.concurrent.Executor;

public class BiometricHelper extends BiometricHelperBase {
    BiometricPrompt biometricPrompt;
    private Executor executor;
    private Integer wrongEntryCount = 0;

    public BiometricHelper(AppCompatActivity _context) {
        super(_context);
        this.executor = ContextCompat.getMainExecutor(this.context);
    }

    public int getBiometricHardwareStatus() {
        BiometricManager biometricManager = BiometricManager.from(this.context);
        return biometricManager.canAuthenticate();
    }

    public Boolean checkBiometricHardware() {
        switch (this.getBiometricHardwareStatus()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return null;
            default:
                return false;
        }
    }

    public void openFingerprintSetting() {
        this.context.startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
        Toast.makeText(
                this.context,
                this.context.getString(R.string.error_msg_biometric_not_setup),
                Toast.LENGTH_LONG
        ).show();
    }

    public void authUser(final RunnableArg<Boolean> runnableArg) {
        wrongEntryCount = 0;
        final AppCompatActivity _context = this.context;

        BiometricPrompt.PromptInfo.Builder builder = new BiometricPrompt.PromptInfo.Builder();
        BiometricPrompt.PromptInfo promptInfo = builder.setTitle(this.context.getString(R.string.auth_title))
                .setSubtitle(this.context.getString(R.string.auth_subtitle))
                .setDescription(this.context.getString(R.string.auth_description))
                .setDeviceCredentialAllowed(true)
                .setConfirmationRequired(true)
                .build();

        biometricPrompt = new BiometricPrompt(_context, this.executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(
                        _context.getApplicationContext(),
                        _context.getString(R.string.error_msg_auth_error, errString),
                        Toast.LENGTH_SHORT
                ).show();
                runnableArg.run(false);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                runnableArg.run(true);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                if (++wrongEntryCount == 3) {
                    Toast.makeText(
                            _context.getApplicationContext(),
                            _context.getString(R.string.error_msg_auth_failed),
                            Toast.LENGTH_SHORT
                    ).show();
                    cancelAuth();
                }
            }
        });

        biometricPrompt.authenticate(promptInfo);
    }

    private void cancelAuth() {
        if (biometricPrompt != null) {
            biometricPrompt.cancelAuthentication();
        }
    }
}
