package com.konew.imagerecognitionvaadin;

import com.konew.imagerecognitionvaadin.model.ImageRecognition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageRestService
{
    @Value("${rapidapi.url}")
    String url;
    @Value("${rapidapi.key.name}")
    String apiKeyName;
    @Value("${rapidapi.key.value}")
    String apiKeyValue;
    @Value("${rapidapi.host.name}")
    String hostName;
    @Value("${rapidapi.host.value}")
    String hostValue;


    public ImageRecognition getImageDataFromUrl(String imageUrl)
    {
        HttpHeaders httpHeaders = addHeaders("application/json");

        Map<String, String> map = new HashMap<>();
        map.put("url", imageUrl);

        HttpEntity httpEntity = new HttpEntity(map,httpHeaders);

        ImageRecognition imageDescription = connectToApi(httpEntity);

        return imageDescription;
    }

    public ImageRecognition getImageDataFromFile(byte[] imageAsBytes) throws IOException {

        HttpHeaders httpHeaders = addHeaders("multipart/form-data");

        MultiValueMap<String, byte[]> map = new LinkedMultiValueMap<>();
        map.add("image", imageAsBytes);
        HttpEntity httpEntity = new HttpEntity(map,httpHeaders);

        ImageRecognition imageDescription = connectToApi(httpEntity);

        return imageDescription;

    }

    private ImageRecognition connectToApi(HttpEntity httpEntity)
    {
        RestTemplate restTemplate = new RestTemplate();
        ImageRecognition imageDescription = restTemplate.exchange(url,
                HttpMethod.POST,
                httpEntity,
                ImageRecognition.class).getBody();

        List<String> mappedTags = imageDescription.getDescription().getTags().stream().map(x -> "#" + x).collect(Collectors.toList());
        imageDescription.getDescription().setTags(mappedTags);

        return  imageDescription;

    }

    private HttpHeaders addHeaders(String contentType)
    {
        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.add(hostName, hostValue);
        httpHeaders.add(apiKeyName, apiKeyValue);
        httpHeaders.add("content-type", contentType);

        return httpHeaders;
    }
}
