/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.pantheon.plugins.services;

/** This service will be available during the registration callbacks. */
public interface PicoCLIOptions {

  /**
   * During the registration callback plugins can register CLI options that should be added to
   * Pantheon's CLI startup.
   *
   * @param namespace A namespace prefix. All registered options must start with this prefix
   * @param optionObject The instance of the object to be inspected. PicoCLI will reflect the fields
   *     of this object to extract the CLI options.
   */
  void addPicoCLIOptions(String namespace, Object optionObject);
}
