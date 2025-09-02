public class Main {
    public static void main(String[] args) {
        try {
            manageFiles Stock = new manageFiles("Stock.txt");
            manageFiles User = new manageFiles("User.txt");
            Thread StockInfo = new Thread(Stock);
            Thread UserData = new Thread(User);
            StockInfo.start();
            UserData.start();
            System.out.println("------MENU------");
            System.out.println("1)Read the file ");
            System.out.println("2)Write in the file");
            StockInfo.join();
            UserData.join();
            Stock.DisplayContent();
            User.DisplayContent();
            String newData = "Hello,This is a message to Stock.txt\nTesting if filewriter works";
            Stock.writeContent(newData);
            Stock.DisplayContent();
        }catch(Exception e){
            System.out.print(e);
        }
    }
}