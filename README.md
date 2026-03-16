# Housify - frontend

## Prerequisites

1. Install Android Studio

## How to run

**1. Running on an Android Emulator**

1. Open the `\mobile-frontend` folder in Android Studio.
2. Wait for Gradle to sync the project.
3. Select an Android emulator from the device dropdown menu.
4. Click the 'Run' button (▶️) or use the shortcut Shift + F10.
5. The app will be built, installed, and launched in the selected emulator.

**2. Running on a Physical Android Device**

To connect the app to a backend running on your local machine, you need to configure the backend's IP address.

1. Find your computer's local IP address
   - Open Command Prompt (cmd) on Windows or Terminal on macOS/Linux.
   - On Windows, run `ipconfig`. Look for the "IPv4 Address" under your active Wi-Fi or Ethernet adapter (e.g., 192.168.1.10).
   - On macOS/Linux, run `ifconfig | grep "inet "`.
2. Update the Host IP in the app:
   - Navigate to the profile screen, then scroll to the very bottom.
   - Tap “Housify @ Version 1.0.0” three times to open the Update Host IP screen.
   ![Profile screen](images/readme1.png)
   - Update the host IP with the IP address you found in the previous step.
   ![Profile screen](images/readme2.png)
   
3. Configure Network Security for Your Local IP
   - Open the file: `/mobile-frontend/app/src/main/res/xml/network_security_config.xml`.
   - Replace the `<domain-config>` section with the following, updating `YOUR_LOCAL_IP_ADDRESS` to your actual local IP (e.g., `192.168.1.10`):

   ```
   <?xml version="1.0" encoding="utf-8"?>
   <network-security-config xmlns:android="http://schemas.android.com/apk/res/android">
      <domain-config cleartextTrafficPermitted="true">
         <domain includeSubdomains="true">10.0.2.2</domain>
         <domain includeSubdomains="true">YOUR_LOCAL_IP_ADDRESS</domain>
      </domain-config>
   </network-security-config>
   ```
   - For example, If your IP is 192.168.1.10, it should look like:
   ```
   <domain includeSubdomains="true">192.168.1.10</domain>
   ```

4. Save the file

5. Run the app

## How to Test Deep Links

You can test deep linking functionality using the Android Debug Bridge (adb). This allows you to open specific screens in the app directly.

1. Make sure the app is installed on your emulator or device.
2. 2.Open a terminal or Command Prompt.
3. 3.Execute the following command to simulate opening a "join group" link:
```
adb shell am start -a android.intent.action.VIEW -d "housify://join-group/F685UYB
```
