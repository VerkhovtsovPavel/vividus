/*
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.ui.action;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.lang3.Validate;
import org.vividus.util.qrCode.QRCodeReader;

public class QRCodeActions
{
    private QRCodeReader qrCodeReader;

    public QRCodeActions(QRCodeReader qrCodeReader)
    {
        this.qrCodeReader = qrCodeReader;
    }

    public String scanQRCode(Path path) throws IOException
    {
        String qrCodeValue = qrCodeReader.readQRCode(path);
        Validate.isTrue(!qrCodeValue.isEmpty(), "There is no QR code in the image");
        return qrCodeValue;
    }
}
