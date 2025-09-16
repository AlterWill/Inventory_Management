package com.inventorymanagement;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

public class manageFiles {
    StringBuffer content = new StringBuffer();
    String filePath;

    manageFiles(String file){
        this.filePath = file;
    }

    private File getFileFromResource() throws URISyntaxException {
        URL resource = getClass().getClassLoader().getResource(filePath);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + filePath + ". Writing to resources is not recommended.");
        }
        return new File(resource.toURI());
    }

    void readContent(File f){
        content.setLength(0);
        if (!f.exists()) {
            return; // Don't try to read a non-existent file
        }
        try(BufferedReader reader = new BufferedReader(new FileReader(f))){
            String line;
            while ((line = reader.readLine())!=null){
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e){
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public void run(){
        try {
            File f = getFileFromResource();
            readContent(f);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void DisplayContent(){
        System.out.println("Contents of " + filePath + ":\n" + content);
    }

    public void writeContent(String data){
        try {
            File f = getFileFromResource();
            try(FileWriter writer = new FileWriter(f)){
                writer.write(data);
                content.setLength(0);
                content.append(data);
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    public void append(String data) {
        try {
            File f = getFileFromResource();
            try (FileWriter writer = new FileWriter(f, true)) {
                writer.write(data);
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println("Error appending to file: " + e.getMessage());
        }
    }
}

