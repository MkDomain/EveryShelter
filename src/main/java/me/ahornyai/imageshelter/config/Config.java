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

package me.ahornyai.imageshelter.config;

import lombok.Getter;
import org.apache.commons.lang3.RandomStringUtils;

@Getter
@SuppressWarnings("FieldMayBeFinal")
public class Config {
    private Integer port = 8282;

    private String[] secrets = new String[]{RandomStringUtils.randomAlphanumeric(32)};

    private String[] allowedExtensions = new String[]
            {"png", "jpg", "jpeg", "bmp", "gif", "webp",
                    "txt", "js", "css", "html", "java", "py", "rar", "zip", "yaml", "yml", "ini", "md",
                    "mov", "mp4", "webm", "mkv", "flv", "vob", "ogg", "drc", "giv", "avi", "wmv", "yuv", "m4p", "m4v", "mpg", "mpeg", "m2v", "3gp", "3g2",
                    "aa", "aac", "alac", "flac", "m4b", "m4p", ".mp3", "opus", "raw", "voc", "wav"
            };

    private String[] compressedExtensions = new String[]{"bmp", "txt", "js", "css", "html", "java", "py", "yaml", "yml", "ini", "md", "raw"};

    private String uploadFolder = "uploads";

    private boolean encrypt = true;

    private boolean backupKeys = false;

    private boolean listingEnabled = false;
}
