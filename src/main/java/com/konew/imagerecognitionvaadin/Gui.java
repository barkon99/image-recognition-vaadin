package com.konew.imagerecognitionvaadin;

import com.konew.imagerecognitionvaadin.model.ImageRecognition;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.InputStream;
import java.util.regex.Pattern;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import com.vaadin.flow.server.StreamResource;



@Route("upload-image")
public class Gui extends VerticalLayout
{

    @Autowired
    public Gui(ImageRestService imageRestService) {
        Icon icon = new Icon(VaadinIcon.CAMERA);
        Label labelInform = new Label("Welcome!");
        Label labelInform2 = new Label("Add your photo to face recognition app ");
        Label labelInform3 = new Label("You can do this using Url address or file with your local directory");

        TextField textFieldUrl = new TextField("Url address");
        Button buttonUrl = new Button("Send");

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setUploadButton(new Button("Add file"));
        upload.setReceiver(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        upload.setMaxFileSize(1048576);

        buttonUrl.addClickListener(buttonClickEvent -> {

            if (!Pattern.matches("http://.*|https://.*|data:image.*", textFieldUrl.getValue()))
            {
                new Notification("Bad address url!!!", 4000).open();
                textFieldUrl.clear();
            }
            else
            {
                removeAll();
                Button buttonAddNewPhoto = new Button("Add another photo");
                ImageRecognition imageDataFromUrl = imageRestService.getImageDataFromUrl(textFieldUrl.getValue());
                Label labelUploadUrl = new Label("Uploaded photo");
                Image image = new Image(textFieldUrl.getValue(), "Bad photo");
                add(buttonAddNewPhoto, labelUploadUrl, image);

                showResult(imageDataFromUrl);

                buttonAddNewPhoto.addClickListener(event -> {
                    removeAll();
                    add(icon,labelInform,labelInform2,labelInform3,textFieldUrl, buttonUrl, upload);
                });

            }
        });
            upload.addFileRejectedListener(fileRejectedEvent -> {
                new Notification("FIle is too big or in a bad format", 4000).open();
            });

            upload.addSucceededListener(succeededEvent -> {
                removeAll();
                try
                {
                    Button buttonAddNewPhoto = new Button("Add another photo");
                    Label labelUploadUrl = new Label("Uploaded photo");
                    byte[] imageAsBytes = IOUtils.toByteArray(buffer.getInputStream(succeededEvent.getFileName()));
                    Image image = convertToImage(imageAsBytes);
                    add(buttonAddNewPhoto, labelUploadUrl, image);
                    ImageRecognition imageDataFromFile = imageRestService.getImageDataFromFile(imageAsBytes);
                    showResult(imageDataFromFile);

                    buttonAddNewPhoto.addClickListener(buttonClickEvent -> {
                        removeAll();
                        add(icon,labelInform,labelInform2,labelInform3,textFieldUrl, buttonUrl, upload);
                    });

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });

        add(icon,labelInform,labelInform2,labelInform3,textFieldUrl, buttonUrl, upload);
    }


    private void showResult(ImageRecognition imageRecognition)
    {
        imageRecognition.getFaces().forEach(face -> {
            Label labelAge = new Label("Age: " + face.getAge());
            Label labelGender = new Label("Gender: " + face.getGender());
            add(labelAge, labelGender);
        });
        Label labelDescription = new Label("Description: " + imageRecognition.getDescription().getCaptions().get(0).getText());
        add(labelDescription);
        Label labelHashTags = new Label("HashTags to instagram :) ");
        add(labelHashTags);
        imageRecognition.getDescription().getTags()
                .forEach(tags -> {
                    Label labelTags = new Label(tags);
                    add(labelTags);
                });
    }

    private Image convertToImage(byte[] imageData) {
        StreamResource streamResource = new StreamResource("image",
                () -> new ByteArrayInputStream(imageData));
        return new Image(streamResource, "photo");
    }
}
