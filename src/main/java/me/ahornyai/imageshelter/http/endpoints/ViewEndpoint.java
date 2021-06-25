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
import lombok.extern.slf4j.Slf4j;
import me.ahornyai.imageshelter.ImageShelter;
import me.ahornyai.imageshelter.http.responses.ErrorResponse;
import me.ahornyai.imageshelter.utils.AESUtil;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.zip.GZIPInputStream;

@Slf4j
public class ViewEndpoint implements Handler {
    private static String EXPECTED_PATH = null;

    @Override
    public void handle(@NotNull Context ctx) throws IOException {
        if (EXPECTED_PATH == null)
            EXPECTED_PATH = new File(ImageShelter.getInstance().getConfig().getUploadFolder()).getCanonicalPath();

        //Get the file, and the decryption key
        String fileParam = ctx.pathParam("file");
        File file = new File(ImageShelter.getInstance().getConfig().getUploadFolder(), fileParam);

        if (!file.getCanonicalPath().startsWith(EXPECTED_PATH)) {
            ctx.json(new ErrorResponse("FILE_DOES_NOT_EXIST", "This file does not exist.")).status(404);
            return;
        }

        if (!file.exists()) {
            ctx.json(new ErrorResponse("FILE_DOES_NOT_EXIST", "This file does not exist.")).status(404);
            return;
        }

        if (!file.canRead()) {
            ctx.json(new ErrorResponse("READ_PERMISSION", "File is not readable.")).status(500);
            return;
        }

        String contentType = Files.probeContentType(Paths.get(file.getAbsolutePath().endsWith(".gz") ? file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 3) : file.getAbsolutePath()));
        if (contentType != null) {
            ctx.contentType(contentType);
        }

        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            boolean shouldDecompress = file.getName().endsWith(".gz");

            if (ImageShelter.getInstance().getConfig().isEncrypt()) {
                SecretKey secretKey;
                try {
                    String keyParam = ctx.pathParam("key");
                    secretKey = AESUtil.getKeyFromString(keyParam);
                } catch (Exception ex) {
                    ctx.json(new ErrorResponse("BAD_KEY_FORMAT", "Bad key format."));
                    return;
                }

                inputStream = new CipherInputStream(inputStream, AESUtil.getDecryptCipher(secretKey));
            }
            if (shouldDecompress) inputStream = new GZIPInputStream(inputStream);
            ctx.result(inputStream);
        } catch (Exception ex) {
            if (ex instanceof InvalidKeyException || ex instanceof BadPaddingException) {
                ctx.json(new ErrorResponse("INVALID_KEY", "Invalid key provided!")).status(400);
            } else {
                ctx.json(new ErrorResponse("FILE_READ_ERROR", "Could not read the file!")).status(500);
                log.error("File read error:", ex);
            }
        }
    }
}
