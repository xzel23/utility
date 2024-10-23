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
 * This module provides utility classes for working with databases in JavaFX applications.
 * <p>
 * The module provides the following features:
 * <ul>
 *   <li>Exports the package {@code com.dua3.fx.util.db} for use by other modules.</li>
 *   <li>Opens the package {@code com.dua3.fx.util.db} to allow for reflection-based access.</li>
 *   <li>Requires transitively the module {@code com.dua3.fx.util} to ensure that all required dependencies are available.</li>
 *   <li>Requires the module {@code com.dua3.utility.db} for the database utility classes.</li>
 *   <li>Requires the module {@code org.apache.logging.log4j} for logging purposes.</li>
 *   <li>Requires the module {@code javafx.controls} for JavaFX controls.</li>
 *   <li>Requires the module {@code javafx.graphics} for JavaFX graphics.</li>
 *   <li>Requires the module {@code com.dua3.cabe.annotations} for the custom annotations used in this module.</li>
 * </ul>
 */
@NullMarked
open module com.dua3.utility.fx.db {
    exports com.dua3.utility.fx.db;

    requires com.dua3.utility.db;
    requires org.apache.logging.log4j;
    requires javafx.controls;
    requires javafx.graphics;
    requires static org.jspecify;    requires com.dua3.utility.fx;
}
