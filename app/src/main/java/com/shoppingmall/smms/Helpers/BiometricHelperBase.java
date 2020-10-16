package com.shoppingmall.smms.Helpers;

import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.shoppingmall.smms.R;
import com.shoppingmall.smms.RunnableArg;

public abstract class BiometricHelperBase {
    protected AppCompatActivity context;

    public BiometricHelperBase(AppCompatActivity _context) {
        this.context = _context;
    }

    public static BiometricHelperBase getInstance(AppCompatActivity _context) {
        if (Build.VERSION.SDK_INT >= 29) {
            return new BiometricHelper(_context);
        } else if (Build.VERSION.SDK_INT >= 23) {
            return new FingerPrintHelper(_context);
        } else {
            return new BiometricHelperBase(_context) {
                @Override
                public Boolean checkBiometricHardware() {
                    return false;
                }

                @Override
                public void authUser(RunnableArg<Boolean> runnableArg) {
                    runnableArg.run(false);
                }
            };
        }
    }

    public abstract Boolean checkBiometricHardware();

    public abstract void authUser(final RunnableArg<Boolean> runnableArg);

    public void openFingerprintSetting() {
        this.context.startActivity(new Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS));
        Toast.makeText(
                this.context.getApplicationContext(),
                this.context.getString(R.string.error_msg_biometric_not_setup),
                Toast.LENGTH_LONG
        ).show();
    }
}
