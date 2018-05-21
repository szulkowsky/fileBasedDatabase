package com;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import static com.Main.pageSize;

public class Page {
    private boolean isFull;
    private Element[] data;

    private int index = 0;
    private boolean isEnd = false;
    private int level = 0;
    public int indexNext = 0;
    public int indexGetNext = 0;
    public int currentOffset = 0;




    public Page(){
        this.isFull = false;
        this.data = new Element[pageSize];
        Main.indexNext = 0;
        for(int i = 0; i< pageSize; i++){
            this.data[i] = new Element();
        }
    }

    public void setData(Element element, int index){
        this.data[index] = element;
    }

    public Element getData(int index){
        return this.data[index];
    }

    public void writePage(String fileName, int offset){
        Main.writes++;
        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rws")) {

            byte bytes[] = new byte[2001];

            bytes[0] = Convert.boolean2ByteArray(isFull);

            int k = 1;
            for (Element x : data) {
                byte[] dataBytes = x.getElementInBytes();
                for(int i = 0; i<20; i++){
                    bytes[k] = dataBytes[i];
                    k++;
                }
            }

            raf.seek(offset*2001);
            raf.write(bytes);
            raf.seek(0);
            raf.close();

        } catch (IOException ex) {
            System.out.println("Writing Error");
            ex.printStackTrace();
        }
    }

    public void readPage(String fileName, int offset){
        Main.reads++;
        try (RandomAccessFile raf2 = new RandomAccessFile(fileName, "rws")) {
           currentOffset = offset;
            byte bytes[] = new byte[2001];
            raf2.seek(offset*2001);
            raf2.read(bytes);
            //raf.read(bytes, offset*2001, 2001);
            int k = 0, index = 0;
            this.isFull = Convert.byteArray2Boolean(bytes[k]);
            k++;
            for(int i = 0; i<100; i++){
                byte[] dataBytes = new byte[20];
                for (int j = 0; j < 20; j++) {
                    dataBytes[j] = bytes[k];
                    k++;
                }

                int it = 0;
                byte[] keyBytes = new byte[8];
                for(int j = 0; j<8; j++){
                    keyBytes[j] = dataBytes[it];
                    it++;
                }
                long key = Convert.byteArray2Long(keyBytes);

                byte[] triangleABytes = new byte[4];
                byte[] triangleHBytes = new byte[4];
                for(int j = 0; j<4; j++){
                    triangleABytes[j] = dataBytes[it];
                    it++;
                }
                for(int j = 0; j<4; j++){
                    triangleHBytes[j] = dataBytes[it];
                    it++;
                }

                float a = Convert.byteArray2Float(triangleABytes);
                float h = Convert.byteArray2Float(triangleHBytes);


                byte[] ofBytes = new byte[4];
                for(int j = 0; j<4; j++){
                    ofBytes[j] = dataBytes[it];
                    it++;
                }
                int ovNum = Convert.byteArray2Int(ofBytes);

                this.data[i] = new Element(key, a, h, ovNum);

            }

            raf2.seek(0);
            raf2.close();

        } catch (IOException ex) {
            System.out.println("Reading Error");
            ex.printStackTrace();
        }
    }

    public boolean getIsFull(){
        return this.isFull;
    }

    public int addElementBetter(Element element) {
        if (!this.getIsFull()) {
            Element[] newData = new Element[pageSize];

            for(int i = 0; i< pageSize; i++){
                newData[i] = new Element();
            }

            int indexWrite = 0;
            int indexRead = 0;

            while(indexRead < pageSize) {
                if (getDataKey(indexRead) == -1) {
                    indexRead++;
                }
                else {
                    newData[indexWrite] = this.data[indexRead];
                    indexRead++;
                    indexWrite++;
                }
            }


            int index = 0;
            while (newData[index].getKey() < element.getKey() && newData[index].getKey() >= 0){
                this.data[index] = newData[index];
                index++;
            }
            if(index == 0){
                Main.index[Main.currentLoadedMainPage] = element.getKey();
            }
            if(newData[index].getKey() == element.getKey())
                return 0;
            else {
                //index--;
                this.data[index] = element;
                index++;
            }
            while (index < pageSize){
                this.data[index] = newData[index-1];
                index++;
            }

            if(indexWrite + 1 == pageSize)
                this.isFull = true;

            //this.data = newData;
        }
        else {
            int index = 0;
            while (index < pageSize && this.data[index].getKey() < element.getKey()){
                index++;
            }
            index--;

            int retAdd = Main.addToOverflowBetter(element, this.data[index]);

            this.data[index].setOverflowPageNo(retAdd);
            /*
            if(this.data[index].getOverflowPageNo() == -1) {
                this.data[index].setOverflowPageNo(Main.currentLoadedOverflowPage);
                Main.addToOverflow(element, this.data[index].getOverflowPageNo(), false, this.getDataKey(index));

            }
            else{
                //do sth with searching/changing numbers
                int ret = Main.addToOverflow(element, this.data[index].getOverflowPageNo(), true, this.getDataKey(index));
                this.data[index].setOverflowPageNo(ret);
            }
*/
            return 2;


        }

        return 1;
    }

    public long getDataKey(int index){
        return this.data[index].getKey();
    }

    public void printPage(){
        for (Element x:data) {
            System.out.println("[ " + x.getKey() + " ] " + x.getData().toString() + " " + x.getOverflowPageNo());
        }
        System.out.println("-------------END OF PAGE----------------");
    }

    public Element findElement(long key){
        Element retValue = new Element();
        long tempKey = 0;
        int tempIndex = 0, tempIndex2 = 0;
        for (Element x: data) {
            if(x.getKey() == key){
                retValue = x;
                return retValue;
            }
            else if (x.getKey() < key && x.getKey() > tempKey){
                tempKey = x.getKey();
                tempIndex = tempIndex2;
            }
            tempIndex2++;
        }
        int nextPointer = this.data[tempIndex].getOverflowPageNo();
        if(retValue.getKey() == -1 && nextPointer == -1){
            System.out.println("No such element in data");
        }
        else if(nextPointer != -1) {
                Page ov = new Page();
                ov.readPage("data.bin", Main.numberOfPagesMain + nextPointer);
                retValue = ov.findElement(key);
        }



        return retValue;
    }

    public int deleteElement(long key){
        Element retValue = new Element();
        int ret = 0;
        long tempKey = 0;
        int tempIndex = 0, tempIndex2 = 0;
        for (Element x: data) {
            if(x.getKey() == key){
                if(x.getOverflowPageNo() == -1){
                    this.data[tempIndex2].setKey(-1);
                    if(this.currentOffset >= Main.numberOfPagesMain)
                        Main.currentOverflowNumber--;

                    this.writePage("data.bin", this.currentOffset);
                    return 1;
                }
                else {
                    Page ov = new Page();
                    ov.readPage("data.bin", Main.numberOfPagesMain + x.getOverflowPageNo());
                    long tmpKey2 = 0;
                    int tempIndex3 = 0;
                    int tempIndex4 = 0;
                    for (Element y: ov.data) {
                        if (y.getKey() <= key && y.getKey() > tmpKey2){
                            tmpKey2 = y.getKey();
                            tempIndex3 = tempIndex4;
                        }
                        tempIndex4++;
                    }
                    //tempIndex3++;
                    this.data[tempIndex2].setData(ov.getData(tempIndex3).getData().getA(),
                                                    ov.getData(tempIndex3).getData().getH());
                    this.data[tempIndex2].setKey(ov.getDataKey(tempIndex3));
                    this.data[tempIndex2].setOverflowPageNo(ov.getData(tempIndex3).getOverflowPageNo());
                    this.writePage("data.bin", this.currentOffset);
                    ov.data[tempIndex3].setKey(-1);
                    if(ov.currentOffset >= Main.numberOfPagesMain)
                        Main.currentOverflowNumber--;

                    ov.writePage("data.bin", ov.currentOffset);
                    return 1;

                    //find element from the pointer, write it here, done
                }
            }
            else if (x.getKey() < key && x.getKey() > tempKey){
                tempKey = x.getKey();
                tempIndex = tempIndex2;
            }
            tempIndex2++;
        }
        int nextPointer = this.data[tempIndex].getOverflowPageNo();
        if(retValue.getKey() == -1 && nextPointer == -1){
            System.out.println("No such element in data");
        }
        else if(nextPointer != -1) {
            Page ov = new Page();
            ov.readPage("data.bin", Main.numberOfPagesMain + nextPointer);
            ret = ov.deleteElement(key);
        }
        return ret;

    }

    public int updateElement(long key, float valueA, float valueH){
        Element retValue = new Element();
        int ret = 0;
        long tempKey = 0;
        int tempIndex = 0, tempIndex2 = 0;
        for (Element x: data) {
            if(x.getKey() == key){
                this.data[tempIndex2].setData(valueA, valueH);
                this.writePage("data.bin", this.currentOffset);
                return 1;
            }
            else if (x.getKey() < key && x.getKey() > tempKey){
                tempKey = x.getKey();
                tempIndex = tempIndex2;
            }
            tempIndex2++;
        }
        int nextPointer = this.data[tempIndex].getOverflowPageNo();
        if(retValue.getKey() == -1 && nextPointer == -1){
            System.out.println("No such element in data");
        }
        else if(nextPointer != -1) {
            Page ov = new Page();
            ov.readPage("data.bin", Main.numberOfPagesMain + nextPointer);
            ret = ov.updateElement(key, valueA, valueH);
        }
        return ret;
    }

    public Element getNext() {
        int nextPointer = this.data[index].getOverflowPageNo();
        int thisLevel = 0;
        Element elementMain = this.data[index];
        Page overFlow = new Page();
        if (index == 0) {
            Element ret = this.data[index];
            if (this.data[index].getOverflowPageNo() == -1){
                isEnd = true;
                index++;
                indexNext++;
            }
            else
                isEnd = false;

            return ret;
        }
        while (true) {
            if (!isEnd) {

                overFlow.readPage("data.bin", Main.numberOfPagesMain + nextPointer);
                //Otwieramy stronę na ktorą wskazuje wskaźnik z obszaru głównego
                int k2 = 0;
                int index2 = 0;
                long tmpKey = Long.MAX_VALUE;
                while (k2 < pageSize) {
                    if (overFlow.getDataKey(k2) > elementMain.getKey() && overFlow.getDataKey(k2) < tmpKey) {
                        tmpKey = overFlow.getDataKey(k2);
                        index2 = k2;
                    }
                    k2++;
                }
                thisLevel++;
                if(overFlow.getData(index2).getOverflowPageNo() == -1 || thisLevel == level){
                    if(overFlow.getData(index2).getOverflowPageNo() == -1) {
                        isEnd = true;
                        index++;
                        indexNext++;
                    }

                    level++;
                    return overFlow.getData(index2);
                }
                else {
                    elementMain = overFlow.getData(index2);
                    nextPointer = overFlow.getData(index2).getOverflowPageNo();
                }

            }
            else {
                Element retValue = this.data[index];
                if (this.data[index].getOverflowPageNo() == -1){
                    isEnd = true;
                    //index++;
                    indexNext++;
                    index++;
                    level = 0;
                }
                else{
                    isEnd = false;
                    level++;
                }

                return retValue;
            }
        }
    }

}

