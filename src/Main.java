import java.util.Scanner;

class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        while (true) {
            if (Authenticator.isLoggedIn()) {
                System.out.println("Welcome " + Authenticator.getLoggedInUser());
                System.out.println("1. Logout");
                System.out.println("2. Exit");
                int choice = sc.nextInt();
                sc.nextLine(); // consume newline
                if (choice == 1) {
                    Authenticator.logOut();
                    System.out.println("Logged out successfully.");
                } else if (choice == 2) {
                    break;
                }
            } else {
                System.out.println("Welcome to Inventory Management System");
                System.out.println("1. Login");
                System.out.println("2. SignUp");
                System.out.println("3. Exit");
                int choice = sc.nextInt();
                sc.nextLine(); // consume newline

                if (choice == 1) {
                    System.out.println("Enter username");
                    String username = sc.nextLine();
                    System.out.println("Enter password");
                    String password = sc.nextLine();
                    if (Authenticator.login(username, password)) {
                        System.out.println("Login successful.");
                    } else {
                        System.out.println("Login failed. Invalid credentials.");
                    }
                } else if (choice == 2) {
                    System.out.println("Enter username");
                    String username = sc.nextLine();
                    System.out.println("Enter password");
                    String password = sc.nextLine();
                    if (Authenticator.signUp(username, password)) {
                        System.out.println("SignUp successful. Please login.");
                    } else {
                        System.out.println("SignUp failed.");
                    }
                } else if (choice == 3) {
                    break;
                }
            }
        }
        sc.close();
    }
}