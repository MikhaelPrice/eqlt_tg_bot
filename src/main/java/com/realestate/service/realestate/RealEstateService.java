package com.realestate.service.realestate;

import com.realestate.EqtApplication;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.InputStream;

public class RealEstateService {
    public InputFile getPictureFromResources(String pictureName) {
        ClassLoader classLoader = EqtApplication.class.getClassLoader();
        InputStream imageStream = classLoader.getResourceAsStream(pictureName);
        return new InputFile(imageStream, pictureName);
    }
}
