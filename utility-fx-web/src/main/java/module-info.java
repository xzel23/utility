// Copyright 2019 Axel Howind
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import org.jspecify.annotations.NullMarked;

/**
 * Module for JavaFX WebView integration.
 * <p>
 * This module exports the package {@code com.dua3.fx.web} and opens it for reflection.
 * It requires the modules {@code com.dua3.fx.controls}, {@code javafx.web}, {@code jdk.jsobject},
 * {@code org.apache.logging.log4j}, and {@code org.jspecify}.
 */
@NullMarked
open module com.dua3.utility.fx.web {
    exports com.dua3.utility.fx.web;

    requires com.dua3.utility.fx.controls;
    requires javafx.base;
    requires javafx.web;
    requires jdk.jsobject;
    requires org.apache.logging.log4j;
    requires org.jspecify;
}
