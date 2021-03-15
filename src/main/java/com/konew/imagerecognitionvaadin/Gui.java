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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.regex.Pattern;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import com.vaadin.flow.server.StreamResource;



@Route("upload-image")
public class Gui extends VerticalLayout
{
    private Logger logger = LoggerFactory.getLogger(Gui.class);

    @Autowired
    public Gui(ImageRestService imageRestService) {
        Icon icon = createIcon();
        Label labelInform = createLabel("Welcome!");
        Label labelInform2 = createLabel("Add your photo to face recognition app ");
        Label labelInform3 = createLabel("You can do this using Url address or file with your local directory");

        TextField textFieldUrl = new TextField("Url address");
        Button buttonUrl = new Button("Send");

        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = configureUpload(buffer);

        buttonUrl.addClickListener(buttonClickEvent -> {

            if (isCorrectUrl(textFieldUrl))
            {
                new Notification("Bad address url!!!", 4000).open();
                textFieldUrl.clear();
            }
            else
            {
                removeAll();
                Button buttonAddNewPhoto = new Button("Add another photo");
                ImageRecognition imageDataFromUrl = getImageDataFromUrl(imageRestService, textFieldUrl);
                Label labelUploadUrl = createLabel("Uploaded photo");
                Image image = new Image(textFieldUrl.getValue(), "Bad photo");
                add(buttonAddNewPhoto, labelUploadUrl, image);

                showResult(imageDataFromUrl);

                addClickListenerToButton(icon, labelInform, labelInform2, labelInform3, textFieldUrl, buttonUrl, upload, buttonAddNewPhoto);

            }
        });
        showNotificationForRejectedFile(upload);

        upload.addSucceededListener(succeededEvent -> {
                removeAll();
                try
                {
                    Button buttonAddNewPhoto = new Button("Add another photo");
                    Label labelUploadUrl = createLabel("Uploaded photo");
                    byte[] imageAsBytes = IOUtils.toByteArray(buffer.getInputStream(succeededEvent.getFileName()));
                    Image image = convertToImage(imageAsBytes);
                    add(buttonAddNewPhoto, labelUploadUrl, image);
                    ImageRecognition imageDataFromFile = getImageDataFromFile(imageRestService, imageAsBytes);
                    showResult(imageDataFromFile);

                    addClickListenerToButton(icon, labelInform, labelInform2, labelInform3, textFieldUrl, buttonUrl, upload, buttonAddNewPhoto);

                }
                catch (IOException e)
                {
                    logger.error("Something goes wrong");
                }
            });

        add(icon,labelInform,labelInform2,labelInform3,textFieldUrl, buttonUrl, upload);
    }

    private void addClickListenerToButton(Icon icon, Label labelInform, Label labelInform2, Label labelInform3, TextField textFieldUrl, Button buttonUrl, Upload upload, Button buttonAddNewPhoto) {
        buttonAddNewPhoto.addClickListener(event -> {
            removeAll();
            add(icon, labelInform, labelInform2, labelInform3, textFieldUrl, buttonUrl, upload);
        });
    }

    private ImageRecognition getImageDataFromFile(ImageRestService imageRestService, byte[] imageAsBytes) throws IOException {
        return imageRestService.getImageDataFromFile(imageAsBytes);
    }

    private void showNotificationForRejectedFile(Upload upload) {
        upload.addFileRejectedListener(fileRejectedEvent -> new Notification("FIle is too big or in a bad format", 4000).open());
    }

    private ImageRecognition getImageDataFromUrl(ImageRestService imageRestService, TextField textFieldUrl) {
        return imageRestService.getImageDataFromUrl(textFieldUrl.getValue());
    }

    private boolean isCorrectUrl(TextField textFieldUrl) {
        return !Pattern.matches("http://.*|https://.*|data:image.*", textFieldUrl.getValue());
    }

    private Upload configureUpload(MultiFileMemoryBuffer buffer) {
        Upload upload = new Upload(buffer);
        upload.setUploadButton(new Button("Add file"));
        upload.setReceiver(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        upload.setMaxFileSize(1048576);
        return upload;
    }

    private Icon createIcon() {
        return new Icon(VaadinIcon.CAMERA);
    }

    private Label createLabel(String s) {
        return new Label(s);
    }


    private void showResult(ImageRecognition imageRecognition)
    {
        showFaceResult(imageRecognition);
        Label labelDescription = createLabel("Description: " + imageRecognition.getDescription().getCaptions().get(0).getText());
        add(labelDescription);
        Label labelHashTags = createLabel("HashTags to instagram :) ");
        add(labelHashTags);
        showDescriptionResult(imageRecognition);
    }

    private void showDescriptionResult(ImageRecognition imageRecognition) {
        imageRecognition.getDescription().getTags()
                .forEach(tags -> {
                    Label labelTags = createLabel(tags);
                    add(labelTags);
                });
    }

    private void showFaceResult(ImageRecognition imageRecognition) {
        imageRecognition.getFaces().forEach(face -> {
            Label labelAge = createLabel("Age: " + face.getAge());
            Label labelGender = createLabel("Gender: " + face.getGender());
            add(labelAge, labelGender);
        });
    }

    private Image convertToImage(byte[] imageData) {
        StreamResource streamResource = new StreamResource("image",
                () -> new ByteArrayInputStream(imageData));
        return new Image(streamResource, "photo");
    }
}
