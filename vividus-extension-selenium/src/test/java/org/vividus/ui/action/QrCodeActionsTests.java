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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.zxing.NotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
public class QrCodeActionsTests
{
    private static final String EXPECTED_VALUE = "https://github.com/vividus-framework/vividus";

    @InjectMocks
    private QrCodeActions qrCodeActions;

    @Test
    void shouldReadQrCode() throws IOException, NotFoundException
    {
        final BufferedImage qrCodeBufferedImage = loadResource("qrCode.png");

        String actualQrCodeValue = qrCodeActions.scanQrCode(qrCodeBufferedImage);

        assertEquals(EXPECTED_VALUE, actualQrCodeValue);
    }

    @Test
    void shouldThrowExceptionInCaseOfQrCodeAbsents() throws IOException
    {
        final BufferedImage squareBufferedImage = loadResource("blackSquare168x168.png");

        assertThrows(NotFoundException.class, () ->
                qrCodeActions.scanQrCode(squareBufferedImage));
    }

    private BufferedImage loadResource(String filePath) throws IOException
    {
        return ImageIO.read(ResourceUtils.loadFile(QrCodeActions.class, filePath));
    }
}
