package com;

import java.io.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Test {

    public static void ReadTestFile(String fileName){

            File testFile = new File(fileName);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(testFile);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }


        try (BufferedReader buffer=new BufferedReader(new InputStreamReader(fis))) {
            boolean toDo = true;
            while (toDo) {
                Main.reads = 0;
                Main.writes = 0;
                String line = buffer.readLine();
                char function = line.charAt(0);
                long key = 0;
                float valueA = 0, valueH = 0;
                Scanner s = new Scanner(line).useDelimiter(" ");
                Pattern p = Pattern.compile("[a-z]");
                s.skip(p);

                switch (function) {
                    case 'p':
                        Main.prepareNewDatabase();
                        break;
                    case 'o':
                        Main.readIndex("index.bin");
                        break;
                    case 'z':
                        Main.writeIndex("index.bin");
                        break;
                    case 's':
                        Main.printAll("data.bin");
                        break;
                    case 'i':
                        Main.printIndex();
                        break;
                    case 'r':
                        Main.reorganize();
                        break;
                    case 'x':
                        Main.addElement(new Element(true));
                        break;
                    case 'g':
                        key = s.nextLong();
                        Element element = Main.findElement(key);
                        element.printElement();
                        break;
                    case 'd':
                        key = s.nextLong();
                        Main.deleteElement(key);
                        break;
                    case 'u':
                        key = s.nextLong();
                        valueA = s.nextFloat();
                        valueH = s.nextFloat();
                        Main.updateElement(key, new Triangle(valueA, valueH));
                        break;
                    case 'a':
                        key = s.nextLong();
                        valueA = s.nextFloat();
                        valueH = s.nextFloat();
                        Main.addElement(new Element(key, valueA, valueH));
                        break;
                    case 'q':
                        toDo = false;
                        System.out.println("Testing Ended Successfully!");
                        break;
                    default:
                        throw new IOException();
                }
                System.out.println("Operation ' " + line + " ' done successfully!");
                System.out.println("Reads: " + Main.reads);
                System.out.println("Writes: " + Main.writes);
                System.out.println();

            }
        }
        catch(IOException ex){
            System.out.println("Testing Error, Check if last line of testing file has only 'q' in it");
            ex.printStackTrace();
        }
    }
}


