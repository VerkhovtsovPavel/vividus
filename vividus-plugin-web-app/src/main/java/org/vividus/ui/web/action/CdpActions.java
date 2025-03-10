/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.action;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Map;

import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;

public class CdpActions
{
    private final IWebDriverProvider webDriverProvider;
    private final IWebDriverManager webDriverManager;

    public CdpActions(IWebDriverProvider webDriverProvider, IWebDriverManager webDriverManager)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverManager = webDriverManager;
    }

    public void executeCdpCommand(String command, Map<String, Object> parameters)
    {
        isTrue(webDriverManager.isBrowserAnyOf(Browser.CHROME), "The step is only supported by Chrome browser.");
        HasCdp hasCdp = webDriverProvider.getUnwrapped(HasCdp.class);
        hasCdp.executeCdpCommand(command, parameters);
    }
}
