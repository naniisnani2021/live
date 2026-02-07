package com.example.live.launcher;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.live.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Set;

public final class SetupActivity extends AppCompatActivity {

    private AppsAdapter adapter;
    private RecyclerView rv;
    private TextView passwordStatus;
    private Button btnChangePassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        rv = findViewById(R.id.setup_pinned_list);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        rv.setLayoutManager(lm);

        adapter = new AppsAdapter(null, true, true);
        rv.setAdapter(adapter);

        List<AppInfo> apps = AppRepository.getLaunchableApps(this);
        adapter.submit(apps);
        adapter.setCheckedPackages(PinnedAppsStore.getPinned(this));

        // Password Logic
        passwordStatus = findViewById(R.id.password_status);
        btnChangePassword = findViewById(R.id.btn_change_password);
        
        updatePasswordUI();

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog();
            }
        });

        findViewById(R.id.setup_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<String> pinned = adapter.getCheckedPackages();
                if (pinned.isEmpty()) {
                    Toast.makeText(SetupActivity.this, R.string.pinned_empty, Toast.LENGTH_SHORT).show();
                    return;
                }
                PinnedAppsStore.setPinned(SetupActivity.this, pinned);

                startActivity(new Intent(SetupActivity.this, LauncherActivity.class));
                finish();
            }
        });

        findViewById(R.id.setup_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If password exists, go to launcher, else just finish (which might exit app if no history)
                if (PasswordManager.isPasswordSet(SetupActivity.this)) {
                    startActivity(new Intent(SetupActivity.this, LauncherActivity.class));
                }
                finish();
            }
        });

        // Keep dashboard data reasonably fresh.
        DashboardDataRefresher.refreshInBackground(this);
    }

    private void updatePasswordUI() {
        boolean isSet = PasswordManager.isPasswordSet(this);
        if (isSet) {
            passwordStatus.setText("Enabled");
            passwordStatus.setTextColor(getResources().getColor(R.color.black));
            btnChangePassword.setText("Edit");
        } else {
            passwordStatus.setText("Not configured");
            passwordStatus.setTextColor(0xFF757575); // Grey
            btnChangePassword.setText("Setup");
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(view);

        final TextInputEditText pwd = view.findViewById(R.id.dialog_password);
        final TextInputEditText pwd2 = view.findViewById(R.id.dialog_password_confirm);
        final TextView error = view.findViewById(R.id.dialog_error);

        builder.setPositiveButton("Save", null); // Set null to override onClick later
        builder.setNegativeButton("Cancel", null);

        final AlertDialog dialog = builder.create();
        dialog.show();

        // Override onClick to prevent auto-close on error
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String p1 = pwd.getText() == null ? "" : pwd.getText().toString();
                String p2 = pwd2.getText() == null ? "" : pwd2.getText().toString();

                if (!PasswordManager.meetsPolicy(p1)) {
                    error.setText(getString(R.string.password_policy));
                    error.setVisibility(View.VISIBLE);
                    return;
                }
                if (!p1.equals(p2)) {
                    error.setText(getString(R.string.password_mismatch));
                    error.setVisibility(View.VISIBLE);
                    return;
                }
                try {
                    PasswordManager.setPassword(SetupActivity.this, p1);
                    updatePasswordUI();
                    dialog.dismiss();
                    Toast.makeText(SetupActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    error.setText(getString(R.string.password_set_failed));
                    error.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
