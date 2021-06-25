/*
 * Copyright (c) 2020 Alex Hornyai
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.ahornyai.imageshelter.http.endpoints;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UploadedFile;
import lombok.extern.slf4j.Slf4j;
import me.ahornyai.imageshelter.ImageShelter;
import me.ahornyai.imageshelter.http.responses.ErrorResponse;
import me.ahornyai.imageshelter.http.responses.SuccessUploadResponse;
import me.ahornyai.imageshelter.utils.AESUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class UploadEndpoint implements Handler {
    private static final String[] ALLOWED_EXTENSIONS = ImageShelter.getInstance().getConfig().getAllowedExtensions();

    @Override
    public void handle(@NotNull Context ctx) {
        //Only accept "multipart/form-data requests"
        if (!ctx.isMultipartFormData()) {
            ctx.json(new ErrorResponse("NOT_FORM_DATA", "The request's type is not multipart/form-data.")).status(400);
            return;
        }

        //Loading the file and the secret from the request
        UploadedFile uploadedFile = ctx.uploadedFile("image");
        String secret = ctx.formParam("secret");

        if (secret == null) {
            ctx.json(new ErrorResponse("MISSING_SECRET", "Secret not provided.")).status(400);
            return;
        }

        if (!ArrayUtils.contains(ImageShelter.getInstance().getConfig().getSecrets(), secret)) {
            ctx.json(new ErrorResponse("INVALID_SECRET", "Secret is not valid.")).status(403);
            return;
        }

        if (uploadedFile == null) {
            ctx.json(new ErrorResponse("MISSING_IMAGE", "Image not provided.")).status(400);
            return;
        }

        if (Arrays.stream(ALLOWED_EXTENSIONS).noneMatch(uploadedFile.getExtension().substring(1)::equalsIgnoreCase)) {
            ctx.json(new ErrorResponse("WRONG_EXTENSION", "Wrong extension (" + uploadedFile.getExtension().substring(1) + "). Supported extensions: " + Arrays.toString(ALLOWED_EXTENSIONS))).status(400);
            return;
        }
        //Only compress BMP files, as they are not always compressed
        boolean shouldCompress = Arrays.stream(ImageShelter.getInstance().getConfig().getCompressedExtensions()).anyMatch(e -> uploadedFile.getExtension().substring(1).equalsIgnoreCase(e));

        //Get a name for the uploaded file
        String rawName = uploadedFile.getFilename() + "-" + UUID.randomUUID().toString().replace("-", "");
        String name = rawName + uploadedFile.getExtension() + (shouldCompress ? ".gz" : "");

        SecretKey key = null;
        try {
            if (ImageShelter.getInstance().getConfig().isEncrypt())
                key = AESUtil.generateKey();
        } catch (Exception ex) {
            log.error("Key generation error:", ex);
            ctx.json(new ErrorResponse("UNEXPECTED_ERROR", "Unexpected error while generating the AES key. If you are the server owner please open a github issue with the exception."));
            return;
        }

        try {
            File outputFile = new File(ImageShelter.getInstance().getConfig().getUploadFolder(), name);
            outputFile.getParentFile().mkdirs();
            outputFile.createNewFile();

            //Allow uploading giant files, without reading them fully into the memory
            InputStream inputStream = uploadedFile.getContent();
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

            //Add encryption and compression if necessary
            if (ImageShelter.getInstance().getConfig().isEncrypt())
                outputStream = new CipherOutputStream(outputStream, AESUtil.getEncryptCipher(key));
            if (shouldCompress) outputStream = new GZIPOutputStream(outputStream);
            IOUtils.copy(inputStream, outputStream, 4096);

            outputStream.close();
            inputStream.close();

            //Backup keys if necessary
            if (ImageShelter.getInstance().getConfig().isBackupKeys()) {
                if (!Files.exists(Paths.get("key_backup")))
                    Files.createDirectory(Paths.get("key_backup"));
                Files.write(Paths.get("key_backup", rawName + ".txt"), AESUtil.getKeyAsString(key).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            }
            if (ImageShelter.getInstance().getConfig().isEncrypt())
                ctx.json(new SuccessUploadResponse(URLEncoder.encode(name, "UTF-8"), URLEncoder.encode(AESUtil.getKeyAsString(key), "UTF-8")));
            else
                ctx.json(new SuccessUploadResponse(URLEncoder.encode(name, "UTF-8")));
        } catch (Exception ex) {
            log.error("File saving error:", ex);
            ctx.json(new ErrorResponse("UNEXPECTED_ERROR", "Unexpected error with file saving. If you are the server owner please open a github issue with the exception."));
        }
    }
}
