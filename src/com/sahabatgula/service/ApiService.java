package com.sahabatgula.service;

import com.google.gson.Gson;
import com.sahabatgula.model.ApiResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Random;

public class ApiService {

    private static final String API_URL = "https://backend-python-sahabat-gula-166777420148.asia-southeast2.run.app/model/predict";
    private final HttpClient client;
    private final Gson gson;

    public ApiService() {
        this.client = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    /**
     * Mengirim gambar ke API dan mengembalikan hasil prediksi.
     * Implementasi stream ada pada Files.readAllBytes() dan BodyPublishers.ofByteArray().
     * Implementasi URL-Connection ada pada HttpClient dan HttpRequest.
     */
    public ApiResponse predictImage(File imageFile) throws IOException, InterruptedException {
        // Membuat boundary untuk multipart request
        String boundary = "Boundary-" + System.currentTimeMillis();

        // Membangun request body untuk multipart/form-data
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "multipart/form-data;boundary=" + boundary)
                .POST(ofMimeMultipartData(imageFile, boundary))
                .build();

        // Mengirim request dan menerima response sebagai String
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Cek jika status code bukan 200 (OK)
        if (response.statusCode() != 200) {
            throw new IOException("Unexpected response code: " + response.statusCode() + " Body: " + response.body());
        }

        // Parsing JSON string ke objek ApiResponse menggunakan Gson
        return gson.fromJson(response.body(), ApiResponse.class);
    }

    /**
     * Helper untuk membuat body request multipart/form-data.
     * Di sini stream dari file (imageFile) dibaca ke dalam byte array.
     */
    private HttpRequest.BodyPublisher ofMimeMultipartData(File imageFile, String boundary) throws IOException {
        var byteArrays = new java.util.ArrayList<byte[]>();
        String separator = "--" + boundary + "\r\n";
        String footer = "--" + boundary + "--\r\n";

        byteArrays.add(separator.getBytes());
        String fileHeader = "Content-Disposition: form-data; name=\"image\"; filename=\"" + imageFile.getName() + "\"\r\n" +
                "Content-Type: image/jpeg\r\n\r\n";
        byteArrays.add(fileHeader.getBytes());
        byteArrays.add(Files.readAllBytes(imageFile.toPath()));
        byteArrays.add("\r\n".getBytes());

        byteArrays.add(footer.getBytes());

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}