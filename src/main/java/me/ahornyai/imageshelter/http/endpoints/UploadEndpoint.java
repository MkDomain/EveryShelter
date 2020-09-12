package me.ahornyai.imageshelter.http.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;
import me.ahornyai.imageshelter.http.responses.ErrorResponse;
import org.jetbrains.annotations.NotNull;

public class UploadEndpoint implements Handler {
    @Override
    public void handle(@NotNull Context ctx) {
        if (!ctx.isMultipartFormData()) {
            ctx.json(new ErrorResponse(404, "The request's content type is not multipart/form-data."))
                .status(404);

            return;
        }

        UploadedFile uploadedFile = ctx.uploadedFile("image");
        String secret = ctx.formParam("secret");


        if (secret == null) {
            ctx.json(new ErrorResponse(404, "I need a secret."))
                    .status(404);

            return;
        }

        //TODO: secret handling

        if (uploadedFile == null) {
            ctx.json(new ErrorResponse(404, "I need an image."))
                    .status(404);

            return;
        }

        //TODO: file validation

        ctx.result("upload endpoint secret: " + secret);
    }
}