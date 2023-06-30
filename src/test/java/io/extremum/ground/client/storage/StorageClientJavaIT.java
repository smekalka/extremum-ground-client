package io.extremum.ground.client.storage;

import kotlin.text.Charsets;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import static io.extremum.ground.client.EnabledConfigKt.ENABLED_STORAGE_EXPRESSION;
import static io.extremum.ground.client.storage.StorageProperties.PATH;
import static io.extremum.ground.client.storage.StorageProperties.TOKEN;
import static io.extremum.ground.client.storage.StorageProperties.URI;
import static io.extremum.ground.client.storage.StorageProperties.X_APP_ID;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class StorageClientJavaIT {

    StorageClient client;

    static String KEY = "documents/123";
    static byte[] OBJ = "Мы — буквы, с нами текст".getBytes(Charsets.UTF_8);

    public StorageClientJavaIT() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, TOKEN);
        WebClient webClient = WebClient.create(URI + PATH);
        client = new StorageClient(
                URI,
                PATH,
                X_APP_ID,
                headers,
                webClient
        );
    }

    @AfterEach
    void after() throws ExecutionException, InterruptedException {
        client.deleteObjectF(KEY).get();
    }

    @Test
    @EnabledIf(expression = ENABLED_STORAGE_EXPRESSION)
    void postObject() throws ExecutionException, InterruptedException {
        createObject();
    }

    @Test
    @EnabledIf(expression = ENABLED_STORAGE_EXPRESSION)
    void getObject() throws ExecutionException, InterruptedException {
        createObject();
        byte[] result = client.getObjectF(KEY).get();
        Assertions.assertThat(result).isEqualTo(OBJ);
    }

    void createObject() throws ExecutionException, InterruptedException {
        client.postObjectF(KEY, OBJ).get();
    }
}
