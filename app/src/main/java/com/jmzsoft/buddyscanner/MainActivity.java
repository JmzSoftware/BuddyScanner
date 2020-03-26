/*
 * Buddy Scanner
 *
 * Authors: James Taylor <james.taylor@jmzsoft.com>
 *
 * Copyright (C) 2020 James Taylor
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.jmzsoft.buddyscanner;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

import static com.jmzsoft.buddyscanner.Internet.disableSslChecks;
import static com.jmzsoft.buddyscanner.Internet.queryApi;

public class MainActivity extends AppCompatActivity {

    private static String URL_KEY = "url";
    private static String IGNORE_SSL_KEY = "ignore_ssl";
    private static String TORCH_KEY = "torch";

    private Context mContext;
    private SharedPrefsHelper sharedPrefsHelper;
    private SharedPrefsHelper sharedPrefsSettings;
    private ArrayList<Item> items;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        sharedPrefsHelper = new SharedPrefsHelper(this, "upc");
        sharedPrefsSettings = new SharedPrefsHelper(this, "settings");

        if (sharedPrefsHelper.getCount() > 0) {
            loadData();
        }

        FloatingActionButton myFab = findViewById(R.id.scanUpc);
        myFab.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setPrompt(getString(R.string.scan_text));
            integrator.setBeepEnabled(false);
            integrator.setBarcodeImageEnabled(true);
            if (sharedPrefsSettings.getBoolean(TORCH_KEY)) {
                integrator.setTorchEnabled(true);
            }
            integrator.initiateScan();
        });
    }

    private void loadData() {
        TextView textView = findViewById(R.id.textView);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        if (items != null) {
            items.clear();
        }
        items = sharedPrefsHelper.getAll();
        RvAdapter mAdapter = new RvAdapter(items);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        textView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    public void settingsDialog() {
        View view = getLayoutInflater().inflate(R.layout.alert_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        builder.setView(view);

        EditText inputTemp = view.findViewById(R.id.editText);
        CheckBox checkBox = view.findViewById(R.id.checkbox);

        if (sharedPrefsSettings.contains(URL_KEY)) {
            inputTemp.setText(sharedPrefsSettings.getString(URL_KEY));
            checkBox.setChecked(sharedPrefsSettings.getBoolean(IGNORE_SSL_KEY));
        }

        builder.setPositiveButton("Save", null);

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (isValidUrl(inputTemp.getText().toString())) {
                sharedPrefsSettings.putString(URL_KEY, inputTemp.getText().toString());
                sharedPrefsSettings.putBoolean(IGNORE_SSL_KEY, checkBox.isChecked());
                dialog.dismiss();
            } else {
                TextInputLayout url = view.findViewById(R.id.url);
                url.setError("Enter valid URL");
            }
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
    }

    private boolean isValidUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            sharedPrefsSettings.Add(scanResult.getContents());
            String url = sharedPrefsSettings.getString(URL_KEY);
            if (!url.equals("")) {
                new JsonTask().execute(url, scanResult.getContents());
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            settingsDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = item.getGroupId();
        if (item.getItemId() == 1) {
            sharedPrefsHelper.Delete(position);
            loadData();
        }
        return super.onContextItemSelected(item);
    }

    @SuppressLint("StaticFieldLeak")
    private class JsonTask extends AsyncTask<String, String, Boolean> {

        protected void onPreExecute() {
            super.onPreExecute();
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setMessage("Please wait");
                progressDialog.setCancelable(false);
                progressDialog.show();
        }

        protected Boolean doInBackground(String... params) {
            if (sharedPrefsSettings.getBoolean(IGNORE_SSL_KEY)) {
                disableSslChecks(mContext);
            }

            return queryApi(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Toast.makeText(mContext, result ? "Successfully sent" : "Something went wrong", Toast.LENGTH_LONG).show();
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
}