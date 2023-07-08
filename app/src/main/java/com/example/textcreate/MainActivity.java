package com.example.textcreate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE =42 ;
    private EditText editText;
    private EditText filename;

    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editTextText);
        filename=findViewById(R.id.fileNameEditText);
        Button createButton = findViewById(R.id.createButton);
        Button saveButton = findViewById(R.id.saveButton);
        Button openButton = findViewById(R.id.openButton);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFile();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToFile();
            }
        });

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile();
            }
        });
    }

    private void createFile() {
        String fileName = filename.getText().toString();
        if(fileName.isEmpty()){
            Toast.makeText(MainActivity.this, "Enter a file name",Toast.LENGTH_SHORT).show();
        }
        File documentsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        file = new File(documentsFolder, fileName);
        try {
            if (file.createNewFile()) {
                Toast.makeText(MainActivity.this, "File created successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "File already exists", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Failed to create file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void saveToFile() {
        if (file == null) {
            Toast.makeText(MainActivity.this, "First create a file", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            FileWriter writer = new FileWriter(file);
            writer.write(editText.getText().toString());
            writer.close();
            Toast.makeText(MainActivity.this, "Content saved successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Failed to save content", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Set the MIME type to all files

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                openSelectedFile(uri);
            }
        }
    }

    private void openSelectedFile(Uri uri) {
        try {
            String fileName = getFileName(uri);
            filename.setText(fileName);

            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append('\n');
                }
                reader.close();

                editText.setText(stringBuilder.toString());
                Toast.makeText(MainActivity.this, "File opened successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Failed to open file", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Failed to open file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    @SuppressLint("Range")
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}
