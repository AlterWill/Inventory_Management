import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.IOException;

class Authenticator {

    private static String loggedInUser = null;

    public static boolean login(String username, String password) {
        try {
            File myObj = new File("User.txt");
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
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return false;
    }

    public static boolean signUp(String username, String password) {
        try {
            File myObj = new File("User.txt");
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
            manageFiles mf = new manageFiles("User.txt");
            mf.append("\n" + username + "," + password);
            return true;
        } catch (FileNotFoundException e) {
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

