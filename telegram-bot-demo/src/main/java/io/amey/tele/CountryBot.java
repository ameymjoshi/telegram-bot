package io.amey.tele;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CountryBot extends TelegramLongPollingBot {

    static String API_KEY = System.getenv("TELEGRAM_BOT_API_KEY");
    static String API_URL_PRE = "https://restcountries.eu/rest/v2/name/";
    static String API_URL_POST = "?fullText=true";
    
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String countryName = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            System.out.println("Sending "+countryName +" to REST API");
            String countryDetails="No data";
            try {
                countryDetails = fetchCountryDetails(countryName);
                countryDetails = printPrettyJSON(countryDetails);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            SendMessage message = new SendMessage(); 
            message.setChatId(String.valueOf(chatId));
            message.setText(countryDetails);
            try {
                execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private String fetchCountryDetails(String countryName) throws IOException, InterruptedException {
        String API_URL = ""+CountryBot.API_URL_PRE+countryName+CountryBot.API_URL_POST;
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpReq = HttpRequest.newBuilder()
                                .uri(URI.create(API_URL))
                                .build();
        java.net.http.HttpResponse<String> httpRes = httpClient.send(httpReq, java.net.http.HttpResponse.BodyHandlers.ofString());
        return httpRes.body().toString();
    }

    public String printPrettyJSON(String data){
        String result = "No data";
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = JsonParser.parseString(data);
        result = gson.toJson(je);
        System.out.println(result);
        return result;        
    }

    @Override
    public String getBotUsername() {
        return "Country-Bot";
    }

    @Override
    public String getBotToken() {
        return CountryBot.API_KEY;
    }
}
