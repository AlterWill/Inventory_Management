package com.inventorymanagement;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class Authenticator {

  private static String loggedInUser = null;

  public static boolean login(String username, String password) {
    try {
      URL resource = Authenticator.class.getClassLoader().getResource("User.txt");
      if (resource == null) {
        throw new IllegalArgumentException("file not found! User.txt");
      }
      File myObj = new File(resource.toURI());
      Scanner myReader = new Scanner(myObj);
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        String[] creds = data.split(",");
        if (creds[0].equals(username) && creds[1].equals(password)) {
          loggedInUser = username;
          myReader.close();
          return true;
        }
      }
      myReader.close();
    } catch (FileNotFoundException | URISyntaxException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    return false;
  }

  public static boolean signUp(String username, String password) {
    try {
      URL resource = Authenticator.class.getClassLoader().getResource("User.txt");
      if (resource == null) {
        throw new IllegalArgumentException("file not found! User.txt");
      }
      File myObj = new File(resource.toURI());
      Scanner myReader = new Scanner(myObj);
      while (myReader.hasNextLine()) {
        String data = myReader.nextLine();
        String[] creds = data.split(",");
        if (creds[0].equals(username)) {
          System.out.println("User already exists.");
          myReader.close();
          return false;
        }
      }

      myReader.close();
      manageFiles mf = new manageFiles(resource.toURI().getPath());
      mf.append("\n" + username + "," + password);
      return true;
    } catch (FileNotFoundException | URISyntaxException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    return false;
  }

  public static void logOut() {
    loggedInUser = null;
  }

  public static boolean isLoggedIn() {
    return loggedInUser != null;
  }

  public static String getLoggedInUser() {
    return loggedInUser;
  }
}
