package com;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.ceil;

public class Main {
    private static float alpha = 0.5f;
    public final static int pageSize = 100;
    public static int numberOfPages = 2;
    public static int numberOfPagesMain = 1;
    public static int numberOfPagesOverflow = 1;
    public final static double overflowSize = 0.1; // 10%
    public static long index[];
    public static boolean isReorganised = true;

    public static int indexNext = 0;
    public static void addIndexNext(int howMuch){
        indexNext+=howMuch;
    }

    public static int currentOverflowNumber = 0;
    public static int maxOverflowNumber = 100;

    private static int recordsOverall = 0;
    private static int recordsInOverflow = 0;
    private static int recordsInMain = 0;

    private static int pagesInMain = 1;
    private static int pagesInOverflow = 1;

    public static int currentLoadedMainPage = 0;
    public static int currentLoadedOverflowPage = 0;



    public static int reads = 0;
    public static int writes = 0;

    private static int getPageNumberByKey(long key){
        int it = 0;
        int maxIter = index.length-1;

        int iterator = 0;
        int retValue = 0;
        for (long x: index) {
            if(x > key){
                //iterator--;
                //currentLoadedMainPage = iterator;
                //return iterator;
                break;
            }
            else {
                retValue = iterator;
                    iterator++;
            }
        }

        //iterator--;
/*
        while(iterator < pagesInMain){
            if(index[iterator] < key) {
                iterator++;
            }
            else
                break;
        }*/
        currentLoadedMainPage = iterator-1;
        return iterator-1;
    }

    public static int addElement(Element element) {
        isReorganised = false;
        int pageNo = getPageNumberByKey(element.getKey());

        Page page = new Page();
        page.readPage("data.bin", pageNo);

        int ifGood = page.addElementBetter(element);
        if(ifGood == 0){
            System.out.println("Key Already Exists");
            //addElement(new Element(true));
        }
        else if (ifGood == 2)
            recordsInOverflow++;
        else
            recordsInMain++;


        recordsOverall++;
        if(!isReorganised) {
            page.writePage("data.bin", pageNo);
        }

        //if (pagesInOverflow == ceil(pagesInMain*overflowSize))
            //reorganize();

        return 1;
    }

    public static int addToOverflowBetter(Element elementToAdd, Element element){
        //isReorganised = false;
        Element elementMain = element;
        int nextPointer = elementMain.getOverflowPageNo();
        int retValue1 = elementMain.getOverflowPageNo();
        int retValue2 = currentLoadedOverflowPage;
        boolean ret = true;
        int level = 0;

        Page overFlow = new Page();
        overFlow.readPage("data.bin", numberOfPagesMain + currentLoadedOverflowPage);
        Page overFlow2 = overFlow;
        int k = 0;

        while (overFlow.getDataKey(k) != -1){
            if(overFlow.getDataKey(k) == elementToAdd.getKey()){
                System.out.println("Key already exists");
                return element.getOverflowPageNo();
            }

            k++;
        }
        overFlow.setData(elementToAdd, k);

        //Zapisujemy do sekcji overflow nasz nowy rekord, chwilowo czekając czy nie tzreba będzie zmienić jego wartości overflow pointer


        if(nextPointer != -1) {
            // zaczynaja sie schody
            while(true) {
                overFlow2.readPage("data.bin", numberOfPagesMain + nextPointer);
                //Otwieramy stronę na ktorą wskazuje wskaźnik z obszaru głównego
                int k2 = 0;
                int index = 0;
                long tmpKey = Long.MAX_VALUE;
                while (k2 < pageSize) {
                    if (overFlow2.getDataKey(k2) > elementMain.getKey() && overFlow2.getDataKey(k2) < tmpKey) {
                        tmpKey = overFlow2.getDataKey(k2);
                        index = k2;
                    }
                    k2++;
                }
                //po znalezieniu już najbliższego klucza sprawdzamy czy klucz ten jest większy czy mniejszy niż dodawany
                //oraz czy jest to już koniec łańcucha wskaźników

                if(overFlow2.getData(index).getOverflowPageNo() == -1){

                    if(overFlow2.getDataKey(index) < elementToAdd.getKey()){
                        overFlow2.setData(new Element(overFlow2.getDataKey(index),
                                overFlow2.getData(index).getData().getA(),
                                overFlow2.getData(index).getData().getH(),
                                currentLoadedOverflowPage), index);

                        overFlow.setData(new Element(elementToAdd.getKey(),
                                elementToAdd.getData().getA(),
                                elementToAdd.getData().getH(),
                                        -1)
                                , k);

                        if(level == 0)
                            ret = false;

                        overFlow2.writePage("data.bin", numberOfPagesMain + nextPointer);
                        break;
                    }
                    else { //klucz z poprzedniego większy niż nowo dodany
                        overFlow.setData(new Element(elementToAdd.getKey(),
                                elementToAdd.getData().getA(),
                                elementToAdd.getData().getH(),
                                nextPointer), k);
                        if(level == 0)
                            ret = true;
                        break;
                    }
                }
                else { //key different than -1 so the chain is not completed YET
                    if(overFlow2.getDataKey(index) < elementToAdd.getKey()){
                        if(level == 0)
                            ret = false;
                        elementMain = overFlow2.getData(index);
                        nextPointer = overFlow2.getData(index).getOverflowPageNo();
                    }
                    else { //klucz z wczytanego większy niż nowo dodany
                        overFlow.setData(new Element(elementToAdd.getKey(),
                                elementToAdd.getData().getA(),
                                elementToAdd.getData().getH(),
                                nextPointer), k);
                        if(level == 0)
                            ret = true;
                        break;
                    }
                }
                level++;
            }
        }


        overFlow.writePage("data.bin", numberOfPagesMain + currentLoadedOverflowPage);


        currentOverflowNumber++;
        if(currentOverflowNumber >= maxOverflowNumber){
            reorganize();
        }
        if(ret)
           return retValue2; // ostatnia overflow
        else
            return retValue1; // poprzednio wskazywana
    }


