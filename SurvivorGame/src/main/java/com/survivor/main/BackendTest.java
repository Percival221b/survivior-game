package com.survivor.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class BackendTest {

    public static String sendMessage(String message) {
        try {
            URL url = new URL("http://127.0.0.1:8001/chat");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // 构造 JSON 请求体
            String jsonInputString = "{\"message\": \"" + message.replace("\"", "\\\"") + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 读取返回内容
            String responseText;
            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8)) {
                responseText = scanner.useDelimiter("\\A").next();
            }

            // 解析 JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(responseText);
            String action = jsonNode.get("answer").asText();
            String rawOutput = jsonNode.get("raw_output").asText();

            System.out.println("Action: " + action);
            System.out.println("Raw output: " + rawOutput);

            return action;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        // 测试
        String action = sendMessage("Answer only one word. If you are in my game. The hero's health is "+100+". The number of monster is "+5+". Who will you attack, hero or monster?");
        System.out.println("Extracted action: " + action);
    }
}//. the hero health is " + 10 + ", number of monster is " + 100 +" .
