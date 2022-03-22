import functionClasses.CustomerFunctions;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.print("\033[H\033[2J"); // To clear the terminal
        Scanner scn = new Scanner(System.in);
        int choice = 0;
        CustomerFunctions customerFunctions = new CustomerFunctions();

        while (choice != 3) {
            showMenu();
            System.out.print("Your Choice: ");
            choice = scn.hasNextInt() ? scn.nextInt() : 0;
            scn.nextLine();
            System.out.print("\033[H\033[2J"); // To clear the terminal
            switch (choice) {
                case 1 -> customerFunctions.startAuthenticationFlow();
                case 2 -> {}
                case 3 -> {}
                default -> System.out.println("!! Please enter a valid choice !!\n");
            }

        }
    }

    public static void showMenu(){
        System.out.println("NORTH DELHI POWER CONSOLE");
        System.out.println("------ Main Menu ------");
        System.out.println(" 1. Customer Login");
        System.out.println(" 2. Admin Login");
        System.out.println(" 3. Exit");
        System.out.println("-----------------------");
    }
}
