package me.ahornyai.imageshelter.http.endpoints;

import io.javalin.core.util.FileUtil;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;
import me.ahornyai.imageshelter.ImageShelter;
import me.ahornyai.imageshelter.http.responses.ErrorResponse;
import me.ahornyai.imageshelter.http.responses.SuccessUploadResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class UploadEndpoint implements Handler {
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg", "bmp", "gif");

    @Override
    public void handle(@NotNull Context ctx) {
        if (!ctx.isMultipartFormData()) {
            ctx.json(new ErrorResponse("NOT_FORM_DATA", "The request's content type is not multipart/form-data."))
                .status(404);

            return;
        }

        UploadedFile uploadedFile = ctx.uploadedFile("image");
        String secret = ctx.formParam("secret");

        if (secret == null) {
            ctx.json(new ErrorResponse("MISSING_SECRET", "I need a secret."))
                    .status(404);

            return;
        }

        if (!ArrayUtils.contains(ImageShelter.getInstance().getConfig().getSecrets(), secret)) {
            ctx.json(new ErrorResponse("WRONG_SECRET", "Wrong secret."))
                    .status(403);

            return;
        }

        if (uploadedFile == null) {
            ctx.json(new ErrorResponse("MISSING_IMAGE", "I need an image."))
                    .status(400);

            return;
        }

        if (ALLOWED_EXTENSIONS.stream().noneMatch(uploadedFile.getExtension().substring(1)::equalsIgnoreCase)) {
            ctx.json(new ErrorResponse("WRONG_EXTENSION", "Wrong extension (" + uploadedFile.getExtension().substring(1) + "). Supported extensions: " + ALLOWED_EXTENSIONS))
                    .status(400);

            return;
        }

        //TODO: encryption, compressing

        String name = RandomStringUtils.randomAlphanumeric(32) + uploadedFile.getExtension();
        FileUtil.streamToFile(uploadedFile.getContent(),"uploads/" + name);

        ctx.json(new SuccessUploadResponse(name, "asdasd"));
    }
}
