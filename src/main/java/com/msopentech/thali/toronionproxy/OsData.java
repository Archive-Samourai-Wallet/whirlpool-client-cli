// TODO https://github.com/thaliproject/Tor_Onion_Proxy_Library/pull/126
// TODO https://github.com/thaliproject/Tor_Onion_Proxy_Library/pull/125
/*
Copyright (C) 2011-2014 Sublime Software Ltd

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

THIS CODE IS PROVIDED ON AN *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED,
INCLUDING WITHOUT LIMITATION ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A PARTICULAR PURPOSE,
MERCHANTABLITY OR NON-INFRINGEMENT.

See the Apache 2 License for the specific language governing permissions and limitations under the License.
*/

package com.msopentech.thali.toronionproxy;

import java.io.IOException;
import java.util.Scanner;

public class OsData {
  public enum OsType {
    WINDOWS,
    LINUX_32,
    LINUX_64,
    MAC,
    ANDROID,
    UNSUPPORTED
  }

  private static OsType detectedType = null;

  public static OsType getOsType() {
    if (detectedType == null) {
      detectedType = actualGetOsType();
    }

    if (detectedType == OsType.UNSUPPORTED) {
      throw new RuntimeException("Unsupported OS");
    }
    return detectedType;
  }

  /**
   * Yes, I should use a proper memoization abstract class but, um, next time.
   *
   * @return Type of OS we are running on
   */
  protected static OsType actualGetOsType() {

    if (System.getProperty("java.vm.name").contains("Dalvik")) {
      return OsType.ANDROID;
    }

    String osName = System.getProperty("os.name");
    if (osName.contains("Windows")) {
      return OsType.WINDOWS;
    } else if (osName.contains("Mac")) {
      return OsType.MAC;
    } else if (osName.contains("Linux")) {
      return getLinuxType();
    }
    return OsType.UNSUPPORTED;
  }

  protected static OsType getLinuxType() {
    String[] cmd = {"uname", "-m"};
    Process unameProcess = null;
    Scanner scanner = null;
    try {
      String unameOutput;
      unameProcess = Runtime.getRuntime().exec(cmd);

      scanner = new Scanner(unameProcess.getInputStream());
      if (scanner.hasNextLine()) {
        unameOutput = scanner.nextLine();
      } else {
        throw new RuntimeException("Couldn't get output from uname call");
      }

      int exit = unameProcess.waitFor();
      if (exit != 0) {
        throw new RuntimeException("Uname returned error code " + exit);
      }

      if (unameOutput.compareTo("i686") == 0) {
        return OsType.LINUX_32;
      }
      if (unameOutput.compareTo("x86_64") == 0) {
        return OsType.LINUX_64;
      }
      return OsType.UNSUPPORTED;
    } catch (IOException e) {
      throw new RuntimeException("Uname failure", e);
    } catch (InterruptedException e) {
      throw new RuntimeException("Uname failure", e);
    } finally {
      if (unameProcess != null) {
        scanner.close();
        unameProcess.destroy();
      }
    }
  }
}
