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

/**
 * The com.dua3.fx.controls module is a JavaFX module that provides custom controls for building UIs.
 * <p>
 * This module exports the {@code com.dua3.fx.controls} package, making it accessible to other modules.
 * <p>
 * This module requires the {@code com.dua3.fx.util} module, which provides utility classes for JavaFX.
 * It also requires the {@code com.dua3.fx.icons} module, which provides custom icons for JavaFX applications.
 * <p>
 * Additionally, this module requires the {@code org.apache.logging.log4j} module for logging purposes.
 * <p>
 * This module also requires the following JavaFX modules:
 * <ul>
 *    <li>{@code javafx.controls}: provides basic JavaFX controls for UI development</li>
 *    <li>{@code javafx.graphics}: provides graphics functionalities for JavaFX applications</li>
 * </ul>
 * <p>
 * It also requires the {@code com.dua3.utility} module for utility classes, and the {@code java.desktop} module
 * for desktop application development purposes.
 * <p>
 * This module has a dependency on the {@code com.dua3.cabe.annotations} module for the use of null check injection
 * into the compiled classes.
 */
module com.dua3.utility.fx.controls {
    exports com.dua3.utility.fx.controls;
    opens com.dua3.utility.fx.controls;

    requires com.dua3.utility.fx.icons;

    requires org.apache.logging.log4j;

    requires javafx.controls;
    requires javafx.graphics;
    requires com.dua3.utility;
    requires java.desktop;

    requires static org.jspecify;    requires com.dua3.utility.fx;
}