    public static  int reorganize(){
        System.out.println("REORGANIZING");
        int offsetWrite = 0;
        int offsetRead = 0;
        int index2 = 0;
        List<Long> indexList = new ArrayList<>();


        Page newPage = new Page();

        Page readPage = new Page();

        Element tmp = new Element();

        readPage.readPage("data.bin", offsetRead);

        while (index2 <= recordsOverall) {
            long key = 0;

            for (int i = 0; i < alpha * pageSize; i++) {
                if(readPage.indexNext == 100){
                    offsetRead++;
                    readPage = new Page();
                    readPage.readPage("data.bin", offsetRead);
                }
                Element ret = readPage.getNext();

                //ret.setOverflowPageNo(-1);
                if(index2 > recordsOverall)
                    break;
                //System.out.println(ret.getKey());
                if(ret.getKey() != -1) {
                    newPage.addElementBetter(new Element(ret.getKey(), ret.getData().getA(), ret.getData().getH(), -1));
                    if(i == 0)
                        key = ret.getKey();
                }
                else {
                    i--;
                    index2--;
                }
                index2++;

            }

            indexList.add(key);

            newPage.writePage("data2.bin", offsetWrite);
            newPage = new Page();
            offsetWrite++;
        }

        Page of = new Page();
        numberOfPagesMain = indexList.size();
        Double temp = ceil(numberOfPages/10.0);
        maxOverflowNumber = temp.intValue()*pageSize;
        for(int i = 0; i<temp; i++) {
            of.writePage("data2.bin", numberOfPagesMain + i);
        }

        File file = new File("data2.bin");
        File file2 = new File("data.bin");
        file2.delete();

        File file3 = new File("data.bin");
        boolean b = file.renameTo(file3);
        index = Convert.toLongListArray(indexList);
        //System.out.print(b);

        isReorganised = true;
        currentOverflowNumber = 0;
        return 1;
    }

    public static void prepareNewDatabase(){
        index = new long[1];
        index[0] = 0;
        File file2 = new File("data.bin");
        file2.delete();

        Page page = new Page();
        page.addElementBetter(new Element(0,0,0, -1));
        page.writePage("data.bin", 0);
        Page of = new Page();
        of.writePage("data.bin", 1);
    }

    public static void printIndex(){
        int iterator = 0;
        System.out.println("index:");
        for (long i : index) {
            if(i > 0 || iterator == 0) {
            System.out.println("[ " + iterator + " : " + i + " ]");
            iterator++;
            }
        }
    }

    public static  Element findElement(long key){
        int pageNo = getPageNumberByKey(key);
        Page page = new Page();
        page.readPage("data.bin", pageNo);

        return page.findElement(key);
    }

    public static int updateElement(long key, Triangle value){
        int pageNo = getPageNumberByKey(key);
        Page page = new Page();
        page.readPage("data.bin", pageNo);
        return page.updateElement(key, value.getA(), value.getH());
    }

    public static int deleteElement(long key){
        int pageNo = getPageNumberByKey(key);
        Page page = new Page();
        page.readPage("data.bin", pageNo);
        return page.deleteElement(key);
    }

    public static void updateIndex(int i, long value){
        index[i] = value;
    }

    public static void writeIndex(String fileName){
        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
            int iterator = 0;
            for (long x: index) {
                raf.seek(8*iterator);
                byte[] byteTable = Convert.long2ByteArray(x);
                raf.write(byteTable);
                iterator++;
            }
            raf.close();
        } catch (IOException ex) {
            System.out.println("Writing Error");
            ex.printStackTrace();
        }
    }

    public static void readIndex(String fileName){
        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
            long readKey = -1;
            int iterator = 0;
            List<Long> indexList = new ArrayList<>();

            while (true){
                raf.seek(iterator*8);
                byte[] bytesRead = new byte[8];
                raf.read(bytesRead);
                readKey = Convert.byteArray2Long(bytesRead);
                if(readKey == 0)
                    break;

                indexList.add(readKey);

            }
            index = Convert.toLongListArray(indexList);
            raf.close();
        } catch (IOException ex) {
            System.out.println("Reading Error");
            ex.printStackTrace();
        }
    }

    public static void printAll(String fileName){
        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
            long readKey = -1;
            int iterator = 0;
            Page newPage = new Page();

            while(iterator <= Main.numberOfPagesMain + 1){
                newPage.readPage(fileName, iterator);
                //readKey = newPage.getDataKey(0);

                newPage.printPage();
                System.out.println("------------------- END OF PAGE " + iterator + " -------------------------");
                iterator++;
            }
            raf.close();
        } catch (IOException ex) {
            System.out.println("Writing Error");
            ex.printStackTrace();
        }
    }

    public static void main(String[] args){

        Test.ReadTestFile(args[0]);

    }
}
