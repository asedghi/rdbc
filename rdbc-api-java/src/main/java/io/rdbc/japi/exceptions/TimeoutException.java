/*
 * Copyright 2016 rdbc contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rdbc.japi.exceptions;

import java.time.Duration;

public class TimeoutException extends RdbcException {

    public TimeoutException(Duration timeout) {
        this(timeout, null);
    }

    public TimeoutException(Duration timeout, Throwable cause) {
        this(String.format("Timeout occurred after waiting for configured time of %s", timeout),
                cause);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimeoutException(String message) {
        super(message, null);
    }
}
