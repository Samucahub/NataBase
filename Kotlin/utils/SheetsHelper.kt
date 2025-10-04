package com.example.natabase.utils

import android.content.Context
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.exemplo.natabase.R

object SheetsHelper {
    fun getSheetsService(context: Context): Sheets {
        val inputStream = context.resources.openRawResource(R.raw.credentials)

        val credentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(
                listOf(
                    "https://www.googleapis.com/auth/spreadsheets",
                    "https://www.googleapis.com/auth/drive"
                )
            )

        val transport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()

        return Sheets.Builder(transport, jsonFactory, HttpCredentialsAdapter(credentials))
            .setApplicationName("CozinhaApp")
            .build()
    }
}
