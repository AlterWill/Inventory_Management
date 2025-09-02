/* This object runs multithreaded
*   DisplayContent method read the file data and puts it to content
*  Thus,no need to reread the file for data
*   writeContent just appends the data and updates content
*   Thus,no need to reread the file here as well
*   However,This object does not delete/remove a part of a file content
*   Let me know,If I need to add that
* */

import java.io.*;

public class manageFiles implements Runnable {
    StringBuffer content = new StringBuffer();
    String filePath;

    manageFiles(String file){
        this.filePath = file;
    }

    void fileExist(File f){
        try{
            if(!f.exists()){
                if(f.createNewFile()) {
                    System.out.print("File didn't exist,Thus created one");
                }else{
                    System.out.println("Couldn't find or create the file");
                }
            }
        }catch(IOException a){
            System.out.println("Writing error");
        }
    }

    void readContent(File f){
        content.setLength(0);
        try(BufferedReader reader = new BufferedReader(new FileReader(f))){
            String line;
            while ((line = reader.readLine())!=null){
                content.append(line+"\n");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Somehow the file is not found");
        }catch (IOException e){
            System.out.println("The error was"+e);
        }
    }
    @Override
    public void run(){
        File f = new File(filePath);
        fileExist(f);
        readContent(f);
    }

    public void DisplayContent(){
        System.out.println("the contents of "+filePath+" are "+content);
    }

    public void writeContent(String data){
        try(FileWriter writer = new FileWriter(filePath)){
            writer.write(data);
            content.append(data);
        } catch (IOException e) {
            System.out.println("File writing got irreupted");
        }
    }
}
