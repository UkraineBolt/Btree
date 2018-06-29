/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365project1;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 *
 * @author alex
 */
public class Btree implements java.io.Serializable {

    static final String F = "C:\\Users\\shado\\Documents\\NetBeansProjects\\CSC365project1\\src\\btree\\BtreeData.txt";
    static final int K = 8;
    static final int NODESIZE = 1000;
    Node root;
    int idt;

    class Node implements java.io.Serializable {

        int cn;//4
        int n;//4
        int key[];
        Long children[];
        int leaf;//4
        long id;//8
        //632

        Node(long i) {
            id = i;
            cn = 0;
            n = 0;
            key=new int[2 * K - 1];
            children=new Long[2 * K];
        }
    }
    Btree() throws Exception {

        idt = 0;
        Node x = new Node(idt);
        x.leaf = 1;
        x.n = 0;
        diskwrite(x);
        root = x;
    }
    Integer search(Node x, int k) throws Exception {
        int i = 0;
        while (i < x.n && k > x.key[i]) {
            i = i + 1;
        }
        if (i <= x.n && k == x.key[i]) {
            return x.key[i];
        } else if (x.leaf == 1) {
            return null;
        } else {
            Node t = diskread(x.children[i]);
            return search(t, k);
        }
    }
    void insert(int k) throws Exception {
        Node r = root;
        if (root.n == 2 * K - 1) {
            idt++;
            Node s = new Node(idt);
            root = s;
            s.leaf = 0;
            s.n = 0;
            s.children[0] = r.id;
            s.cn++;
            splitchild(s,r);
            insertnf(s, k);
        } else {
            insertnf(r, k);
        }
    }
    private void splitchild(Node x,Node y) throws IOException {//need to figure out how to change where it starts and finishes
        idt++;
        Node z = new Node(idt);
        z.leaf = y.leaf;
        for (int j = 0; j < K-1; j++) {//move last half of y keys to first half z keys
            z.key[j] = y.key[j + K];
            z.n++;
            y.key[j + K] = 0;
            y.n--;
        }

        if (y.leaf == 0) {
            for (int j = 0; j < K; j++) {//move last half of y pointers to first half z pointers
                z.children[j] = y.children[j + K];
                z.cn++;
                y.children[j + K] = null;
                y.cn--;
            }
        }
        //z pointer can not ever become a pointer at 0
        int g = x.n - 1;//-1 puts x.n into index form
        while(g>-1&&y.key[K-1]<x.key[g]){
            x.key[g+1]=x.key[g];
            g--;
        }g++;
        x.key[g] = y.key[K - 1];        
        x.n++;
        y.key[K-1] = 0;
        y.n--;
        
        int m = x.cn-1;
        while(m>g){
            x.children[m+1]=x.children[m];
            m--;
        }m++;
        x.children[m] = z.id;
        x.cn++;

        diskwrite(x);
        diskwrite(y);
        diskwrite(z);
    }
    private void insertnf(Node x, int k) throws Exception {//broke
        int i = x.n-1;
        if (x.leaf == 1) {

                while (i > -1 && k < x.key[i]) {
                    x.key[i+1] = x.key[i];
                    i--;
                }i++;
                x.key[i] = k;
                x.n = 1 + x.n;
                diskwrite(x);
        } else {
            while (i > -1 && k < x.key[i]) {
                i--;
            }
            i++;

            Node t = diskread(x.children[i]);

            if (t.n == 2 * K - 1) {
                splitchild(x,t);
                if(k>x.key[i]){
                    Node b = diskread(x.children[i+1]);
                    t=b;
                }
            }
            insertnf(t, k);
        }

    }
    private void diskwrite(Node x) throws IOException {
        try {
            RandomAccessFile store = new RandomAccessFile(F, "rw");
            store.seek(x.id*NODESIZE);
            FileChannel fc = store.getChannel();
            ByteBuffer b = ByteBuffer.allocate(NODESIZE);
            b.putInt(x.leaf);
            b.putLong(x.id);
            b.putInt(x.n);
            for (int i = 0; i < x.n; i++) {
                b.putInt(x.key[i]);
            }
            b.putInt(x.cn);
            for (int i = 0; i < x.cn; i++) {
                b.putLong(x.children[i]);
            }
            b.flip();
            fc.write(b);
            b.clear();
            fc.close();
            store.close();
        } catch (Exception e) {
            System.out.println("write broke by" + e);
        }

    }
    private Node diskread(long x) throws Exception {
        try {
            Node t = new Node(0);
            RandomAccessFile store = new RandomAccessFile(F, "rw");            
            store.seek(x * NODESIZE);
            FileChannel fc=store.getChannel();
            ByteBuffer b = ByteBuffer.allocate(NODESIZE);
            fc.read(b);
            b.flip();
            t.leaf=b.getInt();
            t.id=b.getLong();
            t.n = b.getInt();
            
            for (int i = 0; i < t.n; i++) {
                t.key[i]=b.getInt();                
            }
            t.cn=b.getInt();
            for (int i = 0; i < t.cn; i++) {
                t.children[i]=b.getLong();
            }
            b.clear();
            fc.close();
            store.close();
            return t;
        } catch (Exception e) {
            System.out.println("read broke by:" + e);
            return null;
        }
    }
}
