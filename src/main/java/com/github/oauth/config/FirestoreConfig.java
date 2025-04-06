package com.github.oauth.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class FirestoreConfig {

    private static final String CREDENTIALS_PATH = "src/main/resources/serviceAccountKey.json";

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) { // Prevent duplicate initialization
            FileInputStream serviceAccount = new FileInputStream(Paths.get(CREDENTIALS_PATH).toFile());

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) { // Ensure FirebaseApp is initialized first
        return FirestoreClient.getFirestore();
    }
}
